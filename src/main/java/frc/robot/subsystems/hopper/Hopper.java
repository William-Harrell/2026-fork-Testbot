package frc.robot.subsystems.hopper;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Hopper extends SubsystemBase {
  private final TalonFX motor1;
  private final TalonFX motor2;

  public Hopper() {
    motor1 = new TalonFX(HopperConstants.HOPPER_MOTOR_1_ID);
    motor2 = new TalonFX(HopperConstants.HOPPER_MOTOR_2_ID);

    TalonFXConfiguration config = new TalonFXConfiguration();
    config.MotorOutput.NeutralMode = NeutralModeValue.Brake;
    config.CurrentLimits.StatorCurrentLimit = HopperConstants.CURRENT_LIMIT;
    config.CurrentLimits.StatorCurrentLimitEnable = true;

    motor1.getConfigurator().apply(config);
    motor2.getConfigurator().apply(config);
  }

  public void feed() {
    motor1.set(HopperConstants.FEED_SPEED);
    motor2.set(HopperConstants.FEED_SPEED);
  }

  public void reverse() {
    motor1.set(HopperConstants.REVERSE_SPEED);
    motor2.set(HopperConstants.REVERSE_SPEED);
  }

  public void stop() {
    motor1.set(0);
    motor2.set(0);
  }
}
