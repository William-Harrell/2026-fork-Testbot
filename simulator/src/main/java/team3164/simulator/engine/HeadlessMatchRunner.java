package team3164.simulator.engine;

import team3164.simulator.Constants;
import team3164.simulator.physics.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs a complete simulated match without any UI or web server.
 * Used by AutoModeBenchmark for statistical analysis.
 */
public class HeadlessMatchRunner {

    private final MultiRobotManager robotManager;
    private final MatchState         matchState;
    private final FuelState          fuelState;
    private final MatchObserver      observer;
    private final List<String>       logs;
    private boolean verbose = false;

    public HeadlessMatchRunner() {
        robotManager = new MultiRobotManager();
        matchState   = new MatchState();
        fuelState    = new FuelState();
        observer     = new MatchObserver();
        logs         = new ArrayList<>();
    }

    /**
     * Run a full 20-second auto period simulation.
     * @param verbose If true, print detailed log to stdout.
     * @return Formatted match summary.
     */
    public String runMatch(boolean verbose) {
        this.verbose = verbose;
        reset();

        // Start match
        matchState.startMatch();
        robotManager.startAuto();
        observer.onMatchStart(matchState);
        log("=== AUTO SIMULATION START ===");
        log("Active hub: " + getActiveHub());

        double dt    = Constants.Simulation.DT;
        double endTime = Constants.Match.AUTO_END; // Simulate only the auto period

        while (matchState.matchTime < endTime && !matchState.matchEnded) {
            matchState.matchTime += dt;
            matchState.updatePhase();

            // AI updates inputs
            InputState playerInput = new InputState(); // empty player input in headless
            robotManager.update(playerInput, matchState, dt);

            // Physics
            updateAllPhysics(dt);

            // Observer
            observer.update(robotManager.getAllRobots(), matchState, dt);
        }

        matchState.matchEnded = true;
        observer.onMatchEnd(matchState);
        log(observer.generateSummary(matchState));
        return observer.generateSummary(matchState);
    }

    /**
     * Run match and return JSON summary.
     */
    public String runMatchJson() {
        runMatch(false);
        return observer.generateJsonSummary(matchState);
    }

    /**
     * Run N matches and aggregate results.
     */
    public String runMultipleMatches(int n, boolean verbose) {
        StringBuilder sb = new StringBuilder();
        int redWins = 0, blueWins = 0, ties = 0;
        double totalRedScore = 0, totalBlueScore = 0;

        for (int i = 0; i < n; i++) {
            runMatch(false);
            totalRedScore  += matchState.redTotalScore;
            totalBlueScore += matchState.blueTotalScore;
            if      (matchState.redTotalScore  > matchState.blueTotalScore) redWins++;
            else if (matchState.blueTotalScore > matchState.redTotalScore)  blueWins++;
            else                                                            ties++;
        }

        sb.append(String.format("=== %d MATCH AGGREGATE ===\n", n));
        sb.append(String.format("RED avg:  %.1f  (wins=%d)\n", totalRedScore  / n, redWins));
        sb.append(String.format("BLUE avg: %.1f  (wins=%d)\n", totalBlueScore / n, blueWins));
        sb.append(String.format("Ties: %d\n", ties));
        return sb.toString();
    }

    private void reset() {
        robotManager.reset();
        matchState.reset();
        fuelState.reset();
        observer.initialize(robotManager.getAllRobots(), robotManager);
        logs.clear();
    }

    private void updateAllPhysics(double dt) {
        RobotState[] allRobots = robotManager.getAllRobots();
        InputState[] allInputs = robotManager.getAllInputs();

        for (int i = 0; i < allRobots.length; i++) {
            RobotState robot = allRobots[i];
            InputState inp   = allInputs[i];
            if (!robot.isEnabled) continue;

            SwervePhysics.update(robot, inp, dt);
            BumpPhysics.update(robot, dt);
            CollisionPhysics.checkAndResolveCollisions(robot);
            IntakePhysics.update(robot, inp, fuelState, dt);
            ShooterPhysics.update(robot, inp, fuelState, matchState, dt);
            TowerPhysics.update(robot, inp, matchState, dt);
        }

        FuelPhysics.update(fuelState, matchState, dt);
        robotManager.resolveRobotCollisions();
    }

    private void logPhaseChange() {
        log("Phase: " + matchState.getPhaseName() + " | " + getActiveHub());
    }

    private String getActiveHub() {
        String red  = matchState.redHubStatus  == MatchState.HubStatus.ACTIVE ? "RED(ACTIVE)"  : "RED(inactive)";
        String blue = matchState.blueHubStatus == MatchState.HubStatus.ACTIVE ? "BLUE(ACTIVE)" : "BLUE(inactive)";
        return red + " " + blue;
    }

    private void log(String msg) {
        logs.add(msg);
        if (verbose) System.out.println("[HEADLESS] " + msg);
    }

    // ── Accessors ──────────────────────────────────────────────────────────
    public MultiRobotManager getRobotManager() { return robotManager; }
    public MatchState        getMatchState()   { return matchState; }
    public MatchObserver     getObserver()     { return observer; }
    public List<String>      getLogs()         { return logs; }

    /** Quick standalone test. */
    public static void main(String[] args) {
        HeadlessMatchRunner runner = new HeadlessMatchRunner();
        // Set player robot to mode 1 (Score & Collect)
        runner.robotManager.getAIController(0).setAutoMode(AutonomousController.AUTO_SCORE_AND_COLLECT);
        System.out.println(runner.runMatch(true));
    }
}
