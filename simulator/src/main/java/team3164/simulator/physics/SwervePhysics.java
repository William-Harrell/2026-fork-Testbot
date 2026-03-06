package team3164.simulator.physics;

import team3164.simulator.Constants;
import team3164.simulator.engine.InputState;
import team3164.simulator.engine.RobotState;

/**
 * Simulates swerve drive kinematics and dynamics.
 * Uses DrivingConstants values: MAX_SPEED=4.2 m/s, MAX_ANGULAR_VELOCITY=9.547 rad/s,
 * MAX_ACCELERATION=3.0 m/s², MAX_ANGULAR_ACCELERATION=12.0 rad/s².
 */
public class SwervePhysics {

    // Module positions relative to robot centre [x, y] in metres
    private static final double[][] MODULE_POSITIONS = {
        { Constants.Swerve.WHEEL_BASE / 2,  Constants.Swerve.TRACK_WIDTH / 2 },  // FL
        { Constants.Swerve.WHEEL_BASE / 2, -Constants.Swerve.TRACK_WIDTH / 2 },  // FR
        {-Constants.Swerve.WHEEL_BASE / 2,  Constants.Swerve.TRACK_WIDTH / 2 },  // RL
        {-Constants.Swerve.WHEEL_BASE / 2, -Constants.Swerve.TRACK_WIDTH / 2 },  // RR
    };

    public static void update(RobotState robot, InputState input, double dt) {
        if (!robot.isEnabled) return;

        // ── Speed multiplier ────────────────────────────────────────────
        double speedMult = robot.slowMode ? Constants.Swerve.MAX_SPEED * 0.3
                                          : Constants.Swerve.MAX_SPEED;
        double omegaMult = robot.slowMode ? Constants.Swerve.MAX_ANGULAR_VELOCITY * 0.3
                                          : Constants.Swerve.MAX_ANGULAR_VELOCITY;

        // ── Commanded velocities ────────────────────────────────────────
        double cmdVx, cmdVy;
        if (robot.fieldRelative) {
            // Rotate controller commands from field frame to robot frame
            double cos = Math.cos(-robot.heading);
            double sin = Math.sin(-robot.heading);
            cmdVx = (input.forward * cos - input.strafe * sin) * speedMult;
            cmdVy = (input.forward * sin + input.strafe * cos) * speedMult;
        } else {
            cmdVx = input.forward * speedMult;
            cmdVy = input.strafe  * speedMult;
        }
        double cmdOmega = input.turn * omegaMult;

        // ── Apply acceleration limits ───────────────────────────────────
        robot.vx    = applyAccelLimit(robot.vx,    cmdVx,    Constants.Swerve.MAX_ACCELERATION,        dt);
        robot.vy    = applyAccelLimit(robot.vy,    cmdVy,    Constants.Swerve.MAX_ACCELERATION,        dt);
        robot.omega = applyAccelLimit(robot.omega, cmdOmega, Constants.Swerve.MAX_ANGULAR_ACCELERATION, dt);

        // ── Update pose ─────────────────────────────────────────────────
        updatePose(robot, dt);

        // ── Module states (for visualisation) ──────────────────────────
        calculateModuleStates(robot);

        // ── Gyro reset ─────────────────────────────────────────────────
        if (input.resetGyro) resetGyro(robot, 0.0);

        // ── Toggles ────────────────────────────────────────────────────
        if (input.toggleFieldRel) robot.fieldRelative = !robot.fieldRelative;
        if (input.toggleSpeed)    robot.slowMode      = !robot.slowMode;
    }

    private static double applyAccelLimit(double current, double target, double maxAccel, double dt) {
        double delta = target - current;
        double maxDelta = maxAccel * dt;
        if (Math.abs(delta) <= maxDelta) return target;
        return current + Math.signum(delta) * maxDelta;
    }

    private static void calculateModuleStates(RobotState robot) {
        for (int i = 0; i < 4; i++) {
            double mx = robot.vx - robot.omega * MODULE_POSITIONS[i][1];
            double my = robot.vy + robot.omega * MODULE_POSITIONS[i][0];
            robot.moduleAngles[i] = Math.atan2(my, mx);
            robot.moduleSpeeds[i] = Math.hypot(mx, my);
        }
    }

    private static void updatePose(RobotState robot, double dt) {
        robot.x       += robot.vx    * dt;
        robot.y       += robot.vy    * dt;
        robot.heading += robot.omega * dt;
        robot.heading  = normalizeAngle(robot.heading);
        applyFieldBoundaries(robot);
    }

    private static void applyFieldBoundaries(RobotState robot) {
        double hw = Constants.Robot.WIDTH_WITH_BUMPERS  / 2.0;
        double hl = Constants.Robot.LENGTH_WITH_BUMPERS / 2.0;
        double minX = hw, maxX = Constants.Field.LENGTH - hw;
        double minY = hl, maxY = Constants.Field.WIDTH  - hl;

        if (robot.x < minX) { robot.x = minX; robot.vx = Math.max(0, robot.vx); }
        if (robot.x > maxX) { robot.x = maxX; robot.vx = Math.min(0, robot.vx); }
        if (robot.y < minY) { robot.y = minY; robot.vy = Math.max(0, robot.vy); }
        if (robot.y > maxY) { robot.y = maxY; robot.vy = Math.min(0, robot.vy); }
    }

    private static double normalizeAngle(double angle) {
        while (angle >  Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    public static void resetGyro(RobotState robot, double angle) {
        robot.heading = angle;
        robot.omega   = 0;
    }
}
