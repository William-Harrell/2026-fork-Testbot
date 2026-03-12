package frc.robot.auto;

import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.path.PathConstraints;
import edu.wpi.first.wpilibj.DataLogManager;
import frc.robot.util.constants.DrivingConstants;

/**
 * Configures CSPPathing before any auto that uses generatePath() runs.
 *
 * <p>Call AutoSetup.configure() once from autonomousInit() (via RobotContainer or Robot), BEFORE
 * getAutonomousCommand() is called. This method is idempotent — safe to call multiple times.
 */
public final class AutoSetup {
  private AutoSetup() {}

  /** True once configure() has succeeded at least once this session. */
  private static boolean configured = false;

  /**
   * Configure CSPPathing with path constraints and robot config.
   *
   * <p>FIX: Previously, if RobotConfig.fromGUISettings() failed, {@code robotConfig} was set to
   * {@code null} and passed to {@code CSPPathing.configureConstraints()}, which calls
   * {@code Objects.requireNonNull(config)}. This produced a confusing NullPointerException at
   * trajectory-generation time during auto, far from the real cause.
   *
   * <p>The fix throws immediately on config load failure with an actionable message, surfacing the
   * error at robot init time rather than silently poisoning the auto scheduler.
   *
   * @throws RuntimeException if the PathPlanner robotconfig.json deploy file is missing or
   *     unreadable. Resolution: open PathPlanner GUI, configure the robot, and re-deploy.
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
      robotConfig = RobotConfig.fromGUISettings();
    } catch (Exception e) {
      // Log before throwing so the message appears in the DataLog even if the
      // exception is caught higher up and swallowed.
      String msg =
          "[AutoSetup] robotconfig.json is missing or invalid — open PathPlanner GUI, "
              + "configure the robot, and re-deploy before running auto. Cause: "
              + e.getMessage();
      DataLogManager.log(msg);
      throw new RuntimeException(msg, e);
    }

    CSPPathing.configureConstraints(constraints, robotConfig);

    // ----------------------------------------------------------------
    // NAV NODES — add obstacle-avoiding waypoints here as needed.
    // Example:
    // CSPPathing.configureNodes(
    //   new CSPPathing.NavNode("HubLeft",  new Pose2d(5.0, 3.0, new Rotation2d()), List.of("HubRight")),
    //   new CSPPathing.NavNode("HubRight", new Pose2d(5.0, 5.0, new Rotation2d()), List.of("HubLeft")));
    // ----------------------------------------------------------------

    configured = true;
    DataLogManager.log("[AutoSetup] CSPPathing configured. constraints=" + constraints);
  }

  /** Returns true if configure() has been called successfully at least once. */
  public static boolean isConfigured() {
    return configured;
  }
}