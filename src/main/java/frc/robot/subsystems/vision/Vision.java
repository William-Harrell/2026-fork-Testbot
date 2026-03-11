package frc.robot.subsystems.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import java.io.IOException;
import java.util.Optional;

public class Vision extends SubsystemBase {
  public record VisionUpdate(
      Pose3d pose3d,
      Pose2d pose2d,
      double timestampSeconds,
      int tagCount,
      double avgDistanceMeters,
      double avgAmbiguity) {
  }

  private final AprilTagFieldLayout fieldLayout;
  private Photon photon;
  // private Limelight limelight;

  public Vision() {
    AprilTagFieldLayout layout = null;

    // First try the custom field JSON in the deploy directory.
    try {
      layout = new AprilTagFieldLayout(
          Filesystem.getDeployDirectory().toPath().resolve("2026-rebuilt-welded.json"));
    } catch (IOException e) {
      // Custom file missing or unreadable — log and fall back.
      System.err.println("[Vision] WARNING: Could not load 2026-rebuilt-welded.json: " + e.getMessage());
    }

    // If the custom file failed, try the WPILib built-in layout for the current game.
    if (layout == null) {
      try {
        layout = AprilTagFieldLayout.loadField(edu.wpi.first.apriltag.AprilTagFields.kDefaultField);
        System.err.println("[Vision] Using WPILib default AprilTag field layout as fallback.");
      } catch (Exception e2) {
        // Still failed — vision pose estimation will be disabled but robot won't crash.
        System.err.println("[Vision] ERROR: Could not load any AprilTag field layout. Vision pose estimation disabled.");
      }
    }

    fieldLayout = layout;
    photon = new Photon(Optional.ofNullable(fieldLayout));
    // limelight = new Limelight(VisionConstants.LIMELIGHT_NAME);
  }

  public Photon getP() {
    return photon;
  }

  /** Called every 20ms by the CommandScheduler via SubsystemBase. Clears the
   *  per-loop vision cache so the next caller gets a fresh read from cameras. */
  public void periodic() {
    photon.invalidateCache();
  }

  /*
   * public Limelight getL() {
   * return limelight;
   * }
   */
  /**
   * Get the robot's 2D pose from AprilTag detection.
   *
   * @param robotPose Current odometry pose (for filtering bad detections)
   * @return Robot pose if valid detection available, empty otherwise
   */
  public Optional<Pose2d> getPose2d(Pose2d robotPose) {
    // TODO LIMELIGHT VERSION HERE
    return photon.getBestVisionUpdate(robotPose).map(VisionUpdate::pose2d);
  }

  /**
   * Get the robot's 3D pose from AprilTag detection.
   *
   * @param robotPose Current odometry pose (for filtering)
   * @return 3D pose if available
   */
  public Optional<Pose3d> getPose3d(Pose2d robotPose) {
    return photon.getBestVisionUpdate(robotPose).map(VisionUpdate::pose3d);
  }

  /**
   * Get the raw vision update with all metadata.
   *
   * @param robotPose Current odometry pose
   * @return Complete vision update with confidence metrics
   */
  public Optional<VisionUpdate> getBestVisionUpdateRaw(Pose2d robotPose) {
    // TODO LIMELIGHT VERSION HERE
    return photon.getBestVisionUpdate(robotPose);
  }
}