package team3164.simulator.physics;

import team3164.simulator.Constants;
import team3164.simulator.engine.RobotState;

/**
 * AABB collision detection against field obstacles (hubs, bumps, walls).
 */
public class CollisionPhysics {

    private static final double ROBOT_HALF_SIZE  = Constants.Robot.WIDTH_WITH_BUMPERS / 2.0;
    private static final double PUSH_OUT_MARGIN  = 0.01;
    private static final double VELOCITY_DAMPING = 0.5;

    public static class Obstacle {
        public final double x, y, halfWidth, halfHeight;
        public final String  name;
        public final boolean isDriveable;

        public Obstacle(double x, double y, double halfWidth, double halfHeight, String name, boolean isDriveable) {
            this.x = x; this.y = y;
            this.halfWidth = halfWidth; this.halfHeight = halfHeight;
            this.name = name;
            this.isDriveable = isDriveable;
        }
    }

    public static class CollisionResult {
        public boolean collided;
        public double  pushX, pushY;
        public String  obstacleName;
        public boolean onBump;

        public void apply(RobotState robot) {
            if (!collided) return;
            robot.x += pushX;
            robot.y += pushY;
            if (pushX != 0) robot.vx *= -VELOCITY_DAMPING;
            if (pushY != 0) robot.vy *= -VELOCITY_DAMPING;
        }
    }

    private static final Obstacle[] OBSTACLES = {
        // Red hub
        new Obstacle(Constants.Field.RED_HUB_X, Constants.Field.RED_HUB_Y,
                     Constants.Field.HUB_SIZE / 2, Constants.Field.HUB_SIZE / 2, "RED_HUB", false),
        // Blue hub
        new Obstacle(Constants.Field.BLUE_HUB_X, Constants.Field.BLUE_HUB_Y,
                     Constants.Field.HUB_SIZE / 2, Constants.Field.HUB_SIZE / 2, "BLUE_HUB", false),
    };

    private static final Obstacle[] BUMP_OBSTACLES = {
        new Obstacle(Constants.Field.RED_BUMP_1_X,  Constants.Field.RED_BUMP_1_Y,
                     Constants.Field.BUMP_WIDTH / 2, Constants.Field.BUMP_LENGTH / 2, "RED_BUMP_1", true),
        new Obstacle(Constants.Field.RED_BUMP_2_X,  Constants.Field.RED_BUMP_2_Y,
                     Constants.Field.BUMP_WIDTH / 2, Constants.Field.BUMP_LENGTH / 2, "RED_BUMP_2", true),
        new Obstacle(Constants.Field.BLUE_BUMP_1_X, Constants.Field.BLUE_BUMP_1_Y,
                     Constants.Field.BUMP_WIDTH / 2, Constants.Field.BUMP_LENGTH / 2, "BLUE_BUMP_1", true),
        new Obstacle(Constants.Field.BLUE_BUMP_2_X, Constants.Field.BLUE_BUMP_2_Y,
                     Constants.Field.BUMP_WIDTH / 2, Constants.Field.BUMP_LENGTH / 2, "BLUE_BUMP_2", true),
    };

    private static CollisionResult checkAABBCollision(double x, double y, Obstacle obs) {
        CollisionResult result = new CollisionResult();
        double minX = obs.x - obs.halfWidth  - ROBOT_HALF_SIZE;
        double maxX = obs.x + obs.halfWidth   + ROBOT_HALF_SIZE;
        double minY = obs.y - obs.halfHeight  - ROBOT_HALF_SIZE;
        double maxY = obs.y + obs.halfHeight   + ROBOT_HALF_SIZE;

        if (x > minX && x < maxX && y > minY && y < maxY) {
            result.collided      = true;
            result.obstacleName  = obs.name;
            result.onBump        = obs.isDriveable;

            // Push out along the axis of least penetration
            double overlapLeft  = x - minX;
            double overlapRight = maxX - x;
            double overlapDown  = y - minY;
            double overlapUp    = maxY - y;

            double minOverlap = Math.min(Math.min(overlapLeft, overlapRight),
                                         Math.min(overlapDown, overlapUp));

            if (minOverlap == overlapLeft)       { result.pushX = -(overlapLeft  + PUSH_OUT_MARGIN); }
            else if (minOverlap == overlapRight) { result.pushX =  (overlapRight + PUSH_OUT_MARGIN); }
            else if (minOverlap == overlapDown)  { result.pushY = -(overlapDown  + PUSH_OUT_MARGIN); }
            else                                  { result.pushY =  (overlapUp   + PUSH_OUT_MARGIN); }
        }
        return result;
    }

    public static CollisionResult checkAndResolveCollisions(RobotState robot) {
        CollisionResult merged = new CollisionResult();
        for (Obstacle obs : OBSTACLES) {
            CollisionResult r = checkAABBCollision(robot.x, robot.y, obs);
            if (r.collided && !obs.isDriveable) {
                robot.x += r.pushX;
                robot.y += r.pushY;
                if (r.pushX != 0) robot.vx *= -VELOCITY_DAMPING;
                if (r.pushY != 0) robot.vy *= -VELOCITY_DAMPING;
                merged.collided = true;
                merged.obstacleName = r.obstacleName;
            }
        }
        return merged;
    }

    public static boolean wouldCollide(double x, double y) {
        for (Obstacle obs : OBSTACLES) {
            if (!obs.isDriveable) {
                CollisionResult r = checkAABBCollision(x, y, obs);
                if (r.collided) return true;
            }
        }
        return false;
    }

    public static boolean isPathBlocked(double x1, double y1, double x2, double y2) {
        // Sample along the path
        double dx = x2 - x1, dy = y2 - y1;
        double dist = Math.hypot(dx, dy);
        int steps = Math.max(1, (int)(dist / 0.2));
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            if (wouldCollide(x1 + t * dx, y1 + t * dy)) return true;
        }
        return false;
    }

    public static Obstacle getNearestObstacle(double x, double y) {
        Obstacle nearest = null;
        double minDist = Double.MAX_VALUE;
        for (Obstacle obs : OBSTACLES) {
            double d = Math.hypot(x - obs.x, y - obs.y);
            if (d < minDist) { minDist = d; nearest = obs; }
        }
        return nearest;
    }

    public static double[][] calculateSafePath(double x1, double y1, double x2, double y2) {
        // Simple straight-line path (no complex pathfinding in simulator)
        return new double[][]{{ x1, y1 }, { x2, y2 }};
    }
}
