// CHECK //

package frc.robot.subsystems.shooter;

public final class ShooterConstants {
  public static final double HUB_RIM_HEIGHT = 1.8288;

  public static final double G_ACCEL = 9.8067; // gravity, duh
  public static final double Z_OFFSET = 0.5; // TODO: measure shooter offset from ground

  public static final double ANGLE_TOLERANCE = 2.0; // degrees

  // SHOOTER HARDWARE
  /** CAN ID for flywheel motor 1 (Kraken/TalonFX) */
  public static final int FLYWHEEL_MOTOR_ID = 18;

  /** CAN ID for flywheel motor 2 (Kraken/TalonFX) */
  public static final int FLYWHEEL_MOTOR_2_ID = 19;

  /** CAN ID for hood motor (Vortex/SparkFlex) */
  public static final int HOOD_MOTOR_ID1 = 16;
  public static final int HOOD_MOTOR_ID2 = 17;

  // Degrees per encoder rotation (based on hood gear ratio) — TODO: get new gear
  // ratio from design
  public static final double HOOD_DEGREES_PER_ROTATION = 10.0;

  /** Hood PID gains — tune on real robot */
  public static final double HOOD_kP = 0.5;
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
  public static final double PITCH_STOW_ANGLE = 45.0; // degrees — tune on real robot

  // ================================================================
  // FLYWHEEL CONFIGURATION
  // ================================================================

  /** Flywheel velocity for scoring (RPM) — tune on real robot */
  public static final double FLYWHEEL_SHOOT_RPM = 8000.0;

  /** Flywheel idle/warmup velocity (RPM) */
  public static final double FLYWHEEL_IDLE_RPM = 2000.0;

  /** Tolerance for flywheel at target speed */
  public static final double FLYWHEEL_RPM_TOLERANCE = 50.0;

  /**
   * Flywheel PID gains — TalonFX units (kP: V/RPS error, kFF/kV: V/RPS).
   * Starting estimates only — characterize with SysId on real robot.
   */
  public static final double FLYWHEEL_kP = 0.2;
  public static final double FLYWHEEL_kI = 0.0;
  public static final double FLYWHEEL_kD = 0.0005;
  public static final double FLYWHEEL_kS = 0.15;
  public static final double FLYWHEEL_kA = 0.015;
  public static final double FLYWHEEL_kV = 12 / ShooterConstants.FLYWHEEL_SHOOT_RPM;

  /**
   * Flywheel wheel radius for exit velocity calculation (meters).
   * Assumes a 4-inch (0.1016m diameter) compliant wheel — measure on real robot.
   */
  public static final double FLYWHEEL_WHEEL_RADIUS_M = 0.0508;

  /**
   * Fraction of flywheel surface speed that transfers to the ball (~80-90% is
   * typical).
   * Tune this with a chronograph or measured shot data.
   */
  public static final double FLYWHEEL_EFFICIENCY = 0.85;

  /** Flywheel stator current limit */
  public static final int FLYWHEEL_CURRENT_LIMIT = 120; // Amps stator

  /** Flywheel supply current limit (protects battery/breaker) */
  public static final int FLYWHEEL_SUPPLY_CURRENT_LIMIT = 80; // Amps from battery

  /**
   * Ramp rate for closed-loop spinup (seconds from 0 to full output) — limits
   * inrush current
   */
  public static final double FLYWHEEL_RAMP_RATE = 0.75; // seconds

  // Limit switch for hood homing (roboRIO DIO port)
  public static final int HOOD_LIMIT_SWITCH_DIO = 6;
  public static final double HOOD_HOMING_SPEED = -0.1; // slow toward stow
}