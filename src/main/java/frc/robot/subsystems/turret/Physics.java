package frc.robot.subsystems.turret;

import frc.robot.subsystems.vision.Limelight;
import frc.robot.subsystems.vision.Vision;
import frc.robot.util.constants.FieldConstants;
import frc.robot.util.constants.RobotPhysicalConstants;

public class Physics {
    private final Vision vision;
    private final Limelight limelight;

    private double last_pitch_req;
    private double last_yaw_req;

    public Physics(Vision vision) {
        this.vision = vision;
        this.limelight = this.vision.getL();
    }

    // Calculate pitch required from x, y (constant z)
    public double getPitchRequired(double current_rpm) {
        var pose_container = limelight.getPose3d();

        if (pose_container.isEmpty()) {
            return last_pitch_req;
        }

        var pose = pose_container.get();

        double gravity = 9.81;
        double dist = pose.getTranslation().getNorm();
        double height = FieldConstants.HUB_HEIGHT - RobotPhysicalConstants.TURRET_HEIGHT;
        double velocity = (2 * Math.PI * TurretConstants.FLYWHEEL_RADIUS * current_rpm) / 60;
        velocity *= TurretConstants.FLYWHEEL_EFFICIENCY;

        // Theta is the angle btwn trajectory & horizontal
        double theta = Math.atan((Math.pow(velocity, 2) / (gravity * dist))
                + Math.sqrt((Math.pow(velocity, 4) / Math.pow(gravity * dist, 2))
                        - ((2 * height * Math.pow(velocity, 2)) / (gravity * Math.pow(dist, 2))) - 1));
        theta += TurretConstants.OFFSET_PITCH;

        last_pitch_req = Math.max(TurretConstants.MIN_PITCH, Math.min(theta, TurretConstants.MAX_PITCH));
        return last_pitch_req;
    }

    // Calculate yaw required x, y (constant z)
    public double getYawError() {
        var pose_container = limelight.getPose3d();

        if (pose_container.isEmpty()) {
            return last_pitch_req;
        }

        var pose = pose_container.get();

        // TODO: MAY need to be inverted (see when testing)
        last_yaw_req = Math.toDegrees(pose.getRotation().getZ());

        return last_yaw_req;
    }

}
