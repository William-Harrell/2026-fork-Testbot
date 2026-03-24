// CHECK //
package frc.robot;

import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.spindexer.Spindexer;
import frc.robot.subsystems.swerve.SwerveDrive;
import frc.robot.subsystems.vision.Vision;

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
  private final Spindexer spindexer;

  // public Superstructure(SwerveDrive swerve, Shooter shooter, Intake intake, Vision vision) {
  //   this.swerve = swerve;
  //   this.shooter = shooter;
  //   this.vision = vision;
  //   this.intake = intake;
  //   // this.hopper = hopper;
  //   // this.rollerbelt = rollerbelt;
  // }

  public Superstructure(SwerveDrive swerve, Shooter shooter, Intake intake, Spindexer spindexer) {
    this.swerve = swerve;
    this.shooter = shooter;
    // this.vision = vision;
    this.intake = intake;
    this.spindexer = spindexer;
  }

  public SwerveDrive getSwerve() {
    return swerve;
  }

  public Shooter getShooter() {
    return shooter;
  }

  // public Vision getVision() {
  // return vision;
  // }

  public Intake getIntake() {
    return intake;
  }

  public Spindexer getSpindexer() {
    return spindexer;
  }
}
