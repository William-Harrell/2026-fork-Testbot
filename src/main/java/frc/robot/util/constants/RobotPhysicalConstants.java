package frc.robot.util.constants;

import edu.wpi.first.math.util.Units;

public final class RobotPhysicalConstants {
  public static final double ROBOT_LENGTH = Units.inchesToMeters(26);
  public static final double ROBOT_WIDTH = Units.inchesToMeters(28);
  public static final double TURRET_HEIGHT = 0.4572; // meters
  public static final double BUMPER_THICKNESS = Units.inchesToMeters(3.5);

  // Total size including bumpers
  public static final double ROBOT_LENGTH_WITH_BUMPERS = ROBOT_LENGTH + 2 * BUMPER_THICKNESS;
  public static final double ROBOT_WIDTH_WITH_BUMPERS = ROBOT_WIDTH + 2 * BUMPER_THICKNESS;
}
