// CHECK //
package frc.robot;

import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.swerve.SwerveDrive;
import frc.robot.subsystems.vision.Vision;
import frc.robot.subsystems.hopper.Hopper;
import frc.robot.subsystems.rollerbelt.RollerBelt;

/**
 * Superstructure holds references to all robot subsystems.
 *
 * <p>
 * This provides a centralized way to access subsystems for coordination and
 * complex sequences
 * that span multiple mechanisms.
 */
public class Superstructure {
  private final SwerveDrive swerve;
  private final Shooter shooter;
  // private final Vision vision;
  private final Intake intake;
  // private final Hopper hopper;
  // private final RollerBelt rollerbelt;

  public Superstructure(SwerveDrive swerve, Shooter shooter, Intake intake) {
    this.swerve = swerve;
    this.shooter = shooter;
    // this.vision = vision;
    this.intake = intake;
    // this.hopper = hopper;
    // this.rollerbelt = rollerbelt;
  }

  public SwerveDrive getSwerve() {
    return swerve;
  }

  public Shooter getShooter() {
    return shooter;
  }

  // public Vision getVision() {
  //   return vision;
  // }

  public Intake getIntake() {
    return intake;
  }

  // public Hopper getHopper() {
  //   return hopper;
  // }

  // public RollerBelt getRollerBelt() {
  //   return rollerbelt;
  // }
}
