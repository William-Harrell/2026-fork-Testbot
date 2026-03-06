package frc.robot.subsystems.intake;

import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkFlexConfig;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/** Two Vortex (SparkFlex) motors that deploy/retract the intake mechanism */
public class Deploy {
  private final SparkFlex deployMotor;
  private final SparkFlex deployMotor2;
  private final RelativeEncoder deployEncoder;
  private final SparkClosedLoopController deployController;
  private double targetPosition;

  /** {@code motor1} is the leader, {@code motor2} follows it */
  public Deploy(SparkFlex motor1, SparkFlex motor2) {
    deployMotor = motor1;
    deployMotor2 = motor2;
    deployEncoder = deployMotor.getEncoder();
    deployController = deployMotor.getClosedLoopController();

    targetPosition = IntakeConstants.STOWED_POSITION;

    SparkFlexConfig deployConfig = new SparkFlexConfig();
    deployConfig.idleMode(IdleMode.kBrake).smartCurrentLimit(30);
    deployConfig.closedLoop
        .feedbackSensor(FeedbackSensor.kPrimaryEncoder)
        .p(IntakeConstants.DEPLOY_kP).i(0).d(0);

    SparkFlexConfig followerConfig = new SparkFlexConfig();
    followerConfig.idleMode(IdleMode.kBrake).smartCurrentLimit(30).follow(deployMotor, false);

    deployMotor.configure(
        deployConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    deployMotor2.configure(
        followerConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    deployEncoder.setPosition(0);
  }

  public SparkFlex get() {
    return deployMotor;
  }

  public void setTargetPosition(double newPos) {
    targetPosition = newPos;
    deployController.setSetpoint(targetPosition, SparkFlex.ControlType.kPosition);
  }

  /** Check if intake is at deployed position */
  public boolean isDeployed() {
    return Math.abs(deployEncoder.getPosition() - IntakeConstants.DEPLOYED_POSITION)
        < IntakeConstants.POSITION_TOLERANCE;
  }

  /** Check if intake is at stowed position */
  public boolean isStowed() {
    return Math.abs(deployEncoder.getPosition() - IntakeConstants.STOWED_POSITION)
        < IntakeConstants.POSITION_TOLERANCE;
  }

  /** Get current deploy position */
  public double getDeployPosition() {
    return deployEncoder.getPosition();
  }

  public void update() {
    SmartDashboard.putNumber("Intake/Position", deployEncoder.getPosition());
    SmartDashboard.putBoolean("Intake/IsDeployed", isDeployed());
    SmartDashboard.putBoolean("Intake/IsStowed", isStowed());
  }
}
