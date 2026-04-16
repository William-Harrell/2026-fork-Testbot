package frc.robot.subsystems.swerve;// ethan feet was here

import edu.wpi.first.math.util.Units;

public final class SwerveConstants {
  // Pose Estimation TODO-tune w/ practice
  public static double XY_BASE_STDDEV = 0.1;
  public static double XY_DIST_FACTOR = 0.1;

  public static double HEADING_BASE_STDDEV = 0.1;
  public static double HEADING_DIST_FACTOR = 0.5;


  // Hardware
      //From wheel center axle TODO: measure for auto
  public static final double TRACK_WIDTH = Units.inchesToMeters(18.5); // Left to right 
  public static final double WHEEL_BASE = Units.inchesToMeters(24.75); // Front to back

  public static final double MAX_SPEED = 4.2; // meters per second
  public static final double MAX_ANGULAR_VELOCITY = 9.547; // rad per second

  public static final double WHEEL_DIAMETER = Units.inchesToMeters(4.0);
  public static final double WHEEL_CIRCUMFERENCE = WHEEL_DIAMETER * Math.PI;

  // SDS Mk4i L2 Gear Ratios
  public static final double DRIVE_GEAR_RATIO = 6.75; // motor spins 6.75 times per wheel rotation
  public static final double AZIMUTH_GEAR_RATIO = 150.0 / 7.0;

  // Front Left Module (IDs 7-9)
  public static final int FL_DRIVE_ID = 7;
  public static final int FL_AZIMUTH_ID = 9;
  public static final int FL_CANCODER_ID = 8;

  // Front Right Module (IDs 4-6)
  public static final int FR_DRIVE_ID = 4;
  public static final int FR_AZIMUTH_ID = 6;
  public static final int FR_CANCODER_ID = 5;

  // Rear Left Module (IDs 10-12)
  public static final int RL_DRIVE_ID = 10;
  public static final int RL_AZIMUTH_ID = 12;
  public static final int RL_CANCODER_ID = 11;

  // Rear Right Module (IDs 1-3)
  public static final int RR_DRIVE_ID = 1;
  public static final int RR_AZIMUTH_ID = 3;
  public static final int RR_CANCODER_ID = 2;

  // Gyro ID (Pigeon2)
  public static final int PIGEON_ID = 0;

  // ENCODER OFFSETS - Calibration for wheel angles

  // These values tell the robot what "forward" looks like for each wheel.
  // HOW TO CALIBRATE:
  // 1. Put robot on blocks (wheels off ground)
  // 2. Rotate each wheel so it points forward
  // 3. Read the CANCoder value in Phoenix Tuner and put it in the encoder offset
  // 4. use an angle finder and fine tune it
  
  // What are the things below:
    // BLANK_IRL_Degrees = 0.0 (whether or not CW or CCW) ((Angle measure) / 180.0);
    // BLANK_ENCODER_OFFSET = 0.0 (sign from Phoenix tuner) (Value from Phoenix Tuner) + BLANK_IRL_Degrees;

  // From the degree caliber in real life (units: Degrees) (CCW +) 
  //    (why is it 180?: tbh I don't know) 
  private static final double FL_IRL_Degrees = 0.0 + (0.0 / 180);
  private static final double FR_IRL_Degrees = 0.0 + (0.0 / 180);
  private static final double BL_IRL_Degrees = 0.0 + (0.0 / 180);
  private static final double BR_IRL_Degrees = 0.0 + (0.0 / 180);
 
  // (units: rotations)
  public static final double FL_ENCODER_OFFSET = 0.0 + 0.140381 + FL_IRL_Degrees;
  public static final double FR_ENCODER_OFFSET = 0.0 + 0.154297 + FR_IRL_Degrees;
  public static final double RL_ENCODER_OFFSET = 0.0 - 0.006592 + BL_IRL_Degrees;
  public static final double RR_ENCODER_OFFSET = 0.0 - 0.361572 + BR_IRL_Degrees;

  // DRIVE MOTOR PID - Tuning for wheel speed control

  // These values were found using SysId characterization
  public static final double DRIVE_kP = 0.064395;
  public static final double DRIVE_kI = 0.0;
  public static final double DRIVE_kD = 0.0;

  // Feedforward values (physics-based compensation)
  public static final double DRIVE_kS = 0.18656; // Static friction
  public static final double DRIVE_kV = 2.5833; // Velocity factor
  public static final double DRIVE_kA = 0.40138; // Acceleration factor

  // AZIMUTH MOTOR PID - Tuning for wheel angle control

  public static final double AZIMUTH_kP = 50.0; // TalonFX units: volts per rotation error — tune on robot
  public static final double AZIMUTH_kI = 0.0;
  public static final double AZIMUTH_kD = 0.0;

  // CURRENT LIMITS - Protects motors from overheating

  public static final int DRIVE_STATOR_LIMIT = 60; // Amps stator (was 60 — lowered for breaker safety)
  public static final int DRIVE_SUPPLY_LIMIT = 40; // Amps from battery
  public static final boolean DRIVE_STATOR_LIMIT_ENABLE = true;
  public static final boolean DRIVE_SUPPLY_LIMIT_ENABLE = true;

  public static final int AZIMUTH_STATOR_LIMIT = 30; // Amps (was 30 — steering mostly holds position)
  public static final int AZIMUTH_SUPPLY_LIMIT = 20; // Supply limit
  public static final boolean AZIMUTH_STATOR_LIMIT_ENABLE = true;
  public static final boolean AZIMUTH_SUPPLY_LIMIT_ENABLE = true;

  // COAST OR BRAKE MOTOR - whether or not motor resists rotation when neutral
    // True = Coast, False = brake
  public static final boolean DRIVE_COAST = true;
  public static final boolean AZIMUTH_COAST = false;

  // INVERT MOTORS / CANcoders - whether or not a motor is inverted
    // True = CW +, False = CCW +  
    //            or  
    // True = Invert, False = no Invert

  public static final boolean FL_DRIVE_INVERT = true;
  public static final boolean FL_AZIMUTH_INVERT = true;
  public static final boolean FL_CANcoder_INVERT = false;

  public static final boolean FR_DRIVE_INVERT = true;
  public static final boolean FR_AZIMUTH_INVERT = true;
  public static final boolean FR_CANcoder_INVERT = false;

  public static final boolean BL_DRIVE_INVERT = true;
  public static final boolean BL_AZIMUTH_INVERT = true;
  public static final boolean BL_CANcoder_INVERT = false;

  public static final boolean BR_DRIVE_INVERT = true;
  public static final boolean BR_AZIMUTH_INVERT = true;
  public static final boolean BR_CANcoder_INVERT = false;

  // RAMP RATES - How quickly motors speed up

  // Open loop = teleop driving, Closed loop = auto
  public static final double DRIVE_OPEN_LOOP_RAMP = 0.25; // Seconds to full power
  public static final double DRIVE_CLOSED_LOOP_RAMP = 0.0; // No ramp for precision

  // AUTONOMOUS PATH FOLLOWING - PID for auto routines

  public static final double AUTO_THETA_kP = 4.0; // Rotation correction
  public static final double AUTO_XY_kP = 2.0; // Position correction

  // VISION AIM - PID for rotating to face a target (hub/AprilTag)
  public static final double AIM_kP = 0.06; // TODO: tune — (rad/s) per degree of error
  public static final double AIM_kI = 0.0;
  public static final double AIM_kD = 0.004;
  public static final double AIM_TOLERANCE_DEG = 2.0; // degrees — "close enough" to target
}
