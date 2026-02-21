package frc.robot.subsystems.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.wpilibj.Filesystem;
import java.io.IOException;
import java.util.Optional;

public class Vision {
  public record VisionUpdate(
      Pose3d pose3d,
      Pose2d pose2d,
      double timestampSeconds,
      int tagCount,
      double avgDistanceMeters,
      double avgAmbiguity) {}

  private final AprilTagFieldLayout fieldLayout;
  private Photon photon;

  public Vision() {
    // Load tags and let us know if it fails. don't screw w/ program @ runtime.
    try {
      fieldLayout =
          new AprilTagFieldLayout(
              Filesystem.getDeployDirectory().toPath().resolve("2026-rebuilt-welded.json"));
    } catch (IOException e) {
      throw new RuntimeException("Failed to load AprilTag layout", e);
    }

    photon = new Photon(Optional.of(fieldLayout));
  }

  public Photon getP() {
    return photon;
  }

  /**
   * Get the robot's 2D pose from AprilTag detection.
   *
   * @param robotPose Current odometry pose (for filtering bad detections)
   * @return Robot pose if valid detection available, empty otherwise
   */
  public Optional<Pose2d> getPose2d(Pose2d robotPose) {
    return photon.getBestVisionUpdate(robotPose).map(VisionUpdate::pose2d);
  }

  /**
   * Get the robot's 3D pose from AprilTag detection.
   *
   * @param robotPose Current odometry pose (for filtering)
   * @return 3D pose if available
   */
  public Optional<Pose3d> getPose3d(Pose2d robotPose) {
    return photon.getBestVisionUpdate(robotPose).map(update -> update.pose3d());
  }

  /**
   * Get the raw vision update with all metadata.
   *
   * @param robotPose Current odometry pose
   * @return Complete vision update with confidence metrics
   */
  public Optional<VisionUpdate> getBestVisionUpdateRaw(Pose2d robotPose) {
    return photon.getBestVisionUpdate(robotPose);
  }
}
