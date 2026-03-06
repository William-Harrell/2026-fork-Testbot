package frc.robot.util.constants;

public final class DrivingConstants {
  public static final int CONTROLLER_PORT = 0;

  public static final boolean OPEN_LOOP =
      true; // Motor runs @ % (true) or exact speed w/ encoder (false)
  public static final double NORMAL_SPEED_MULTIPLIER = 1.0; // Scale according to joystick
  public static final double SLOW_SPEED_MULTIPLIER = 0.3;

  // Match SwerveConstants.MAX_SPEED / MAX_ANGULAR_VELOCITY — tune acceleration on real robot
  public static final double DRIVE_MAX_VEL = 4.2; // m/s
  public static final double DRIVE_MAX_ACC = 3.0; // m/s²

  public static final double TURN_MAX_VEL = 9.547; // rad/s
  public static final double TURN_MAX_ACC = 12.0; // rad/s²
}
