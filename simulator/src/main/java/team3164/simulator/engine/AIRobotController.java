package team3164.simulator.engine;

import team3164.simulator.Constants;

import java.util.Random;

/**
 * AI controller for non-player robots (alliance partners and opponents).
 * Uses the same AutonomousController for auto mode and simple rule-based teleop.
 */
public class AIRobotController {

    public enum AIPhase { IDLE, MOVING_TO_TARGET, SHOOTING, INTAKING, CLIMBING, DEFENDING }

    public enum AIBehavior { AGGRESSIVE, DEFENSIVE, CLIMBER, COLLECTOR }

    private static final Random random = new Random();

    private final int                   robotId;
    private final MatchState.Alliance   alliance;
    private final int                   teamNumber;

    private int            selectedAutoMode;
    private AIBehavior     teleopBehavior;
    private AutonomousController autoController;

    private double  targetX, targetY;
    private double  actionTimer;
    private boolean hasTarget;
    private AIPhase currentPhase = AIPhase.IDLE;

    public AIRobotController(int robotId, MatchState.Alliance alliance, int teamNumber) {
        this.robotId      = robotId;
        this.alliance     = alliance;
        this.teamNumber   = teamNumber;
        this.selectedAutoMode = AutonomousController.AUTO_PRELOAD_ONLY;
        this.teleopBehavior   = randomBehavior();
        this.autoController   = new AutonomousController();
        this.autoController.setSelectedMode(selectedAutoMode);
    }

    private AIBehavior randomBehavior() {
        AIBehavior[] values = AIBehavior.values();
        return values[random.nextInt(values.length)];
    }

    public int         getSelectedAutoMode() { return selectedAutoMode; }
    public void        setAutoMode(int mode) {
        selectedAutoMode = mode;
        autoController.setSelectedMode(mode);
    }
    public String      getAutoModeName()  { return autoController.getSelectedModeName(); }
    public AIBehavior  getTeleopBehavior(){ return teleopBehavior; }
    public int         getTeamNumber()    { return teamNumber; }

    public void startAuto(RobotState robot) {
        autoController.startAuto(robot);
    }

    public void reset() {
        autoController.reset();
        currentPhase = AIPhase.IDLE;
        hasTarget    = false;
        actionTimer  = 0.0;
    }

    /** Called every sim tick. Delegates to auto or teleop logic. */
    public void update(RobotState robot, InputState input, MatchState match, RobotState[] allRobots, double dt) {
        if (!robot.isEnabled) return;

        if (match.isAuto()) {
            updateAuto(robot, input, dt);
        } else {
            updateTeleop(robot, input, match, allRobots, dt);
        }
    }

    private void updateAuto(RobotState robot, InputState input, double dt) {
        autoController.update(robot, input, dt);
    }

    private void updateTeleop(RobotState robot, InputState input, MatchState match, RobotState[] allRobots, double dt) {
        switch (teleopBehavior) {
            case AGGRESSIVE: updateAggressive(robot, input, match, allRobots, dt); break;
            case DEFENSIVE:  updateDefensive(robot, input, match, allRobots, dt);  break;
            case CLIMBER:    updateClimber(robot, input, match, allRobots, dt);    break;
            case COLLECTOR:  updateCollector(robot, input, match, allRobots, dt);  break;
        }
    }

    private void updateAggressive(RobotState robot, InputState input, MatchState match, RobotState[] allRobots, double dt) {
        actionTimer += dt;

        if (match.isEndGame()) {
            // Switch to climbing in end game
            updateClimber(robot, input, match, allRobots, dt);
            return;
        }

        switch (currentPhase) {
            case IDLE:
                pickRandomNeutralTarget();
                currentPhase = AIPhase.MOVING_TO_TARGET;
                break;

            case MOVING_TO_TARGET:
                if (driveToTarget(robot, input, targetX, targetY, 0.8, allRobots)) {
                    currentPhase = AIPhase.INTAKING;
                    actionTimer  = 0;
                } else if (actionTimer > 5.0) {
                    pickRandomNeutralTarget();
                    actionTimer = 0;
                }
                input.intake = true;
                break;

            case INTAKING:
                input.intake = true;
                input.forward = 0.2;
                if (robot.fuelCount >= 3 || actionTimer > 3.0) {
                    double[] sp = getShootingPos(robot);
                    targetX = sp[0]; targetY = sp[1];
                    currentPhase = AIPhase.SHOOTING;
                    actionTimer  = 0;
                }
                break;

            case SHOOTING:
                if (driveToTarget(robot, input, targetX, targetY, 0.8, allRobots)) {
                    input.shoot = true; input.spinUp = true;
                    if (robot.fuelCount == 0 || actionTimer > 3.0) {
                        currentPhase = AIPhase.IDLE;
                        actionTimer  = 0;
                    }
                }
                break;

            default:
                currentPhase = AIPhase.IDLE;
        }
    }

    private void updateDefensive(RobotState robot, InputState input, MatchState match, RobotState[] allRobots, double dt) {
        actionTimer += dt;
        // Patrol neutral zone
        if (!hasTarget) pickRandomFieldTarget();
        if (driveToTarget(robot, input, targetX, targetY, 0.6, allRobots)) {
            hasTarget = false;
        }
        if (actionTimer > 3.0) { hasTarget = false; actionTimer = 0; }
    }

    private void updateClimber(RobotState robot, InputState input, MatchState match, RobotState[] allRobots, double dt) {
        actionTimer += dt;
        boolean isRed = alliance == MatchState.Alliance.RED;
        double towerX = isRed ? Constants.Field.RED_TOWER_X - 1.0 : Constants.Field.BLUE_TOWER_X + 1.0;
        double towerY = isRed ? Constants.Field.RED_TOWER_Y : Constants.Field.BLUE_TOWER_Y;

        switch (currentPhase) {
            case IDLE:
            case MOVING_TO_TARGET:
                if (!driveToTarget(robot, input, towerX, towerY, 0.7, allRobots)) {
                    currentPhase = AIPhase.MOVING_TO_TARGET;
                } else {
                    currentPhase = AIPhase.CLIMBING;
                    actionTimer  = 0;
                }
                break;
            case CLIMBING:
                input.level1 = true; input.climberUp = true;
                if (robot.climbComplete || actionTimer > 10.0) {
                    currentPhase = AIPhase.IDLE;
                }
                break;
            default:
                currentPhase = AIPhase.MOVING_TO_TARGET;
        }
    }

    private void updateCollector(RobotState robot, InputState input, MatchState match, RobotState[] allRobots, double dt) {
        actionTimer += dt;
        if (!hasTarget) pickRandomNeutralTarget();

        if (driveToTarget(robot, input, targetX, targetY, 0.7, allRobots)) {
            input.intake = true;
            if (robot.fuelCount >= 4 || actionTimer > 4.0) {
                double[] sp = getShootingPos(robot);
                targetX = sp[0]; targetY = sp[1];
                hasTarget = true;
                actionTimer = 0;
            }
        } else {
            input.intake = true;
        }
    }

    private void pickRandomNeutralTarget() {
        double cx = Constants.Field.CENTER_X;
        double cy = Constants.Field.CENTER_Y;
        boolean isRed = alliance == MatchState.Alliance.RED;
        targetX = cx + (isRed ? -1.0 - random.nextDouble() * 2.0 : 1.0 + random.nextDouble() * 2.0);
        targetY = cy + (random.nextDouble() - 0.5) * 3.0;
        hasTarget = true;
        actionTimer = 0;
    }

    private void pickRandomFieldTarget() {
        boolean isRed = alliance == MatchState.Alliance.RED;
        targetX = isRed ? Constants.Field.CENTER_X + random.nextDouble() * 2.0 :
                          Constants.Field.CENTER_X - random.nextDouble() * 2.0;
        targetY = Constants.Field.CENTER_Y + (random.nextDouble() - 0.5) * 4.0;
        hasTarget = true;
        actionTimer = 0;
    }

    private double[] getShootingPos(RobotState robot) {
        boolean isRed = alliance == MatchState.Alliance.RED;
        double x = isRed ? Constants.Field.LENGTH - 2.5 : 2.5;
        return new double[]{ x, Constants.Field.CENTER_Y };
    }

    /** Returns true when robot has reached the target. */
    private boolean driveToTarget(RobotState robot, InputState input,
                                  double tx, double ty, double speed,
                                  RobotState[] allRobots) {
        double dx   = tx - robot.x;
        double dy   = ty - robot.y;
        double dist = Math.hypot(dx, dy);
        if (dist < 0.4) {
            input.forward = 0; input.strafe = 0; input.turn = 0;
            return true;
        }
        double nx = dx / dist;
        double ny = dy / dist;
        double s  = Math.min(speed, dist / 0.8);
        input.forward = nx * s;
        input.strafe  = ny * s;
        double tAngle = Math.atan2(dy, dx);
        double hErr   = normalizeAngle(tAngle - robot.heading);
        input.turn    = Math.max(-0.8, Math.min(0.8, hErr * 2.0));
        return false;
    }

    private void clearInputs(InputState input) {
        input.forward = 0; input.strafe = 0; input.turn = 0;
        input.shoot   = false; input.intake = false; input.spinUp = false;
    }

    private double normalizeAngle(double a) {
        while (a >  Math.PI) a -= 2 * Math.PI;
        while (a < -Math.PI) a += 2 * Math.PI;
        return a;
    }

    private void manageTrenchMode(RobotState robot)  {}
    private void enableTrenchMode(RobotState robot)   {}
    private void disableTrenchMode(RobotState robot)  {}
}
