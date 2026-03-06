package frc.robot.auto;

public final class AutoConstants {
  // ================================================================
  // DIP SWITCH CONFIGURATION
  // ================================================================
  // 5-bit selector allows up to 32 auto modes (we use 4)
  public static final int DIP_SWITCH_BIT_0_PORT = 1; // LSB (1)
  public static final int DIP_SWITCH_BIT_1_PORT = 2; // (2)
  public static final int DIP_SWITCH_BIT_2_PORT = 3; // (4)
  public static final int DIP_SWITCH_BIT_3_PORT = 4; // (8)
  public static final int DIP_SWITCH_BIT_4_PORT = 5; // MSB (16)

  public static final int NUM_AUTO_MODES = 4;

  // ================================================================
  // AUTO MODE IDENTIFIERS
  // ================================================================
  public static final int AUTO_DO_NOTHING = 0;
  public static final int AUTO_SCORE_COLLECT_CLIMB = 1;
  public static final int AUTO_QUICK_CLIMB = 2;
  public static final int AUTO_PRELOAD_ONLY = 3;

  // ================================================================
  // AUTO MODE NAMES (for SmartDashboard display)
  // ================================================================
  public static final String[] AUTO_MODE_NAMES = {
    "0: Do Nothing",
    "1: Score, Collect & Climb",
    "2: Quick Climb",
    "3: Preload Only",
  };

  // ================================================================
  // TIMING CONSTANTS (seconds)
  // ================================================================
  public static final double INTAKE_TIMEOUT = 4.0;
  public static final double CLIMB_TIMEOUT = 12.0;
  public static final double DEPOT_COLLECTION_TIME = 3.0;

  // ================================================================
  // SPEED CONSTANTS (m/s)
  // ================================================================
  public static final double AUTO_DRIVE_SPEED = 2.0;
  public static final double AUTO_FAST_DRIVE_SPEED = 2.5;
  public static final double AUTO_INTAKE_DRIVE_SPEED = 1.5;
  public static final double AUTO_SLOW_DRIVE_SPEED = 1.0;
}
