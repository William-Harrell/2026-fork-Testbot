package frc.robot.subsystems.turret;

import com.revrobotics.AbsoluteEncoder;
import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkFlexConfig;

public class Pitch {
    private final SparkFlex motor;
    private final AbsoluteEncoder abs_encoder;
    private final RelativeEncoder rel_encoder;
    private final SparkClosedLoopController controller;
    private double target_angle;

    public Pitch(SparkFlex motor, AbsoluteEncoder abs_encoder) {
        this.motor = motor;
        this.abs_encoder = abs_encoder;

        rel_encoder = motor.getEncoder();
        controller = motor.getClosedLoopController();
        target_angle = TurretConstants.INIT_PITCH;

        SparkFlexConfig config = new SparkFlexConfig();
        config.idleMode(IdleMode.kBrake).smartCurrentLimit(70);
        config.closedLoop
                .feedbackSensor(FeedbackSensor.kPrimaryEncoder);

        motor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

        rel_encoder.setPosition(degreesToRotations(target_angle));
    }

    public double getAbsoluteAngle() {
        return abs_encoder.getPosition();
    }

    public double getRelativeAngle() {
        return rel_encoder.getPosition() * TurretConstants.PITCH_DEGREE_RATIO;
    }

    /** Check if the hood is at its target angle within tolerance. */
    public boolean isPitchAtTarget() {
        return Math.abs(getAbsoluteAngle() - target_angle) < TurretConstants.PITCH_TOLERANCE;
    }

    public void turnTo(double goal) {

    }

    public void reset() {
    }

    private double degreesToRotations(double degrees) {
        return degrees / TurretConstants.PITCH_DEGREE_RATIO;
    }

}
