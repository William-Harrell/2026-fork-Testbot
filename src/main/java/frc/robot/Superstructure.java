// CHECK //
package frc.robot;

import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.spindexer.Spindexer;
import frc.robot.subsystems.swerve.SwerveDrive;
import frc.robot.subsystems.turret.Turret;
import frc.robot.subsystems.vision.Vision;

public class Superstructure {
  private final SwerveDrive swerve;
  private final Turret turret;
  private final Vision vision;
  private final Intake intake;
  private final Spindexer spindexer;

  public Superstructure(SwerveDrive swerve, Turret turret, Intake intake, Spindexer spindexer, Vision vision) {
    this.swerve = swerve;
    this.turret = turret;
    this.vision = vision;
    this.intake = intake;
    this.spindexer = spindexer;
  }

  public SwerveDrive getSwerve() {
    return swerve;
  }

  public Turret getTurret() {
    return turret;
  }

  public Vision getVision() {
  return vision;
  }

  public Intake getIntake() {
    return intake;
  }

  public Spindexer getSpindexer() {
    return spindexer;
  }
}
