package frc.robot.auto;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.commands.SwerveCommands;
import frc.robot.subsystems.hopper.Hopper;
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
    // Heading matches alliance facing direction so robot doesn't spin 180°
    return new Pose2d(x, FieldConstants.CENTER_Y,
        Rotation2d.fromDegrees(isRedAlliance() ? 180 : 0));
  }

  // getTowerPose() is correct but currently unreferenced by any auto routine.
  // Wire it up when a climb auto is implemented. Leave in place to avoid losing the logic.
  private static Pose2d getTowerPose() {
    double towerX = isRedAlliance() ? FieldConstants.RED_TOWER_X : FieldConstants.BLUE_TOWER_X;
    double towerY = isRedAlliance() ? FieldConstants.RED_TOWER_Y : FieldConstants.BLUE_TOWER_Y;
    double offset = isRedAlliance() ? -1.0 : 1.0;
    return new Pose2d(towerX + offset, towerY, Rotation2d.fromDegrees(0));
  }

  // ================================================================
  // VISION POSE SEEDING
  // ================================================================

  public static Command seedPoseFromVision(SwerveDrive swerve, Vision vision) {
    return Commands.runOnce(
            () ->
                vision
                    .getBestVisionUpdateRaw(swerve.getPose())
                    .ifPresent(update -> swerve.resetPose(update.pose2d())),
            swerve)
        .withName("Seed Pose From Vision");
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
  // MODE 2: SCORE ONLY (shoot preload, hold)
  // ================================================================

  public static Command scoreOnlyAuto(
      SwerveDrive swerve, Intake intake, Shooter shooter, Hopper hopper) {
    return Commands.sequence(
            AutoCommands.logMessage("Mode 2: Score Only"),
            AutoCommands.shootAllFuel(shooter, intake, hopper),
            AutoCommands.stopDriving(swerve),
            AutoCommands.holdPosition(swerve, 10.0),
            AutoCommands.logMessage("Mode 2 Complete"))
        .withName("2: Score Only");
  }

  // ================================================================
  // MODE 1: SCORE & COLLECT
  // ================================================================

  public static Command scoreCollectAuto(
      SwerveDrive swerve, Intake intake, Shooter shooter, Hopper hopper) {
    return Commands.sequence(
            AutoCommands.logMessage("Mode 1: Score & Collect"),

            // Phase 1: Shoot preload
            AutoCommands.shootAllFuel(shooter, intake, hopper),

            // Phase 2: Drive to neutral, collect FUEL (belt+hopper run to stage FUEL)
            AutoCommands.driveToPose(swerve, getNeutralPose()).withTimeout(4.0),
            Commands.parallel(
                    // Robot-relative +X = "forward" from robot's perspective.
                    // Both alliances face toward neutral after driveToPose, so
                    // positive speed always drives toward FUEL.
                    new SwerveCommands.DriveDistanceCommand(
                        swerve, AutoConstants.AUTO_INTAKE_DRIVE_SPEED, 0, 2.0),
                    Commands.sequence(
                        Commands.runOnce(intake::deployIntakeMechanism, intake),
                        Commands.waitUntil(intake.getD()::isDeployed).withTimeout(1.0),
                        Commands.run(intake.getR()::runIntake, intake)),
                    Commands.startEnd(hopper::feed, hopper::stop, hopper))
                .withTimeout(AutoConstants.INTAKE_TIMEOUT),
            Commands.runOnce(intake.getR()::stopRollers, intake),
            Commands.runOnce(intake::retractIntakeMechanism, intake),

            // Phase 3: Return and shoot
            AutoCommands.driveToPose(swerve, getShootingPose()).withTimeout(4.0),
            AutoCommands.shootAllFuel(shooter, intake, hopper),
            AutoCommands.logMessage("Mode 1 Complete"))
        .withName("1: Score & Collect");
  }

  // ================================================================
  // MODE 3: PRELOAD ONLY
  // ================================================================

  public static Command preloadOnlyAuto(
      SwerveDrive swerve, Intake intake, Shooter shooter, Hopper hopper) {
    return Commands.sequence(
            AutoCommands.logMessage("Mode 3: Preload Only"),
            AutoCommands.shootAllFuel(shooter, intake, hopper),
            AutoCommands.stopDriving(swerve),
            AutoCommands.holdPosition(swerve, 10.0),
            AutoCommands.logMessage("Mode 3 Complete"))
        .withName("3: Preload Only");
  }

  // ================================================================
  // MODE SELECTOR
  // ================================================================

  public static Command getAutoFromSelection(
      int selection,
      SwerveDrive swerve,
      Intake intake,
      Shooter shooter,
      Vision vision,
      Hopper hopper) {
    Command routine;
    switch (selection) {
      case AutoConstants.AUTO_DO_NOTHING:
        return doNothing();
      case AutoConstants.AUTO_SCORE_COLLECT:
        routine = scoreCollectAuto(swerve, intake, shooter, hopper);
        break;
      case AutoConstants.AUTO_SCORE_ONLY:
        routine = scoreOnlyAuto(swerve, intake, shooter, hopper);
        break;
      case AutoConstants.AUTO_PRELOAD_ONLY:
        routine = preloadOnlyAuto(swerve, intake, shooter, hopper);
        break;
      default:
        return doNothing();
    }
    return Commands.sequence(seedPoseFromVision(swerve, vision), routine);
  }
}