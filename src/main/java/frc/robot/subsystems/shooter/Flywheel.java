package frc.robot.subsystems.shooter;

import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;
import frc.robot.subsystems.shooter.ShooterState.shooter_state;

public class Flywheel {
  // Motor stuff
  private final SparkMax flywheelMotor;
  private final RelativeEncoder flywheelEncoder;
  private final SparkClosedLoopController flywheelController;
  private ShooterState state_machine;
  private final Orientation orientation;

  // other
  private double targetFlywheelRPM = 0.0;

  public Flywheel(SparkMax motor, Orientation myO) {
    // Instance vars
    flywheelMotor = motor;
    flywheelEncoder = flywheelMotor.getEncoder();
    flywheelController = flywheelMotor.getClosedLoopController();
    orientation = myO;

    // Personal config
    SparkMaxConfig flywheelConfig = new SparkMaxConfig();
    flywheelConfig
        .idleMode(IdleMode.kCoast)
        .smartCurrentLimit(ShooterConstants.FLYWHEEL_CURRENT_LIMIT)
        .closedLoopRampRate(ShooterConstants.FLYWHEEL_RAMP_RATE);

    flywheelConfig
        .closedLoop
        .p(ShooterConstants.FLYWHEEL_kP)
        .i(ShooterConstants.FLYWHEEL_kI)
        .d(ShooterConstants.FLYWHEEL_kD)
        .feedForward
        .kV(ShooterConstants.FLYWHEEL_kFF);

    flywheelMotor.configure(
        flywheelConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
  }

  public void setStateMachine(ShooterState sm) {
    state_machine = sm;
  }

  /**
   * Set the flywheel to a target velocity.
   *
   * @param rpm Target velocity in RPM
   */
  public void setFlywheelRPM(double rpm) {
    targetFlywheelRPM = rpm;

    if (rpm <= 0) {
      flywheelMotor.set(0);
      state_machine.set(shooter_state.IDLE);
    } else {
      flywheelController.setSetpoint(rpm, SparkMax.ControlType.kVelocity);
      state_machine.set(shooter_state.SPINNING_UP);
    }
  }

  /** Spin up the flywheel to shooting speed. */
  public void spinUp() {
    setFlywheelRPM(ShooterConstants.FLYWHEEL_SHOOT_RPM);
  }

  /** Spin up the flywheel to idle/warmup speed. */
  public void spinUpIdle() {
    setFlywheelRPM(ShooterConstants.FLYWHEEL_IDLE_RPM);
  }

  /** Stop the flywheel. */
  public void stopFlywheel() {
    setFlywheelRPM(0);
    state_machine.set(shooter_state.SPINNING_DOWN);
  }

  /**
   * Get current flywheel velocity.
   *
   * @return Current velocity in RPM
   */
  public double getFlywheelRPM() {
    return flywheelEncoder.getVelocity();
  }

  /**
   * Check if flywheel is at target speed.
   *
   * @return true if flywheel is within tolerance of target
   */
  public boolean isFlywheelAtSpeed() {
    return Math.abs(getFlywheelRPM() - targetFlywheelRPM) < ShooterConstants.FLYWHEEL_RPM_TOLERANCE;
  }

  /**
   * Check if shooter is ready to fire (flywheel at speed and pitch set).
   *
   * @return true if ready to shoot
   */
  public boolean isReadyToShoot() {
    return isFlywheelAtSpeed() && orientation.isPitchAtTarget() && targetFlywheelRPM > 0;
  }

  public double getTargetFlywheelRPM() {
    return targetFlywheelRPM;
  }
}
