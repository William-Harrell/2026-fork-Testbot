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
                .positionConversionFactor(360.0 * TurretConstants.PITCH_DEGREE_RATIO)
                .velocityConversionFactor(360.0 * 60.0 * TurretConstants.PITCH_DEGREE_RATIO);
        
        config.closedLoop
                .feedbackSensor(FeedbackSensor.kPrimaryEncoder)
                .p(TurretConstants.PITCH_kP)
                .i(TurretConstants.PITCH_kI)
                .d(TurretConstants.PITCH_kD);
        config.softLimit
                .forwardSoftLimit(TurretConstants.MAX_PITCH)
                .forwardSoftLimitEnabled(true)
                .reverseSoftLimit(TurretConstants.MIN_PITCH)
                .reverseSoftLimitEnabled(true);
        config.smartCurrentLimit(TurretConstants.PITCH_STATOR_CURRENT_LIMIT,
            TurretConstants.PITCH_SUPPLY_CURRENT_LIMIT);
        config.idleMode(TurretConstants.PITCH_COAST ?
            IdleMode.kCoast : IdleMode.kBrake);
        this.motor.configure(config, ResetMode.kNoResetSafeParameters, PersistMode.kNoPersistParameters);

        rel_encoder.setPosition(target_angle);
    }

    public double getDegrees() {
        return rel_encoder.getPosition();
    }

    public boolean isPitchAtTarget() {
        return Math.abs(getDegrees() - target_angle) < TurretConstants.PITCH_TOLERANCE;
    }

    public void turnTo(double goal) {
        target_angle = Math.max(TurretConstants.MIN_PITCH,
                Math.min(TurretConstants.MAX_PITCH, goal));
        controller.setSetpoint(target_angle, ControlType.kPosition);
    }

    public void reset() {
        turnTo(TurretConstants.INIT_PITCH);
    }
}
