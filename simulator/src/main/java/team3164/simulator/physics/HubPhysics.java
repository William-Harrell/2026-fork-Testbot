package team3164.simulator.physics;

import team3164.simulator.Constants;
import team3164.simulator.engine.FuelState;
import team3164.simulator.engine.MatchState;
import team3164.simulator.engine.RobotState;

/**
 * Determines whether in-flight FUEL enters a hub and scores.
 */
public class HubPhysics {

    private static final double HUB_HALF_SIZE    = Constants.Field.HUB_SIZE / 2.0;
    private static final double HUB_HEIGHT       = Constants.Field.HUB_HEIGHT;
    private static final double FUEL_RADIUS      = Constants.Fuel.RADIUS;
    private static final double MIN_ENTRY_VELOCITY = 2.0; // m/s minimum to enter hub
    private static final double HUB_OPENING_RADIUS = HUB_HALF_SIZE + FUEL_RADIUS;

    /**
     * Check if in-flight FUEL has entered a hub.
     * @return 1 if scored in RED hub, -1 if scored in BLUE hub, 0 otherwise.
     */
    public static int checkScoring(FuelState.Fuel fuel, MatchState match, MatchState.Alliance shootingAlliance) {
        // Check RED hub
        if (isWithinHubBounds(fuel.x, fuel.y, Constants.Field.RED_HUB_X, Constants.Field.RED_HUB_Y)) {
            if (fuel.z <= HUB_HEIGHT && fuel.z >= 0) {
                match.scoreFuel(MatchState.Alliance.RED, 1);
                return 1;
            }
        }
        // Check BLUE hub
        if (isWithinHubBounds(fuel.x, fuel.y, Constants.Field.BLUE_HUB_X, Constants.Field.BLUE_HUB_Y)) {
            if (fuel.z <= HUB_HEIGHT && fuel.z >= 0) {
                match.scoreFuel(MatchState.Alliance.BLUE, 1);
                return -1;
            }
        }
        return 0;
    }

    private static boolean isWithinHubBounds(double x, double y, double hubX, double hubY) {
        return Math.abs(x - hubX) <= HUB_OPENING_RADIUS
            && Math.abs(y - hubY) <= HUB_OPENING_RADIUS;
    }

    public static boolean willHitHub(double x, double y, double vx, double vy, double vz,
                                     double dt, MatchState.Alliance alliance) {
        double hx = (alliance == MatchState.Alliance.RED) ? Constants.Field.RED_HUB_X : Constants.Field.BLUE_HUB_X;
        double hy = (alliance == MatchState.Alliance.RED) ? Constants.Field.RED_HUB_Y : Constants.Field.BLUE_HUB_Y;
        // Simulate a few steps
        double px = x, py = y, pz = 0.5;
        double pvx = vx, pvy = vy, pvz = vz;
        for (int i = 0; i < 60; i++) {
            px += pvx * dt; py += pvy * dt; pz += pvz * dt;
            pvz -= Constants.Fuel.GRAVITY * dt;
            if (pz < 0) break;
            if (isWithinHubBounds(px, py, hx, hy) && pz <= HUB_HEIGHT) return true;
        }
        return false;
    }

    public static double[] calculateOptimalShot(RobotState robot, MatchState.Alliance alliance) {
        double hx = (alliance == MatchState.Alliance.RED) ? Constants.Field.RED_HUB_X : Constants.Field.BLUE_HUB_X;
        double hy = (alliance == MatchState.Alliance.RED) ? Constants.Field.RED_HUB_Y : Constants.Field.BLUE_HUB_Y;
        double dx = hx - robot.x;
        double dy = hy - robot.y;
        double dist = Math.hypot(dx, dy);
        double hDiff = HUB_HEIGHT - 0.5;
        double angle = Math.toDegrees(Math.atan2(hDiff + 0.1 * dist, dist));
        angle = Math.max(Constants.Shooter.MIN_ANGLE, Math.min(Constants.Shooter.MAX_ANGLE, angle));
        double vel = Math.min(Constants.Shooter.MAX_VELOCITY, dist * 2.5 + hDiff * 2.0);
        return new double[]{ angle, vel };
    }

    public static double getDistanceToHub(RobotState robot, MatchState.Alliance alliance) {
        double hx = (alliance == MatchState.Alliance.RED) ? Constants.Field.RED_HUB_X : Constants.Field.BLUE_HUB_X;
        double hy = (alliance == MatchState.Alliance.RED) ? Constants.Field.RED_HUB_Y : Constants.Field.BLUE_HUB_Y;
        return Math.hypot(robot.x - hx, robot.y - hy);
    }

    public static double getAngleToHub(RobotState robot, MatchState.Alliance alliance) {
        double hx = (alliance == MatchState.Alliance.RED) ? Constants.Field.RED_HUB_X : Constants.Field.BLUE_HUB_X;
        double hy = (alliance == MatchState.Alliance.RED) ? Constants.Field.RED_HUB_Y : Constants.Field.BLUE_HUB_Y;
        return Math.atan2(hy - robot.y, hx - robot.x);
    }

    public static boolean isInShootingRange(RobotState robot, MatchState.Alliance alliance) {
        return getDistanceToHub(robot, alliance) < 6.0;
    }
}
