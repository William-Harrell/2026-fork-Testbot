package frc.robot.subsystems.spindexer;

import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.config.SparkFlexConfig;

public class Spinner {
    private final SparkFlex spindmotor;
    private final RelativeEncoder SpinEncoder;
    private final SparkClosedLoopController controller;
    private double targetRPM;

    public Spinner(SparkFlex motor) {
        spindmotor = motor;
        SpinEncoder = motor.getEncoder();
        controller = motor.getClosedLoopController();

        SparkFlexConfig config = new SparkFlexConfig();
        config.idleMode(SpindexerConstants.SPIN_COAST ? IdleMode.kCoast : IdleMode.kBrake)
                .smartCurrentLimit(
                        SpindexerConstants.SPIN_STATOR_CURRENT_LIMIT,
                        SpindexerConstants.SPIN_SUPPLY_CURRENT_LIMIT);

        config.closedLoopRampRate(SpindexerConstants.RAMPRATE);

        // (For future reference)
        // All slots are default (kSlot0)
        config.closedLoop
                .p(SpindexerConstants.SPIN_kP)
                .i(SpindexerConstants.SPIN_kI)
                .d(SpindexerConstants.SPIN_kD);

        config.closedLoop.feedForward.kV(SpindexerConstants.SPIN_kV);
        config.closedLoop.feedForward.kS(SpindexerConstants.SPIN_kS);
        config.closedLoop.feedForward.kA(SpindexerConstants.SPIN_kA);
        config.voltageCompensation(12);

        spindmotor.configure(config,
                ResetMode.kNoResetSafeParameters, PersistMode.kNoPersistParameters);

        // setRPM(0);
    }

    // public void setRPM(double rpm) {
    //     targetRPM = rpm;
    //     if (rpm == 0) {
    //         spindmotor.set(0);
    //     } else {
    //         controller.setSetpoint(rpm, ControlType.kVelocity);
    //     }
    // }

    public void run(double rpm) {
        spindmotor.set(-1 * (rpm/SpindexerConstants.MAX_RPM));
    }

    public void stop() {
        spindmotor.set(0);
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
