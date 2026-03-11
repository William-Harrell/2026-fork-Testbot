package frc.robot.commands;

import java.util.Objects;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.shooter.Physics.VisionAimedShot;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.ShooterConstants;
import frc.robot.util.Elastic;

public class ShooterCommands {
  private ShooterCommands() {}

  /** Command to spin up flywheel and wait until ready. */
  public static Command spinUpCommand(Shooter shooter) {
    return Commands.sequence(
            Commands.runOnce(shooter.getF()::spinUp, shooter),
            Commands.waitUntil(shooter.getF()::isFlywheelAtSpeed))
        .withName("Spin Up Shooter");
  }

  /**
   * Command to prepare shooter with automatic pitch calculation. Uses trajectory calculation to
   * determine optimal pitch angle.
   */
  public static Command prepareAutoShotCommand(Shooter shooter) {
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
   * Command to prepare shooter using vision-assisted targeting. Uses AprilTags for more precise
   * distance/angle calculation. Falls back to odometry if no tags visible.
   */
  public static Command prepareVisionShotCommand(Shooter shooter) {
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
   * Command to continuously track hub with vision and update pitch. Run this while waiting for a
   * shot opportunity.
   */
  public static Command trackHubCommand(Shooter shooter) {
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
   * Command to spin up and aim using vision, then wait for driver trigger. Continuously updates
   * pitch based on AprilTag data.
   */
  public static Command aimAndSpinUpCommand(Shooter shooter) {
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
  public static Command stopCommand(Shooter shooter) {
    return Commands.runOnce(shooter::stop, shooter).withName("Stop Shooter");
  }

  /** Command to hold flywheel at idle speed (for warmup). */
  public static Command idleCommand(Shooter shooter) {
    return Commands.run(shooter.getF()::spinUpIdle, shooter).withName("Idle Shooter");
  }

  /**
   * Hold-to-shoot command for teleop.
   *
   * <p>While button is held: spins up flywheel + sets pitch, waits until ready, then feeds FUEL.
   * On release: stops shooter and rollers.
   */
  public static Command shootCommand(Shooter shooter, Intake intake) {
    Objects.requireNonNull(shooter, "Shooter must not be null");
    Objects.requireNonNull(intake,  "Intake must not be null");
    
    return Commands.sequence(
            Commands.runOnce(shooter::prepareDefaultShot, shooter),
            Commands.waitUntil(shooter.getF()::isReadyToShoot),
            // Check hub active status
            Commands.runOnce(
                () -> {
                  if (!shooter.getP().isHubActive()) {
                    Elastic.sendNotification(
                        new Elastic.Notification()
                            .withLevel(Elastic.NotificationLevel.WARNING)
                            .withTitle("HUB INACTIVE")
                            .withDescription("Your hub is off this shift — 0 pts!")
                            .withDisplaySeconds(3.0));
                  }
                }),
            // G407: only feed if robot is inside the alliance zone
            Commands.either(
                Commands.run(intake.getR()::feedToShooter, intake),
                Commands.runOnce(
                    () -> {
                      SmartDashboard.putString("Shooter/Warning", "G407: NOT IN ALLIANCE ZONE");
                      Elastic.sendNotification(
                          new Elastic.Notification()
                              .withLevel(Elastic.NotificationLevel.ERROR)
                              .withTitle("G407 VIOLATION")
                              .withDescription("Not in alliance zone — cannot shoot!")
                              .withDisplaySeconds(5.0));
                    }),
                shooter.getP()::isInAllianceZone))
        .finallyDo(
            interrupted -> {
              shooter.stop();
              intake.getR().stopRollers();
            })
        .withName("Shoot");
  }
}