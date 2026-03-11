package frc.robot.subsystems.rollerbelt;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkFlexConfig;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class RollerBelt extends SubsystemBase {
  private final SparkFlex motor1;
  private final SparkFlex motor2;

  public RollerBelt() {
    motor1 = new SparkFlex(RollerBeltConstants.BELT_MOTOR_1_ID, MotorType.kBrushless);
    motor2 = new SparkFlex(RollerBeltConstants.BELT_MOTOR_2_ID, MotorType.kBrushless);

    SparkFlexConfig config = new SparkFlexConfig();
    SparkFlexConfig follower = new SparkFlexConfig();

    config.idleMode(IdleMode.kBrake).smartCurrentLimit(RollerBeltConstants.CURRENT_LIMIT);
    follower.follow(motor1, /* inverted= */ true); // verify invert on real robot

    motor1.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    motor2.configure(follower, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
  }

  @Override
  public void periodic() {
    SmartDashboard.putNumber("RollerBelt/Motor1/Current", motor1.getOutputCurrent());
    SmartDashboard.putNumber("RollerBelt/Motor2/Current", motor2.getOutputCurrent());
    SmartDashboard.putNumber("RollerBelt/Motor1/Output", motor1.get());
    SmartDashboard.putNumber("RollerBelt/Motor2/Output", motor2.get());
  }

  // Then only command motor1:
  public void run() {
    motor1.set(RollerBeltConstants.BELT_SPEED);
  }

  public void reverse() {
    motor1.set(RollerBeltConstants.REVERSE_SPEED);
  }

  public void stop() {
    motor1.set(0);
  }
}