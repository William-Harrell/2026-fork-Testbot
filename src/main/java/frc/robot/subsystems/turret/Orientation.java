//CHECK//

package frc.robot.subsystems.turret;

import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkClosedLoopController;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkFlexConfig;
import edu.wpi.first.wpilibj.DigitalInput;
import edu.wpi.first.wpilibj.motorcontrol.Spark;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.SubsystemBase;

public class Orientation {
  private final SparkFlex hoodMotor;
  private final RelativeEncoder hoodEncoder;
  private final SparkClosedLoopController hoodController;
  private final SparkFlex hoodMotor2;
  private final SparkClosedLoopController hoodController2;
  // private final DigitalInput limitSwitch;
  private double targetPitchAngle;
  // private boolean homed = false;

  public Orientation(SparkFlex motor, SparkFlex motor2) {
    hoodMotor = motor;
    hoodMotor2 = motor2;
    hoodEncoder = hoodMotor.getEncoder();
    hoodController = hoodMotor.getClosedLoopController();
    hoodController2 = hoodMotor2.getClosedLoopController();

    SparkFlexConfig config = new SparkFlexConfig();
    config.idleMode(IdleMode.kBrake).smartCurrentLimit(70);
    config.closedLoop
        .feedbackSensor(FeedbackSensor.kPrimaryEncoder)
    // .p(TurretConstants.HOOD_kP)
    // .i(TurretConstants.HOOD_kI)
    // .d(TurretConstants.HOOD_kD)
    ;

    hoodMotor.configure(config, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);

    // limitSwitch = new DigitalInput(TurretConstants.HOOD_LIMIT_SWITCH_DIO);
    targetPitchAngle = TurretConstants.INIT_PITCH;

    // If already at home on boot, zero to stow angle immediately
    // if (isAtHome()) {
    hoodEncoder.setPosition(degreesToRotations(TurretConstants.INIT_PITCH));
    // homed = true;
    // }
  }

  /**
   * Set the pitch angle of the shooter hood.
   *
   * @param angleDegrees Target angle in degrees
   */
  public void setPitchAngle(double angleDegrees) {
    targetPitchAngle = Math.max(
        TurretConstants.MIN_PITCH,
        Math.min(TurretConstants.MAX_PITCH, angleDegrees));
    hoodController.setSetpoint(degreesToRotations(targetPitchAngle), SparkFlex.ControlType.kPosition);
    hoodController2.setSetpoint(degreesToRotations(targetPitchAngle), SparkFlex.ControlType.kPosition);
  }

  /** Get the target pitch angle. */
  public double getTargetPitchAngle() {
    return targetPitchAngle;
  }

  /** Get the actual pitch angle from the hex encoder. */
  public double getActualPitchAngle() {
    return hoodEncoder.getPosition() * TurretConstants.PITCH_DEGREE_RATIO;
  }

  /** Check if the hood is at its target angle within tolerance. */
  public boolean isPitchAtTarget() {
    return Math.abs(getActualPitchAngle() - targetPitchAngle) < TurretConstants.PITCH_TOLERANCE;
  }

  private double degreesToRotations(double degrees) {
    return degrees / TurretConstants.PITCH_DEGREE_RATIO;
  }
}