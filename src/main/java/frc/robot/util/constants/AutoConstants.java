package frc.robot.util.constants;

public final class AutoConstants {
    // ================================================================
    // DIP SWITCH CONFIGURATION
    // ================================================================
    // 5-bit selector allows up to 32 auto modes (we use 20)
    public static final int DIP_SWITCH_BIT_0_PORT = 1; // LSB (1)
    public static final int DIP_SWITCH_BIT_1_PORT = 2; // (2)
    public static final int DIP_SWITCH_BIT_2_PORT = 3; // (4)
    public static final int DIP_SWITCH_BIT_3_PORT = 4; // (8)
    public static final int DIP_SWITCH_BIT_4_PORT = 5; // MSB (16)

    public static final int NUM_AUTO_MODES = 20;

    // ================================================================
    // AUTO MODE IDENTIFIERS
    // ================================================================
    // Original Modes (0-9)
    public static final int AUTO_DO_NOTHING = 0; // Safety default
    public static final int AUTO_SCORE_AND_COLLECT = 1; // Score preload, collect from neutral, score
    public static final int AUTO_QUICK_CLIMB = 2; // Score preload, climb L1
    public static final int AUTO_SCORE_THEN_CLIMB = 3; // Score preload, climb L1 (optimized)
    public static final int AUTO_DEPOT_RAID = 4; // Collect from depot, score all
    public static final int AUTO_FAR_NEUTRAL = 5; // Score, drive to far neutral, collect, score
    public static final int AUTO_PRELOAD_ONLY = 6; // Just score preload, hold position
    public static final int AUTO_MAX_CYCLES = 7; // Pure scoring: shoot-collect-shoot loop
    public static final int AUTO_CLIMB_SUPPORT = 8; // Score preload, position for TELEOP climb
    public static final int AUTO_WIN_AUTO = 9; // Aggressive scoring to win AUTO period

    // Optimized Modes (10-14) - Discovered via Simulator Benchmarking
    public static final int AUTO_SCORE_COLLECT_CLIMB = 10; // Score, quick collect, score, climb
    public static final int AUTO_FAST_CLIMB = 11; // Drive to tower, climb immediately
    public static final int AUTO_BALANCED = 12; // Score preload + climb (18 pts)
    public static final int AUTO_DEPOT_CLIMB = 13; // * BEST: Depot collection then climb (20 pts)
    public static final int AUTO_MAX_POINTS = 14; // Dynamic: maximize total points

    // Additional Strategic Modes (15-19)
    public static final int AUTO_SAFE_CLIMB = 15; // Conservative climb with fallback
    public static final int AUTO_DUAL_CYCLE = 16; // Two full scoring cycles
    public static final int AUTO_DENY_FUEL = 17; // Collect FUEL to deny opponents
    public static final int AUTO_CENTER_CONTROL = 18; // Control neutral zone
    public static final int AUTO_ALLIANCE_SUPPORT = 19; // Support alliance scoring

    // ================================================================
    // AUTO MODE NAMES (for SmartDashboard display)
    // ================================================================
    public static final String[] AUTO_MODE_NAMES = {
            "0: Do Nothing",
            "1: Score & Collect (8 pts)",
            "2: Quick Climb (18 pts)",
            "3: Score Then Climb (18 pts)",
            "4: Depot Raid (5 pts)",
            "5: Far Neutral (3-4 pts)",
            "6: Preload Only (3 pts)",
            "7: Max Cycles (8 pts)",
            "8: Climb Support (3 pts)",
            "9: Win AUTO (4 pts)",
            "10: Score+Collect+Climb (18 pts)",
            "11: Fast Climb (15 pts)",
            "12: Balanced (18 pts)",
            "13: Depot+Climb * (20 pts)",
            "14: Max Points (18 pts)",
            "15: Safe Climb (15-18 pts)",
            "16: Dual Cycle (6-8 pts)",
            "17: Deny FUEL (strategic)",
            "18: Center Control (strategic)",
            "19: Alliance Support (strategic)"
    };

    // ================================================================
    // TIMING CONSTANTS (seconds)
    // ================================================================
    public static final double SHOOT_TIME_PER_FUEL = 0.75;
    public static final double DRIVE_TO_NEUTRAL_TIME = 8.0; // Increased for reliability
    public static final double INTAKE_TIMEOUT = 4.0; // Reduced for faster cycles
    public static final double CLIMB_TIMEOUT = 12.0;
    public static final double DRIVE_TO_TOWER_TIME = 5.0;
    public static final double DEPOT_COLLECTION_TIME = 3.0;

    // ================================================================
    // SPEED CONSTANTS (m/s)
    // ================================================================
    public static final double AUTO_DRIVE_SPEED = 2.0;
    public static final double AUTO_FAST_DRIVE_SPEED = 2.5;
    public static final double AUTO_INTAKE_DRIVE_SPEED = 1.5;
    public static final double AUTO_SLOW_DRIVE_SPEED = 1.0;
}