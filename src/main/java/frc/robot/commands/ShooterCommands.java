package frc.robot.commands;

import java.util.Objects;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.shooter.Physics.VisionAimedShot;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.ShooterConstants;
import frc.robot.subsystems.shooter.ShooterState.shooter_state;
import frc.robot.util.Elastic;

public class ShooterCommands {
  private ShooterCommands() {}

  /** Command to spin up flywheel and wait until ready. */
  public static Command spinUpCommand(Shooter shooter) {
    return Commands.sequence(
            Commands.runOnce(shooter.getF()::spinUp, shooter),
            Commands.waitUntil(shooter.getF()::isReadyToShoot))
        .withName("Spin Up Shooter");
  }

  /**
   * Command to prepare shooter with automatic pitch calculation. Uses trajectory calculation to
   * determine optimal pitch angle.
   */
}