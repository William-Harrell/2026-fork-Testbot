package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.intake.Intake;

public final class IntakeCommands {
  private IntakeCommands() {
  }

  public static Command toggleDirection(Intake intake) {
    return Commands.runOnce(intake.getR()::toggleIntakeOutake);
  }

  // /** Command to run outtake while button held */
  // private Command outtakeCommand(Intake intake) {
  // return Commands.startEnd(intake.getR()::runOuttake,
  // intake.getR()::stopRollers, intake)
  // .withName("Outtake FUEL");
  // }

  /** Command to deploy and continuously intake (hold to run) */
  public static Command holdToIntakeCommand(Intake intake) {
    return Commands.sequence(
        Commands.runOnce(intake::deployIntakeMechanism, intake),
        Commands.waitUntil(intake.getD()::isDeployed),
        Commands.run(intake.getR()::runIntake, intake))
        .finallyDo(
            interrupted -> {
              intake.getR().stopRollers();
              intake.retractIntakeMechanism();
            })
        .withName("Hold to Intake");
  }

  /**
   * Deploy intake, run rollers until FUEL detected, then retract.
   *
   * @param intake The intake subsystem
   * @return Command that completes a full intake cycle
   */
  public static Command intakeFuelCommand(Intake intake) {
    return Commands.sequence(
        // Deploy command
        Commands.sequence(
            Commands.runOnce(intake::deployIntakeMechanism, intake),
            Commands.waitUntil(intake.getD()::isDeployed),
            Commands.runOnce(intake.getR()::runIntake, intake))
            .withName("Intake FUEL"),

        // Retract command
        Commands.sequence(
            Commands.runOnce(intake::retractIntakeMechanism, intake),
            Commands.waitUntil(intake.getD()::isStowed))
            .withName("Retract Intake"))
        .withName("Intake and Retract");
  }

  /**
   * Deploy intake and run continuously until cancelled.
   *
   * @param intake The intake subsystem
   * @return Command that runs until interrupted
   */
  public static Command continuousIntakeCommand(Intake intake) {
    return Commands.sequence(
        Commands.runOnce(intake::deployIntakeMechanism, intake),
        Commands.waitUntil(intake.getD()::isDeployed),
        Commands.run(intake.getR()::runIntake, intake))
        .finallyDo(
            interrupted -> {
              intake.getR().stopRollers();
              intake.retractIntakeMechanism();
            })
        .withName("Continuous Intake");
  }

  /**
   * Outtake FUEL while button is held.
   *
   * @param intake The intake subsystem
   * @return Command that runs outtake until released
   */
  public static Command outtakeCommand(Intake intake) {
    return Commands.sequence(
        Commands.runOnce(intake::deployIntakeMechanism, intake),
        Commands.waitUntil(intake.getD()::isDeployed),
        Commands.run(intake.getR()::runOuttake, intake))
        .finallyDo(
            interrupted -> {
              intake.getR().stopRollers();
              intake.retractIntakeMechanism();
            })
        .withName("Outtake");
  }

  // ================================================================
  // SHOOTER INTEGRATION
  // ================================================================

  /**
   * Feed FUEL to the shooter.
   *
   * @param intake The intake subsystem
   * @return Command that feeds FUEL
   */
  public static Command feedCommand(Intake intake) {
    return Commands.startEnd(intake.getR()::feedToShooter, intake.getR()::stopRollers, intake)
        .withName("Feed FUEL");
  }

  // ================================================================
  // UTILITY COMMANDS
  // ================================================================

  /**
   * Emergency stop: stop all intake motors immediately.
   *
   * @param intake The intake subsystem
   * @return Instant command that stops everything
   */
  public static Command emergencyStopCommand(Intake intake) {
    return Commands.runOnce(
        () -> {
          intake.getR().stopRollers();
          intake.retractIntakeMechanism();
        },
        intake)
        .withName("Intake Emergency Stop");
  }
}
