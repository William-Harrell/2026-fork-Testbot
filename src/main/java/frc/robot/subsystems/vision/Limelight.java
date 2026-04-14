package frc.robot.subsystems.vision;

import java.util.Optional;

import edu.wpi.first.math.geometry.Pose3d;
import frc.robot.util.LimelightHelpers;
import frc.robot.util.LimelightHelpers.LimelightResults;

public class Limelight {
    private String name;
    private Pose3d pose_cache;

    public Limelight(String n) {
        name = n;
        LimelightHelpers.setPipelineIndex(name, 0);
        LimelightHelpers.setRewindEnabled(name, true);
        pose_cache = getPose3d().get().orElse(null);
    }

    public boolean hasTarget() {
        return LimelightHelpers.getTV(name);
    }

    public LimelightResults getResults() {
        return LimelightHelpers.getLatestResults(name);
    }

    public Optional<Pose3d> getLastPose3d() {
        return Optional.ofNullable(pose_cache);
    }

    public Optional<Pose3d> getPose3d() {
        var results = getResults();

        if (results.targets_Fiducials.length <= 0) {
            return Optional.ofNullable(pose_cache);
        }
        // X, Y, Z, & Rotation
        // 0 degrees rot is facing camera

        var tag = results.targets_Fiducials[0]; // best tag
        Pose3d new_pose = tag.getCameraPose_TargetSpace();

        pose_cache = new_pose;
        return Optional.ofNullable(new_pose);
    }

    public void rewindRecord(double time) {
        LimelightHelpers.triggerRewindCapture(name, time);
    }
}