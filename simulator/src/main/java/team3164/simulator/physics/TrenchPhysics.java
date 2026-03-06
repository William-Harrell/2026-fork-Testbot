package team3164.simulator.physics;

import team3164.simulator.Constants;
import team3164.simulator.engine.RobotState;

/**
 * Trench physics — robots must be in low-profile mode to pass under trenches.
 */
public class TrenchPhysics {

    private static final double TRENCH_LENGTH    = Constants.Field.TRENCH_LENGTH;
    private static final double TRENCH_WIDTH     = Constants.Field.TRENCH_WIDTH;
    private static final double TRENCH_CLEARANCE = Constants.Field.TRENCH_CLEARANCE;

    private static final double[][] TRENCH_POSITIONS = {
        { Constants.Field.RED_TRENCH_1_X,  Constants.Field.RED_TRENCH_1_Y  },
        { Constants.Field.RED_TRENCH_2_X,  Constants.Field.RED_TRENCH_2_Y  },
        { Constants.Field.BLUE_TRENCH_1_X, Constants.Field.BLUE_TRENCH_1_Y },
        { Constants.Field.BLUE_TRENCH_2_X, Constants.Field.BLUE_TRENCH_2_Y },
    };

    public static class TrenchInfo {
        public boolean inTrench;
        public double  trenchX, trenchY;
    }

    public static boolean canPassTrench(RobotState robot) {
        return robot.trenchMode && robot.robotHeight <= TRENCH_CLEARANCE;
    }

    public static TrenchInfo getTrenchInfo(double x, double y) {
        TrenchInfo info = new TrenchInfo();
        for (double[] tp : TRENCH_POSITIONS) {
            if (Math.abs(x - tp[0]) <= TRENCH_LENGTH / 2
             && Math.abs(y - tp[1]) <= TRENCH_WIDTH  / 2) {
                info.inTrench = true;
                info.trenchX  = tp[0];
                info.trenchY  = tp[1];
                return info;
            }
        }
        return info;
    }

    public static boolean update(RobotState robot, double dt) {
        TrenchInfo info = getTrenchInfo(robot.x, robot.y);
        if (!info.inTrench) return true;
        if (canPassTrench(robot)) return true;
        // Blocked — push robot back
        double[] pushOut = getPushOutVector(robot.x, robot.y);
        robot.x += pushOut[0] * 0.02;
        robot.y += pushOut[1] * 0.02;
        robot.vx = 0; robot.vy = 0;
        return false;
    }

    public static boolean isMovementBlocked(double x, double y, double heading, double vx, double vy) {
        TrenchInfo info = getTrenchInfo(x, y);
        return info.inTrench;
    }

    public static double[] getPushOutVector(double x, double y) {
        double[] nearest = getNearestTrench(x, y);
        if (nearest == null) return new double[]{ 0, 0 };
        double dx = x - nearest[0];
        double dy = y - nearest[1];
        double d  = Math.hypot(dx, dy);
        if (d < 0.001) return new double[]{ 1, 0 };
        return new double[]{ dx / d, dy / d };
    }

    public static void toggleTrenchMode(RobotState robot) {
        robot.trenchMode = !robot.trenchMode;
    }

    public static double[] getNearestTrench(double x, double y) {
        double minDist = Double.MAX_VALUE;
        double[] nearest = null;
        for (double[] tp : TRENCH_POSITIONS) {
            double d = Math.hypot(x - tp[0], y - tp[1]);
            if (d < minDist) { minDist = d; nearest = tp; }
        }
        return nearest;
    }

    public static boolean isApproachingTrench(double x, double y, double vx, double vy) {
        double[] nearest = getNearestTrench(x, y);
        if (nearest == null) return false;
        double dx = nearest[0] - x;
        double dy = nearest[1] - y;
        double dist = Math.hypot(dx, dy);
        if (dist > 2.0) return false;
        // Approaching if velocity is toward trench
        return (dx * vx + dy * vy) > 0;
    }
}
