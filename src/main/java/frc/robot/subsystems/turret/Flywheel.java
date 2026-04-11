//CHECK//

package frc.robot.subsystems.turret;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;

public class Flywheel {
  private final TalonFX motor;
  private final VelocityVoltage velocityRequest = new VelocityVoltage(0).withSlot(0);
  private double targetFlywheelRPM = 0.0;

  public Flywheel(TalonFX motor) {
    this.motor = motor;

    TalonFXConfiguration config = new TalonFXConfiguration();
    config.MotorOutput.NeutralMode = NeutralModeValue.Coast;
    config.CurrentLimits.StatorCurrentLimit = TurretConstants.FLYWHEEL_STATOR_CURRENT_LIMIT;
    config.CurrentLimits.StatorCurrentLimitEnable = true;
    config.CurrentLimits.SupplyCurrentLimit = TurretConstants.FLYWHEEL_SUPPLY_CURRENT_LIMIT;
    config.CurrentLimits.SupplyCurrentLimitEnable = true;
    config.ClosedLoopRamps.VoltageClosedLoopRampPeriod = TurretConstants.FLYWHEEL_RAMP_RATE;
    config.Slot0.kP = TurretConstants.FLYWHEEL_kP;
    config.Slot0.kI = TurretConstants.FLYWHEEL_kI;
    config.Slot0.kD = TurretConstants.FLYWHEEL_kD;
    config.Slot0.kV = TurretConstants.FLYWHEEL_kV;
    config.Slot0.kS = TurretConstants.FLYWHEEL_kS;
    config.Slot0.kA = TurretConstants.FLYWHEEL_kA;

    motor.getConfigurator().apply(config);
  }

  private void setFlywheelRPM(double rpm) {
    targetFlywheelRPM = rpm;

    if (rpm <= 0) {
      motor.set(0);
    } else {
      motor.setControl(velocityRequest.withVelocity(RPMToVelocity(rpm)));
    }
  }

  public void spinUp() {
    setFlywheelRPM(velocityToRPM(TurretConstants.FLYWHEEL_SHOT_VELOCITY));
  }

  public void stop() {
    setFlywheelRPM(0);
  }

  public double getRPM() {
    return velocityToRPM(motor.getVelocity().getValueAsDouble());
  }

  public boolean atTargetRPM() {
    return Math.abs(getRPM() - targetFlywheelRPM) < TurretConstants.FLYWHEEL_RPM_TOLERANCE;
  }

  public double getTargetRPM() {
    return targetFlywheelRPM;
  }

  private double velocityToRPM(double v) {
    return (v * 30) / (Math.PI * TurretConstants.FLYWHEEL_WHEEL_RADIUS_M);
  }

  private double RPMToVelocity(double rpm) {
    return (rpm * Math.PI * TurretConstants.FLYWHEEL_WHEEL_RADIUS_M) / 30;
  }
}
