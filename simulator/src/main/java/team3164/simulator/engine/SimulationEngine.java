package team3164.simulator.engine;

import team3164.simulator.Constants;
import team3164.simulator.physics.*;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * Core simulation loop. Runs at TICK_RATE Hz, updating physics for all robots.
 */
public class SimulationEngine {

    private final MultiRobotManager    robotManager;
    private final RobotState           state;    // player robot alias
    private final InputState           input;    // player input alias
    private final MatchState           matchState;
    private final FuelState            fuelState;
    private final AutonomousController autoController;

    private boolean multiRobotEnabled = true;

    private ScheduledExecutorService executor;
    private Consumer<RobotState>      stateListener;
    private Consumer<MatchState>      matchStateListener;
    private Consumer<FuelState>       fuelStateListener;
    private Consumer<String>          logListener;

    private boolean running = false;
    private boolean paused  = false;
    private long    tickCount  = 0;
    private long    startTimeMs;
    private MatchState.MatchPhase lastPhase = MatchState.MatchPhase.PRE_MATCH;

    public SimulationEngine() {
        robotManager   = new MultiRobotManager();
        matchState     = new MatchState();
        fuelState      = new FuelState();
        autoController = new AutonomousController();

        state  = robotManager.getPlayerRobot();
        input  = robotManager.getPlayerInput();
    }

    // ── Lifecycle ──────────────────────────────────────────────────────────
    public void start() {
        if (running) return;
        running = true;
        paused  = false;
        startTimeMs = System.currentTimeMillis();

        long periodMs = (long)(1000.0 / Constants.Simulation.TICK_RATE);
        executor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sim-engine");
            t.setDaemon(true);
            return t;
        });
        executor.scheduleAtFixedRate(this::tick, 0, periodMs, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        running = false;
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }

    public void startMatch() {
        matchState.startMatch();
        robotManager.reset();
        fuelState.reset();
        autoController.startAuto(state);
        robotManager.startAuto();
        log("Match started — auto mode: " + autoController.getSelectedModeName());
        lastPhase = MatchState.MatchPhase.AUTO;
    }

    // ── Main tick ─────────────────────────────────────────────────────────
    private void tick() {
        if (!running || paused) return;
        tickCount++;
        double dt = Constants.Simulation.DT;

        // Advance match time
        if (matchState.matchStarted && !matchState.matchEnded) {
            matchState.matchTime += dt;
            matchState.updatePhase();
        }

        // Update physics
        updatePhysics(dt);

        // Process toggles and match control
        processToggles();
        processMatchControl();

        // Phase change logging
        logPhaseChange();

        // Broadcast to listeners
        if (tickCount % Constants.Simulation.BROADCAST_RATE == 0) {
            notifyListeners();
        }
    }

    private void updatePhysics(double dt) {
        if (!matchState.matchStarted) return;

        // Update all robots
        if (multiRobotEnabled) {
            updateAllRobotsPhysics(dt);
        } else {
            // Only update player robot
            updateRobotPhysics(state, input, dt);
        }

        // Fuel physics
        FuelPhysics.update(fuelState, matchState, dt);

        // Outpost
        OutpostPhysics.update(fuelState, matchState, input, dt);

        // Robot collisions
        robotManager.resolveRobotCollisions();
    }

    private void updateAllRobotsPhysics(double dt) {
        // Let AI update inputs
        robotManager.update(input, matchState, dt);

        // Auto controller for player during auto
        if (matchState.isAuto()) {
            autoController.update(state, input, dt);
        }

        // Physics for each robot
        RobotState[] allRobots = robotManager.getAllRobots();
        InputState[] allInputs = robotManager.getAllInputs();
        for (int i = 0; i < allRobots.length; i++) {
            updateRobotPhysics(allRobots[i], allInputs[i], dt);
        }
    }

    private void updateRobotPhysics(RobotState robot, InputState inp, double dt) {
        if (!robot.isEnabled) return;

        SwervePhysics.update(robot, inp, dt);
        BumpPhysics.update(robot, dt);
        TrenchPhysics.update(robot, dt);
        CollisionPhysics.checkAndResolveCollisions(robot);
        IntakePhysics.update(robot, inp, fuelState, dt);

        // Shooter
        FuelState.Fuel fired = ShooterPhysics.update(robot, inp, fuelState, matchState, dt);

        // Tower / Climber
        TowerPhysics.update(robot, inp, matchState, dt);

        updateCommandName(robot, inp);
    }

    private void processToggles() {
        if (input.toggleTrenchMode) {
            TrenchPhysics.toggleTrenchMode(state);
            input.toggleTrenchMode = false;
        }
    }

    private void processMatchControl() {
        if (input.startMatch && !matchState.matchStarted) {
            startMatch();
            input.startMatch = false;
        }
        if (input.resetRobot) {
            matchState.reset();
            robotManager.reset();
            fuelState.reset();
            autoController.reset();
            input.resetRobot = false;
        }
    }

    private void logPhaseChange() {
        if (matchState.currentPhase != lastPhase) {
            lastPhase = matchState.currentPhase;
            log("Phase: " + matchState.getPhaseName()
                + " | " + getActiveHubString()
                + " | Score RED=" + matchState.redTotalScore + " BLUE=" + matchState.blueTotalScore);
            if (matchState.matchEnded) logFinalScore();
        }
    }

    private String getActiveHubString() {
        String red  = matchState.redHubStatus  == MatchState.HubStatus.ACTIVE ? "ACTIVE" : "inactive";
        String blue = matchState.blueHubStatus == MatchState.HubStatus.ACTIVE ? "ACTIVE" : "inactive";
        return "HUB red=" + red + " blue=" + blue;
    }

    private void logFinalScore() {
        log("=== FINAL SCORE === RED: " + matchState.redTotalScore
            + "  BLUE: " + matchState.blueTotalScore
            + " | FUEL red=" + matchState.redFuelScored + " blue=" + matchState.blueFuelScored
            + " | TOWER red=" + matchState.redTowerPoints + " blue=" + matchState.blueTowerPoints);
    }

    private void updateCommandName(RobotState robot, InputState inp) {
        if (inp.shoot)       robot.currentCommand = "Shooting";
        else if (inp.intake) robot.currentCommand = "Intaking";
        else if (inp.hasDriveInput()) robot.currentCommand = "Driving";
        else if (inp.climberUp || inp.climberDown) robot.currentCommand = "Climbing";
        else                 robot.currentCommand = "Idle";
    }

    private void notifyListeners() {
        if (stateListener      != null) stateListener.accept(state);
        if (matchStateListener != null) matchStateListener.accept(matchState);
        if (fuelStateListener  != null) fuelStateListener.accept(fuelState);
    }

    // ── Input / state access ───────────────────────────────────────────────
    public void updateInput(InputState newInput) {
        copyInput(newInput, input);
    }

    private void copyInput(InputState src, InputState dst) {
        dst.forward  = src.forward;  dst.strafe   = src.strafe;  dst.turn     = src.turn;
        dst.shooterAngle = src.shooterAngle; dst.shooterPower = src.shooterPower;
        dst.intake   = src.intake;   dst.shoot    = src.shoot;   dst.spinUp   = src.spinUp;
        dst.climberUp = src.climberUp; dst.climberDown = src.climberDown;
        dst.level1   = src.level1;   dst.level2   = src.level2;  dst.level3   = src.level3;
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

    // ── Getters ────────────────────────────────────────────────────────────
    public RobotState  getState()          { return state; }
    public MatchState  getMatchState()     { return matchState; }
    public FuelState   getFuelState()      { return fuelState; }
    public boolean     isRunning()         { return running; }
    public boolean     isPaused()          { return paused; }
    public long        getTickCount()      { return tickCount; }
    public AutonomousController getAutoController() { return autoController; }
    public MultiRobotManager    getRobotManager()   { return robotManager; }
    public boolean     isMultiRobotEnabled(){ return multiRobotEnabled; }
    public RobotState[] getAllRobots()     { return robotManager.getAllRobots(); }

    public void setAutoMode(int mode)            { autoController.setSelectedMode(mode); }
    public int  getAutoMode()                    { return autoController.getSelectedMode(); }
    public String getAutoModeName()              { return autoController.getSelectedModeName(); }
    public void setMultiRobotEnabled(boolean b)  { multiRobotEnabled = b; }

    // ── Listeners ──────────────────────────────────────────────────────────
    public void setStateListener(Consumer<RobotState> l)   { stateListener      = l; }
    public void setMatchStateListener(Consumer<MatchState> l){ matchStateListener = l; }
    public void setFuelStateListener(Consumer<FuelState> l) { fuelStateListener  = l; }
    public void setLogListener(Consumer<String> l)          { logListener        = l; }

    private void log(String msg) {
        if (logListener != null) logListener.accept(msg);
        else System.out.println("[SIM] " + msg);
    }
}
