package team3164.simulator.physics;

import team3164.simulator.Constants;
import team3164.simulator.engine.InputState;
import team3164.simulator.engine.MatchState;
import team3164.simulator.engine.RobotState;

/**
 * Manages tower engagement and climb scoring.
 */
public class TowerPhysics {

    private static final double TOWER_ENGAGEMENT_RADIUS = Constants.Field.TOWER_ENGAGEMENT_RADIUS;
    private static final double RUNG_LOW   = Constants.Field.LOW_RUNG_HEIGHT;
    private static final double RUNG_MID   = Constants.Field.MID_RUNG_HEIGHT;
    private static final double RUNG_HIGH  = Constants.Field.HIGH_RUNG_HEIGHT;
    private static final double RUNG_TOLERANCE = 0.10; // metres — wider tolerance to account for sim dt

    public static void update(RobotState robot, InputState input, MatchState match, double dt) {
        if (!isNearTower(robot)) return;

        updateClimberMotion(robot, input, dt);
        if (!robot.climbComplete) checkRungEngagement(robot, match);
    }

    public static boolean isNearTower(RobotState robot) {
        return getDistanceToTower(robot) <= TOWER_ENGAGEMENT_RADIUS;
    }

    private static void updateClimberMotion(RobotState robot, InputState input, double dt) {
        ClimberPhysics.update(robot, input, dt);
    }

    private static void checkRungEngagement(RobotState robot, MatchState match) {
        double h = robot.robotHeight;
        int level = 0;
        if (Math.abs(h - RUNG_HIGH) < RUNG_TOLERANCE) level = 3;
        else if (Math.abs(h - RUNG_MID) < RUNG_TOLERANCE) level = 2;
        else if (Math.abs(h - RUNG_LOW) < RUNG_TOLERANCE) level = 1;

        if (level > 0 && robot.climbLevel > 0 && level >= robot.climbLevel && !robot.climbComplete) {
            completeClimb(robot, match);
        }
    }

    private static void completeClimb(RobotState robot, MatchState match) {
        robot.climbComplete   = true;
        robot.climberVelocity = 0.0;
        int robotIdx = robot.robotId % MultiRobotManager.ROBOTS_PER_ALLIANCE;
        int pts = match.scoreTowerClimb(robot.alliance, robotIdx, robot.climbLevel);
        robot.towerPoints += pts;
    }

    public static double getRungHeight(int level) {
        switch (level) {
            case 1: return RUNG_LOW;
            case 2: return RUNG_MID;
            case 3: return RUNG_HIGH;
            default: return 0.0;
        }
    }

    public static boolean isLevelValidForPhase(int level, MatchState match) {
        if (match.isAuto()) return level == 1; // only L1 in auto
        return level >= 1 && level <= 3;
    }

    public static int getPointsForLevel(int level, MatchState match) {
        boolean inAuto = match.isAuto();
        if (inAuto) return level >= 1 ? Constants.Scoring.TOWER_L1_AUTO : 0;
        switch (level) {
            case 1: return Constants.Scoring.TOWER_L1_TELEOP;
            case 2: return Constants.Scoring.TOWER_L2_TELEOP;
            case 3: return Constants.Scoring.TOWER_L3_TELEOP;
            default: return 0;
        }
    }

    public static double getDistanceToTower(RobotState robot) {
        boolean isRed = (robot.alliance == MatchState.Alliance.RED);
        double tx = isRed ? Constants.Field.RED_TOWER_X  : Constants.Field.BLUE_TOWER_X;
        double ty = isRed ? Constants.Field.RED_TOWER_Y  : Constants.Field.BLUE_TOWER_Y;
        return Math.hypot(robot.x - tx, robot.y - ty);
    }

    public static void resetClimb(RobotState robot) {
        robot.climbComplete   = false;
        robot.climbLevel      = 0;
        robot.climberPosition = 0;
        robot.climberVelocity = 0;
        robot.robotHeight     = 0;
    }
}

// Needed for robotIdx calculation
class MultiRobotManager {
    static final int ROBOTS_PER_ALLIANCE = 3;
}
