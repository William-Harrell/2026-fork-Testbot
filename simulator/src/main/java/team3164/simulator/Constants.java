package team3164.simulator;

/**
 * Central constants file for the REBUILT 2026 simulator.
 * Values match frc.robot source: FieldConstants, SwerveConstants,
 * DrivingConstants, ShooterConstants, ClimberConstants.
 */
public final class Constants {

    private Constants() {}

    // =====================================================================
    // FIELD  (from frc.robot.util.constants.FieldConstants)
    // =====================================================================
    public static final class Field {
        /** Full field length — 651.2 in = 16.5405 m */
        public static final double LENGTH = 16.5405;
        /** Full field width  — 317.7 in = 8.0696 m */
        public static final double WIDTH  = 8.0696;

        public static final double CENTER_X = LENGTH / 2.0;   // 8.27025
        public static final double CENTER_Y = WIDTH  / 2.0;   // 4.0348

        /** Alliance zone depth from each end wall (158.6 in = 4.03 m) */
        public static final double ALLIANCE_ZONE_DEPTH = 4.03;

        // ── HUB ──────────────────────────────────────────────────────────
        public static final double HUB_SIZE   = 1.194; // 47 in square
        public static final double HUB_HEIGHT = 1.83;  // 72 in opening

        /** Distance of each hub centre from its alliance wall */
        public static final double HUB_DISTANCE_FROM_WALL = 4.03;

        public static final double RED_HUB_X  = LENGTH - HUB_DISTANCE_FROM_WALL; // 12.5105
        public static final double RED_HUB_Y  = CENTER_Y;
        public static final double BLUE_HUB_X = HUB_DISTANCE_FROM_WALL;          // 4.03
        public static final double BLUE_HUB_Y = CENTER_Y;

        // ── BUMP ─────────────────────────────────────────────────────────
        public static final double BUMP_LENGTH     = 1.854;
        public static final double BUMP_WIDTH      = 1.128;
        public static final double BUMP_HEIGHT     = 0.165;
        public static final double BUMP_RAMP_ANGLE = 15.0;  // degrees

        // Bumps flank each hub (above/below in Y, same X as hub)
        public static final double RED_BUMP_1_X  = RED_HUB_X;
        public static final double RED_BUMP_1_Y  = RED_HUB_Y  - 1.5;
        public static final double RED_BUMP_2_X  = RED_HUB_X;
        public static final double RED_BUMP_2_Y  = RED_HUB_Y  + 1.5;
        public static final double BLUE_BUMP_1_X = BLUE_HUB_X;
        public static final double BLUE_BUMP_1_Y = BLUE_HUB_Y - 1.5;
        public static final double BLUE_BUMP_2_X = BLUE_HUB_X;
        public static final double BLUE_BUMP_2_Y = BLUE_HUB_Y + 1.5;

        // ── TRENCH ───────────────────────────────────────────────────────
        public static final double TRENCH_LENGTH    = 3.0;
        public static final double TRENCH_WIDTH     = 1.2;
        public static final double TRENCH_HEIGHT    = 0.508; // 20 in clearance
        public static final double TRENCH_CLEARANCE = 0.508;

        public static final double RED_TRENCH_1_X  = LENGTH - 5.0;
        public static final double RED_TRENCH_1_Y  = 0.9;
        public static final double RED_TRENCH_2_X  = LENGTH - 5.0;
        public static final double RED_TRENCH_2_Y  = WIDTH - 0.9;
        public static final double BLUE_TRENCH_1_X = 5.0;
        public static final double BLUE_TRENCH_1_Y = 0.9;
        public static final double BLUE_TRENCH_2_X = 5.0;
        public static final double BLUE_TRENCH_2_Y = WIDTH - 0.9;

        // ── TOWER (climbing structure) ────────────────────────────────────
        /** Distance of tower from the back wall */
        public static final double TOWER_DISTANCE_FROM_WALL = 0.625;

        public static final double TOWER_LENGTH = 1.5;
        public static final double TOWER_WIDTH  = 1.0;
        public static final double TOWER_HEIGHT = 2.0;

        public static final double RED_TOWER_X  = LENGTH - TOWER_DISTANCE_FROM_WALL;
        public static final double RED_TOWER_Y  = CENTER_Y + 2.0;
        public static final double BLUE_TOWER_X = TOWER_DISTANCE_FROM_WALL;
        public static final double BLUE_TOWER_Y = CENTER_Y + 2.0;

        public static final double TOWER_ENGAGEMENT_RADIUS = 2.0;

        // ── DEPOT (FUEL storage area) ─────────────────────────────────────
        public static final double RED_DEPOT_X  = LENGTH - 1.0;
        public static final double RED_DEPOT_Y  = 1.5;
        public static final double BLUE_DEPOT_X = 1.0;
        public static final double BLUE_DEPOT_Y = 1.5;

        // ── NEUTRAL ZONE ──────────────────────────────────────────────────
        public static final double NEUTRAL_ZONE_X        = CENTER_X;
        public static final double NEUTRAL_ZONE_Y        = CENTER_Y;
        public static final double NEUTRAL_CLOSE_OFFSET  = 2.0;
        public static final double NEUTRAL_FAR_OFFSET    = 3.0;

        // ── ALLIANCE ZONE BOUNDARIES ──────────────────────────────────────
        public static final double BLUE_ALLIANCE_ZONE_MAX_X = ALLIANCE_ZONE_DEPTH;
        public static final double RED_ALLIANCE_ZONE_MIN_X  = LENGTH - ALLIANCE_ZONE_DEPTH;

        // ── OUTPOST (alias names used in OutpostPhysics) ──────────────────
        public static final double RED_OUTPOST_X  = RED_DEPOT_X;
        public static final double RED_OUTPOST_Y  = RED_DEPOT_Y;
        public static final double BLUE_OUTPOST_X = BLUE_DEPOT_X;
        public static final double BLUE_OUTPOST_Y = BLUE_DEPOT_Y;

        // ── RUNG HEIGHTS ──────────────────────────────────────────────────
        public static final double LOW_RUNG_HEIGHT  = 0.686; // Level 2 threshold
        public static final double MID_RUNG_HEIGHT  = 1.143; // Level 3 threshold
        public static final double HIGH_RUNG_HEIGHT = 1.600;
    }

    // =====================================================================
    // ROBOT  (from RobotPhysicalConstants / measured robot)
    // =====================================================================
    public static final class Robot {
        public static final double LENGTH              = 0.762; // 30 in
        public static final double WIDTH               = 0.762; // 30 in
        public static final double BUMPER_THICKNESS    = 0.076; // 3 in
        public static final double LENGTH_WITH_BUMPERS = LENGTH + 2 * BUMPER_THICKNESS;
        public static final double WIDTH_WITH_BUMPERS  = WIDTH  + 2 * BUMPER_THICKNESS;
        public static final double MAX_HEIGHT          = 1.22;  // 48 in
        public static final double TRENCH_CONFIG_HEIGHT = 0.508; // 20 in — must match TRENCH_CLEARANCE
    }

    // =====================================================================
    // SWERVE  (from SwerveConstants / DrivingConstants)
    // =====================================================================
    public static final class Swerve {
        /** Track width (left–right distance between wheel centres) */
        public static final double TRACK_WIDTH = 0.45085; // 17.75 in
        /** Wheel base (front–back distance between wheel centres) */
        public static final double WHEEL_BASE  = 0.75565; // 29.75 in

        /** Maximum drive speed (m/s) — SwerveConstants.MAX_SPEED */
        public static final double MAX_SPEED            = 4.2;
        /** Maximum angular velocity (rad/s) — SwerveConstants.MAX_ANGULAR_VELOCITY */
        public static final double MAX_ANGULAR_VELOCITY = 9.547;
        /** Maximum linear acceleration (m/s²) — DrivingConstants.DRIVE_MAX_ACC */
        public static final double MAX_ACCELERATION     = 3.0;
        /** Maximum angular acceleration (rad/s²) — DrivingConstants.TURN_MAX_ACC */
        public static final double MAX_ANGULAR_ACCELERATION = 12.0;
    }

    // =====================================================================
    // SHOOTER  (from ShooterConstants)
    // =====================================================================
    public static final class Shooter {
        public static final double MIN_ANGLE   = 8.0;   // degrees (PITCH_MIN_ANGLE ≈ 8.5)
        public static final double MAX_ANGLE   = 55.5;  // degrees (PITCH_MAX_ANGLE ≈ 55.5)
        /** Default angle — ShooterConstants.PITCH_STOW_ANGLE */
        public static final double MAX_VELOCITY  = 25.0;  // m/s exit speed at max flywheel
        public static final double MIN_VELOCITY  =  5.0;
        public static final double ANGLE_RATE    = 30.0;  // degrees/s
        /** Time to spin up flywheel to target speed (FLYWHEEL_RAMP_RATE = 0.75 s) */
        public static final double SPINUP_TIME   = 0.75;
    }

    // =====================================================================
    // CLIMBER  (from ClimberConstants)
    // =====================================================================
    public static final class Climber {
        public static final double MIN_POSITION    = 0.0;    // retracted (rotations)
        public static final double MAX_POSITION    = 50.0;   // fully extended (rotations)
        public static final double MAX_VELOCITY    = 20.0;   // rotations/s (sim approximation)
        public static final double MAX_ACCELERATION = 40.0;  // rotations/s²
    }

    // =====================================================================
    // SCORING  (REBUILT 2026 game manual)
    // =====================================================================
    public static final class Scoring {
        /** Points per FUEL scored into an ACTIVE hub */
        public static final int FUEL_ACTIVE_HUB  = 1;

        /** Tower climb — L1 during autonomous */
        public static final int TOWER_L1_AUTO    = 15;
        /** Tower climb — L1 during teleop */
        public static final int TOWER_L1_TELEOP  = 5;
        /** Tower climb — L2 during teleop */
        public static final int TOWER_L2_TELEOP  = 20;
        /** Tower climb — L3 during teleop */
        public static final int TOWER_L3_TELEOP  = 30;

        /** Fuel required for Energized ranking point */
        public static final int ENERGIZED_THRESHOLD   = 100;
        /** Tower points required for Supercharged ranking point */
        public static final int SUPERCHARGED_THRESHOLD = 50;
        /** Tower points required for Traversal ranking point */
        public static final int TRAVERSAL_THRESHOLD    = 50;
    }

    // =====================================================================
    // FUEL  (game piece physics)
    // =====================================================================
    public static final class Fuel {
        public static final double DIAMETER          = 0.178;   // 7 in
        public static final double RADIUS            = DIAMETER / 2.0;
        public static final double MASS              = 0.270;   // ~9.5 oz
        public static final int    TOTAL_PER_MATCH   = 80;
        public static final double DRAG_COEFFICIENT  = 0.47;
        public static final double RESTITUTION       = 0.6;
        public static final double AIR_DENSITY       = 1.225;
        public static final double GRAVITY           = 9.8067;
        /** Preloaded FUEL per robot at match start */
        public static final int    PRELOAD_PER_ROBOT  = 3;
        /** FUEL dispensed by the human player chute at start */
        public static final int    CHUTE_START_COUNT  = 5;
    }

    // =====================================================================
    // INTAKE
    // =====================================================================
    public static final class Intake {
        /** Physical width of intake mechanism (m) */
        public static final double WIDTH       = 0.60;
        /** Max FUEL the robot can carry */
        public static final int    MAX_CAPACITY = 5;
        /** Time to intake one FUEL cell (s) */
        public static final double INTAKE_TIME  = 0.3;
        /** Time to transfer FUEL from intake to shooter (s) */
        public static final double TRANSFER_TIME = 0.2;
    }

    // =====================================================================
    // MATCH TIMING  (REBUILT 2026 — 2:40 match)
    // =====================================================================
    public static final class Match {
        public static final double TOTAL_DURATION = 160.0; // 2:40
        public static final double AUTO_END        =  20.0; // 20 s autonomous
        public static final double TRANSITION_END  =  23.0; // 3 s transition
        public static final double SHIFT_1_END     =  55.0;
        public static final double SHIFT_2_END     =  87.0;
        public static final double SHIFT_3_END     = 119.0;
        public static final double SHIFT_4_END     = 140.0;
        public static final double END_GAME_START  = 130.0;
        public static final double SHIFT_DURATION  =  30.0;
    }

    // =====================================================================
    // SIMULATION
    // =====================================================================
    public static final class Simulation {
        /** Simulation tick rate (Hz) */
        public static final double TICK_RATE     = 50.0;
        /** Simulation time step (s) */
        public static final double DT            = 1.0 / TICK_RATE;
        /** Broadcast state to web clients every N ticks */
        public static final int    BROADCAST_RATE = 2;
    }

    // =====================================================================
    // COLLISION AVOIDANCE
    // =====================================================================
    public static final class CollisionAvoidance {
        public static final double LOOKAHEAD_TIME        = 1.0;   // s
        public static final double AVOIDANCE_BUFFER      = 0.3;   // m
        public static final double MAX_AVOIDANCE_STEERING = 1.0;
        public static final double AVOIDANCE_RAMP_START  = 2.0;   // m from obstacle
        public static final double AVOIDANCE_RAMP_END    = 0.8;   // m from obstacle
        public static final double MAX_SPEED_REDUCTION   = 0.5;
        public static final double MAX_AVOIDANCE_BLEND   = 0.8;
    }
}
