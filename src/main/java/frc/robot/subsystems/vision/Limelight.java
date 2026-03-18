package frc.robot.subsystems.vision;

public class Limelight {
    private String name;

    public Limelight(String n) {
        name = n; // Stored first so all reads below use the correct camera name.

        // double txnc = LimelightHelpers.getTXNC(name);
        // double tync = LimelightHelpers.getTYNC(name);

        // Switch to pipeline 0
        LimelightHelpers.setPipelineIndex(name, 0);
    }

    public boolean hasTarget() {
        return LimelightHelpers.getTV(name);
    }

}