//CHECK//

package frc.robot.subsystems.shooter;

import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkFlexConfig;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Orientation {
  private final SparkFlex hoodMotor;
  private final RelativeEncoder hoodEncoder;
  private final SparkClosedLoopController hoodController;
  private final DigitalInput limitSwitch;
  private double targetPitchAngle;
  private boolean homed = false;

  public Orientation(SparkFlex motor) {
    hoodMotor = motor;
    hoodEncoder = hoodMotor.getEncoder();
    hoodController = hoodMotor.getClosedLoopController();

    SparkFlexConfig config = new SparkFlexConfig();
    config.idleMode(IdleMode.kBrake).smartCurrentLimit(20);
    config.closedLoop
        .feedbackSensor(FeedbackSensor.kPrimaryEncoder)
        .p(ShooterConstants.HOOD_kP)
        .i(ShooterConstants.HOOD_kI)
        .d(ShooterConstants.HOOD_kD);

    hoodMotor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    limitSwitch = new DigitalInput(ShooterConstants.HOOD_LIMIT_SWITCH_DIO);
    targetPitchAngle = ShooterConstants.PITCH_STOW_ANGLE;

    // If already at home on boot, zero to stow angle immediately
    if (isAtHome()) {
      hoodEncoder.setPosition(degreesToRotations(ShooterConstants.PITCH_STOW_ANGLE));
      homed = true;
    }
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
    hoodController.setSetpoint(degreesToRotations(targetPitchAngle), SparkFlex.ControlType.kPosition);
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

  /** True when the limit switch is triggered (hood at stow/home position). */
  public boolean isAtHome() {
    return !limitSwitch.get(); // active-low: grounded = false = triggered
  }

  /** Whether the encoder has been homed since boot. */
  public boolean isHomed() {
    return homed;
  }

  /**
   * Command that slowly drives the hood toward stow until the limit switch triggers,
   * then zeros the encoder to the stow angle. If already home, zeros immediately.
   */
  /**
   * @param owner The subsystem that owns this mechanism (passed so the CommandScheduler
   *              can prevent concurrent commands from fighting over this motor).
   */
  public Command homeCommand(SubsystemBase owner) {
    return Commands.either(
        // Already home — just zero
        Commands.runOnce(() -> {
          hoodEncoder.setPosition(degreesToRotations(ShooterConstants.PITCH_STOW_ANGLE));
          homed = true;
        }, owner),
        // Not home — drive slowly toward stow until switch triggers
        Commands.run(() -> hoodMotor.set(ShooterConstants.HOOD_HOMING_SPEED), owner)
            .until(this::isAtHome)
            .andThen(Commands.runOnce(() -> {
              hoodMotor.set(0);
              hoodEncoder.setPosition(degreesToRotations(ShooterConstants.PITCH_STOW_ANGLE));
              homed = true;
            }, owner)),
        this::isAtHome)
        .withName("Home Shooter Hood");
  }

  /** Call from Shooter.periodic() to update dashboard and auto-correct drift. */
  public void updateDashboard() {
    // Auto-correct: if limit switch triggers, re-zero to stow angle
    if (isAtHome()) {
      double stowRot = degreesToRotations(ShooterConstants.PITCH_STOW_ANGLE);
      if (Math.abs(hoodEncoder.getPosition() - stowRot) > degreesToRotations(ShooterConstants.PITCH_TOLERANCE)) {
        hoodEncoder.setPosition(stowRot);
        homed = true;
      }
    }

    SmartDashboard.putBoolean("Shooter/HoodLimitSwitch", isAtHome());
    SmartDashboard.putBoolean("Shooter/HoodHomed", homed);
  }

  private double degreesToRotations(double degrees) {
    return degrees / ShooterConstants.HOOD_DEGREES_PER_ROTATION;
  }
}