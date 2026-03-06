package team3164.simulator.physics;

import team3164.simulator.Constants;
import team3164.simulator.engine.RobotState;

/**
 * Handles the two bumps flanking each hub.
 */
public class BumpPhysics {

    private static final double BUMP_LENGTH = Constants.Field.BUMP_LENGTH;
    private static final double BUMP_WIDTH  = Constants.Field.BUMP_WIDTH;
    private static final double BUMP_HEIGHT = Constants.Field.BUMP_HEIGHT;
    private static final double BUMP_ANGLE  = Math.toRadians(Constants.Field.BUMP_RAMP_ANGLE);
    private static final double BUMP_SPEED_FACTOR = 0.7;
    private static final double RAMP_LENGTH = 0.3;

    private static final double[][] BUMP_POSITIONS = {
        { Constants.Field.RED_BUMP_1_X,  Constants.Field.RED_BUMP_1_Y  },
        { Constants.Field.RED_BUMP_2_X,  Constants.Field.RED_BUMP_2_Y  },
        { Constants.Field.BLUE_BUMP_1_X, Constants.Field.BLUE_BUMP_1_Y },
        { Constants.Field.BLUE_BUMP_2_X, Constants.Field.BLUE_BUMP_2_Y },
    };

    public static class BumpInfo {
        public boolean onBump;
        public boolean onRamp;
        public double  rampProgress; // 0..1
        public double  bumpX, bumpY;

        public double getHeightOffset() {
            if (!onBump && !onRamp) return 0.0;
            if (onRamp) return rampProgress * BUMP_HEIGHT;
            return BUMP_HEIGHT;
        }
    }

    public static void update(RobotState robot, double dt) {
        BumpInfo info = getBumpInfo(robot.x, robot.y);
        robot.onBump = info.onBump || info.onRamp;
        if (robot.onBump) {
            // Slow down on bump
            robot.vx *= BUMP_SPEED_FACTOR;
            robot.vy *= BUMP_SPEED_FACTOR;
            // Bump height overrides climber height (robot is on the ground feature)
            robot.robotHeight = info.getHeightOffset();
        }
        // Note: do NOT zero robotHeight when off bump — climber may have raised it
    }

    public static BumpInfo getBumpInfo(double x, double y) {
        BumpInfo info = new BumpInfo();
        for (double[] bp : BUMP_POSITIONS) {
            double dx = x - bp[0];
            double dy = y - bp[1];
            double hw = BUMP_WIDTH  / 2;
            double hl = BUMP_LENGTH / 2;
            if (Math.abs(dx) <= hw + RAMP_LENGTH && Math.abs(dy) <= hl) {
                if (Math.abs(dx) <= hw) {
                    info.onBump = true;
                } else {
                    info.onRamp = true;
                    info.rampProgress = 1.0 - (Math.abs(dx) - hw) / RAMP_LENGTH;
                }
                info.bumpX = bp[0];
                info.bumpY = bp[1];
                return info;
            }
        }
        return info;
    }

    public static boolean isBlockedByBump(double x, double y, double robotWidth) {
        for (double[] bp : BUMP_POSITIONS) {
            double hw = BUMP_WIDTH  / 2 + robotWidth / 2;
            double hl = BUMP_LENGTH / 2 + robotWidth / 2;
            if (Math.abs(x - bp[0]) < hw && Math.abs(y - bp[1]) < hl) return true;
        }
        return false;
    }

    public static double getSpeedMultiplier(double x, double y) {
        BumpInfo info = getBumpInfo(x, y);
        if (info.onBump) return BUMP_SPEED_FACTOR;
        if (info.onRamp) return 1.0 - (1.0 - BUMP_SPEED_FACTOR) * info.rampProgress;
        return 1.0;
    }

    public static double[] getNearestBump(double x, double y) {
        double minDist = Double.MAX_VALUE;
        double[] nearest = null;
        for (double[] bp : BUMP_POSITIONS) {
            double d = Math.hypot(x - bp[0], y - bp[1]);
            if (d < minDist) { minDist = d; nearest = bp; }
        }
        return nearest;
    }
}
