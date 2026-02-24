#!/usr/bin/env python3
"""
Orange Pi Vision Service - Main Entry Point

Multi-camera ML inference service for detecting robots and FUEL game pieces.
Runs one CameraPipeline per camera, fuses results, and publishes to NetworkTables.
"""

import argparse
import signal
import sys
import threading
import time
from typing import List, Optional

from config import CAMERAS, CameraConfig, FUSION_INTERVAL, INTAKE_RANGE_DISTANCE
from detectors import RobotDetector, FuelDetector, Detection
from network import NTPublisher
from utils import Camera


class CameraPipeline:
    """Runs ML detection on a single camera stream in its own thread."""

    def __init__(self, cam_config: CameraConfig, simulate: bool = False):
        self.config = cam_config
        self.simulate = simulate
        self.name = cam_config.name

        # Components
        self.camera = Camera(cam_config)
        self.robot_detector = RobotDetector()
        self.fuel_detector = FuelDetector()

        # Latest results (thread-safe)
        self._lock = threading.Lock()
        self._robot_detections: List[Detection] = []
        self._fuel_detections: List[Detection] = []
        self._closest_robot: Optional[Detection] = None
        self._best_fuel: Optional[Detection] = None
        self._intake_ready: bool = False
        self._timestamp: float = 0.0

        # Thread
        self._running = False
        self._thread: Optional[threading.Thread] = None

        # Stats
        self._frame_count = 0
        self._fps = 0.0

    def start(self) -> bool:
        """Initialize detectors and start the pipeline thread."""
        if not self.simulate:
            if not self.camera.start():
                print(f"[{self.name}] Failed to start camera")
                return False

            if not self.robot_detector.load():
                print(f"[{self.name}] Warning: Robot detector failed to load")
            if not self.fuel_detector.load():
                print(f"[{self.name}] Warning: FUEL detector failed to load")
        else:
            print(f"[{self.name}] Simulation mode")

        self._running = True
        self._thread = threading.Thread(
            target=self._loop, daemon=True, name=f"pipeline-{self.name}"
        )
        self._thread.start()
        print(f"[{self.name}] Pipeline started")
        return True

    def _loop(self):
        """Main processing loop for this camera."""
        fps_time = time.time()
        fps_count = 0

        while self._running:
            if self.simulate:
                self._process_simulated()
                time.sleep(1.0 / 30.0)
            else:
                self._process_frame()

            fps_count += 1
            now = time.time()
            if now - fps_time >= 1.0:
                self._fps = fps_count / (now - fps_time)
                fps_count = 0
                fps_time = now

    def _process_frame(self):
        """Process a single frame from this camera."""
        frame, timestamp = self.camera.get_frame()
        if frame is None:
            time.sleep(0.001)
            return

        # Run robot detection
        robot_detections = []
        if self.robot_detector.is_loaded:
            robot_detections = self.robot_detector.detect(
                frame,
                fov_horizontal=self.config.fov_horizontal,
                fov_vertical=self.config.fov_vertical,
                mount_yaw=self.config.mount_yaw,
            )

        closest_robot = None
        if robot_detections:
            closest_robot = self.robot_detector.get_closest_robot(robot_detections)

        # Run FUEL detection
        fuel_detections = []
        if self.fuel_detector.is_loaded:
            fuel_detections = self.fuel_detector.detect(
                frame,
                fov_horizontal=self.config.fov_horizontal,
                fov_vertical=self.config.fov_vertical,
                mount_yaw=self.config.mount_yaw,
            )

        best_fuel = None
        intake_ready = False
        if fuel_detections:
            best_fuel = self.fuel_detector.get_best_fuel(fuel_detections)
            if best_fuel:
                intake_ready = self.fuel_detector.is_intake_ready(best_fuel)

        # Store results thread-safely
        with self._lock:
            self._robot_detections = robot_detections
            self._fuel_detections = fuel_detections
            self._closest_robot = closest_robot
            self._best_fuel = best_fuel
            self._intake_ready = intake_ready
            self._timestamp = timestamp

    def _process_simulated(self):
        """Produce empty results in simulation mode."""
        with self._lock:
            self._robot_detections = []
            self._fuel_detections = []
            self._closest_robot = None
            self._best_fuel = None
            self._intake_ready = False
            self._timestamp = time.time()

    def get_results(self):
        """Get latest detection results from this pipeline.

        Returns:
            Tuple of (robot_detections, fuel_detections, closest_robot,
                       best_fuel, intake_ready, timestamp)
        """
        with self._lock:
            return (
                list(self._robot_detections),
                list(self._fuel_detections),
                self._closest_robot,
                self._best_fuel,
                self._intake_ready,
                self._timestamp,
            )

    def stop(self):
        """Stop the pipeline."""
        self._running = False

        if self._thread is not None:
            self._thread.join(timeout=2.0)
            self._thread = None

        self.camera.stop()
        self.robot_detector.release()
        self.fuel_detector.release()
        print(f"[{self.name}] Pipeline stopped (avg FPS: {self._fps:.1f})")

    @property
    def fps(self) -> float:
        return self._fps


class VisionService:
    """Main vision service orchestrating multi-camera detection and publishing."""

    def __init__(self, simulate: bool = False):
        self.simulate = simulate
        self._running = False

        self.pipelines: List[CameraPipeline] = []
        self.publisher: Optional[NTPublisher] = None

        # Statistics
        self._start_time = 0.0

    def initialize(self) -> bool:
        """Initialize all pipelines and NetworkTables."""
        print("Initializing Vision Service (multi-camera)...")

        # Create a pipeline per camera
        for cam_cfg in CAMERAS:
            pipeline = CameraPipeline(cam_cfg, simulate=self.simulate)
            self.pipelines.append(pipeline)

        # Start all pipelines
        for pipeline in self.pipelines:
            if not pipeline.start():
                print(f"Failed to start pipeline: {pipeline.name}")
                return False

        # Initialize NetworkTables publisher
        self.publisher = NTPublisher()
        if not self.publisher.connect():
            print("Warning: NetworkTables connection failed")

        print(f"Vision Service initialized with {len(self.pipelines)} cameras")
        return True

    def run(self):
        """Run the main fusion loop."""
        print("Starting fusion loop...")
        self._running = True
        self._start_time = time.time()
        last_log_time = self._start_time

        while self._running:
            loop_start = time.time()

            self._fuse_and_publish()

            # Maintain target fusion rate
            elapsed = time.time() - loop_start
            sleep_time = FUSION_INTERVAL - elapsed
            if sleep_time > 0:
                time.sleep(sleep_time)

            # Log status every second
            now = time.time()
            if now - last_log_time >= 1.0:
                fps_str = ", ".join(
                    f"{p.name}={p.fps:.1f}" for p in self.pipelines
                )
                print(
                    f"FPS: [{fps_str}], "
                    f"NT Connected: {self.publisher.is_connected if self.publisher else False}"
                )
                last_log_time = now

    def _fuse_and_publish(self):
        """Gather detections from all pipelines, fuse, and publish."""
        all_robot_detections: List[Detection] = []
        all_fuel_detections: List[Detection] = []
        latest_timestamp = 0.0

        for pipeline in self.pipelines:
            (
                robot_dets,
                fuel_dets,
                _closest,
                _best,
                _intake,
                ts,
            ) = pipeline.get_results()

            all_robot_detections.extend(robot_dets)
            all_fuel_detections.extend(fuel_dets)
            if ts > latest_timestamp:
                latest_timestamp = ts

        # Fuse: pick closest robot across all cameras
        fused_closest_robot = None
        if all_robot_detections:
            fused_closest_robot = min(all_robot_detections, key=lambda d: d.distance)

        # Fuse: pick best fuel across all cameras
        # Prefer intake-ready targets, then closest
        fused_best_fuel = None
        fused_intake_ready = False
        if all_fuel_detections:
            intake_candidates = [
                d
                for d in all_fuel_detections
                if d.distance <= INTAKE_RANGE_DISTANCE and abs(d.angle) <= 15.0
            ]
            if intake_candidates:
                fused_best_fuel = max(intake_candidates, key=lambda d: d.confidence)
                fused_intake_ready = True
            else:
                fused_best_fuel = min(all_fuel_detections, key=lambda d: d.distance)

        # Publish fused results
        if self.publisher:
            self.publisher.publish_robots(
                all_robot_detections, fused_closest_robot, latest_timestamp
            )
            self.publisher.publish_fuel(
                all_fuel_detections, fused_best_fuel, fused_intake_ready, latest_timestamp
            )

            # Per-camera debug publishing
            for pipeline in self.pipelines:
                (
                    robot_dets,
                    fuel_dets,
                    closest,
                    best,
                    intake,
                    ts,
                ) = pipeline.get_results()
                self.publisher.publish_robots(
                    robot_dets, closest, ts, camera_name=pipeline.name
                )
                self.publisher.publish_fuel(
                    fuel_dets, best, intake, ts, camera_name=pipeline.name
                )

    def stop(self):
        """Stop the vision service."""
        print("Stopping Vision Service...")
        self._running = False

        for pipeline in self.pipelines:
            pipeline.stop()

        if self.publisher:
            self.publisher.disconnect()

        print("Vision Service stopped")

    def print_stats(self):
        """Print runtime statistics."""
        runtime = time.time() - self._start_time
        print(f"\n--- Vision Service Statistics ---")
        print(f"Runtime: {runtime:.1f} seconds")
        for p in self.pipelines:
            print(f"  {p.name}: {p.fps:.1f} FPS")
        print(f"NT Connected: {self.publisher.is_connected if self.publisher else False}")


def main():
    """Main entry point."""
    parser = argparse.ArgumentParser(description="Orange Pi Vision Service")
    parser.add_argument(
        "--simulate",
        action="store_true",
        help="Run in simulation mode (no camera/models)",
    )
    args = parser.parse_args()

    # Create service
    service = VisionService(simulate=args.simulate)

    # Handle shutdown signals
    def signal_handler(sig, frame):
        print("\nShutdown signal received")
        service.stop()
        service.print_stats()
        sys.exit(0)

    signal.signal(signal.SIGINT, signal_handler)
    signal.signal(signal.SIGTERM, signal_handler)

    # Initialize and run
    if not service.initialize():
        print("Failed to initialize service")
        sys.exit(1)

    try:
        service.run()
    except Exception as e:
        print(f"Error in main loop: {e}")
        service.stop()
        sys.exit(1)


if __name__ == "__main__":
    main()
