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

  /** Move climber to Level 2 position (bumpers above low rung). */
  public static Command climbToLevel2Command(Climber climber) {
    if (climber == null) return Commands.none();
    return Commands.startEnd(climber::climbToLevel2, climber::stop, climber)
        .withName("Climber Level 2");
  }

  /** Move climber to Level 3 position (bumpers above mid rung). */
  public static Command climbToLevel3Command(Climber climber) {
    if (climber == null) return Commands.none();
    return Commands.startEnd(climber::climbToLevel3, climber::stop, climber)
        .withName("Climber Level 3");
  }
}
