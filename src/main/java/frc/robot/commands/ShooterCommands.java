package frc.robot.commands;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.Constants.ShooterConstants;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.Physics.VisionAimedShot;

public class ShooterCommands {
    private ShooterCommands() {
    }

    /** Command to spin up flywheel and wait until ready. */
    public Command spinUpCommand(Shooter shooter) {
        return Commands.sequence(
                Commands.runOnce(shooter.getF()::spinUp, shooter),
                Commands.waitUntil(shooter.getF()::isFlywheelAtSpeed))
                .withName("Spin Up Shooter");
    }

    /**
     * Command to prepare shooter with automatic pitch calculation. Uses trajectory
     * calculation to
     * determine optimal pitch angle.
     */
    public Command prepareAutoShotCommand(Shooter shooter) {
        return Commands.sequence(
                Commands.runOnce(
                        () -> {
                            double calculatedPitch = shooter.getP().calculateOptimalPitch();
                            shooter.prepareShot(calculatedPitch, ShooterConstants.FLYWHEEL_SHOOT_RPM);
                        },
                        shooter),
                Commands.waitUntil(shooter.getF()::isReadyToShoot))
                .withName("Prepare Auto Shot");
    }

    /**
     * Command to prepare shooter using vision-assisted targeting. Uses AprilTags
     * for more precise
     * distance/angle calculation. Falls back to odometry if no tags visible.
     */
    public Command prepareVisionShotCommand(Shooter shooter) {
        return Commands.sequence(
                Commands.runOnce(
                        () -> {
                            VisionAimedShot shot = shooter.getP().calculateOptimalPitchWithVision();
                            shooter.prepareShot(shot.pitchAngle(), ShooterConstants.FLYWHEEL_SHOOT_RPM);

                            // Log the shot info for driver feedback
                            SmartDashboard.putBoolean("Shooter/VisionAssisted", shot.visionAssisted());
                            SmartDashboard.putString("Shooter/ShotConfidence", shot.confidenceDescription());
                            SmartDashboard.putNumber("Shooter/VisionDistance", shot.distanceToHub());
                        },
                        shooter),
                Commands.waitUntil(shooter.getF()::isReadyToShoot))
                .withName("Prepare Vision Shot");
    }

    /**
     * Command to continuously track hub with vision and update pitch. Run this
     * while waiting for a
     * shot opportunity.
     */
    public Command trackHubCommand(Shooter shooter) {
        return Commands.run(
                () -> {
                    if (shooter.getP().hasReliableVisionTarget()) {
                        VisionAimedShot shot = shooter.getP().calculateOptimalPitchWithVision();
                        shooter.getO().setPitchAngle(shot.pitchAngle());
                    }
                },
                shooter)
                .withName("Track Hub");
    }

    /**
     * Command to spin up and aim using vision, then wait for driver trigger.
     * Continuously updates
     * pitch based on AprilTag data.
     */
    public Command aimAndSpinUpCommand(Shooter shooter) {
        return Commands.parallel(
                // Keep flywheel spinning
                Commands.run(shooter.getF()::spinUp, shooter),
                // Continuously track the hub
                Commands.run(
                        () -> {
                            VisionAimedShot shot = shooter.getP().calculateOptimalPitchWithVision();
                            shooter.getO().setPitchAngle(shot.pitchAngle());

                            // Update dashboard
                            SmartDashboard.putBoolean("Shooter/VisionAssisted", shot.visionAssisted());
                            SmartDashboard.putBoolean("Shooter/HighConfidence", shot.isHighConfidence());
                            SmartDashboard.putNumber("Shooter/AimPitch", shot.pitchAngle());
                        },
                        shooter))
                .withName("Aim and Spin Up");
    }

    /** Command to stop the shooter. */
    public Command stopCommand(Shooter shooter) {
        return Commands.runOnce(shooter::stop, shooter).withName("Stop Shooter");
    }

    /** Command to hold flywheel at idle speed (for warmup). */
    public Command idleCommand(Shooter shooter) {
        return Commands.run(shooter.getF()::spinUpIdle, shooter).withName("Idle Shooter");
    }
}
