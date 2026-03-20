//CHECK//

package frc.robot.subsystems.shooter;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;
import frc.robot.subsystems.shooter.ShooterState.shooter_state;

public class Flywheel {
  private final TalonFX flywheelMotor1;
  private final TalonFX flywheelMotor2;
  private final VelocityVoltage velocityRequest = new VelocityVoltage(0).withSlot(0);
  private ShooterState state_machine;
  private final Orientation orientation;

  private double targetFlywheelRPM = 0.0;

  public Flywheel(TalonFX motor1, TalonFX motor2, Orientation myO) {
    // public Flywheel(TalonFX motor1, Orientation myO) {
    flywheelMotor1 = motor1;
    flywheelMotor2 = motor2;
    orientation = myO;

    TalonFXConfiguration config = new TalonFXConfiguration();
    config.MotorOutput.NeutralMode = NeutralModeValue.Coast;
    config.CurrentLimits.StatorCurrentLimit = ShooterConstants.FLYWHEEL_CURRENT_LIMIT;
    config.CurrentLimits.StatorCurrentLimitEnable = true;
    config.CurrentLimits.SupplyCurrentLimit = ShooterConstants.FLYWHEEL_SUPPLY_CURRENT_LIMIT;
    config.CurrentLimits.SupplyCurrentLimitEnable = true;
    config.ClosedLoopRamps.VoltageClosedLoopRampPeriod = ShooterConstants.FLYWHEEL_RAMP_RATE;
    config.Slot0.kP = ShooterConstants.FLYWHEEL_kP;
    config.Slot0.kI = ShooterConstants.FLYWHEEL_kI;
    config.Slot0.kD = ShooterConstants.FLYWHEEL_kD;
    config.Slot0.kV = ShooterConstants.FLYWHEEL_kV;
    config.Slot0.kS = ShooterConstants.FLYWHEEL_kS;
    config.Slot0.kA = ShooterConstants.FLYWHEEL_kA;

    flywheelMotor1.getConfigurator().apply(config);

    // on real robot
    TalonFXConfiguration config2 = new TalonFXConfiguration();
    config2.MotorOutput.NeutralMode = NeutralModeValue.Coast;
    config2.CurrentLimits.StatorCurrentLimit = ShooterConstants.FLYWHEEL_CURRENT_LIMIT;
    config2.CurrentLimits.StatorCurrentLimitEnable = true;
    config2.CurrentLimits.SupplyCurrentLimit = ShooterConstants.FLYWHEEL_SUPPLY_CURRENT_LIMIT;
    config2.CurrentLimits.SupplyCurrentLimitEnable = true;
    config2.ClosedLoopRamps.VoltageClosedLoopRampPeriod = ShooterConstants.FLYWHEEL_RAMP_RATE;
    config.Slot0.kP = ShooterConstants.FLYWHEEL_kP;
    config.Slot0.kI = ShooterConstants.FLYWHEEL_kI;
    config.Slot0.kD = ShooterConstants.FLYWHEEL_kD;
    config.Slot0.kV = ShooterConstants.FLYWHEEL_kV;
    config.Slot0.kS = ShooterConstants.FLYWHEEL_kS;
    config.Slot0.kA = ShooterConstants.FLYWHEEL_kA;
    config2.MotorOutput.Inverted = InvertedValue.Clockwise_Positive;

    flywheelMotor2.getConfigurator().apply(config2);
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

    if (rpm == 0) {
      flywheelMotor1.set(0);
      // flywheelMotor2.set(0);
      state_machine.set(shooter_state.IDLE);
    } else if (rpm < 0) {
      double targetRPS = rpm / 60.0;
      flywheelMotor1.setControl(velocityRequest.withVelocity(targetRPS));
      flywheelMotor2.setControl(velocityRequest.withVelocity(targetRPS));
      state_machine.set(shooter_state.SPINNING_UP);
    } else {
      double targetRPS = rpm / 60.0;
      flywheelMotor1.setControl(velocityRequest.withVelocity(targetRPS));
      flywheelMotor2.setControl(velocityRequest.withVelocity(targetRPS));
      state_machine.set(shooter_state.SPINNING_UP);
    }
  }

  /** Spin up the flywheel to shooting speed. */
  public void spinUp() {
    setFlywheelRPM(ShooterConstants.FLYWHEEL_SHOOT_RPM);
  }

  /** Spin up the flywheel to shooting speed but backwards. */
  public void spinBack() {
    setFlywheelRPM(-ShooterConstants.FLYWHEEL_SHOOT_RPM);
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
    return flywheelMotor1.getVelocity().getValueAsDouble() * 60.0;
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
