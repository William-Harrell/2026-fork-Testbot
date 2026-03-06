package team3164.simulator.physics;

import team3164.simulator.Constants;
import team3164.simulator.engine.FuelState;
import team3164.simulator.engine.MatchState;
import team3164.simulator.engine.RobotState;

import java.util.ArrayList;
import java.util.List;

/**
 * Updates in-flight and on-ground FUEL physics.
 */
public class FuelPhysics {

    private static final double GRAVITY          = Constants.Fuel.GRAVITY;
    private static final double DRAG_COEFF       = Constants.Fuel.DRAG_COEFFICIENT;
    private static final double AIR_DENSITY      = Constants.Fuel.AIR_DENSITY;
    private static final double RESTITUTION      = Constants.Fuel.RESTITUTION;
    private static final double FUEL_RADIUS      = Constants.Fuel.RADIUS;
    private static final double FUEL_MASS        = Constants.Fuel.MASS;
    private static final double CROSS_SECTION    = Math.PI * FUEL_RADIUS * FUEL_RADIUS;
    private static final double GROUND_FRICTION  = 0.6;
    private static final double ROLLING_FRICTION = 0.1;
    private static final double VELOCITY_THRESHOLD = 0.05;

    public static void update(FuelState fuelState, MatchState match, double dt) {
        List<FuelState.Fuel> toScore  = new ArrayList<>();
        List<FuelState.Fuel> toLand   = new ArrayList<>();

        for (FuelState.Fuel f : fuelState.getFlightFuel()) {
            boolean scored = updateFlightPhysics(f, dt);
            if (scored) toScore.add(f);
            else if (f.z <= FUEL_RADIUS) toLand.add(f);
        }

        for (FuelState.Fuel f : toScore) {
            fuelState.markScored(f);
        }
        for (FuelState.Fuel f : toLand) {
            fuelState.landOnField(f);
        }

        // Update ground physics
        for (FuelState.Fuel f : new ArrayList<>(fuelState.getFieldFuel())) {
            if (f.isMoving) updateGroundPhysics(f, dt);
        }
    }

    /** Returns true if FUEL entered a hub. */
    private static boolean updateFlightPhysics(FuelState.Fuel f, double dt) {
        // Aerodynamic drag
        double speed   = f.getSpeed();
        double dragMag = 0.5 * DRAG_COEFF * AIR_DENSITY * CROSS_SECTION * speed * speed;
        if (speed > 0.001) {
            double dragAccel = dragMag / FUEL_MASS;
            f.vx -= (f.vx / speed) * dragAccel * dt;
            f.vy -= (f.vy / speed) * dragAccel * dt;
        }
        f.vz -= GRAVITY * dt;

        f.x += f.vx * dt;
        f.y += f.vy * dt;
        f.z += f.vz * dt;

        // Hub collision check
        if (checkHubEntry(f)) return true;

        // Field boundaries
        checkFieldBoundaries(f);
        return false;
    }

    private static boolean checkHubEntry(FuelState.Fuel f) {
        double hubHalf = Constants.Field.HUB_SIZE / 2.0 + FUEL_RADIUS;
        if (f.z >= 0 && f.z <= Constants.Field.HUB_HEIGHT) {
            if (Math.abs(f.x - Constants.Field.RED_HUB_X) < hubHalf
             && Math.abs(f.y - Constants.Field.RED_HUB_Y) < hubHalf) return true;
            if (Math.abs(f.x - Constants.Field.BLUE_HUB_X) < hubHalf
             && Math.abs(f.y - Constants.Field.BLUE_HUB_Y) < hubHalf) return true;
        }
        return false;
    }

    private static void updateGroundPhysics(FuelState.Fuel f, double dt) {
        double speed = Math.hypot(f.vx, f.vy);
        if (speed < VELOCITY_THRESHOLD) {
            f.vx = 0; f.vy = 0; f.isMoving = false;
            return;
        }
        double friction = ROLLING_FRICTION * GRAVITY * dt;
        double newSpeed = Math.max(0, speed - friction);
        if (speed > 0) {
            f.vx = f.vx / speed * newSpeed;
            f.vy = f.vy / speed * newSpeed;
        }
        f.x += f.vx * dt;
        f.y += f.vy * dt;
        checkFieldBoundaries(f);
    }

    private static void checkFieldBoundaries(FuelState.Fuel f) {
        if (f.x < FUEL_RADIUS) { f.x = FUEL_RADIUS; f.vx = -f.vx * RESTITUTION; }
        if (f.x > Constants.Field.LENGTH - FUEL_RADIUS) { f.x = Constants.Field.LENGTH - FUEL_RADIUS; f.vx = -f.vx * RESTITUTION; }
        if (f.y < FUEL_RADIUS) { f.y = FUEL_RADIUS; f.vy = -f.vy * RESTITUTION; }
        if (f.y > Constants.Field.WIDTH - FUEL_RADIUS) { f.y = Constants.Field.WIDTH - FUEL_RADIUS; f.vy = -f.vy * RESTITUTION; }
    }

    public static double[] calculateLaunchVelocity(double fromX, double fromY, double fromZ,
                                                    double toX, double toY, double toZ) {
        double dx = toX - fromX;
        double dy = toY - fromY;
        double dz = toZ - fromZ;
        double dist = Math.hypot(dx, dy);
        double angle = Math.atan2(dz + 0.1 * dist, dist);
        double speed = Math.sqrt((dist * GRAVITY) / Math.sin(2 * angle));
        double hSpeed = speed * Math.cos(angle);
        double vSpeed = speed * Math.sin(angle);
        double dir = Math.atan2(dy, dx);
        return new double[]{ hSpeed * Math.cos(dir), hSpeed * Math.sin(dir), vSpeed };
    }

    public static double[] predictLanding(double x, double y, double z, double vx, double vy, double vz) {
        double px = x, py = y, pz = z;
        double pvz = vz;
        double dt = 0.02;
        for (int i = 0; i < 200; i++) {
            px += vx * dt; py += vy * dt; pz += pvz * dt;
            pvz -= GRAVITY * dt;
            if (pz <= FUEL_RADIUS) return new double[]{ px, py };
        }
        return new double[]{ px, py };
    }

    public static void applyKick(FuelState.Fuel f, double fx, double fy) {
        f.vx += fx / FUEL_MASS;
        f.vy += fy / FUEL_MASS;
        f.isMoving = true;
    }

    public static boolean checkRobotCollision(FuelState.Fuel f, RobotState robot) {
        double hw = Constants.Robot.WIDTH_WITH_BUMPERS  / 2.0;
        double hl = Constants.Robot.LENGTH_WITH_BUMPERS / 2.0;
        return Math.abs(f.x - robot.x) < hw + FUEL_RADIUS
            && Math.abs(f.y - robot.y) < hl + FUEL_RADIUS;
    }
}
