package frc.robot.subsystems.turret;

import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.SparkAbsoluteEncoder;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.config.SparkMaxConfig;

public class Yaw {
    private final SparkMax motor;
    private final SparkAbsoluteEncoder encoder;
    private final SparkClosedLoopController controller;

    public Yaw(SparkMax motor) {
        this.motor = motor;
        encoder = this.motor.getAbsoluteEncoder();

        controller = this.motor.getClosedLoopController();

        SparkMaxConfig config = new SparkMaxConfig();
        config.absoluteEncoder
                .positionConversionFactor(360.0) // (rotations to degrees)
                .velocityConversionFactor(60.0) // (rotations to degrees) per second
                .zeroOffset(TurretConstants.OFFSET_YAW);

        config.closedLoop
                .feedbackSensor(FeedbackSensor.kAbsoluteEncoder)
                .p(TurretConstants.YAW_kP)
                .i(TurretConstants.YAW_kI)
                .d(TurretConstants.YAW_kD);

        this.motor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    }

    public double getDegrees() {
        return encoder.getPosition();
    }

    public void moveTo(double goal) {
        goal = Math.min(Math.max(goal, TurretConstants.MIN_YAW), TurretConstants.MAX_YAW);
        controller.setSetpoint(goal + TurretConstants.OFFSET_YAW, ControlType.kPosition);
    }
}
