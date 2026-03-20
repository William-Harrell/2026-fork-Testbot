package frc.robot.subsystems.vision;

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
    public static final String ORANGE_PI_IP = "10.31.64.12";

    /** PhotonVision web interface port */
    public static final int PHOTONVISION_PORT = 5800;

    /** PhotonVision HTTP API port */
    public static final int PHOTONVISION_API_PORT = 5800;

    /**
     * Full URL to PhotonVision dashboard. Access this from a laptop on the robot
     * network to configure
     * cameras.
     */
    public static final String PHOTONVISION_URL = "http://" + ORANGE_PI_IP + ":" + PHOTONVISION_PORT;

    // ================================================================
    // CAMERA CONFIGURATION
    // ================================================================
    // public static final String LIMELIGHT_NAME = "ye";

    public static final double AMBIGUITY_THRESHOLD = 0.2;

    // Filters
    public static final double MAX_TAG_DISTANCE = 5.0; // meters
    public static final double MAX_FRAME_AGE = 0.3; // seconds
    public static final int MIN_TAG_COUNT = 1;
    public static final double MIN_AREA = 0.05; // meters squared
    public static final double MAX_POSE_DIFFERENCE = 1.5; // meters

    // ================================================================
    // CAMERA TRANSFORMS
    // ================================================================
    // Camera positions relative to robot center. Transform3d(x, y, z, rotation)
    // - x: forward/back from center (positive = forward)
    // - y: left/right from center (positive = left)
    // - z: up/down from ground (positive = up)

    public static final Transform3d FRONT_LEFT_CAMERA_TRANSFORM = new Transform3d(
            new Translation3d(
                    Units.inchesToMeters(13.557),
                    Units.inchesToMeters(AMBIGUITY_THRESHOLD),
                    Units.inchesToMeters(24.0)),
            new Rotation3d(0.0, Math.toRadians(-15.0), 0.0));

    public static final Transform3d FRONT_RIGHT_CAMERA_TRANSFORM = new Transform3d(
            new Translation3d(
                    Units.inchesToMeters(12.0),
                    0.0,
                    Units.inchesToMeters(24.0)),
            new Rotation3d(0.0, Math.toRadians(-15.0), 0.0));

    public static final Transform3d BACK_LEFT_CAMERA_TRANSFORM = new Transform3d(
            new Translation3d(
                    Units.inchesToMeters(12.0),
                    0.0,
                    Units.inchesToMeters(24.0)),
            new Rotation3d(0.0, Math.toRadians(-15.0), 0.0));

    public static final Transform3d BACK_RIGHT_CAMERA_TRANSFORM = new Transform3d(
            new Translation3d(
                    Units.inchesToMeters(12.0),
                    0.0,
                    Units.inchesToMeters(24.0)),
            new Rotation3d(0.0, Math.toRadians(-15.0), 0.0));

}