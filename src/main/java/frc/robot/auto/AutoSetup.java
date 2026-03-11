package frc.robot.auto;

import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.path.PathConstraints;
import edu.wpi.first.wpilibj.DataLogManager;
import frc.robot.util.constants.DrivingConstants;
import frc.robot.util.constants.RobotPhysicalConstants;

/**
 * Configures CSPPathing before any auto that uses generatePath() runs.
 *
 * <p>Call AutoSetup.configure() once from autonomousInit() (via RobotContainer or Robot),
 * BEFORE getAutonomousCommand() is called. This method is idempotent — safe to call multiple times.
 */
public final class AutoSetup {
  private AutoSetup() {}

  /** True once configure() has succeeded at least once this session. */
  private static boolean configured = false;

  /**
   * Configure CSPPathing with path constraints and robot config derived from DrivingConstants
   * and RobotPhysicalConstants. Safe to call every autonomousInit().
   */
  public static void configure() {
    PathConstraints constraints =
        new PathConstraints(
            DrivingConstants.DRIVE_MAX_VEL,
            DrivingConstants.DRIVE_MAX_ACC,
            DrivingConstants.TURN_MAX_VEL,
            DrivingConstants.TURN_MAX_ACC);

    RobotConfig robotConfig;
    try {
      // Load from PathPlanner GUI deploy file if present; otherwise derive from constants.
      robotConfig = RobotConfig.fromGUISettings();
    } catch (Exception e) {
      // Fall back to a manually constructed config using measured robot parameters.
      // TODO: tune massKg and MOI on the real robot (use PathPlanner GUI to generate).
      DataLogManager.log("[AutoSetup] RobotConfig.fromGUISettings() failed: " + e.getMessage()
          + " — using fallback constants.");
      robotConfig = new RobotConfig(
          /* massKg          */ 60.0,
          /* MOI             */ 6.0,
          /* moduleConfig    */ null,   // TODO: fill in from SwerveConstants when available
          /* trackwidthMeters*/ RobotPhysicalConstants.ROBOT_WIDTH_WITH_BUMPERS);
    }

    CSPPathing.configureConstraints(constraints, robotConfig);

    // ----------------------------------------------------------------
    // NAV NODES — add obstacle-avoiding waypoints here as needed.
    // Example:
    //   CSPPathing.configureNodes(
    //       new CSPPathing.NavNode("HubLeft",  new Pose2d(5.0, 3.0, new Rotation2d()), List.of("HubRight")),
    //       new CSPPathing.NavNode("HubRight", new Pose2d(5.0, 5.0, new Rotation2d()), List.of("HubLeft")));
    // ----------------------------------------------------------------

    configured = true;
    DataLogManager.log("[AutoSetup] CSPPathing configured. constraints=" + constraints);
  }

  /** Returns true if configure() has been called successfully at least once. */
  public static boolean isConfigured() {
    return configured;
  }
}