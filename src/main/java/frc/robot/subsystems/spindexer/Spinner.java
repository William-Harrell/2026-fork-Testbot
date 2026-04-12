package frc.robot.subsystems.spindexer;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.config.SparkFlexConfig;


public class Spinner {
    private final SparkFlex spindmotor;
    private final RelativeEncoder SpinEncoder;
    private double targetRPM;

    public Spinner(SparkFlex motor) {
        spindmotor = motor;
        SpinEncoder = motor.getEncoder();

    SparkFlexConfig config = new SparkFlexConfig();
      config.idleMode(SpindexerConstants.SPIN_COAST ? 
        IdleMode.kCoast : IdleMode.kBrake)
        .smartCurrentLimit(
          SpindexerConstants.SPIN_STATOR_CURRENT_LIMIT, 
          SpindexerConstants.SPIN_SUPPLY_CURRENT_LIMIT);

      config.closedLoopRampRate(SpindexerConstants.RAMPRATE);

    spindmotor.configure(config, 
        ResetMode.kNoResetSafeParameters, PersistMode.kNoPersistParameters);
    }

    public void setRPM(double rpm) {
        targetRPM = rpm;
        spindmotor.set(rpm / SpindexerConstants.RPMMAX);
    }

    public double getTargetRPM() {
        return targetRPM;
    }

    public double getRPM() {
        var currentRPM = SpinEncoder.getVelocity();
        return currentRPM;
    }

    public boolean isAtSpeed() {
        return Math.abs(getRPM() - targetRPM) < SpindexerConstants.RPMTOLERANCE;
    }
}
