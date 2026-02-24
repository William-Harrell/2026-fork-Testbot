"""
Camera capture utilities for MJPEG streams from PhotonVision.

PhotonVision owns the USB cameras. This module reads frames from
PhotonVision's MJPEG HTTP stream endpoints to avoid camera contention.
"""

import cv2
import numpy as np
import threading
import time
from typing import Optional, Tuple

from config import CameraConfig, STREAM_RECONNECT_DELAY


class Camera:
    """MJPEG stream capture with reconnection and frame buffering."""

    def __init__(self, config: CameraConfig):
        """
        Initialize camera from a CameraConfig.

        Args:
            config: CameraConfig with stream URL and camera parameters
        """
        self.config = config
        self.stream_url = config.stream_url
        self.name = config.name

        self._cap: Optional[cv2.VideoCapture] = None
        self._frame: Optional[np.ndarray] = None
        self._frame_time: float = 0.0
        self._lock = threading.Lock()
        self._running = False
        self._thread: Optional[threading.Thread] = None
        self._connected = False

    def start(self) -> bool:
        """Start camera capture in a background thread."""
        self._running = True
        self._thread = threading.Thread(
            target=self._capture_loop, daemon=True, name=f"cam-{self.name}"
        )
        self._thread.start()
        print(f"[{self.name}] Stream capture thread started for {self.stream_url}")
        return True

    def _connect(self) -> bool:
        """Attempt to connect to the MJPEG stream."""
        if self._cap is not None:
            self._cap.release()

        self._cap = cv2.VideoCapture(self.stream_url)

        if not self._cap.isOpened():
            self._connected = False
            return False

        # Minimize buffer for low latency
        self._cap.set(cv2.CAP_PROP_BUFFERSIZE, 1)

        self._connected = True
        print(f"[{self.name}] Connected to stream {self.stream_url}")
        return True

    def _capture_loop(self):
        """Background thread for continuous frame capture with reconnection."""
        while self._running:
            # Connect if not connected
            if not self._connected:
                if not self._connect():
                    print(
                        f"[{self.name}] Stream not available, retrying in "
                        f"{STREAM_RECONNECT_DELAY}s..."
                    )
                    time.sleep(STREAM_RECONNECT_DELAY)
                    continue

            ret, frame = self._cap.read()
            if ret:
                with self._lock:
                    self._frame = frame
                    self._frame_time = time.time()
            else:
                # Stream dropped — mark disconnected so we reconnect
                self._connected = False
                print(f"[{self.name}] Stream read failed, reconnecting...")
                time.sleep(0.1)

    def get_frame(self) -> Tuple[Optional[np.ndarray], float]:
        """
        Get the latest captured frame.

        Returns:
            Tuple of (frame, timestamp) or (None, 0.0) if no frame available
        """
        with self._lock:
            if self._frame is not None:
                return self._frame.copy(), self._frame_time
            return None, 0.0

    def stop(self):
        """Stop camera capture and release resources."""
        self._running = False

        if self._thread is not None:
            self._thread.join(timeout=2.0)
            self._thread = None

        if self._cap is not None:
            self._cap.release()
            self._cap = None

        self._connected = False
        print(f"[{self.name}] Camera stopped")

    @property
    def is_running(self) -> bool:
        """Check if camera is currently capturing."""
        return self._running

    @property
    def is_connected(self) -> bool:
        """Check if stream is currently connected."""
        return self._connected

    def __enter__(self):
        self.start()
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.stop()
        return False
