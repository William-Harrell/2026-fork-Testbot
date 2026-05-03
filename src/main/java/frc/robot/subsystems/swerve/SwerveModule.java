package frc.robot.subsystems.swerve;

/*
 * ========================================================================
 * SWERVE MODULE - One wheel unit of the swerve drive
 * ========================================================================
 *
 * Each swerve module has:
 *   - A DRIVE motor (TalonFX) that spins the wheel
 *   - An AZIMUTH motor (TalonFX) that steers the wheel
 *   - A CANCoder that knows the absolute wheel angle
 *
 * -> Set desired state: module.setDesiredState(state, openLoop)
 * -> Reset encoder: module.resetToAbsolute()
 *
 * ========================================================================
 */

import com.ctre.phoenix6.configs.CANcoderConfiguration;
import com.ctre.phoenix6.hardware.CANcoder;
import com.ctre.phoenix6.signals.SensorDirectionValue;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkBase.ControlType;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkMaxConfig;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.ResetMode;
import com.revrobotics.PersistMode;
import com.revrobotics.RelativeEncoder;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.math.kinematics.SwerveModuleState;

public class SwerveModule {

  private final int moduleNumber;
  private final boolean invertDrive;
  private final boolean invertAzimuth;
  private final boolean invertCANcoder;
  

  private final SparkMax driveMotor;
  private final SparkMax azimuthMotor;
  private final CANcoder canCoder;

  private final RelativeEncoder driveEncoder;
  private final RelativeEncoder azimuthEncoder;
  private final Rotation2d encoderOffset;


  public SwerveModule(
      int moduleNumber,
      int driveMotorId,
      int azimuthMotorId,
      int canCoderId,
      double encoderOffset,
      boolean invertDrive,
      boolean invertAzimuth,
      boolean invertCANcoder) {
    this.moduleNumber = moduleNumber;
    this.encoderOffset = Rotation2d.fromRotations(encoderOffset);

    this.invertDrive = invertDrive;
    this.invertAzimuth = invertAzimuth;
    this.invertCANcoder = invertCANcoder;

    // Drive motor 
    driveMotor = new SparkMax(driveMotorId, MotorType.kBrushless);
    this.driveEncoder = driveMotor.getEncoder();

    // Azimuth motor 
    azimuthMotor = new SparkMax(azimuthMotorId, MotorType.kBrushless);
    this.azimuthEncoder = azimuthMotor.getEncoder();

    // CANCoder (absolute encoder)
    canCoder = new CANcoder(canCoderId);

    configureDriveMotor();
    configureAzimuthMotor();
    configureCANCoder();
    resetToAbsolute();
  }

  private void configureDriveMotor() {
    SparkMaxConfig configDrive = new SparkMaxConfig();

    configDrive.idleMode(SwerveConstants.DRIVE_COAST ? 
      IdleMode.kCoast : IdleMode.kBrake);
    configDrive.inverted(invertDrive);
    configDrive.smartCurrentLimit(SwerveConstants.DRIVE_STATOR_LIMIT, SwerveConstants.DRIVE_SUPPLY_LIMIT);

    configDrive.openLoopRampRate(SwerveConstants.DRIVE_OPEN_LOOP_RAMP);
    configDrive.closedLoopRampRate(SwerveConstants.DRIVE_CLOSED_LOOP_RAMP);

    configDrive.closedLoop.pid(
      SwerveConstants.DRIVE_kP,
      SwerveConstants.DRIVE_kI,
      SwerveConstants.DRIVE_kD);

    configDrive.encoder.positionConversionFactor(SwerveConstants.DRIVE_GEAR_RATIO);
    configDrive.encoder.velocityConversionFactor(SwerveConstants.DRIVE_GEAR_RATIO);

    driveMotor.configure(configDrive, 
    ResetMode.kResetSafeParameters, 
    PersistMode.kPersistParameters);
  }

  private void configureAzimuthMotor() {
    SparkMaxConfig configAzimuth = new SparkMaxConfig();

    configAzimuth.idleMode(SwerveConstants.AZIMUTH_COAST ? 
      IdleMode.kCoast : IdleMode.kBrake);
    configAzimuth.inverted(invertAzimuth);
    configAzimuth.smartCurrentLimit(SwerveConstants.AZIMUTH_STATOR_LIMIT, SwerveConstants.AZIMUTH_SUPPLY_LIMIT);

    // configAzimuth.closedLoop.feedbackSensor(FeedbackSensor.kAbsoluteEncoder, CancoderID); TODO: maybe
    configAzimuth.closedLoop.positionWrappingEnabled(true);
      double mininput = -0.5;
      double maxinput = 0.5;
    configAzimuth.closedLoop.positionWrappingInputRange(mininput, maxinput);
    configAzimuth.closedLoop.pid(
      SwerveConstants.AZIMUTH_kP,
      SwerveConstants.AZIMUTH_kI,
      SwerveConstants.AZIMUTH_kD);

    configAzimuth.encoder.positionConversionFactor(SwerveConstants.AZIMUTH_GEAR_RATIO);
    configAzimuth.encoder.velocityConversionFactor(SwerveConstants.AZIMUTH_GEAR_RATIO);
    
    azimuthMotor.configure(configAzimuth,
    ResetMode.kResetSafeParameters,
    PersistMode.kPersistParameters);
  }

  private void configureCANCoder() {
    CANcoderConfiguration CANcoderconfig = new CANcoderConfiguration();
    CANcoderconfig.MagnetSensor.SensorDirection = invertCANcoder ?
      SensorDirectionValue.Clockwise_Positive : SensorDirectionValue.CounterClockwise_Positive;

    // Apply the magnet offset so CANCoder reads 0 when wheel faces forward
    CANcoderconfig.MagnetSensor.MagnetOffset = encoderOffset.getRotations();
    canCoder.getConfigurator().apply(CANcoderconfig);
  }

  /** Sync is not needed — CANCoder is the direct feedback sensor for the azimuth TalonFX. */
  public void resetToAbsolute() {
    azimuthEncoder.setPosition(canCoder.getPosition().getValueAsDouble());
  }

  // ================================================================
  // GETTERS
  // ================================================================

  public Rotation2d getAbsoluteAngle() {
    return Rotation2d.fromRotations(canCoder.getAbsolutePosition().getValueAsDouble());
  }

  /** Raw CANCoder reading in degrees (no offset applied). For calibration only. */
  public double getRawCANCoderDegrees() {
    return canCoder.getAbsolutePosition().getValueAsDouble() * 360.0;
  }

  /** Get the current angle from the azimuth feedback (CANCoder via TalonFX). */
  public Rotation2d getAngle() {
    // TalonFX position with RemoteCANcoder returns rotations
    return Rotation2d.fromRotations(azimuthEncoder.getPosition());
  }

  public double getVelocity() {
    return driveEncoder.getVelocity();
  }

  public double getPosition() {
    return driveEncoder.getPosition();
  }

  public SwerveModuleState getState() {
    return new SwerveModuleState(getVelocity(), getAngle());
  }

  public SwerveModulePosition getModulePosition() {
    return new SwerveModulePosition(getPosition(), getAngle());
  }

  // ================================================================
  // CONTROL
  // ================================================================

  public void setDesiredState(SwerveModuleState desiredState, boolean openLoop) {
    desiredState.optimize(getAngle());

    // Azimuth — position control in rotations (CANCoder units)
    double targetRotations = desiredState.angle.getRotations();
    azimuthMotor.getClosedLoopController().setSetpoint(targetRotations, ControlType.kPosition);

    // Drive
    if (openLoop) {
      double percentOutput = desiredState.speedMetersPerSecond / SwerveConstants.MAX_SPEED;
      driveMotor.set(percentOutput);
    } else {
      driveMotor.getClosedLoopController().setSetpoint(desiredState.speedMetersPerSecond, ControlType.kVelocity);
    }
  }

  public void stop() {
    driveMotor.stopMotor();
    azimuthMotor.stopMotor();
  }

  public int getModuleNumber() {
    return moduleNumber;
  }
}
