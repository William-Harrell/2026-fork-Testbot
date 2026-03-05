package frc.robot.subsystems.skidplate;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class SkidPlate extends SubsystemBase {
  private final SparkMax motor;

  public SkidPlate() {
    motor = new SparkMax(SkidPlateConstants.MOTOR_ID, MotorType.kBrushless);

    SparkMaxConfig config = new SparkMaxConfig();
    config.idleMode(IdleMode.kBrake).smartCurrentLimit(SkidPlateConstants.CURRENT_LIMIT);

    motor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
  }

  public void deploy() {
    motor.set(SkidPlateConstants.DEPLOY_SPEED);
  }

  public void retract() {
    motor.set(SkidPlateConstants.RETRACT_SPEED);
  }

  public void stop() {
    motor.set(0);
  }
}
