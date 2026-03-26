//CHECK//

package frc.robot.subsystems.turret;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
import com.ctre.phoenix6.signals.NeutralModeValue;

public class Flywheel {
  private final TalonFX flywheelMotor1;
  private final VelocityVoltage velocityRequest = new VelocityVoltage(0).withSlot(0);

  private double targetFlywheelRPM = 0.0;

  public Flywheel(TalonFX motor1) {
    flywheelMotor1 = motor1;
    
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

    flywheelMotor1.getConfigurator().apply(config);
  }

  private void setFlywheelRPM(double rpm) {
    targetFlywheelRPM = rpm;

    if (rpm <= 0) {
      flywheelMotor1.set(0);
    } else {
      flywheelMotor1.setControl(velocityRequest.withVelocity(rpm / 60.0));
    }
  }

  public void spinUp() {
    setFlywheelRPM(TurretConstants.FLYWHEEL_SHOT_VELOCITY * 60);
  }

  public void stop() {
    setFlywheelRPM(0);
  }

  public double getRPM() {
    return flywheelMotor1.getVelocity().getValueAsDouble() * 60.0;
  }

  public boolean atTargetRPM() {
    return Math.abs(getRPM() - targetFlywheelRPM) < TurretConstants.FLYWHEEL_RPM_TOLERANCE;
  }

  public double getTargetRPM() {
    return targetFlywheelRPM;
  }
}
