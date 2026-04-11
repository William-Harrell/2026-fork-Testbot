package frc.robot.subsystems.turret;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkFlexConfig;

public class Kicker {
    private final SparkFlex motor;

    public Kicker(SparkFlex motor) {
        this.motor = motor;

        SparkFlexConfig config = new SparkFlexConfig();
        config.idleMode(IdleMode.kBrake).smartCurrentLimit(TurretConstants.KICKER_CURRENT_LIMIT);
        config.closedLoop
                .feedbackSensor(FeedbackSensor.kPrimaryEncoder);

        this.motor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    }

    public void run() {
        motor.set(1); // TODO: may need to be inverted
    }

    public void stop() {
        motor.set(0);
    }
}
