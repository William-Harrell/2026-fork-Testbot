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
  private Orientation orientation;
  private Physics physics;
  private ShooterState state_machine;
  private Flywheel flywheel;

  // Track hub active state for shift change notifications
  // private boolean lastHubActive = true;

  public Shooter(Vision vision, SwerveDrive swerve) {
    orientation = new Orientation(new SparkFlex(ShooterConstants.HOOD_MOTOR_ID1, MotorType.kBrushless),
        new SparkFlex(ShooterConstants.HOOD_MOTOR_ID2, MotorType.kBrushless));
    physics = new Physics(vision, swerve);
    flywheel = new Flywheel(
        new TalonFX(ShooterConstants.FLYWHEEL_MOTOR_ID),
        new TalonFX(ShooterConstants.FLYWHEEL_MOTOR_2_ID),
        orientation);
    state_machine = new ShooterState(flywheel);
    flywheel.setStateMachine(state_machine);
  }

  public Orientation getO() {
    return orientation;
  }

  public Physics getP() {
    return physics;
  }

  public ShooterState getState() {
    return state_machine;
  }

  public Flywheel getF() {
    return flywheel;
  }

  public void periodic() {
    state_machine.update();

    // SmartDashboard
    SmartDashboard.putString("Shooter/State", state_machine.get().toString());
    SmartDashboard.putNumber("Shooter/PitchAngle", orientation.getTargetPitchAngle());
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
    }
  }

  public void prepareShot(double pitchDegrees, double flywheelRPM) {
    orientation.setPitchAngle(pitchDegrees);
    flywheel.setFlywheelRPM(flywheelRPM);
  }

  public void stop() {
    flywheel.stopFlywheel();
    orientation.stowPitch();
    state_machine.set(shooter_state.IDLE);
  }
}