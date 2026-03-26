package frc.robot.subsystems.turret;

public final class TurretConstants {
  // Physical
  public static final double FLYWHEEL_WHEEL_RADIUS_M = 0.0508;
  public static final double Z_OFFSET = 0.5; // TODO: measure shooter offset from ground
  public static final double FLYWHEEL_EFFICIENCY = 0.85;
  // ^ "Motors are moving, but how much of that is transferred to the ball?"
  //
  //
  // Yaw
  public static final double YAW_TOLERANCE = 2.0 + 1.5; // degrees
  public static final double MAX_YAW = 300 - YAW_TOLERANCE;
  public static final double MIN_YAW = 0.0 + YAW_TOLERANCE;
  public static final double INIT_YAW = 180; // TODO: placeholder
  public static final double YAW_DEGREE_RATIO = 10.0; // TODO: get new gear ratio from design
  //
  //
  // Pitch
  public static final double PITCH_TOLERANCE = 2.0 + 1.5; // degrees
  public static final double MAX_PITCH = 60.0 - PITCH_TOLERANCE;
  public static final double MIN_PITCH = 5.0 + PITCH_TOLERANCE;
  public static final double INIT_PITCH = 45.0; // TODO: placeholder
  public static final double PITCH_DEGREE_RATIO = 10.0; // TODO: get new gear ratio from design
  // ^ Degrees changed in pitch per encoder rotation
  //
  //
  //
  // CAN IDs
  public static final int HOOD_MOTOR_1_ID = 16; // Pitch

  public static final int FLYWHEEL_MOTOR_1_ID = 18; // Shooter

  // Yaw
  public static final int TURN_MOTOR_ID = 0; // TODO: placeholders
  //
  //
  //
  // Flywheel Motor Configuration
  public static final double FLYWHEEL_SHOT_VELOCITY = 0; // TODO: replace w/ real shot velocity
  public static final double FLYWHEEL_RPM_TOLERANCE = 50.0;

  public static final double FLYWHEEL_RAMP_RATE = 0.75; // seconds (0, 1]

  public static final double FLYWHEEL_kP = 0.2;
  public static final double FLYWHEEL_kI = 0.0;
  public static final double FLYWHEEL_kD = 0.0005;
  public static final double FLYWHEEL_kS = 0.15;
  public static final double FLYWHEEL_kA = 0.015;
  public static final double FLYWHEEL_kV = 12 / FLYWHEEL_SHOT_VELOCITY * 60;

  public static final int FLYWHEEL_STATOR_CURRENT_LIMIT = 120; // Amps stator
  public static final int FLYWHEEL_SUPPLY_CURRENT_LIMIT = 80; // Amps from battery
  //
  //
  //
  // Pitch Motor Configuration
  // public static final double HOOD_kP = 0.5;
  // public static final double HOOD_kI = 0.0;
  // public static final double HOOD_kD = 0.0;
}