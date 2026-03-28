package frc.robot.subsystems.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.vision.templates.VisionUpdate;
import java.io.IOException;
import java.util.Optional;

public class Vision extends SubsystemBase {
  private final AprilTagFieldLayout fieldLayout;
  private final Photon photon;
  private final Limelight limelight;

  public Vision() {
    AprilTagFieldLayout layout = null;
    try {
      layout = new AprilTagFieldLayout(
          Filesystem.getDeployDirectory().toPath().resolve("2026-rebuilt-welded.json"));
    } catch (IOException e) {
      e.printStackTrace();
    }

    if (layout == null) {
      try {
        layout = AprilTagFieldLayout.loadField(edu.wpi.first.apriltag.AprilTagFields.kDefaultField);
        System.err.println("[Vision] Using WPILib default AprilTag field layout as fallback.");
      } catch (Exception e2) {
        e2.printStackTrace();
      }
    }

    this.fieldLayout = layout;
    photon = new Photon(Optional.ofNullable(fieldLayout));
    limelight = new Limelight(VisionConstants.LIMELIGHT_NAME);
  }

  // Sub-subsystem getters
  public Photon getP() {
    return photon;
  }

  public Limelight getL() {
    return limelight;
  }

  @Override
  public void periodic() {
    photon.invalidateCache();
  }

  // Parameter is odometry
  public Optional<Pose2d> getPose2d(Pose2d robotPose) {
    return photon.getBestVisionUpdate(robotPose).map(VisionUpdate::pose2d);
  }

  // Parameter is odometry
  public Optional<Pose3d> getPose3d(Pose2d robotPose) {
    return photon.getBestVisionUpdate(robotPose).map(VisionUpdate::pose3d);
  }

  // Parameter is odometry
  public Optional<VisionUpdate> getBestVisionUpdateRaw(Pose2d robotPose) {
    return photon.getBestVisionUpdate(robotPose);
  }
}