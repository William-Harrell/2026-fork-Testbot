package frc.robot.subsystems.turret;

import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkFlexConfig;

public class Pitch {
    private final SparkFlex motor;
    private final RelativeEncoder rel_encoder;
    private final SparkClosedLoopController controller;
    private double target_angle;

    public Pitch(SparkFlex motor) {
        this.motor = motor;

        rel_encoder = this.motor.getEncoder();

        controller = this.motor.getClosedLoopController();
        target_angle = TurretConstants.INIT_PITCH;

        SparkFlexConfig config = new SparkFlexConfig();
        config.encoder
                .positionConversionFactor(360.0) // (rotations to degrees)
                .velocityConversionFactor(60.0); // (rotations to degrees) per second
        config.idleMode(IdleMode.kBrake).smartCurrentLimit(TurretConstants.PITCH_CURRENT_LIMIT);
        config.closedLoop
                .feedbackSensor(FeedbackSensor.kPrimaryEncoder);

        this.motor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

        rel_encoder.setPosition(target_angle);
    }

    public double getDegrees() {
        return rel_encoder.getPosition() * TurretConstants.PITCH_DEGREE_RATIO;
    }

    public double getMotorDegrees() {
        return rel_encoder.getPosition();
    }

    public boolean isPitchAtTarget() {
        return Math.abs(getDegrees() - target_angle) < TurretConstants.PITCH_TOLERANCE;
    }

    public void turnTo(double goal) {
        target_angle = Math.min(TurretConstants.MAX_PITCH, Math.min(goal, TurretConstants.MIN_PITCH));
        controller.setSetpoint(goal, ControlType.kPosition);
    }

    public void reset() {
        turnTo(0.0);
    }
}
