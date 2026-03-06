package team3164.simulator.engine;

import team3164.simulator.Constants;

import java.util.Random;

/**
 * Manages 6 robots (3 per alliance). One is the player robot; the rest are AI.
 */
public class MultiRobotManager {

    public static final int ROBOTS_PER_ALLIANCE = 3;
    public static final int TOTAL_ROBOTS        = ROBOTS_PER_ALLIANCE * 2;
    private static final double ROBOT_COLLISION_RADIUS = 0.5;

    private final RobotState[]        robots       = new RobotState[TOTAL_ROBOTS];
    private final InputState[]        inputs       = new InputState[TOTAL_ROBOTS];
    private final AIRobotController[] aiControllers= new AIRobotController[TOTAL_ROBOTS];

    private int playerRobotIndex = 0;

    private static final int[] BLUE_TEAM_NUMBERS = { 3164, 254, 1678 };
    private static final int[] RED_TEAM_NUMBERS  = { 118, 971, 2910 };

    private final Random random = new Random();

    public MultiRobotManager() {
        for (int i = 0; i < TOTAL_ROBOTS; i++) {
            robots[i] = new RobotState();
            inputs[i] = new InputState();
        }
        initializeRobots();
    }

    private void initializeRobots() {
        // Blue alliance: indices 0-2
        for (int i = 0; i < ROBOTS_PER_ALLIANCE; i++) {
            int team = BLUE_TEAM_NUMBERS[i];
            boolean isPlayer = (i == 0);
            robots[i].initialize(i, team, MatchState.Alliance.BLUE, i, isPlayer);
            aiControllers[i] = new AIRobotController(i, MatchState.Alliance.BLUE, team);
        }
        // Red alliance: indices 3-5
        for (int i = 0; i < ROBOTS_PER_ALLIANCE; i++) {
            int idx  = i + ROBOTS_PER_ALLIANCE;
            int team = RED_TEAM_NUMBERS[i];
            robots[idx].initialize(idx, team, MatchState.Alliance.RED, i, false);
            aiControllers[idx] = new AIRobotController(idx, MatchState.Alliance.RED, team);
        }
        playerRobotIndex = 0;
    }

    public void reset() {
        for (int i = 0; i < TOTAL_ROBOTS; i++) {
            robots[i].reset();
            inputs[i].reset();
        }
        initializeRobots();
    }

    public void startAuto() {
        for (int i = 0; i < TOTAL_ROBOTS; i++) {
            if (aiControllers[i] != null) {
                aiControllers[i].startAuto(robots[i]);
            }
        }
    }

    /** Called each tick to update all AI robots.
     *  Player input overrides AI if it has drive/shoot input; otherwise AI controls the player robot too.
     */
    public void update(InputState playerInput, MatchState match, double dt) {
        for (int i = 0; i < TOTAL_ROBOTS; i++) {
            inputs[i].reset();
        }

        // Always run AI for all robots (including player in headless/benchmark)
        for (int i = 0; i < TOTAL_ROBOTS; i++) {
            if (aiControllers[i] != null) {
                aiControllers[i].update(robots[i], inputs[i], match, robots, dt);
            }
        }

        // Override player robot input only if the player is actively driving
        if (playerInput.hasDriveInput() || playerInput.hasShooterInput()
                || playerInput.startMatch || playerInput.resetRobot) {
            copyInput(playerInput, inputs[playerRobotIndex]);
        }
    }

    /** Resolve inter-robot collisions using a simple push-out. */
    public void resolveRobotCollisions() {
        for (int i = 0; i < TOTAL_ROBOTS; i++) {
            for (int j = i + 1; j < TOTAL_ROBOTS; j++) {
                resolveCollision(robots[i], robots[j]);
            }
        }
    }

    private void resolveCollision(RobotState a, RobotState b) {
        double dx   = b.x - a.x;
        double dy   = b.y - a.y;
        double dist = Math.hypot(dx, dy);
        double minDist = Constants.Robot.WIDTH_WITH_BUMPERS;
        if (dist < minDist && dist > 0.001) {
            double overlap = (minDist - dist) / 2.0;
            double nx = dx / dist;
            double ny = dy / dist;
            a.x -= nx * overlap;
            a.y -= ny * overlap;
            b.x += nx * overlap;
            b.y += ny * overlap;
        }
    }

    // ── Accessors ──────────────────────────────────────────────────────────
    public RobotState[]        getAllRobots()    { return robots; }
    public InputState[]        getAllInputs()    { return inputs; }
    public RobotState          getPlayerRobot() { return robots[playerRobotIndex]; }
    public InputState          getPlayerInput() { return inputs[playerRobotIndex]; }
    public RobotState          getRobot(int i)  { return robots[i]; }
    public InputState          getInput(int i)  { return inputs[i]; }
    public AIRobotController   getAIController(int i) { return aiControllers[i]; }

    public RobotState[] getBlueRobots() {
        RobotState[] blue = new RobotState[ROBOTS_PER_ALLIANCE];
        System.arraycopy(robots, 0, blue, 0, ROBOTS_PER_ALLIANCE);
        return blue;
    }

    public RobotState[] getRedRobots() {
        RobotState[] red = new RobotState[ROBOTS_PER_ALLIANCE];
        System.arraycopy(robots, ROBOTS_PER_ALLIANCE, red, 0, ROBOTS_PER_ALLIANCE);
        return red;
    }

    public void setPlayerRobot(int index) {
        playerRobotIndex = Math.max(0, Math.min(TOTAL_ROBOTS - 1, index));
        for (int i = 0; i < TOTAL_ROBOTS; i++) {
            robots[i].isPlayerControlled = (i == playerRobotIndex);
        }
    }

    public int getPlayerRobotIndex() { return playerRobotIndex; }

    public void convertPlayerToAI(int robotIdx, int autoMode) {
        if (robotIdx == playerRobotIndex) return; // can't convert player
        AIRobotController ai = aiControllers[robotIdx];
        if (ai != null) ai.setAutoMode(autoMode);
    }

    private void copyInput(InputState src, InputState dst) {
        dst.forward   = src.forward;
        dst.strafe    = src.strafe;
        dst.turn      = src.turn;
        dst.shooterAngle = src.shooterAngle;
        dst.shooterPower = src.shooterPower;
        dst.intake    = src.intake;
        dst.shoot     = src.shoot;
        dst.spinUp    = src.spinUp;
        dst.climberUp = src.climberUp;
        dst.climberDown = src.climberDown;
        dst.level1    = src.level1;
        dst.level2    = src.level2;
        dst.level3    = src.level3;
        dst.toggleTrenchMode = src.toggleTrenchMode;
        dst.toggleSpeed      = src.toggleSpeed;
        dst.toggleFieldRel   = src.toggleFieldRel;
        dst.resetGyro        = src.resetGyro;
        dst.skiStop          = src.skiStop;
        dst.resetRobot       = src.resetRobot;
        dst.redChuteRelease  = src.redChuteRelease;
        dst.blueChuteRelease = src.blueChuteRelease;
        dst.redCorralTransfer  = src.redCorralTransfer;
        dst.blueCorralTransfer = src.blueCorralTransfer;
        dst.startMatch  = src.startMatch;
        dst.pauseMatch  = src.pauseMatch;
    }

    public String getAutoModesSummary() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < TOTAL_ROBOTS; i++) {
            AIRobotController ai = aiControllers[i];
            if (ai != null) {
                sb.append(String.format("  Robot %d (T%d %s): %s\n",
                        i, robots[i].teamNumber,
                        robots[i].alliance,
                        ai.getAutoModeName()));
            }
        }
        return sb.toString();
    }
}
