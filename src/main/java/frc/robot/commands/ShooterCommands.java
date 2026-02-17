package frc.robot.commands;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.Constants.ShooterConstants;
import frc.robot.subsystems.shooter.Physics.VisionAimedShot;

public class ShooterCommands {

    /** Command to spin up flywheel and wait until ready. */
    public Command spinUpCommand() {
        return Commands.sequence(
                Commands.runOnce(this::spinUp, this), Commands.waitUntil(this::isFlywheelAtSpeed))
                .withName("Spin Up Shooter");
    }

    /**
     * Command to prepare shooter with automatic pitch calculation. Uses trajectory
     * calculation to
     * determine optimal pitch angle.
     */
    public Command prepareAutoShotCommand() {
        return Commands.sequence(
                Commands.runOnce(
                        () -> {
                            double calculatedPitch = calculateOptimalPitch();
                            prepareShot(calculatedPitch, ShooterConstants.FLYWHEEL_SHOOT_RPM);
                        },
                        this),
                Commands.waitUntil(this::isReadyToShoot))
                .withName("Prepare Auto Shot");
    }

    /**
     * Command to prepare shooter using vision-assisted targeting. Uses AprilTags
     * for more precise
     * distance/angle calculation. Falls back to odometry if no tags visible.
     */
    public Command prepareVisionShotCommand() {
        return Commands.sequence(
                Commands.runOnce(
                        () -> {
                            VisionAimedShot shot = calculateOptimalPitchWithVision();
                            prepareShot(shot.pitchAngle(), ShooterConstants.FLYWHEEL_SHOOT_RPM);

                            // Log the shot info for driver feedback
                            SmartDashboard.putBoolean("Shooter/VisionAssisted", shot.visionAssisted());
                            SmartDashboard.putString("Shooter/ShotConfidence", shot.confidenceDescription());
                            SmartDashboard.putNumber("Shooter/VisionDistance", shot.distanceToHub());
                        },
                        this),
                Commands.waitUntil(this::isReadyToShoot))
                .withName("Prepare Vision Shot");
    }

    /**
     * Command to continuously track hub with vision and update pitch. Run this
     * while waiting for a
     * shot opportunity.
     */
    public Command trackHubCommand() {
        return Commands.run(
                () -> {
                    if (hasReliableVisionTarget()) {
                        VisionAimedShot shot = calculateOptimalPitchWithVision();
                        setPitchAngle(shot.pitchAngle());
                    }
                },
                this)
                .withName("Track Hub");
    }

    /**
     * Command to spin up and aim using vision, then wait for driver trigger.
     * Continuously updates
     * pitch based on AprilTag data.
     */
    public Command aimAndSpinUpCommand() {
        return Commands.parallel(
                // Keep flywheel spinning
                Commands.run(this::spinUp, this),
                // Continuously track the hub
                Commands.run(
                        () -> {
                            VisionAimedShot shot = calculateOptimalPitchWithVision();
                            setPitchAngle(shot.pitchAngle());

                            // Update dashboard
                            SmartDashboard.putBoolean("Shooter/VisionAssisted", shot.visionAssisted());
                            SmartDashboard.putBoolean("Shooter/HighConfidence", shot.isHighConfidence());
                            SmartDashboard.putNumber("Shooter/AimPitch", shot.pitchAngle());
                        },
                        this))
                .withName("Aim and Spin Up");
    }

    /** Command to stop the shooter. */
    public Command stopCommand() {
        return Commands.runOnce(this::stop, this).withName("Stop Shooter");
    }

    /** Command to hold flywheel at idle speed (for warmup). */
    public Command idleCommand() {
        return Commands.run(this::spinUpIdle, this).withName("Idle Shooter");
    }
}
