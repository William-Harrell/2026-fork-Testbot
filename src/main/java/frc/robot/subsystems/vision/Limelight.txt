package frc.robot.subsystems.vision;

import java.util.List;
import java.util.Optional;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystems.vision.LimelightHelpers.LimelightResults;
import frc.robot.subsystems.vision.Vision.VisionUpdate;

public class Limelight {
    private double tx;
    private double ty;
    private double ta;
    private String name;
    private boolean hasTargets;

    public Limelight(String n) {
        // Basic targeting data
        tx = LimelightHelpers.getTX(""); // Horizontal offset from crosshair to target in degrees
        ty = LimelightHelpers.getTY(""); // Vertical offset from crosshair to target in degrees
        ta = LimelightHelpers.getTA(""); // Target area (0% to 100% of image)
        name = n;

        double txnc = LimelightHelpers.getTXNC(""); // Horizontal offset from principal pixel/point to target in degrees
        double tync = LimelightHelpers.getTYNC(""); // Vertical offset from principal pixel/point to target in degrees

        // Switch to pipeline 0
        LimelightHelpers.setPipelineIndex("", 0);
    }

    public boolean hasTarget() {
        return LimelightHelpers.getTV(name);
    }

}
