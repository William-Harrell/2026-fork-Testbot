package frc.robot.auto;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.climber.Climber;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.swerve.SwerveDrive;
import frc.robot.subsystems.vision.Vision;
import frc.robot.util.constants.FieldConstants;

public final class AutoRoutines {

  private AutoRoutines() {}

  // ================================================================
  // ALLIANCE-AWARE HELPERS
  // ================================================================

  private static boolean isRedAlliance() {
    return DriverStation.getAlliance().orElse(Alliance.Blue) == Alliance.Red;
  }

  private static Pose2d getShootingPose() {
    double x = isRedAlliance() ? FieldConstants.FIELD_LENGTH - 2.5 : 2.5;
    return new Pose2d(
        x, FieldConstants.CENTER_Y, Rotation2d.fromDegrees(isRedAlliance() ? 180 : 0));
  }

  private static Pose2d getNeutralPose() {
    double x =
        FieldConstants.CENTER_X
            + (isRedAlliance()
                ? -FieldConstants.NEUTRAL_CLOSE_OFFSET
                : FieldConstants.NEUTRAL_CLOSE_OFFSET);
    return new Pose2d(x, FieldConstants.CENTER_Y, Rotation2d.fromDegrees(0));
  }

  private static Pose2d getTowerPose() {
    double towerX = isRedAlliance() ? FieldConstants.RED_TOWER_X : FieldConstants.BLUE_TOWER_X;
    double towerY = isRedAlliance() ? FieldConstants.RED_TOWER_Y : FieldConstants.BLUE_TOWER_Y;
    double offset = isRedAlliance() ? -1.0 : 1.0;
    return new Pose2d(towerX + offset, towerY, Rotation2d.fromDegrees(0));
  }

  // ================================================================
  // VISION POSE SEEDING
  // ================================================================

  static Command seedPoseFromVision(SwerveDrive swerve, Vision vision) {
    return Commands.runOnce(
            () ->
                vision
                    .getBestVisionUpdateRaw(swerve.getPose())
                    .ifPresent(update -> swerve.resetPose(update.pose2d())),
            swerve)
        .withName("Seed Pose From Vision");
  }

  // ================================================================
  // AUTO CLIMB helper
  // ================================================================

  private static Command autoClimb(SwerveDrive swerve, Climber climber) {
    return Commands.sequence(
        AutoCommands.driveToPose(swerve, getTowerPose()),
        Commands.runOnce(climber::extend, climber),
        Commands.waitUntil(climber::isExtended).withTimeout(AutoConstants.CLIMB_TIMEOUT),
        Commands.runOnce(climber::stop, climber));
  }

  // ================================================================
  // MODE 0: DO NOTHING
  // ================================================================

  public static Command doNothing() {
    return Commands.sequence(
            AutoCommands.logMessage("Mode 0: Do Nothing"), Commands.waitSeconds(15.0))
        .withName("0: Do Nothing");
  }

  // ================================================================
  // MODE 1: SCORE, COLLECT & CLIMB
  // ================================================================

  public static Command scoreCollectAndClimbAuto(
      SwerveDrive swerve, Intake intake, Shooter shooter, Climber climber) {
    return Commands.sequence(
            AutoCommands.logMessage("Mode 1: Score, Collect & Climb"),

            // Phase 1: Shoot preload
            AutoCommands.shootAllFuel(shooter, intake),

            // Phase 2: Drive to neutral, collect FUEL
            AutoCommands.driveToPose(swerve, getNeutralPose()),
            Commands.parallel(
                    AutoCommands.driveForward(swerve, 2.0, AutoConstants.AUTO_INTAKE_DRIVE_SPEED),
                    Commands.sequence(
                        Commands.runOnce(intake::deployIntakeMechanism, intake),
                        Commands.waitUntil(intake.getD()::isDeployed),
                        Commands.run(intake.getR()::runIntake, intake)))
                .withTimeout(AutoConstants.INTAKE_TIMEOUT),
            Commands.runOnce(intake.getR()::stopRollers, intake),
            Commands.runOnce(intake::retractIntakeMechanism, intake),

            // Phase 3: Return and shoot
            AutoCommands.driveToPose(swerve, getShootingPose()),
            AutoCommands.shootAllFuel(shooter, intake),

            // Phase 4: Climb
            autoClimb(swerve, climber),
            AutoCommands.logMessage("Mode 1 Complete"))
        .withName("1: Score, Collect & Climb");
  }

  // ================================================================
  // MODE 2: QUICK CLIMB
  // ================================================================

  public static Command quickClimbAuto(SwerveDrive swerve, Intake intake, Shooter shooter, Climber climber) {
    return Commands.sequence(
            AutoCommands.logMessage("Mode 2: Quick Climb"),
            AutoCommands.shootAllFuel(shooter, intake),
            autoClimb(swerve, climber),
            AutoCommands.logMessage("Mode 2 Complete"))
        .withName("2: Quick Climb");
  }

  // ================================================================
  // MODE 3: PRELOAD ONLY
  // ================================================================

  public static Command preloadOnlyAuto(SwerveDrive swerve, Intake intake, Shooter shooter) {
    return Commands.sequence(
            AutoCommands.logMessage("Mode 3: Preload Only"),
            AutoCommands.shootAllFuel(shooter, intake),
            AutoCommands.stopDriving(swerve),
            AutoCommands.holdPosition(swerve, 10.0),
            AutoCommands.logMessage("Mode 3 Complete"))
        .withName("3: Preload Only");
  }

  // ================================================================
  // MODE SELECTOR
  // ================================================================

  public static Command getAutoFromSelection(
      int selection, SwerveDrive swerve, Intake intake, Shooter shooter, Climber climber, Vision vision) {
    Command routine;
    switch (selection) {
      case AutoConstants.AUTO_DO_NOTHING:
        return doNothing();
      case AutoConstants.AUTO_SCORE_COLLECT_CLIMB:
        routine = scoreCollectAndClimbAuto(swerve, intake, shooter, climber);
        break;
      case AutoConstants.AUTO_QUICK_CLIMB:
        routine = quickClimbAuto(swerve, intake, shooter, climber);
        break;
      case AutoConstants.AUTO_PRELOAD_ONLY:
        routine = preloadOnlyAuto(swerve, intake, shooter);
        break;
      default:
        return doNothing();
    }
    return Commands.sequence(seedPoseFromVision(swerve, vision), routine);
  }
}
