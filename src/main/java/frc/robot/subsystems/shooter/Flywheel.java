package frc.robot.subsystems.shooter;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkMax;

import frc.robot.Constants.ShooterConstants;

public class Flywheel {
    private double targetPitchAngle = ShooterConstants.PITCH_STOW_ANGLE;
    private double targetFlywheelRPM = 0.0;

    private final SparkMax flywheelMotor;
    private final RelativeEncoder flywheelEncoder;
    private final SparkClosedLoopController flywheelController;

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
