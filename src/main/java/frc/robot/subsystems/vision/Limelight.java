package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Pose3d;

public class Limelight {
    private String name;

    public Limelight(String n) {
        name = n;
        LimelightHelpers.setPipelineIndex(name, 0);
    }

    public boolean hasTarget() {
        return LimelightHelpers.getTV(name);
    }

    public Pose3d getCFrame() {
        // X, Y, Z, & Rotation
        // 0 degrees rot is facing camera
        return LimelightHelpers.getCameraPose3d_TargetSpace(name);
    }
}