package frc.robot.util.constants;

import frc.robot.auto.Auto.FieldObject;

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

  // Wall thickness (perimeter rail ~2in)
  private static final double WALL_T = 0.135;

  // Hub bounding box half-size (47in = 1.194m is given as HUB_SIZE)
  // The hub is ~47in across, so half = 0.597m
  private static final double HUB_HALF = FieldConstants.HUB_SIZE / 2.0;

  // Trench dimensions: 73in wide (1.854m) x 22.25in deep (0.565m)
  private static final double TRENCH_X_SIZE = 1.854; // along field length axis
  private static final double TRENCH_Y_SIZE = 0.565; // depth into field

  // Outpost/Tower footprint against alliance wall: ~28in wide (0.711m), ~22in
  // deep (0.559m)
  private static final double STATION_ELEMENT_WIDTH = 0.432; // Y span (17in gap between pairs)
  private static final double STATION_ELEMENT_DEPTH = 0.559; // X depth off wall

  public static FieldObject[] nogos = {
      // =====================================================================
      // PERIMETER WALLS
      // =====================================================================
      new FieldObject(0, 0, FieldConstants.FIELD_LENGTH, WALL_T), // Bottom (scoring table side)
      new FieldObject(0, FieldConstants.FIELD_WIDTH - WALL_T, FieldConstants.FIELD_LENGTH, WALL_T), // Top (audience
                                                                                                    // side)
      new FieldObject(0, 0, WALL_T, FieldConstants.FIELD_WIDTH), // Left (Blue alliance wall)
      new FieldObject(FieldConstants.FIELD_LENGTH - WALL_T, 0, WALL_T, FieldConstants.FIELD_WIDTH), // Right (Red
                                                                                                    // alliance wall)

      // =====================================================================
      // HUB — RED (center at RED_HUB_X, RED_HUB_Y)
      // =====================================================================
      new FieldObject(
          FieldConstants.RED_HUB_X - HUB_HALF,
          FieldConstants.RED_HUB_Y - HUB_HALF,
          FieldConstants.HUB_SIZE,
          FieldConstants.HUB_SIZE),

      // =====================================================================
      // HUB — BLUE (center at BLUE_HUB_X, BLUE_HUB_Y)
      // =====================================================================
      new FieldObject(
          FieldConstants.BLUE_HUB_X - HUB_HALF,
          FieldConstants.BLUE_HUB_Y - HUB_HALF,
          FieldConstants.HUB_SIZE,
          FieldConstants.HUB_SIZE),

      // =====================================================================
      // TRENCHES — RED (x ≈ 11.878m from Blue wall)
      // One at scoring table side (y=0), one at audience side (y=max)
      // =====================================================================
      new FieldObject(
          FieldConstants.FIELD_LENGTH - FieldConstants.ALLIANCE_ZONE_DEPTH - TRENCH_X_SIZE,
          WALL_T, // flush with bottom wall interior
          TRENCH_X_SIZE,
          TRENCH_Y_SIZE),
      new FieldObject(
          FieldConstants.FIELD_LENGTH - FieldConstants.ALLIANCE_ZONE_DEPTH - TRENCH_X_SIZE,
          FieldConstants.FIELD_WIDTH - WALL_T - TRENCH_Y_SIZE, // flush with top wall interior
          TRENCH_X_SIZE,
          TRENCH_Y_SIZE),

      // =====================================================================
      // TRENCHES — BLUE (x ≈ 4.663m from Blue wall)
      // =====================================================================
      new FieldObject(
          FieldConstants.ALLIANCE_ZONE_DEPTH,
          WALL_T,
          TRENCH_X_SIZE,
          TRENCH_Y_SIZE),
      new FieldObject(
          FieldConstants.ALLIANCE_ZONE_DEPTH,
          FieldConstants.FIELD_WIDTH - WALL_T - TRENCH_Y_SIZE,
          TRENCH_X_SIZE,
          TRENCH_Y_SIZE),

      // =====================================================================
      // OUTPOST — RED (against Red wall, audience-side corner y≈6.97m)
      // From sheet 11: spans y=274.47–291.47in → 6.971–7.403m
      // =====================================================================
      new FieldObject(
          FieldConstants.FIELD_LENGTH - WALL_T - STATION_ELEMENT_DEPTH,
          6.971,
          STATION_ELEMENT_DEPTH,
          STATION_ELEMENT_WIDTH),

      // =====================================================================
      // OUTPOST — BLUE (against Blue wall, scoring-table-side corner y≈0.666m)
      // From sheet 11: spans y=26.22–43.22in → 0.666–1.098m
      // =====================================================================
      new FieldObject(
          WALL_T,
          0.666,
          STATION_ELEMENT_DEPTH,
          STATION_ELEMENT_WIDTH),

      // =====================================================================
      // TOWER — RED (against Red wall, mid-field y≈3.892m)
      // From sheet 11: spans y=153.22–170.22in → 3.892–4.323m
      // =====================================================================
      new FieldObject(
          FieldConstants.FIELD_LENGTH - WALL_T - STATION_ELEMENT_DEPTH,
          3.892,
          STATION_ELEMENT_DEPTH,
          STATION_ELEMENT_WIDTH),

      // =====================================================================
      // TOWER — BLUE (against Blue wall, mid-field y≈3.746m)
      // From sheet 11: spans y=147.47–164.47in → 3.746–4.178m
      // =====================================================================
      new FieldObject(
          WALL_T,
          3.746,
          STATION_ELEMENT_DEPTH,
          STATION_ELEMENT_WIDTH),
  };
}
