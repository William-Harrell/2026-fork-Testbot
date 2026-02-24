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
  public static final String[] CAMERA_NAMES = {"front_cam", "back_left_cam", "back_right_cam"};

  public static final double AMBIGUITY_THRESHOLD = 0.2;

  // Filters
  public static final double MAX_TAG_DISTANCE = 5.0; // meters
  public static final double MAX_FRAME_AGE = 0.3; // seconds
  public static final double MIN_TAG_COUNT = 1;
  public static final double MIN_AREA = 0.05; // meters squared
  public static final double MAX_POSE_DIFFERENCE = 1.5; // meters

  // ================================================================
  // CAMERA TRANSFORMS
  // ================================================================
  // Camera positions relative to robot center. Transform3d(x, y, z, rotation)
  // - x: forward/back from center (positive = forward)
  // - y: left/right from center (positive = left)
  // - z: up/down from ground (positive = up)
  // NOTE: These are placeholder values — measure actual positions once cameras are mounted.

  /** Front camera — centered, facing forward */
  public static final Transform3d FRONT_CAMERA_TRANSFORM =
      new Transform3d(
          new Translation3d(Units.inchesToMeters(12.0), 0.0, Units.inchesToMeters(24.0)),
          new Rotation3d(0.0, Math.toRadians(-15.0), 0.0));

  /** Back-left camera — offset left and back, facing 135 degrees */
  public static final Transform3d BACK_LEFT_CAMERA_TRANSFORM =
      new Transform3d(
          new Translation3d(
              Units.inchesToMeters(-10.0),
              Units.inchesToMeters(10.0),
              Units.inchesToMeters(24.0)),
          new Rotation3d(0.0, Math.toRadians(-15.0), Math.toRadians(135.0)));

  /** Back-right camera — offset right and back, facing 225 degrees */
  public static final Transform3d BACK_RIGHT_CAMERA_TRANSFORM =
      new Transform3d(
          new Translation3d(
              Units.inchesToMeters(-10.0),
              Units.inchesToMeters(-10.0),
              Units.inchesToMeters(24.0)),
          new Rotation3d(0.0, Math.toRadians(-15.0), Math.toRadians(225.0)));
}
