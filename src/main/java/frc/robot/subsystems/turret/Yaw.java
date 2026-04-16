package frc.robot.subsystems.turret;

import com.revrobotics.spark.FeedbackSensor;
// import com.revrobotics.spark.SparkAbsoluteEncoder;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;

public class Yaw {
    private final SparkMax motor;
    private final RelativeEncoder rel_Encoder;
    private final SparkClosedLoopController controller;

    public Yaw(SparkMax motor) {
        this.motor = motor;
        rel_Encoder = this.motor.getEncoder();

        controller = this.motor.getClosedLoopController();

        SparkMaxConfig config = new SparkMaxConfig();
        config.smartCurrentLimit(TurretConstants.YAW_STATOR_CURRENT_LIMIT,
            TurretConstants.YAW_SUPPLY_CURRENT_LIMIT);
        config.idleMode(TurretConstants.YAW_COAST ?
            IdleMode.kCoast : IdleMode.kBrake);

        /*
        config.absoluteEncoder
                .positionConversionFactor(360.0) // (rotations to degrees)
                .velocityConversionFactor((360.0 * 60.0)) // (rotations/m to degrees) per second (360 * 60)
                .zeroOffset(TurretConstants.OFFSET_YAW)
                .inverted(TurretConstants.INVERT_ABS_ENCODER);
        */

        config.encoder
            .positionConversionFactor((360.0) * TurretConstants.YawGearRatio) // (rotations to degrees)
            .velocityConversionFactor((360.0 * 60.0) * TurretConstants.YawGearRatio); // (rotations/m to degrees) per second (360 * 60)

        config.closedLoop
                .feedbackSensor(FeedbackSensor.kPrimaryEncoder)
                .p(TurretConstants.YAW_kP)
                .i(TurretConstants.YAW_kI)
                .d(TurretConstants.YAW_kD);

        config.softLimit
                .forwardSoftLimit(TurretConstants.MAX_YAW)
                .forwardSoftLimitEnabled(true)
                .reverseSoftLimit(TurretConstants.MIN_YAW)
                .reverseSoftLimitEnabled(true);

        this.motor.configure(config, ResetMode.kNoResetSafeParameters, PersistMode.kNoPersistParameters);
    }

    public double getDegrees() {
        return rel_Encoder.getPosition();
    }

    public double getVelocity() {
        return rel_Encoder.getVelocity();
    }

    public void zeroYaw(){
       rel_Encoder.setPosition(0);
    }

    public void moveTo(double goal) {
        // goal = Math.min(Math.max(goal, TurretConstants.MIN_YAW), TurretConstants.MAX_YAW);
        // controller.setSetpoint(goal, ControlType.kPosition);
        System.out.println("YAW DISABLED");
    }
}
