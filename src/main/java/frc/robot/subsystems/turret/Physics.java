package frc.robot.subsystems.turret;

import frc.robot.subsystems.vision.Limelight;
import frc.robot.subsystems.vision.Vision;
import frc.robot.util.constants.RobotPhysicalConstants;

public class Physics {
    private final Vision vision;
    private final Limelight limelight;

    private double last_pitch_req;
    private double last_yaw_req;

    public Physics(Vision vision) {
        this.vision = vision;
        this.limelight = vision.getL();
    }

    // Calculate pitch required from x, y (constant z)
    public double getPitch() {
        var pose_container = limelight.getPose3d();

        if (pose_container.isEmpty()) {
            return last_pitch_req;
        }

        var pose = pose_container.get();

        double dist = Math.hypot(pose.getX(), pose.getY()); // meters
        double theta;
        double height = RobotPhysicalConstants.TURRET_HEIGHT;

        // TODO Tristan says height changes with pitch

        // Rhys' math

        return theta;
    }

    // Calculate yaw required x, y (constant z)
    public double getYawError() {
        var pose_container = limelight.getPose3d();

        if (pose_container.isEmpty()) {
            return last_pitch_req;
        }

        var pose = pose_container.get();

        return Math.toDegrees(pose.getRotation().getZ());
    }

}
