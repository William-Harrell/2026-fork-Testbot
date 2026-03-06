package team3164.simulator.physics;

import team3164.simulator.Constants;
import team3164.simulator.engine.FuelState;
import team3164.simulator.engine.MatchState;
import team3164.simulator.engine.RobotState;

/**
 * Handles FUEL pickup from the alliance depots.
 */
public class DepotPhysics {

    private static final double DEPOT_LENGTH  = 1.0;
    private static final double DEPOT_WIDTH   = 1.0;
    private static final double PICKUP_RANGE  = 1.2;

    public static class DepotInfo {
        public boolean           inDepot;
        public MatchState.Alliance alliance;
        public double            depotX, depotY;
    }

    public static boolean isNearDepot(RobotState robot) {
        return isNearRedDepot(robot) || isNearBlueDepot(robot);
    }

    public static boolean isNearRedDepot(RobotState robot) {
        return Math.hypot(robot.x - Constants.Field.RED_DEPOT_X, robot.y - Constants.Field.RED_DEPOT_Y) < PICKUP_RANGE;
    }

    public static boolean isNearBlueDepot(RobotState robot) {
        return Math.hypot(robot.x - Constants.Field.BLUE_DEPOT_X, robot.y - Constants.Field.BLUE_DEPOT_Y) < PICKUP_RANGE;
    }

    public static double[] getDepotPosition(MatchState.Alliance alliance) {
        if (alliance == MatchState.Alliance.RED)
            return new double[]{ Constants.Field.RED_DEPOT_X, Constants.Field.RED_DEPOT_Y };
        return new double[]{ Constants.Field.BLUE_DEPOT_X, Constants.Field.BLUE_DEPOT_Y };
    }

    public static DepotInfo getDepotInfo(double x, double y) {
        DepotInfo info = new DepotInfo();
        if (Math.hypot(x - Constants.Field.RED_DEPOT_X, y - Constants.Field.RED_DEPOT_Y) < PICKUP_RANGE) {
            info.inDepot = true; info.alliance = MatchState.Alliance.RED;
            info.depotX  = Constants.Field.RED_DEPOT_X; info.depotY = Constants.Field.RED_DEPOT_Y;
        } else if (Math.hypot(x - Constants.Field.BLUE_DEPOT_X, y - Constants.Field.BLUE_DEPOT_Y) < PICKUP_RANGE) {
            info.inDepot = true; info.alliance = MatchState.Alliance.BLUE;
            info.depotX  = Constants.Field.BLUE_DEPOT_X; info.depotY = Constants.Field.BLUE_DEPOT_Y;
        }
        return info;
    }

    public static boolean attemptPickup(RobotState robot, FuelState fuelState) {
        DepotInfo info = getDepotInfo(robot.x, robot.y);
        if (!info.inDepot) return false;
        // Only pick up from your own alliance's depot
        if (info.alliance != robot.alliance) return false;
        FuelState.Fuel f = fuelState.pickupFromDepot(robot.x, robot.y, robot.alliance, PICKUP_RANGE);
        if (f == null) return false;
        robot.addFuel();
        return true;
    }

    public static double getDistanceToNearestDepot(RobotState robot) {
        double red  = Math.hypot(robot.x - Constants.Field.RED_DEPOT_X, robot.y - Constants.Field.RED_DEPOT_Y);
        double blue = Math.hypot(robot.x - Constants.Field.BLUE_DEPOT_X, robot.y - Constants.Field.BLUE_DEPOT_Y);
        return Math.min(red, blue);
    }

    public static double getDistanceToAllianceDepot(RobotState robot) {
        if (robot.alliance == MatchState.Alliance.RED)
            return Math.hypot(robot.x - Constants.Field.RED_DEPOT_X, robot.y - Constants.Field.RED_DEPOT_Y);
        return Math.hypot(robot.x - Constants.Field.BLUE_DEPOT_X, robot.y - Constants.Field.BLUE_DEPOT_Y);
    }

    public static int countFuelNearDepot(FuelState fuelState, MatchState.Alliance alliance) {
        double dx = alliance == MatchState.Alliance.RED ? Constants.Field.RED_DEPOT_X : Constants.Field.BLUE_DEPOT_X;
        double dy = alliance == MatchState.Alliance.RED ? Constants.Field.RED_DEPOT_Y : Constants.Field.BLUE_DEPOT_Y;
        int count = 0;
        for (FuelState.Fuel f : fuelState.getFieldFuel()) {
            if (Math.hypot(f.x - dx, f.y - dy) < PICKUP_RANGE * 2) count++;
        }
        return count;
    }
}
