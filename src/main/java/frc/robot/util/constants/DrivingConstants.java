package frc.robot.util.constants;

public final class DrivingConstants {
  public static final int CONTROLLER_PORT = 0;

  public static final boolean OPEN_LOOP =
      true; // Motor runs @ % (true) or exact speed w/ encoder (false)
  public static final double NORMAL_SPEED_MULTIPLIER = 1.0; // Scale according to joystick
  public static final double SLOW_SPEED_MULTIPLIER = 0.3;
}
