package frc.robot.subsystems.intake;

import java.util.Optional;

import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystems.intake.IntakeState.intake_state;
import frc.robot.util.constants.IntakeConstants;

/** Motor that deploys/retracts the intake mechanism */
public class Deploy {
    private final SparkMax deployMotor;
    private final RelativeEncoder deployEncoder;
    private final SparkClosedLoopController deployController;
    private double targetPosition;

    /** {@code myDM} is the deploy motor */
    public Deploy(SparkMax myDM) {
        // Instance vars
        deployMotor = myDM;
        deployEncoder = deployMotor.getEncoder();
        deployController = deployMotor.getClosedLoopController();

        targetPosition = IntakeConstants.STOWED_POSITION;

        // Personal Configuration
        SparkMaxConfig deployConfig = new SparkMaxConfig();
        deployConfig.idleMode(IdleMode.kBrake).smartCurrentLimit(30);

        deployConfig.closedLoop.p(IntakeConstants.DEPLOY_kP).i(0).d(0);

        deployMotor.configure(
                deployConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

        deployEncoder.setPosition(0); // reset encoder (assuming we start @ stowed)
    }

    public SparkMax get() {
        return deployMotor;
    }

    public void setTargetPosition(double newPos) {
        targetPosition = newPos;
        deployController.setSetpoint(targetPosition, SparkMax.ControlType.kPosition);
    }

    /** Check if intake is at deployed position */
    public boolean isDeployed() {
        return Math.abs(
                deployEncoder.getPosition() - IntakeConstants.DEPLOYED_POSITION) < IntakeConstants.POSITION_TOLERANCE;
    }

    /** Check if intake is at stowed position */
    public boolean isStowed() {
        return Math.abs(
                deployEncoder.getPosition() - IntakeConstants.STOWED_POSITION) < IntakeConstants.POSITION_TOLERANCE;
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
