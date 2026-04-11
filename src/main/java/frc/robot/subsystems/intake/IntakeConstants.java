package frc.robot.subsystems.intake;

public final class IntakeConstants {
  public static final int DEPLOY_MOTOR_ID = 15; // Vortex (SparkFlex)
  public static final int ROLLER_MOTOR_ID = 16; // Kraken X60 (TalonFX)
  public static final int KICKER_MOTOR_ID = 18; // SparkFlex
  // ---------------------------------------------------------------
  // DEPLOY POSITION CONSTANTS
  // Units: raw SparkFlex encoder rotations (no conversion factor set).
  // 1 encoder rotation = 1 motor shaft rotation.
  // The actual arm travel per motor rotation depends on the gear ratio.
  // Steps to calibrate:
  // 1. Home the mechanism (limit switch triggers → position = 0).
  // 2. Manually deploy to the fully-extended position.
  // 3. Read deployEncoder.getPosition() and set DEPLOYED_POSITION to that value.
  // Then verify POSITION_TOLERANCE is a reasonable fraction of the full travel.
  // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
  public static final double DEPLOYED_POSITION = 2.425;
  public static final double STOWED_POSITION = 0.0; // Home / limit-switch position
  public static final double POSITION_TOLERANCE = 0.05; // ~5% of 1 rotation — adjust after calibration

  public static final double DEPLOY_kP = 0.1;
  public static final double INTAKE_SPEED = -.95;
  public static final double OUTTAKE_SPEED = .7;

  // Limit switch for deploy homing (roboRIO DIO port)
  public static final int DEPLOY_LIMIT_SWITCH_DIO = 0;
  public static final double HOMING_SPEED = -0.15; // slow retract toward stow
}