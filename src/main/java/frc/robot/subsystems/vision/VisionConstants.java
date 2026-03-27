package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Transform3d;
import edu.wpi.first.math.geometry.Translation3d;
import edu.wpi.first.math.util.Units;

public final class VisionConstants {
        // Orange Pi
        public static final String ORANGE_PI_IP = "10.31.64.12";
        public static final int PHOTONVISION_PORT = 5800;
        public static final String PHOTONVISION_URL = "http://" + ORANGE_PI_IP + ":" + PHOTONVISION_PORT;

        // Limelight
        public static final String LIMELIGHT_NAME = "ye";

        public static final double AMBIGUITY_THRESHOLD = 0.2;
        public static final double MAX_TAG_DISTANCE = 5.0; // meters
        public static final double MAX_FRAME_AGE = 0.3; // seconds
        public static final int MIN_TAG_COUNT = 1;
        public static final double MIN_AREA = 0.05; // meters squared
        public static final double MAX_POSE_DIFFERENCE = 1.5; // meters

        // Arducams
        // - x: forward/back from center (positive = forward)
        // - y: left/right from center (positive = left)
        // - z: up/down from ground (positive = up)
        // TODO: placeholders. rewrite all w/ real camera position info.
        public static final Transform3d FRONT_LEFT_CAMERA_TRANSFORM = new Transform3d(
                        new Translation3d(
                                        Units.inchesToMeters(0),
                                        Units.inchesToMeters(0),
                                        Units.inchesToMeters(0)),
                        new Rotation3d(0, 0, 0));

        public static final Transform3d FRONT_RIGHT_CAMERA_TRANSFORM = new Transform3d(
                        new Translation3d(
                                        Units.inchesToMeters(0),
                                        Units.inchesToMeters(0),
                                        Units.inchesToMeters(0)),
                        new Rotation3d(0, 0, 0));

        public static final Transform3d BACK_LEFT_CAMERA_TRANSFORM = new Transform3d(
                        new Translation3d(
                                        Units.inchesToMeters(0),
                                        Units.inchesToMeters(0),
                                        Units.inchesToMeters(0)),
                        new Rotation3d(0, 0, 0));

        public static final Transform3d BACK_RIGHT_CAMERA_TRANSFORM = new Transform3d(
                        new Translation3d(
                                        Units.inchesToMeters(0),
                                        Units.inchesToMeters(0),
                                        Units.inchesToMeters(0)),
                        new Rotation3d(0, 0, 0));



}