
package frc.robot.subsystems.intake;

import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkFlexConfig;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Deploy {
  private final SparkFlex motor;
  private final RelativeEncoder deployEncoder;
  private final SparkClosedLoopController deployController;
  private final DigitalInput limitSwitch;
  private double targetPosition;
  private boolean homed = false;

  public Deploy(SparkFlex motor) {
    this.motor = motor;
    deployEncoder = motor.getEncoder();
    deployController = motor.getClosedLoopController();

    targetPosition = IntakeConstants.STOWED_POSITION;

    SparkFlexConfig deployConfig = new SparkFlexConfig();
    deployConfig.idleMode(IdleMode.kBrake).smartCurrentLimit(40);
    deployConfig.closedLoop
        .feedbackSensor(FeedbackSensor.kPrimaryEncoder)
        .p(IntakeConstants.DEPLOY_kP).i(0).d(0);

    SparkFlexConfig motor2Config = new SparkFlexConfig();
    motor2Config.idleMode(IdleMode.kBrake).smartCurrentLimit(47).inverted(true);
    motor2Config.closedLoop
        .feedbackSensor(FeedbackSensor.kPrimaryEncoder)
        .p(IntakeConstants.DEPLOY_kP).i(0).d(0);

    motor.configure(
        deployConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    limitSwitch = new DigitalInput(IntakeConstants.DEPLOY_LIMIT_SWITCH_DIO);

    // If already at home position on boot, zero immediately
    if (isAtHome()) {
      deployEncoder.setPosition(0);
      homed = true;
    }
  }

  public void setTargetPosition(double newPos) {
    targetPosition = newPos;
    deployController.setSetpoint(targetPosition, SparkFlex.ControlType.kPosition);
  }

  /** Check if intake is at deployed position */
  public boolean isDeployed() {
    return Math.abs(deployEncoder.getPosition() - IntakeConstants.DEPLOYED_POSITION)
        < IntakeConstants.POSITION_TOLERANCE;
  }

  /** Check if intake is at stowed position */
  public boolean isStowed() {
    return Math.abs(deployEncoder.getPosition() - IntakeConstants.STOWED_POSITION)
        < IntakeConstants.POSITION_TOLERANCE;
  }

  /** Get current deploy position */
  public double getDeployPosition() {
    return deployEncoder.getPosition();
  }

  /** True when the limit switch is triggered (mechanism at stow/home position). */
  public boolean isAtHome() {
    return !limitSwitch.get(); // active-low: grounded = false = triggered
  }

  /** Whether the encoder has been homed since boot. */
  public boolean isHomed() {
    return homed;
  }

  /**
   * Command that slowly retracts toward stow until the limit switch triggers,
   * then zeros the encoder. If already at home, zeros immediately.
   */
  /**
   * @param owner The subsystem that owns this mechanism (passed so the CommandScheduler
   *              can prevent concurrent commands from fighting over this motor).
   */
  public Command homeCommand(SubsystemBase owner) {
    return Commands.either(
        // Already home — just zero
        Commands.runOnce(() -> {
          deployEncoder.setPosition(0);
          homed = true;
        }, owner),
        // Not home — drive slowly toward stow until switch triggers
        Commands.run(() -> {
          motor.set(IntakeConstants.HOMING_SPEED);
        }, owner)
            .until(this::isAtHome)
            .andThen(Commands.runOnce(() -> {
              motor.set(0);
              deployEncoder.setPosition(0);
              homed = true;
            }, owner)),
        this::isAtHome)
        .withName("Home Intake Deploy");
  }

  public void update() {
    // Auto-correct encoder drift: if limit switch triggers, re-zero
    if (isAtHome()) {
      if (Math.abs(deployEncoder.getPosition()) > IntakeConstants.POSITION_TOLERANCE) {
        deployEncoder.setPosition(0);
      }
      homed = true;
    }

    SmartDashboard.putNumber("Intake/Position", deployEncoder.getPosition());
    SmartDashboard.putBoolean("Intake/IsDeployed", isDeployed());
    SmartDashboard.putBoolean("Intake/IsStowed", isStowed());
    SmartDashboard.putBoolean("Intake/LimitSwitch", isAtHome());
    SmartDashboard.putBoolean("Intake/Homed", homed);
  }
}