package frc.robot.subsystems.climber;

import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Climber extends SubsystemBase {
  private final SparkMax leftLeader;
  private final SparkMax leftFollower;
  private final SparkMax rightLeader;
  private final SparkMax rightFollower;

  private final RelativeEncoder leftEncoder;
  private final RelativeEncoder rightEncoder;
  private final SparkClosedLoopController leftController;
  private final SparkClosedLoopController rightController;

  public Climber() {
    leftLeader = new SparkMax(ClimberConstants.LEFT_LEADER_ID, MotorType.kBrushless);
    leftFollower = new SparkMax(ClimberConstants.LEFT_FOLLOWER_ID, MotorType.kBrushless);
    rightLeader = new SparkMax(ClimberConstants.RIGHT_LEADER_ID, MotorType.kBrushless);
    rightFollower = new SparkMax(ClimberConstants.RIGHT_FOLLOWER_ID, MotorType.kBrushless);

    leftEncoder = leftLeader.getAlternateEncoder();
    rightEncoder = rightLeader.getAlternateEncoder();
    leftController = leftLeader.getClosedLoopController();
    rightController = rightLeader.getClosedLoopController();

    SparkMaxConfig leaderConfig = new SparkMaxConfig();
    leaderConfig
        .idleMode(IdleMode.kBrake)
        .smartCurrentLimit(ClimberConstants.CURRENT_LIMIT);
    leaderConfig
        .alternateEncoder
        .countsPerRevolution(ClimberConstants.ENCODER_CPR);
    leaderConfig
        .closedLoop
        .feedbackSensor(FeedbackSensor.kAlternateOrExternalEncoder)
        .p(ClimberConstants.HOOD_kP)
        .i(ClimberConstants.HOOD_kI)
        .d(ClimberConstants.HOOD_kD);

    SparkMaxConfig leftFollowerConfig = new SparkMaxConfig();
    leftFollowerConfig
        .idleMode(IdleMode.kBrake)
        .smartCurrentLimit(ClimberConstants.CURRENT_LIMIT)
        .follow(leftLeader, false);

    SparkMaxConfig rightFollowerConfig = new SparkMaxConfig();
    rightFollowerConfig
        .idleMode(IdleMode.kBrake)
        .smartCurrentLimit(ClimberConstants.CURRENT_LIMIT)
        .follow(rightLeader, true); // inverted to mirror left side

    leftLeader.configure(leaderConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    leftFollower.configure(leftFollowerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    rightLeader.configure(leaderConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    rightFollower.configure(rightFollowerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    leftEncoder.setPosition(0);
    rightEncoder.setPosition(0);
  }

  public void extend() {
    setPosition(ClimberConstants.EXTENDED_POSITION);
  }

  public void retract() {
    setPosition(ClimberConstants.RETRACTED_POSITION);
  }

  public void climbToLevel2() {
    setPosition(ClimberConstants.LEVEL_2_POSITION);
  }

  public void climbToLevel3() {
    setPosition(ClimberConstants.LEVEL_3_POSITION);
  }

  public void setPosition(double rotations) {
    leftController.setSetpoint(rotations, SparkMax.ControlType.kPosition);
    rightController.setSetpoint(rotations, SparkMax.ControlType.kPosition);
  }

  /** Manual override — use only when not in closed-loop mode */
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

  public boolean isExtended() {
    return Math.abs(leftEncoder.getPosition() - ClimberConstants.EXTENDED_POSITION)
            < ClimberConstants.POSITION_TOLERANCE
        && Math.abs(rightEncoder.getPosition() - ClimberConstants.EXTENDED_POSITION)
            < ClimberConstants.POSITION_TOLERANCE;
  }

  public boolean isRetracted() {
    return Math.abs(leftEncoder.getPosition() - ClimberConstants.RETRACTED_POSITION)
            < ClimberConstants.POSITION_TOLERANCE
        && Math.abs(rightEncoder.getPosition() - ClimberConstants.RETRACTED_POSITION)
            < ClimberConstants.POSITION_TOLERANCE;
  }

  public double getLeftPosition() {
    return leftEncoder.getPosition();
  }

  public double getRightPosition() {
    return rightEncoder.getPosition();
  }

  @Override
  public void periodic() {
    SmartDashboard.putNumber("Climber/LeftPosition", getLeftPosition());
    SmartDashboard.putNumber("Climber/RightPosition", getRightPosition());
    SmartDashboard.putBoolean("Climber/IsExtended", isExtended());
    SmartDashboard.putBoolean("Climber/IsRetracted", isRetracted());
  }
}
