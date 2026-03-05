package frc.robot.subsystems.shooter;

import edu.wpi.first.wpilibj.DriverStation.Alliance;

public final class ShooterConstants {
  public static final Alliance DEFAULT_ALLIANCE = Alliance.Blue; // Default alliance

  public static final double BLUE_X = 11.915394;
  public static final double RED_X = 4.625594;
  public static final double UNIV_Y = 4.034663;

  public static final double HUB_RIM_HEIGHT = 1.8288;

  public static final double LAUNCH_ANGLE = 60.0; // degrees (placeholder TODO)
  public static final double G_ACCEL = 9.8067; // acceleration due to gravity (g) m/s^2
  public static final double Z_OFFSET = 0.5; // meters (placeholder TODO)

  public static final double ANGLE_TOLERANCE = 10.0; // degrees (placeholder TODO)

  // ================================================================
  // SHOOTER HARDWARE
  // ================================================================

  /** CAN ID for flywheel motor */
  public static final int FLYWHEEL_MOTOR_ID = 11; // (placeholder TODO)

  /** CAN ID for hood motor */
  public static final int HOOD_MOTOR_ID = 12; // (placeholder TODO)

  // Hex encoder (REV Through Bore, 8192 CPR)
  public static final int ENCODER_CPR = 8192;

  /** Degrees per encoder rotation (based on hood gear ratio) — TODO: measure on real robot */
  public static final double HOOD_DEGREES_PER_ROTATION = 10.0;

  /** Hood PID gains */
  public static final double HOOD_kP = 0.05; // TODO: tune
  public static final double HOOD_kI = 0.0;
  public static final double HOOD_kD = 0.0;

  // ================================================================
  // PITCH SERVO CONFIGURATION
  // ================================================================

  /** Tolerance for pitch angle reached */
  public static final double PITCH_TOLERANCE = 2.0; // degrees

  // ABS_LIMIT +/- (TOLERANCE + 1.5)
  public static final double PITCH_MIN_ANGLE = 5.0 + (PITCH_TOLERANCE + 1.5);
  public static final double PITCH_MAX_ANGLE = 60.0 - (PITCH_TOLERANCE + 1.5);

  /** Default/resting pitch angle */
  public static final double PITCH_STOW_ANGLE = 45.0; // (placeholder TODO)

  // ================================================================
  // FLYWHEEL CONFIGURATION
  // ================================================================

  /** Flywheel velocity for scoring (RPM) */
  public static final double FLYWHEEL_SHOOT_RPM = 4000.0; // (placeholder TODO)

  /** Flywheel idle/warmup velocity (RPM) */
  public static final double FLYWHEEL_IDLE_RPM = 1000.0; // (placeholder TODO)

  /** Tolerance for flywheel at target speed */
  public static final double FLYWHEEL_RPM_TOLERANCE = 100.0; // (placeholder TODO)

  /** Flywheel PID gains */
  public static final double FLYWHEEL_kP = 0.0005; // (placeholder TODO)

  public static final double FLYWHEEL_kI = 0.0;
  public static final double FLYWHEEL_kD = 0.0;
  public static final double FLYWHEEL_kFF = 0.000175; // (placeholder TODO)

  /** Flywheel current limit */
  public static final int FLYWHEEL_CURRENT_LIMIT = 40; // Amps

  /** Ramp rate for closed-loop spinup (seconds from 0 to full output) — limits inrush current */
  public static final double FLYWHEEL_RAMP_RATE = 0.75; // seconds
}
