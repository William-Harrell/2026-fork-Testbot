package frc.robot.subsystems.shooter;

import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;

import frc.robot.Constants.ShooterConstants;

public class Flywheel {
    // Motor stuff
    private final SparkMax flywheelMotor;
    private final RelativeEncoder flywheelEncoder;
    private final SparkClosedLoopController flywheelController;

    // other
    private double targetFlywheelRPM = 0.0;

    public Flywheel(SparkMax motor) {
        // Instance vars
        flywheelMotor = motor;
        flywheelEncoder = flywheelMotor.getEncoder();
        flywheelController = flywheelMotor.getClosedLoopController();

        // Personal config
        SparkMaxConfig flywheelConfig = new SparkMaxConfig();
        flywheelConfig
                .idleMode(IdleMode.kCoast)
                .smartCurrentLimit(ShooterConstants.FLYWHEEL_CURRENT_LIMIT);

        flywheelConfig.closedLoop
                .p(ShooterConstants.FLYWHEEL_kP)
                .i(ShooterConstants.FLYWHEEL_kI)
                .d(ShooterConstants.FLYWHEEL_kD).feedForward
                .kV(ShooterConstants.FLYWHEEL_kFF);

        flywheelMotor.configure(
                flywheelConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    }

    /**
     * Set the flywheel to a target velocity.
     *
     * @param rpm Target velocity in RPM
     */
    public void setFlywheelRPM(double rpm) {
        targetFlywheelRPM = rpm;

        if (rpm <= 0) {
            flywheelMotor.set(0);
            state = ShooterState.IDLE;
        } else {
            flywheelController.setSetpoint(rpm, SparkMax.ControlType.kVelocity);
            state = ShooterState.SPINNING_UP;
        }
    }

    /** Spin up the flywheel to shooting speed. */
    public void spinUp() {
        setFlywheelRPM(ShooterConstants.FLYWHEEL_SHOOT_RPM);
    }

    /** Spin up the flywheel to idle/warmup speed. */
    public void spinUpIdle() {
        setFlywheelRPM(ShooterConstants.FLYWHEEL_IDLE_RPM);
    }

    /** Stop the flywheel. */
    public void stopFlywheel() {
        setFlywheelRPM(0);
        state = ShooterState.SPINNING_DOWN;
    }

    /**
     * Get current flywheel velocity.
     *
     * @return Current velocity in RPM
     */
    public double getFlywheelRPM() {
        return flywheelEncoder.getVelocity();
    }

    /**
     * Check if flywheel is at target speed.
     *
     * @return true if flywheel is within tolerance of target
     */
    public boolean isFlywheelAtSpeed() {
        return Math.abs(getFlywheelRPM() - targetFlywheelRPM) < ShooterConstants.FLYWHEEL_RPM_TOLERANCE;
    }

    /**
     * Check if shooter is ready to fire (flywheel at speed and pitch set).
     *
     * @return true if ready to shoot
     */
    public boolean isReadyToShoot() {
        return isFlywheelAtSpeed() && isPitchAtTarget() && targetFlywheelRPM > 0;
    }

    public double getTargetFlywheelRPM() {
        return targetFlywheelRPM;
    }
}
