package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.intake.Intake;

public final class IntakeCommands {
  private IntakeCommands() {
  }

  public static Command toggleDeployCommand(Intake intake) {
    if (intake == null) return Commands.none();
    return Commands.runOnce(
        () -> {
          if (intake.getD().isStowed()) {
            intake.deployIntakeMechanism();
          } else {
            intake.getR().stop();
            intake.retractIntakeMechanism();
          }
        },
        intake)
        .withName("Toggle Deploy");
  }


  public static Command holdToIntakeCommand(Intake intake) {
    if (intake == null) return Commands.none();
    return Commands.sequence(
        Commands.runOnce(intake::deployIntakeMechanism, intake),
        Commands.waitUntil(intake.getD()::isDeployed),
        Commands.run(intake.getR()::runIntake, intake))
        .finallyDo(
            interrupted -> {
              intake.getR().stop();
              // intake.retractIntakeMechanism();
            })
        .withName("Hold to Intake");
  }
}