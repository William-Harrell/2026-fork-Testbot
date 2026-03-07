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
  private boolean lastHubActive = true;

  public Shooter(Vision vision, SwerveDrive swerve) {
    // Intra (w/ overload constructors)
    // Investigate what u don't know (ctrl+click)
    orientation = new Orientation(new SparkFlex(ShooterConstants.HOOD_MOTOR_ID, MotorType.kBrushless));
    physics = new Physics(vision, swerve); // this guy really just wants ALL the fancy stuff huh
    flywheel =
        new Flywheel( // Just so its pretty
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

  public ShooterState getS() {
    return state_machine;
  }

  public Flywheel getF() {
    return flywheel;
  }

  private void updateDashboard() {
    // Main stats (Hover for Javadocs explanations)
    // Too many methods? Nah.
    SmartDashboard.putString("Shooter/State", state_machine.get().toString());
    SmartDashboard.putNumber("Shooter/PitchAngle", orientation.getTargetPitchAngle());
    SmartDashboard.putNumber("Shooter/FlywheelRPM", flywheel.getFlywheelRPM());
    SmartDashboard.putNumber("Shooter/TargetRPM", flywheel.getTargetFlywheelRPM());
    SmartDashboard.putBoolean("Shooter/AtSpeed", flywheel.isFlywheelAtSpeed());
    SmartDashboard.putBoolean("Shooter/ReadyToShoot", flywheel.isReadyToShoot());
    physics.hubDistance().ifPresent(d -> SmartDashboard.putNumber("Shooter/HubDistance", d));

    // Hub active/inactive (FMS game-specific message)
    SmartDashboard.putBoolean("Shooter/HubActive", physics.isHubActive());
    // G407 alliance zone (robot must be inside to score)
    SmartDashboard.putBoolean("Shooter/InAllianceZone", physics.isInAllianceZone());

    // Vision-related (can-shoot? & what the shot would look like)
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

  // Runs every ~20 ms
  public void periodic() {
    state_machine.update();
    orientation.updateDashboard();
    updateDashboard();

    // Notify driver when hub active status changes
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

  // Like a mini superstructure, if you will...
  // porbably all commands. will touch them later...
  public void prepareShot(double pitchDegrees, double flywheelRPM) {
    orientation.setPitchAngle(pitchDegrees);
    flywheel.setFlywheelRPM(flywheelRPM);
  }

  public void prepareDefaultShot() {
    prepareShot(ShooterConstants.LAUNCH_ANGLE, ShooterConstants.FLYWHEEL_SHOOT_RPM);
  }

  public void stop() { // no idea what this is for tbh. prolly cmds.
    flywheel.stopFlywheel();
    orientation.stowPitch();
    state_machine.set(shooter_state.IDLE);
  }
}
