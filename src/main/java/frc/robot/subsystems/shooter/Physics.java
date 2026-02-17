package frc.robot.subsystems.shooter;

import java.util.Optional;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import frc.robot.Constants;
import frc.robot.Constants.ShooterConstants;
import frc.robot.subsystems.swerve.SwerveDrive;
import frc.robot.subsystems.vision.Vision;

public class Physics {
    private double x, y, z;
    private Rotation2d heading;
    private final Vision vision;
    private final SwerveDrive swerve;
    private double[] hub = new double[2];
    private Alliance alliance;

    public Physics(Vision myV, SwerveDrive mySD) {
        vision = myV;
        swerve = mySD;
        refreshAlliance();
    }

    private void refreshAlliance() {
        alliance = DriverStation.getAlliance().orElse(Constants.ShooterConstants.DEFAULT_ALLIANCE);
    }

    /** Get robot pose from swerve odometry. */
    private Pose2d getRobotPose() {
        return swerve.getPose();
    }

    /** Update hub location based on alliance. */
    private void UpdateHubLocation() {
        hub[1] = ShooterConstants.UNIV_Y;

        hub[0] = (alliance == Alliance.Blue) ? ShooterConstants.BLUE_X
                : (alliance == Alliance.Red) ? ShooterConstants.RED_X
                : -180.0; // it's called ternary operator. look it up.
    }

    /** Check if scoring is allowed based on game state and position. */
    private boolean canScore() {
        String gameData = DriverStation.getGameSpecificMessage();
        char active = (gameData.length() > 0) ? gameData.charAt(0) : 'Z';

        return ((active == 'B' && alliance.equals(Alliance.Blue) && x >= ShooterConstants.BLUE_X)
                || (active == 'R' && alliance.equals(Alliance.Red) && x <= ShooterConstants.RED_X));
    }

    /** Calculate angle from robot heading to shooter direction. */
    private Rotation2d robotToShooter() {
        return Rotation2d.fromDegrees(0.0);
    }

    /**
     * Calculate angle difference between robot and hub.
     *
     * @return Angle difference in degrees
     */
    public double robotToHub() {
        UpdateHubLocation();

        Optional<Pose3d> poseOpt = vision.getPose3d(getRobotPose());
        if (poseOpt.isEmpty()) {
            return 0.0;
        }

        Pose3d pose = poseOpt.get();
        x = pose.getX();
        y = pose.getY();
        z = pose.getZ();
        heading = pose.getRotation().toRotation2d();

        Rotation2d targetAngle = new Rotation2d(hub[0] - x, hub[1] - y);
        return heading.minus(targetAngle).minus(robotToShooter()).getDegrees();
    }

    /**
     * Calculate distance to hub.
     *
     * @return Optional distance in meters, empty if scoring not allowed
     */
    public Optional<Double> hubDistance() {
        UpdateHubLocation();

        if (!canScore()) {
            return Optional.empty();
        }

        Optional<Pose3d> poseOpt = vision.getPose3d(getRobotPose());
        if (poseOpt.isEmpty()) {
            return Optional.empty();
        }

        Pose3d pose = poseOpt.get();
        x = pose.getX();
        y = pose.getY();
        z = pose.getZ();
        heading = pose.getRotation().toRotation2d();

        Translation2d a = new Translation2d(x, y);
        Translation2d b = new Translation2d(hub[0], hub[1]);

        return Optional.of(a.getDistance(b));
    }

    /**
     * Calculate required launch velocity based on distance.
     *
     * @return Required velocity in m/s, or 0 if shot not possible
     */
    public double getRequiredVelocity() {
        Optional<Double> dx_proxy = hubDistance();

        if (dx_proxy.isEmpty()) {
            return 0.0;
        }

        Optional<Pose3d> poseOpt = vision.getPose3d(getRobotPose());
        if (poseOpt.isEmpty()) {
            return 0.0;
        }

        Pose3d pose = poseOpt.get();
        z = pose.getZ();

        double dx = dx_proxy.get();
        double dy = ShooterConstants.HUB_RIM_HEIGHT - (z + ShooterConstants.Z_OFFSET);
        double g = ShooterConstants.G_ACCEL;
        double theta = Math.toRadians(ShooterConstants.LAUNCH_ANGLE);

        return dx / Math.cos(theta) * Math.sqrt(g / 2 * (-dy + dx * Math.tan(theta)));
    }

    /**
     * Calculate optimal pitch angle for current position. This is a simplified
     * calculation - adjust
     * based on testing.
     *
     * @return Optimal pitch angle in degrees
     */
    public double calculateOptimalPitch() {
        Optional<Double> distanceOpt = hubDistance();

        if (distanceOpt.isEmpty()) {
            return ShooterConstants.LAUNCH_ANGLE;
        }

        double distance = distanceOpt.get();
        return calculatePitchFromDistance(distance);
    }

    /**
     * Calculate optimal pitch angle using AprilTag vision for precise targeting.
     *
     * <p>
     * This method uses the vision system to get a more accurate position estimate
     * when AprilTags
     * are visible. Falls back to odometry-based calculation if vision data is
     * unavailable or
     * unreliable.
     *
     * @return VisionAimedShot containing pitch angle and confidence metrics
     */
    public VisionAimedShot calculateOptimalPitchWithVision() {
        UpdateHubLocation();

        // Try to get vision-based pose
        Optional<Vision.VisionUpdate> visionUpdateOpt = vision.getP().getBestVisionUpdate(getRobotPose());

        if (visionUpdateOpt.isEmpty()) {
            // No vision data - fall back to odometry
            double odometryPitch = calculateOptimalPitch();
            return new VisionAimedShot(
                    odometryPitch,
                    false, // not vision-assisted
                    0, // no tags
                    1.0, // max ambiguity (no confidence)
                    0.0, // unknown distance
                    "No AprilTags detected - using odometry");
        }

        Vision.VisionUpdate visionUpdate = visionUpdateOpt.get();

        // Calculate distance to hub using vision pose
        double visionX = visionUpdate.pose3d().getX();
        double visionY = visionUpdate.pose3d().getY();
        double visionZ = visionUpdate.pose3d().getZ();

        double dx = hub[0] - visionX;
        double dy = hub[1] - visionY;
        double horizontalDistance = Math.sqrt(dx * dx + dy * dy);

        // Calculate vertical component for more accurate pitch
        double dz = ShooterConstants.HUB_RIM_HEIGHT - (visionZ + ShooterConstants.Z_OFFSET);

        // Use physics-based pitch calculation when we have good vision data
        double pitch;
        if (visionUpdate.tagCount() >= 2 && visionUpdate.avgAmbiguity() < 0.2) {
            // High confidence - use physics-based calculation
            pitch = calculatePitchPhysics(horizontalDistance, dz);
        } else {
            // Lower confidence - use interpolation table
            pitch = calculatePitchFromDistance(horizontalDistance);
        }

        // Determine confidence level
        String confidence;
        if (visionUpdate.tagCount() >= 2 && visionUpdate.avgAmbiguity() < 0.15) {
            confidence = "HIGH - Multiple tags, low ambiguity";
        } else if (visionUpdate.tagCount() >= 1 && visionUpdate.avgAmbiguity() < 0.3) {
            confidence = "MEDIUM - Vision-assisted";
        } else {
            confidence = "LOW - High ambiguity, verify manually";
        }

        return new VisionAimedShot(
                pitch,
                true,
                visionUpdate.tagCount(),
                visionUpdate.avgAmbiguity(),
                horizontalDistance,
                confidence);
    }

    /**
     * Check if vision targeting is available and reliable.
     *
     * @return true if AprilTags are visible with acceptable ambiguity
     */
    public boolean hasReliableVisionTarget() {
        Optional<Vision.VisionUpdate> visionUpdateOpt = vision.getP().getBestVisionUpdate(getRobotPose());

        if (visionUpdateOpt.isEmpty()) {
            return false;
        }

        Vision.VisionUpdate update = visionUpdateOpt.get();
        return update.tagCount() >= 1 && update.avgAmbiguity() < 0.4;
    }

    /**
     * Calculate pitch angle from horizontal distance using interpolation. Tune
     * these values based on
     * testing.
     *
     * @param distance Horizontal distance to hub in meters
     * @return Pitch angle in degrees
     */
    private double calculatePitchFromDistance(double distance) {
        // Interpolation table - tune based on testing
        double minDistance = 2.0; // meters
        double maxDistance = 8.0; // meters
        double closeAngle = 70.0; // degrees for close shots
        double farAngle = 35.0; // degrees for far shots

        if (distance <= minDistance) {
            return closeAngle;
        } else if (distance >= maxDistance) {
            return farAngle;
        }

        // Linear interpolation
        double t = (distance - minDistance) / (maxDistance - minDistance);
        return closeAngle + t * (farAngle - closeAngle);
    }

    /**
     * Calculate pitch angle using physics-based projectile motion. Used when we
     * have high-confidence
     * vision data.
     *
     * @param horizontalDistance Distance to target (meters)
     * @param verticalDistance   Height difference to target (meters, positive =
     *                           target higher)
     * @return Optimal pitch angle in degrees
     */
    private double calculatePitchPhysics(double horizontalDistance, double verticalDistance) {
        // Projectile motion: we need to solve for launch angle
        // Using the equation: tan(theta) = (v^2 +/- sqrt(v^4 - g(gx^2 + 2yv^2))) / (gx)
        // For simplicity, we'll use an iterative approach

        double g = ShooterConstants.G_ACCEL;

        // Estimate launch velocity from flywheel RPM (this needs calibration)
        // Assuming linear relationship between RPM and ball exit velocity
        double estimatedVelocity = ShooterConstants.FLYWHEEL_SHOOT_RPM * 0.001; // (placeholder TODO - calibrate)

        // Use high arc solution (+ sqrt term) for better accuracy
        double v2 = estimatedVelocity * estimatedVelocity;
        double v4 = v2 * v2;
        double x = horizontalDistance;
        double y = verticalDistance;

        double discriminant = v4 - g * (g * x * x + 2 * y * v2);

        if (discriminant < 0) {
            // Target unreachable at current velocity - return max angle
            return ShooterConstants.PITCH_MAX_ANGLE;
        }

        double sqrtDiscriminant = Math.sqrt(discriminant);
        double tanTheta = (v2 + sqrtDiscriminant) / (g * x);
        double theta = Math.toDegrees(Math.atan(tanTheta));

        // Clamp to valid range
        return Math.max(
                ShooterConstants.PITCH_MIN_ANGLE, Math.min(ShooterConstants.PITCH_MAX_ANGLE, theta));
    }

    /** Result of vision-assisted shot calculation. */
    public record VisionAimedShot(
            double pitchAngle, // Calculated pitch angle in degrees
            boolean visionAssisted, // True if AprilTags were used
            int tagCount, // Number of AprilTags detected
            double ambiguity, // Vision ambiguity (0 = perfect, 1 = bad)
            double distanceToHub, // Calculated distance to hub in meters
            String confidenceDescription // Human-readable confidence level
    ) {
        /** Check if this shot has high confidence. */
        public boolean isHighConfidence() {
            return visionAssisted && tagCount >= 2 && ambiguity < 0.2;
        }

        /** Check if this shot should be taken automatically. */
        public boolean isSafeForAutoShot() {
            return visionAssisted && tagCount >= 1 && ambiguity < 0.3;
        }
    }
}
