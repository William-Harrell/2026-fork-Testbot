package frc.robot.util.constants;

public final class FieldConstants {
  // Field size: 651.2in x 317.7in (from REBUILT 2026 game manual)
  public static final double FIELD_LENGTH = 16.5405; // meters
  public static final double FIELD_WIDTH = 8.0696; // meters

  // Field center
  public static final double CENTER_X = FIELD_LENGTH / 2.0;
  public static final double CENTER_Y = FIELD_WIDTH / 2.0;

  // Alliance Zone boundary (158.6 inches from each alliance wall)
  public static final double ALLIANCE_ZONE_DEPTH = 4.03; // meters

  // ================================================================
  // HUB (2 per field, one per alliance)
  // ================================================================
  public static final double HUB_SIZE = 1.194; // 47in x 47in
  public static final double HUB_HEIGHT = 1.83; // 72in opening height
  public static final double HUB_DISTANCE_FROM_WALL = 4.03;

  // Red HUB position (near red alliance wall, x = max)
  public static final double RED_HUB_X = FIELD_LENGTH - HUB_DISTANCE_FROM_WALL;
  public static final double RED_HUB_Y = CENTER_Y;

  // Blue HUB position (near blue alliance wall, x = 0)
  public static final double BLUE_HUB_X = HUB_DISTANCE_FROM_WALL;
  public static final double BLUE_HUB_Y = CENTER_Y;

  // ================================================================
  // BUMP (flanking each HUB)
  // ================================================================
  public static final double BUMP_LENGTH = 1.854;
  public static final double BUMP_WIDTH = 1.128;
  public static final double BUMP_HEIGHT = 0.165;

  // ================================================================
  // TOWER (climbing structure, one per alliance)
  // ================================================================
  public static final double TOWER_DISTANCE_FROM_WALL = 0.625;

  public static final double RED_TOWER_X = FIELD_LENGTH - TOWER_DISTANCE_FROM_WALL;
  public static final double RED_TOWER_Y = CENTER_Y + 2.0;

  public static final double BLUE_TOWER_X = TOWER_DISTANCE_FROM_WALL;
  public static final double BLUE_TOWER_Y = CENTER_Y + 2.0;

  // Tower engagement radius
  public static final double TOWER_ENGAGEMENT_RADIUS = 2.0; // meters

  // ================================================================
  // DEPOT (FUEL collection area, one per alliance)
  // ================================================================
  public static final double RED_DEPOT_X = FIELD_LENGTH - 1.0;
  public static final double RED_DEPOT_Y = 1.5;

  public static final double BLUE_DEPOT_X = 1.0;
  public static final double BLUE_DEPOT_Y = 1.5;

  // ================================================================
  // NEUTRAL ZONE (field center area)
  // ================================================================
  public static final double NEUTRAL_ZONE_X = CENTER_X;
  public static final double NEUTRAL_ZONE_Y = CENTER_Y;
  public static final double NEUTRAL_CLOSE_OFFSET = 2.0; // closer collection point
  public static final double NEUTRAL_FAR_OFFSET = 3.0; // farther collection point
}
