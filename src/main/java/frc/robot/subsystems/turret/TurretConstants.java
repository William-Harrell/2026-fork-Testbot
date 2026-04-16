package frc.robot.subsystems.turret;

public final class TurretConstants {
  // Config
  // public static final boolean AUTO_AIM_ENABLED = false;
  //
  //
  // Physical
  public static final double FLYWHEEL_RADIUS = 0.0508; // meters
  public static final double FLYWHEEL_EFFICIENCY = 1; // TODO: calibrate
  // ^ "Motors are moving, but how much of that is transferred to the ball?"
  //
  //
  // Yaw'll
  public static final double YAW_TOLERANCE = 1.0; // degrees
  public static final double MAX_YAW = 30.0 - YAW_TOLERANCE;
  public static final double MIN_YAW = 30.0 + YAW_TOLERANCE;
  public static final double INIT_YAW = 0.0;
  public static final double OFFSET_YAW = 0.0; // degrees TODO: calibrate
  public static final boolean INVERT_ABS_ENCODER = false;
  public static final double YAW_kP = 0.1; // TODO: placeholder
  public static final double YAW_kI = 0.0; // TODO: placeholder
  public static final double YAW_kD = 0.01; // TODO: placeholder
  public static final int YAW_STATOR_CURRENT_LIMIT = 40;
  public static final int YAW_SUPPLY_CURRENT_LIMIT = 30;
  // True = coast, false = break
  public static final boolean YAW_COAST = false;
  public static final double YawGearRatio = 462; // TODO: Measure gear ratio b/c from armabot website
  public static final boolean printYawPosition = true; //prints the current Yaw encoder position to the dashboard when enabled. In periodic of turret subsystem.

  //
  //
  // Pitch
  public static final double PITCH_TOLERANCE = 2.0 + 1.5; // degrees
  public static final double MAX_PITCH = 60.0 - PITCH_TOLERANCE;
  public static final double MIN_PITCH = 5.0 + PITCH_TOLERANCE;
  public static final double INIT_PITCH = 0.0;
  public static final double PITCH_DEGREE_RATIO = 3.0 / 25.0; // 25 motor rot = 3 big rot | 1:8.33
  public static final double PITCH_kP = 0.5;
  public static final double PITCH_kI = 0.0;
  public static final double PITCH_kD = 0.0;
  public static final int PITCH_STATOR_CURRENT_LIMIT = 40;
  public static final int PITCH_SUPPLY_CURRENT_LIMIT = 30;
  // True = coast, false = break
  public static final boolean PITCH_COAST = false;
  public static final double OFFSET_PITCH = 0.0; // degrees TODO: calibrate
  // ^ Degrees changed in pitch per encoder rotation
  //
  //
  //
  // CAN IDs
  public static final int HOOD_MOTOR_ID = 21; // Pitch
  public static final int FLYWHEEL_MOTOR_ID = 20; // Shooter
  public static final int KICKER_MOTOR_ID = 18; // Kicker
  public static final int TURN_MOTOR_ID = 22; // Yaw
  //
  //
  // Flywheel Motor Configuration
  public static final double FLYWHEEL_RPM_LIMIT = 6000;
  public static final double FLYWHEEL_SHOT_RPM = FLYWHEEL_RPM_LIMIT;
  public static final double FLYWHEEL_STARTUP_RPM = FLYWHEEL_RPM_LIMIT * 0.5;
  public static final double FLYWHEEL_RPM_TOLERANCE = 50.0;

  public static final double FLYWHEEL_RAMP_RATE = 0.75; // seconds (0, 1]

  // Look these up if you're interested.
  // It's called PID control. It's for error correction & smoothing.
  public static final double FLYWHEEL_kP = 0.2; // Proportional Gain
  public static final double FLYWHEEL_kI = 0.0; // Integral Gain
  public static final double FLYWHEEL_kD = 0.0005; // Derivative Gain
  public static final double FLYWHEEL_kS = 0.15; // Signum Gain (static friction)
  public static final double FLYWHEEL_kA = 0.015; // Acceleration Gain
  public static final double FLYWHEEL_kV = 12 / FLYWHEEL_SHOT_RPM * 60; // Velocity Gain

  public static final int FLYWHEEL_STATOR_CURRENT_LIMIT = 80; // Amps stator
  public static final int FLYWHEEL_SUPPLY_CURRENT_LIMIT = 60; // Amps from battery
  //
  //
  // Kicker Motor Configuration
  public static final int KICKER_STATOR_CURRENT_LIMIT = 80;
  public static final int KICKER_SUPPLY_CURRENT_LIMIT = 40;
  // True = coast, false = break
  public static final boolean KICKER_COAST = false;
  // True = motor reverse, false = motor spin forward.
  public static final boolean KICKER_SPIN_REVERSE = false;

  public static final int KICKER_SPIN_SPEED_MAX = 100; // TODO:Measure w/ rev client
  public static final int KICKER_SPIN_SPEED = 50; // TODO: Measure w/ rev client
}