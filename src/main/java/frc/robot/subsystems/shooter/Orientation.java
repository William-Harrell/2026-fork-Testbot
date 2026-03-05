package frc.robot.subsystems.shooter;

import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;

public class Orientation {
  private final SparkMax hoodMotor;
  private final RelativeEncoder hoodEncoder;
  private final SparkClosedLoopController hoodController;
  private double targetPitchAngle;

  public Orientation(SparkMax motor) {
    hoodMotor = motor;
    hoodEncoder = hoodMotor.getAlternateEncoder();
    hoodController = hoodMotor.getClosedLoopController();

    SparkMaxConfig config = new SparkMaxConfig();
    config.idleMode(IdleMode.kBrake).smartCurrentLimit(20);
    config.alternateEncoder.countsPerRevolution(ShooterConstants.ENCODER_CPR);
    config.closedLoop
        .feedbackSensor(FeedbackSensor.kAlternateOrExternalEncoder)
        .p(ShooterConstants.HOOD_kP)
        .i(ShooterConstants.HOOD_kI)
        .d(ShooterConstants.HOOD_kD);

    hoodMotor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    hoodEncoder.setPosition(degreesToRotations(ShooterConstants.PITCH_STOW_ANGLE));
    targetPitchAngle = ShooterConstants.PITCH_STOW_ANGLE;
  }

  /**
   * Set the pitch angle of the shooter hood.
   *
   * @param angleDegrees Target angle in degrees
   */
  public void setPitchAngle(double angleDegrees) {
    targetPitchAngle =
        Math.max(
            ShooterConstants.PITCH_MIN_ANGLE,
            Math.min(ShooterConstants.PITCH_MAX_ANGLE, angleDegrees));
    hoodController.setSetpoint(degreesToRotations(targetPitchAngle), SparkMax.ControlType.kPosition);
  }

  /** Get the target pitch angle. */
  public double getTargetPitchAngle() {
    return targetPitchAngle;
  }

  /** Get the actual pitch angle from the hex encoder. */
  public double getActualPitchAngle() {
    return hoodEncoder.getPosition() * ShooterConstants.HOOD_DEGREES_PER_ROTATION;
  }

  /** Check if the hood is at its target angle within tolerance. */
  public boolean isPitchAtTarget() {
    return Math.abs(getActualPitchAngle() - targetPitchAngle) < ShooterConstants.PITCH_TOLERANCE;
  }

  /** Set pitch to stow position. */
  public void stowPitch() {
    setPitchAngle(ShooterConstants.PITCH_STOW_ANGLE);
  }

  private double degreesToRotations(double degrees) {
    return degrees / ShooterConstants.HOOD_DEGREES_PER_ROTATION;
  }
}
