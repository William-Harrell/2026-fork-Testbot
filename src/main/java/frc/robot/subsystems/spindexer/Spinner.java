package frc.robot.subsystems.spindexer;

import java.nio.MappedByteBuffer;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.controls.VelocityVoltage;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;

public class Spinner {
    private final TalonFX spindmotor;
    private double targetRPM;
    private final VelocityVoltage velVolts = new VelocityVoltage(0).withSlot(0);

    public Spinner(TalonFX motor) {
        spindmotor = motor;

        var config = new TalonFXConfiguration();

        // Current stuff
        config.CurrentLimits.StatorCurrentLimitEnable = true;
        config.CurrentLimits.SupplyCurrentLimitEnable = true;
        config.CurrentLimits.StatorCurrentLimit = SpindexerConstants.CURRENTLIMIT;
        config.CurrentLimits.SupplyCurrentLimit = SpindexerConstants.SUPPLYCURRENTLIMIT;

        // etc
        config.ClosedLoopRamps.VoltageClosedLoopRampPeriod = SpindexerConstants.RAMPRATE;
        config.MotorOutput.NeutralMode = NeutralModeValue.Coast;

        spindmotor.getConfigurator().apply(config);
    }

    public void setRPM(double rpm) {
        targetRPM = rpm;
        spindmotor.setControl(velVolts.withVelocity(rpm / 60.0));
    }

    public double getTargetRPM() {
        return targetRPM;
    }

    public double getRPM() {
        return spindmotor.getVelocity().getValueAsDouble() * 60;
    }

    public boolean isAtSpeed() {
        return Math.abs(getRPM() - targetRPM) < SpindexerConstants.RPMTOLERANCE;
    }
}
