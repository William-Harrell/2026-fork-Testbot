package frc.robot.subsystems.climber;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Climber extends SubsystemBase {
  private final SparkMax leftLeader;
  private final SparkMax leftFollower;
  private final SparkMax rightLeader;
  private final SparkMax rightFollower;

  public Climber() {
    leftLeader = new SparkMax(ClimberConstants.LEFT_LEADER_ID, MotorType.kBrushless);
    leftFollower = new SparkMax(ClimberConstants.LEFT_FOLLOWER_ID, MotorType.kBrushless);
    rightLeader = new SparkMax(ClimberConstants.RIGHT_LEADER_ID, MotorType.kBrushless);
    rightFollower = new SparkMax(ClimberConstants.RIGHT_FOLLOWER_ID, MotorType.kBrushless);

    SparkMaxConfig leaderConfig = new SparkMaxConfig();
    leaderConfig.idleMode(IdleMode.kBrake).smartCurrentLimit(ClimberConstants.CURRENT_LIMIT);

    SparkMaxConfig followerConfig = new SparkMaxConfig();
    followerConfig.idleMode(IdleMode.kBrake).smartCurrentLimit(ClimberConstants.CURRENT_LIMIT);

    SparkMaxConfig rightFollowerConfig = new SparkMaxConfig();
    rightFollowerConfig
        .idleMode(IdleMode.kBrake)
        .smartCurrentLimit(ClimberConstants.CURRENT_LIMIT)
        .follow(rightLeader, true); // inverted to mirror left side

    leftLeader.configure(leaderConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    leftFollower.configure(followerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    rightLeader.configure(leaderConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    rightFollower.configure(rightFollowerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    SparkMaxConfig leftFollowerConfig = new SparkMaxConfig();
    leftFollowerConfig
        .idleMode(IdleMode.kBrake)
        .smartCurrentLimit(ClimberConstants.CURRENT_LIMIT)
        .follow(leftLeader, false);
    leftFollower.configure(leftFollowerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
  }

  public void climb() {
    leftLeader.set(ClimberConstants.CLIMB_SPEED);
    rightLeader.set(ClimberConstants.CLIMB_SPEED);
  }

  public void lower() {
    leftLeader.set(ClimberConstants.LOWER_SPEED);
    rightLeader.set(ClimberConstants.LOWER_SPEED);
  }

  public void stop() {
    leftLeader.set(0);
    rightLeader.set(0);
  }
}
