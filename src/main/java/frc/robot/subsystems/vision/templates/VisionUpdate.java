package frc.robot.subsystems.vision.templates;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;

public record VisionUpdate(
        Pose3d pose3d,
        Pose2d pose2d,
        double timestampSeconds,
        int tagCount,
        double avgDistanceMeters,
        double avgAmbiguity) {
}