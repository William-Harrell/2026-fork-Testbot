"""
Configuration constants for Orange Pi vision system.
Matches NetworkTables keys defined in Vision.java.

Multi-camera setup: 3 cameras running through PhotonVision MJPEG streams.
"""

from dataclasses import dataclass


# NetworkTables Configuration
NT_SERVER_IP = "10.31.64.2"  # roboRIO IP address
NT_TABLE_NAME = "Vision"


@dataclass
class CameraConfig:
    """Configuration for a single camera in the multi-camera setup."""

    name: str
    stream_url: str  # PhotonVision MJPEG endpoint
    fov_horizontal: float  # degrees
    fov_vertical: float  # degrees
    mount_height: float  # meters above ground
    mount_pitch: float  # degrees (0=level, positive=tilted down)
    mount_yaw: float  # degrees from robot front (0=front, CW positive)


# Camera Configurations — one per PhotonVision camera stream
CAMERAS = [
    CameraConfig(
        "front_cam",
        "http://localhost:1182/stream.mjpg",
        68.0, 41.0, 0.3, 0.0, 0.0,
    ),
    CameraConfig(
        "back_left_cam",
        "http://localhost:1184/stream.mjpg",
        68.0, 41.0, 0.3, 0.0, 135.0,
    ),
    CameraConfig(
        "back_right_cam",
        "http://localhost:1186/stream.mjpg",
        68.0, 41.0, 0.3, 0.0, 225.0,
    ),
]

# Detection Thresholds
ROBOT_CONFIDENCE_THRESHOLD = 0.5
FUEL_CONFIDENCE_THRESHOLD = 0.4
INTAKE_RANGE_DISTANCE = 0.5  # meters - distance considered "in intake range"

# Model Paths
ROBOT_MODEL_PATH = "models/robot_detector.rknn"
FUEL_MODEL_PATH = "models/fuel_detector.rknn"

# Known object sizes for distance estimation (meters)
ROBOT_WIDTH_ESTIMATE = 0.7  # approximate robot width
FUEL_DIAMETER = 0.178  # 7 inch FUEL ball diameter

# Fusion interval — how often the main thread gathers and publishes fused results
FUSION_INTERVAL = 1.0 / 30.0  # 30 Hz

# Stream reconnection settings
STREAM_RECONNECT_DELAY = 2.0  # seconds between reconnection attempts
STREAM_RECONNECT_MAX_ATTEMPTS = 0  # 0 = unlimited


class NTKeys:
    """NetworkTables keys matching Vision.java contract."""

    # Robot detection keys (Vision/Robots/)
    class Robots:
        TABLE = "Vision/Robots"
        COUNT = "robotCount"
        CLOSEST_X = "closestRobotX"
        CLOSEST_Y = "closestRobotY"
        CLOSEST_DISTANCE = "closestRobotDistance"
        CLOSEST_VEL_X = "closestRobotVelX"
        CLOSEST_VEL_Y = "closestRobotVelY"
        CLOSEST_CONFIDENCE = "closestConfidence"
        TIMESTAMP = "timestamp"

    # FUEL detection keys (Vision/Fuel/)
    class Fuel:
        TABLE = "Vision/Fuel"
        COUNT = "fuelCount"
        HAS_FUEL = "hasFuel"
        BEST_X = "bestFuelX"
        BEST_Y = "bestFuelY"
        BEST_DISTANCE = "bestFuelDistance"
        BEST_ANGLE = "bestFuelAngle"
        BEST_CONFIDENCE = "bestFuelConfidence"
        IN_INTAKE_RANGE = "bestFuelInIntakeRange"
        INTAKE_READY = "intakeReady"
        TIMESTAMP = "timestamp"

    # AprilTag keys (Vision/AprilTag/) — published by PhotonVision,
    # mirrored here for reference / future use from Python side
    class AprilTag:
        TABLE = "Vision/AprilTag"
        HAS_TARGET = "hasTarget"
        TAG_COUNT = "tagCount"
        ROBOT_POSE_X = "robotPoseX"
        ROBOT_POSE_Y = "robotPoseY"
        ROBOT_POSE_THETA = "robotPoseTheta"
        TIMESTAMP = "timestamp"
        CONFIDENCE = "poseConfidence"
