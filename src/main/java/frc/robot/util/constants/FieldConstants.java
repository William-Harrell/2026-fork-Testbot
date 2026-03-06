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

  // ================================================================
  // CENTER LINE (bisects the neutral zone — G403 auto boundary)
  // ================================================================
  public static final double CENTER_LINE_X = CENTER_X; // 8.27m

  // ================================================================
  // ALLIANCE ZONE BOUNDARIES (G407 — must be in zone to shoot)
  // Alliance zone is 158.6in (4.03m) deep from each alliance wall.
  // ================================================================
  public static final double BLUE_ALLIANCE_ZONE_MAX_X = ALLIANCE_ZONE_DEPTH;        // 4.03m
  public static final double RED_ALLIANCE_ZONE_MIN_X  = FIELD_LENGTH - ALLIANCE_ZONE_DEPTH; // 12.51m

  // ================================================================
  // OUTPOST / CHUTE collection positions
  // OUTPOST is at each alliance wall end on the non-scoring-table side.
  // X: ~1.0m from alliance wall. Y: ~1.5m from guardrail. TODO: verify on real field.
  // ================================================================
  public static final double BLUE_OUTPOST_X = 1.0;           // meters from Blue wall
  public static final double BLUE_OUTPOST_Y = 1.5;           // meters — TODO: measure
  public static final double RED_OUTPOST_X  = FIELD_LENGTH - 1.0;
  public static final double RED_OUTPOST_Y  = 1.5;           // TODO: measure

  // ================================================================
  // TOWER RUNG HEIGHTS (section 5.8 of game manual)
  // ================================================================
  public static final double LOW_RUNG_HEIGHT  = 0.686; // 27.0in — Level 2 threshold
  public static final double MID_RUNG_HEIGHT  = 1.143; // 45.0in — Level 3 threshold
  public static final double HIGH_RUNG_HEIGHT = 1.600; // 63.0in
}
