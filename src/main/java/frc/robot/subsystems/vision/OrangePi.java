package frc.robot.subsystems.vision;

import java.util.List;
import java.util.Optional;

import frc.robot.subsystems.vision.Vision.VisionUpdate;

// Background work that we can attach/detach like a module and read easily (QoL, but isn't that half the battle?)

public class OrangePi {
    // ========================================================================
    // STUB RECORDS - Data structures for Orange Pi integration (TODO: implement)
    // ========================================================================

    /**
     * STUB: Detected robot data from Orange Pi ML model.
     *
     * <p>
     * [ORANGE PI WILL PROVIDE] - Position relative to our robot (robot-centric
     * coordinates) -
     * Estimated velocity for prediction - Alliance color if determinable -
     * Confidence score from ML
     * model
     *
     * @param x          X position relative to robot (meters, positive = front)
     * @param y          Y position relative to robot (meters, positive = left)
     * @param distance   Direct distance to robot (meters)
     * @param angle      Angle to robot (radians, 0 = directly ahead)
     * @param velocityX  Estimated X velocity (m/s)
     * @param velocityY  Estimated Y velocity (m/s)
     * @param confidence ML detection confidence (0-1)
     * @param alliance   Detected alliance ("red", "blue", "unknown")
     * @param timestamp  FPGA timestamp of detection
     */
    public record DetectedRobot(
            double x,
            double y,
            double distance,
            double angle,
            double velocityX,
            double velocityY,
            double confidence,
            String alliance,
            double timestamp) {

        /**
         * Check if this robot is on a collision course with us.
         *
         * @param timeHorizon  How far ahead to predict (seconds)
         * @param safeDistance Minimum safe distance (meters)
         * @return True if predicted to be within safeDistance
         */
        public boolean isCollisionThreat(double timeHorizon, double safeDistance) {
            // Predict where robot will be
            double predictedX = x + velocityX * timeHorizon;
            double predictedY = y + velocityY * timeHorizon;
            double predictedDistance = Math.sqrt(predictedX * predictedX + predictedY * predictedY);
            return predictedDistance < safeDistance;
        }
    }

    /**
     * STUB: Detected FUEL (game piece) data from Orange Pi.
     *
     * <p>
     * [ORANGE PI WILL PROVIDE] - Position relative to robot (for driving to it) -
     * Position on
     * field (if AprilTags visible) - Intake alignment assistance
     *
     * @param x          X position relative to robot (meters)
     * @param y          Y position relative to robot (meters)
     * @param distance   Distance to FUEL (meters)
     * @param angle      Angle to FUEL (radians)
     * @param confidence Detection confidence (0-1)
     * @param fieldX     Field-relative X (meters), NaN if unknown
     * @param fieldY     Field-relative Y (meters), NaN if unknown
     * @param timestamp  FPGA timestamp of detection
     */
    public record DetectedFuel(
            double x,
            double y,
            double distance,
            double angle,
            double confidence,
            double fieldX,
            double fieldY,
            double timestamp) {

        /** Distance threshold for "ready to intake" */
        public static final double INTAKE_RANGE = 1.0; // meters

        /** Angle threshold for "aligned with intake" */
        public static final double INTAKE_ANGLE_TOLERANCE = Math.toRadians(5); // 5 degrees

        /** Check if FUEL is close enough to intake. */
        public boolean inIntakeRange() {
            return distance < INTAKE_RANGE;
        }

        /** Check if robot is aligned to intake this FUEL. */
        public boolean isAligned() {
            return Math.abs(angle) < INTAKE_ANGLE_TOLERANCE;
        }

        /** Check if field position is known (AprilTags were visible). */
        public boolean hasFieldPosition() {
            return !Double.isNaN(fieldX) && !Double.isNaN(fieldY);
        }
    }

    /**
     * STUB: Complete vision frame from Orange Pi containing all detections.
     *
     * <p>
     * [ORANGE PI PUBLISHES ONE OF THESE EACH FRAME] Contains all detections from a
     * single camera
     * frame.
     *
     * @param timestamp        When the image was captured (FPGA time)
     * @param robots           List of detected robots (may be empty)
     * @param fuels            List of detected FUEL (may be empty)
     * @param aprilTagPose     Robot pose from AprilTags (empty if no tags)
     * @param processingTimeMs How long Orange Pi took to process (for monitoring)
     */
    public record VisionFrame(
            double timestamp,
            List<DetectedRobot> robots,
            List<DetectedFuel> fuels,
            Optional<VisionUpdate> aprilTagPose,
            double processingTimeMs) {

        /** Get the closest detected robot, if any. */
        public Optional<DetectedRobot> getClosestRobot() {
            return robots.stream().min((a, b) -> Double.compare(a.distance(), b.distance()));
        }

        /** Get the best FUEL to target (closest with high confidence). */
        public Optional<DetectedFuel> getBestFuel() {
            return fuels.stream()
                    .filter(f -> f.confidence() > 0.5) // Minimum confidence
                    .min((a, b) -> Double.compare(a.distance(), b.distance()));
        }

        /** Get all robots that are collision threats. */
        public List<DetectedRobot> getCollisionThreats(double timeHorizon, double safeDistance) {
            return robots.stream().filter(r -> r.isCollisionThreat(timeHorizon, safeDistance)).toList();
        }
    }

    // ========================================================================
    // NETWORKTABLES KEYS - Where Orange Pi publishes data (for reference)
    // ========================================================================

    /**
     * NetworkTables paths where Orange Pi will publish data. These are constants
     * for consistency
     * between Orange Pi and roboRIO code.
     */
    public static final class NTKeys {
        // AprilTag data
        public static final String APRILTAG_TABLE = "Vision/AprilTag";
        public static final String APRILTAG_HAS_TARGET = "hasTarget";
        public static final String APRILTAG_TAG_COUNT = "tagCount";
        public static final String APRILTAG_ROBOT_POSE_X = "robotPoseX";
        public static final String APRILTAG_ROBOT_POSE_Y = "robotPoseY";
        public static final String APRILTAG_ROBOT_POSE_THETA = "robotPoseTheta";
        public static final String APRILTAG_TIMESTAMP = "timestamp";
        public static final String APRILTAG_CONFIDENCE = "poseConfidence";

        // Robot detection data
        public static final String ROBOT_TABLE = "Vision/Robots";
        public static final String ROBOT_COUNT = "robotCount";
        public static final String ROBOT_CLOSEST_X = "closestRobotX";
        public static final String ROBOT_CLOSEST_Y = "closestRobotY";
        public static final String ROBOT_CLOSEST_DISTANCE = "closestRobotDistance";
        public static final String ROBOT_CLOSEST_VEL_X = "closestRobotVelX";
        public static final String ROBOT_CLOSEST_VEL_Y = "closestRobotVelY";
        public static final String ROBOT_CLOSEST_CONFIDENCE = "closestConfidence";
        public static final String ROBOT_TIMESTAMP = "timestamp";

        // FUEL detection data
        public static final String FUEL_TABLE = "Vision/Fuel";
        public static final String FUEL_COUNT = "fuelCount";
        public static final String FUEL_HAS_FUEL = "hasFuel";
        public static final String FUEL_BEST_X = "bestFuelX";
        public static final String FUEL_BEST_Y = "bestFuelY";
        public static final String FUEL_BEST_DISTANCE = "bestFuelDistance";
        public static final String FUEL_BEST_ANGLE = "bestFuelAngle";
        public static final String FUEL_BEST_CONFIDENCE = "bestFuelConfidence";
        public static final String FUEL_IN_INTAKE_RANGE = "bestFuelInIntakeRange";
        public static final String FUEL_INTAKE_READY = "intakeReady";
        public static final String FUEL_TIMESTAMP = "timestamp";
    }
}
