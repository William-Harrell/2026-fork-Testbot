package frc.robot.commands;

import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.climber.Climber;

public final class ClimberCommands {
  private ClimberCommands() {}

  /** Extend the climber while button is held, stop on release. */
  public static Command extendCommand(Climber climber) {
    if (climber == null) return Commands.none();
    return Commands.startEnd(climber::extend, climber::stop, climber).withName("Climber Extend");
  }

  /** Retract the climber while button is held, stop on release. */
  public static Command retractCommand(Climber climber) {
    if (climber == null) return Commands.none();
    return Commands.startEnd(climber::retract, climber::stop, climber).withName("Climber Retract");
  }
}
