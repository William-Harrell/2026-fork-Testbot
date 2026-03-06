package team3164.simulator.physics;

import team3164.simulator.Constants;
import team3164.simulator.engine.RobotState;

/**
 * Predictive robot-robot collision avoidance.
 */
public class CollisionPrediction {

    private static final double COLLISION_RADIUS = Constants.Robot.WIDTH_WITH_BUMPERS;

    public static class PredictionResult {
        public boolean willCollide;
        public double  timeToCollision;
        public double  closestApproachDist;
        public double  collisionX, collisionY;
    }

    public static class AvoidanceVector {
        public double  steerX, steerY;
        public double  urgency;
        public boolean needsAvoidance;
    }

    public static PredictionResult predictCollision(RobotState a, RobotState b, double lookaheadTime) {
        PredictionResult result = new PredictionResult();
        // Relative position and velocity
        double rx = b.x - a.x;
        double ry = b.y - a.y;
        double rvx = b.vx - a.vx;
        double rvy = b.vy - a.vy;

        double rrDot = rx * rx + ry * ry;
        double rvDot = rx * rvx + ry * rvy;
        double vvDot = rvx * rvx + rvy * rvy;

        if (vvDot < 1e-6) {
            result.closestApproachDist = Math.sqrt(rrDot);
            return result;
        }

        double t = -rvDot / vvDot;
        t = Math.max(0, Math.min(t, lookaheadTime));

        double closeX = rx + rvx * t;
        double closeY = ry + rvy * t;
        double closeDist = Math.hypot(closeX, closeY);

        result.closestApproachDist = closeDist;
        result.timeToCollision     = t;

        if (closeDist < COLLISION_RADIUS) {
            result.willCollide = true;
            result.collisionX  = a.x + a.vx * t;
            result.collisionY  = a.y + a.vy * t;
        }
        return result;
    }

    public static AvoidanceVector calculateAvoidance(RobotState robot, RobotState other) {
        AvoidanceVector av = new AvoidanceVector();
        PredictionResult pred = predictCollision(robot, other,
                Constants.CollisionAvoidance.LOOKAHEAD_TIME);

        if (!pred.willCollide && pred.closestApproachDist > COLLISION_RADIUS * 1.5) return av;

        double dx = robot.x - other.x;
        double dy = robot.y - other.y;
        double dist = Math.hypot(dx, dy);
        if (dist < 0.001) { av.steerX = 1; av.steerY = 0; av.urgency = 1; av.needsAvoidance = true; return av; }

        double urgency = Math.max(0, 1.0 - dist / (COLLISION_RADIUS * 2));
        av.steerX      = dx / dist;
        av.steerY      = dy / dist;
        av.urgency     = urgency;
        av.needsAvoidance = urgency > 0.1;
        return av;
    }

    public static AvoidanceVector calculateCombinedAvoidance(RobotState robot, RobotState[] others) {
        AvoidanceVector combined = new AvoidanceVector();
        double sumX = 0, sumY = 0, maxUrgency = 0;
        int count = 0;

        for (RobotState other : others) {
            if (other == robot || other.robotId == robot.robotId) continue;
            AvoidanceVector av = calculateAvoidance(robot, other);
            if (av.needsAvoidance) {
                sumX += av.steerX * av.urgency;
                sumY += av.steerY * av.urgency;
                maxUrgency = Math.max(maxUrgency, av.urgency);
                count++;
            }
        }

        if (count > 0) {
            combined.steerX = sumX / count;
            combined.steerY = sumY / count;
            combined.urgency = maxUrgency;
            combined.needsAvoidance = true;
        }
        return combined;
    }
}
