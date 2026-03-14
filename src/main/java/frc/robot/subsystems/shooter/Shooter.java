// CHECK //

package frc.robot.subsystems.shooter;

import com.ctre.phoenix6.hardware.TalonFX;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.shooter.Physics.VisionAimedShot;
import frc.robot.subsystems.shooter.ShooterState.shooter_state;
import frc.robot.subsystems.swerve.SwerveDrive;
import frc.robot.subsystems.vision.Vision;
import frc.robot.util.Elastic;

public class Shooter extends SubsystemBase {
  // Sub-subsystems
  private Orientation orientation1;
  private Orientation orientation2;
  private Physics physics;
  private ShooterState state_machine;
  private Flywheel flywheel;

  // Track hub active state for shift change notifications
  private boolean lastHubActive = true;

  public Shooter(Vision vision, SwerveDrive swerve) {
    orientation1 = new Orientation(new SparkFlex(ShooterConstants.HOOD_MOTOR_ID1, MotorType.kBrushless));
    orientation2 = new Orientation(new SparkFlex(ShooterConstants.HOOD_MOTOR_ID2, MotorType.kBrushless));
    physics = new Physics(vision, swerve);
    flywheel =
        new Flywheel(
            new TalonFX(ShooterConstants.FLYWHEEL_MOTOR_ID),
            new TalonFX(ShooterConstants.FLYWHEEL_MOTOR_2_ID),
            orientation1, orientation2);
    state_machine = new ShooterState(flywheel);
    flywheel.setStateMachine(state_machine);
  }

  public Orientation getO() {
    return orientation1;
  }

  public Physics getP() {
    return physics;
  }

  /** @deprecated Use {@link #getState()} — getS() is ambiguous next to getO()/getP()/getF(). */
  @Deprecated
  public ShooterState getS() {
    return state_machine;
  }

  /**
   * Returns the ShooterState state machine.
   * Use this to read or set the current shooter phase (IDLE, SPINNING_UP, READY, SHOOTING, etc.).
   */
  public ShooterState getState() {
    return state_machine;
  }

  public Flywheel getF() {
    return flywheel;
  }

  private void updateDashboard() {
    SmartDashboard.putString("Shooter/State", state_machine.get().toString());
    SmartDashboard.putNumber("Shooter/PitchAngle", orientation1.getTargetPitchAngle());
    SmartDashboard.putNumber("Shooter/FlywheelRPM", flywheel.getFlywheelRPM());
    SmartDashboard.putNumber("Shooter/TargetRPM", flywheel.getTargetFlywheelRPM());
    SmartDashboard.putBoolean("Shooter/AtSpeed", flywheel.isFlywheelAtSpeed());
    SmartDashboard.putBoolean("Shooter/ReadyToShoot", flywheel.isReadyToShoot());
    physics.hubDistance().ifPresent(d -> SmartDashboard.putNumber("Shooter/HubDistance", d));

    SmartDashboard.putBoolean("Shooter/HubActive", physics.isHubActive());
    SmartDashboard.putBoolean("Shooter/InAllianceZone", physics.isInAllianceZone());

    SmartDashboard.putBoolean("Shooter/VisionAvailable", physics.hasReliableVisionTarget());
    if (physics.hasReliableVisionTarget()) {
      VisionAimedShot visionShot = physics.calculateOptimalPitchWithVision();
      SmartDashboard.putNumber("Shooter/Vision/RecommendedPitch", visionShot.pitchAngle());
      SmartDashboard.putNumber("Shooter/Vision/TagCount", visionShot.tagCount());
      SmartDashboard.putNumber("Shooter/Vision/Ambiguity", visionShot.ambiguity());
      SmartDashboard.putNumber("Shooter/Vision/Distance", visionShot.distanceToHub());
      SmartDashboard.putBoolean("Shooter/Vision/HighConfidence", visionShot.isHighConfidence());
    }
  }

  public void periodic() {
    state_machine.update();
    orientation1.updateDashboard();
    updateDashboard();

    boolean hubActive = physics.isHubActive();
    if (hubActive != lastHubActive) {
      if (hubActive) {
        Elastic.sendNotification(
            new Elastic.Notification()
                .withLevel(Elastic.NotificationLevel.INFO)
                .withTitle("HUB ACTIVE")
                .withDescription("Your hub is now scoring!")
                .withDisplaySeconds(3.0));
      } else {
        Elastic.sendNotification(
            new Elastic.Notification()
                .withLevel(Elastic.NotificationLevel.WARNING)
                .withTitle("HUB INACTIVE")
                .withDescription("Your hub is off — hold fire!")
                .withDisplaySeconds(5.0));
      }
      lastHubActive = hubActive;
    }
  }

  public void prepareShot(double pitchDegrees, double flywheelRPM) {
    orientation1.setPitchAngle(pitchDegrees);
    flywheel.setFlywheelRPM(flywheelRPM);
  }

  public void prepareDefaultShot() {
    prepareShot(ShooterConstants.LAUNCH_ANGLE, ShooterConstants.FLYWHEEL_SHOOT_RPM);
  }

  public void stop() {
    flywheel.stopFlywheel();
    orientation1.stowPitch();
    state_machine.set(shooter_state.IDLE);
  }
}