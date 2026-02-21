package frc.robot.util.constants;

import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;

public final class VisionConstants {
  // ================================================================
  // ORANGE PI NETWORK CONFIGURATION
  // ================================================================
  // These values must match the Orange Pi's static IP configuration.
  // See Vision.java header for full setup instructions.

  /** Orange Pi static IP address (must be in 10.TE.AM.x range) */
  public static final String ORANGE_PI_IP = "10.31.64.11";

  /** PhotonVision web interface port */
  public static final int PHOTONVISION_PORT = 5800;

  /** PhotonVision HTTP API port */
  public static final int PHOTONVISION_API_PORT = 5800;

  /**
   * Full URL to PhotonVision dashboard. Access this from a laptop on the robot network to configure
   * cameras.
   */
  public static final String PHOTONVISION_URL = "http://" + ORANGE_PI_IP + ":" + PHOTONVISION_PORT;

  // ================================================================
  // CAMERA CONFIGURATION
  // ================================================================
  // Camera names (must match what's in PhotonVision)
  public static final String[] CAMERA_NAMES = {
    "example_cam_1", "example_cam_2"
  }; // (placeholder TODO)
  public static final double AMBIGUITY_THRESHOLD = 0.4; // (placeholder TODO)

  // Filters
  public static final double MAX_TAG_DISTANCE = 100; // meters (placeholder TODO)
  public static final double MAX_FRAME_AGE = 100; // seconds (placeholder TODO)
  public static final double MIN_TAG_COUNT = 1; // (placeholder TODO)
  public static final double MIN_AREA = 1.0; // meters squared (placeholder TODO)
  public static final double MAX_POSE_DIFFERENCE = 1000.0; // meters (placeholder TODO)

  /**
   * Camera positions relative to robot center. Transform3d(x, y, z, rotation) - x: forward/back
   * from center (positive = forward) - y: left/right from center (positive = left) - z: up/down
   * from ground (positive = up)
   */
  public static final Transform3d EXAMPLE_CAMERA_TRANSFORM_1 =
      new Transform3d(
          new Translation3d(Units.inchesToMeters(12.0), 0.0, Units.inchesToMeters(24.0)),
          new Rotation3d(
              0.0, Math.toRadians(-15.0), 0.0) // Roll, Pitch, and Yaw = X, Y, Z axis rotations
          ); // (placeholder TODO)

  public static final Transform3d EXAMPLE_CAMERA_TRANSFORM_2 =
      new Transform3d(
          new Translation3d(Units.inchesToMeters(-12.0), 0.0, Units.inchesToMeters(24.0)),
          new Rotation3d(0.0, Math.toRadians(-15.0), Math.toRadians(180.0)) // Facing backward
          ); // (placeholder TODO)
}
