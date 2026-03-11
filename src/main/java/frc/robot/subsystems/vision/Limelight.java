package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystems.vision.LimelightHelpers.LimelightResults;
import frc.robot.subsystems.vision.Vision.VisionUpdate;
import java.util.List;
import java.util.Optional;

public class Limelight {
    private double tx;
    private double ty;
    private double ta;
    private String name;
    private boolean hasTargets;

    public Limelight(String n) {
        name = n; // Stored first so all reads below use the correct camera name.

        // Basic targeting data — use name, not "" (which reads from the default camera).
        tx = LimelightHelpers.getTX(name);
        ty = LimelightHelpers.getTY(name);
        ta = LimelightHelpers.getTA(name);

        double txnc = LimelightHelpers.getTXNC(name);
        double tync = LimelightHelpers.getTYNC(name);

        // Switch to pipeline 0
        LimelightHelpers.setPipelineIndex(name, 0);
    }

    public boolean hasTarget() {
        return LimelightHelpers.getTV(name);
    }

}