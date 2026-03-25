package frc.robot.util.constants;

import edu.wpi.first.math.util.Units;

public final class FieldConstants {
  public static final double FIELD_LENGTH = Units.inchesToMeters(651.2);
  public static final double FIELD_WIDTH = Units.inchesToMeters(317.7);

  public static final double CENTER_X = FIELD_LENGTH / 2.0;
  public static final double CENTER_Y = FIELD_WIDTH / 2.0;

  public static final double ALLIANCE_ZONE_DEPTH = Units.inchesToMeters(158.6);

  public static final double HUB_SIZE = Units.inchesToMeters(47) * Units.inchesToMeters(47);
  public static final double HUB_HEIGHT = Units.inchesToMeters(72);
  public static final double HUB_DISTANCE_FROM_WALL = 4.03; // meters

  public static final double RED_HUB_X = FIELD_LENGTH - HUB_DISTANCE_FROM_WALL;
  public static final double RED_HUB_Y = CENTER_Y;

  public static final double BLUE_HUB_X = HUB_DISTANCE_FROM_WALL;
  public static final double BLUE_HUB_Y = CENTER_Y;

  public static final double BLUE_ALLIANCE_ZONE_MAX_X = ALLIANCE_ZONE_DEPTH;
  public static final double RED_ALLIANCE_ZONE_MIN_X = FIELD_LENGTH - ALLIANCE_ZONE_DEPTH;
}
