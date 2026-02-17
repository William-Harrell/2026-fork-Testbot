package frc.robot.subsystems.shooter;

import edu.wpi.first.wpilibj.Servo;
import frc.robot.Constants.ShooterConstants;

public class Orientation {
    private final Servo pitchServo;
    private double targetPitchAngle;

    public Orientation(Servo motor) {
        pitchServo = motor;
        targetPitchAngle = ShooterConstants.PITCH_STOW_ANGLE;
        setPitchAngle(targetPitchAngle);
    }

    public Orientation(Servo motor, double start_angle) {
        pitchServo = motor;
        targetPitchAngle = start_angle;
        setPitchAngle(targetPitchAngle);
    }

    /**
     * Set the pitch angle of the shooter.
     *
     * @param angleDegrees Target angle in degrees (0 = horizontal, 90 = vertical)
     */
    public void setPitchAngle(double angleDegrees) {
        // Clamp to valid range
        targetPitchAngle = Math.max(
                ShooterConstants.PITCH_MIN_ANGLE,
                Math.min(ShooterConstants.PITCH_MAX_ANGLE, angleDegrees));

        // Convert angle to servo position (0.0 to 1.0)
        double servoPosition = (targetPitchAngle - ShooterConstants.PITCH_MIN_ANGLE)
                / (ShooterConstants.PITCH_MAX_ANGLE - ShooterConstants.PITCH_MIN_ANGLE);

        pitchServo.set(servoPosition);
    }

    /**
     * Get the current target pitch angle.
     *
     * @return Target pitch angle in degrees
     */
    public double getTargetPitchAngle() {
        return targetPitchAngle;
    }

    /**
     * Check if the pitch servo is approximately at the target angle. Note: Standard
     * servos don't
     * provide position feedback, so this assumes the servo reaches position after a
     * brief delay.
     *
     * @return true if servo should be at target
     */
    public boolean isPitchAtTarget() {
        // Servos typically reach position within ~200ms
        // For more precision, consider using a servo with feedback
        return true;
    }

    /** Set pitch to stow position. */
    public void stowPitch() {
        setPitchAngle(ShooterConstants.PITCH_STOW_ANGLE);
    }

}
