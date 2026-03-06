package team3164.simulator.physics;

import team3164.simulator.Constants;
import team3164.simulator.engine.FuelState;
import team3164.simulator.engine.InputState;
import team3164.simulator.engine.RobotState;

import java.util.List;

/**
 * Handles FUEL pickup from the field when the intake is deployed.
 */
public class IntakePhysics {

    private static final double INTAKE_REACH = 0.45;  // metres forward from robot centre
    private static final double INTAKE_WIDTH = Constants.Intake.WIDTH;

    public static boolean update(RobotState robot, InputState input, FuelState fuelState, double dt) {
        if (!input.intake) return false;
        if (!robot.canIntakeFuel()) return false;

        double[] intakePos = getIntakePosition(robot);
        FuelState.Fuel f   = findFuelInIntakeZone(intakePos[0], intakePos[1], robot.heading, fuelState);
        if (f == null) f = findNearestFuelInRange(robot.x, robot.y, INTAKE_REACH + INTAKE_WIDTH / 2, fuelState);

        if (f != null) {
            return pickupFuel(robot, f, fuelState);
        }
        return false;
    }

    private static FuelState.Fuel findNearestFuelInRange(double x, double y, double radius, FuelState fuelState) {
        FuelState.Fuel nearest = null;
        double minDist = radius;
        for (FuelState.Fuel f : fuelState.getFieldFuel()) {
            if (f.location != FuelState.FuelLocation.ON_FIELD) continue;
            double d = Math.hypot(f.x - x, f.y - y);
            if (d < minDist) { minDist = d; nearest = f; }
        }
        return nearest;
    }

    private static FuelState.Fuel findFuelInIntakeZone(double ix, double iy, double heading, FuelState fuelState) {
        FuelState.Fuel nearest = null;
        double minDist = INTAKE_REACH + 0.3;
        double cos = Math.cos(heading);
        double sin = Math.sin(heading);
        for (FuelState.Fuel f : fuelState.getFieldFuel()) {
            if (f.location != FuelState.FuelLocation.ON_FIELD) continue;
            double dx = f.x - ix;
            double dy = f.y - iy;
            // Local frame
            double fwdDist  = dx * cos + dy * sin;
            double sideDist = -dx * sin + dy * cos;
            if (fwdDist >= -0.1 && fwdDist <= INTAKE_REACH
             && Math.abs(sideDist) <= INTAKE_WIDTH / 2 + Constants.Fuel.RADIUS) {
                double d = Math.hypot(dx, dy);
                if (d < minDist) { minDist = d; nearest = f; }
            }
        }
        return nearest;
    }

    private static boolean pickupFuel(RobotState robot, FuelState.Fuel f, FuelState fuelState) {
        fuelState.getFieldFuel().remove(f);
        f.location = FuelState.FuelLocation.IN_ROBOT;
        f.owningRobotIndex = robot.robotId;
        robot.addFuel();
        if (robot.intakeState == RobotState.IntakeState.IDLE) {
            robot.intakeState = RobotState.IntakeState.READY_TO_SHOOT;
        }
        return true;
    }

    public static double[] getIntakePosition(RobotState robot) {
        return new double[]{
            robot.x + INTAKE_REACH * Math.cos(robot.heading),
            robot.y + INTAKE_REACH * Math.sin(robot.heading)
        };
    }

    public static boolean isFuelAvailable(RobotState robot, FuelState fuelState) {
        return !fuelState.getFieldFuel().isEmpty()
            || !fuelState.getRedDepotFuel().isEmpty()
            || !fuelState.getBlueDepotFuel().isEmpty();
    }

    public static int countNearbyFuel(RobotState robot, FuelState fuelState, double radius) {
        int count = 0;
        for (FuelState.Fuel f : fuelState.getFieldFuel()) {
            if (Math.hypot(f.x - robot.x, f.y - robot.y) < radius) count++;
        }
        return count;
    }

    public static double getDistanceToNearestFuel(RobotState robot, FuelState fuelState) {
        double min = Double.MAX_VALUE;
        for (FuelState.Fuel f : fuelState.getFieldFuel()) {
            double d = Math.hypot(f.x - robot.x, f.y - robot.y);
            if (d < min) min = d;
        }
        return min == Double.MAX_VALUE ? -1.0 : min;
    }

    public static double[] getNearestFuelPosition(RobotState robot, FuelState fuelState) {
        double min = Double.MAX_VALUE;
        double[] pos = null;
        for (FuelState.Fuel f : fuelState.getFieldFuel()) {
            double d = Math.hypot(f.x - robot.x, f.y - robot.y);
            if (d < min) { min = d; pos = new double[]{ f.x, f.y }; }
        }
        return pos;
    }
}
