package frc.robot.subsystems.hopper;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkFlexConfig;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Hopper extends SubsystemBase {
  private final SparkFlex motor;

  public Hopper() {
    motor = new SparkFlex(HopperConstants.HOPPER_MOTOR_1_ID, MotorType.kBrushless);

    SparkFlexConfig config = new SparkFlexConfig();
    config.idleMode(IdleMode.kBrake).smartCurrentLimit(HopperConstants.CURRENT_LIMIT);

    motor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
  }

  public void feed() {
    motor.set(HopperConstants.FEED_SPEED);
  }

  public void reverse() {
    motor.set(HopperConstants.REVERSE_SPEED);
  }

  public void stop() {
    motor.set(0);
  }
}
