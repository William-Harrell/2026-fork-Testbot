package frc.robot.subsystems.rollerbelt;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class RollerBelt extends SubsystemBase {
  private final SparkMax motor1;
  private final SparkMax motor2;

  public RollerBelt() {
    motor1 = new SparkMax(RollerBeltConstants.BELT_MOTOR_1_ID, MotorType.kBrushless);
    motor2 = new SparkMax(RollerBeltConstants.BELT_MOTOR_2_ID, MotorType.kBrushless);

    SparkMaxConfig config = new SparkMaxConfig();
    config.idleMode(IdleMode.kBrake).smartCurrentLimit(RollerBeltConstants.CURRENT_LIMIT);

    motor1.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    motor2.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
  }

  public void run() {
    motor1.set(RollerBeltConstants.BELT_SPEED);
    motor2.set(RollerBeltConstants.BELT_SPEED);
  }

  public void reverse() {
    motor1.set(RollerBeltConstants.REVERSE_SPEED);
    motor2.set(RollerBeltConstants.REVERSE_SPEED);
  }

  public void stop() {
    motor1.set(0);
    motor2.set(0);
  }
}
