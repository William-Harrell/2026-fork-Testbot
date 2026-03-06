package team3164.simulator.engine;

import team3164.simulator.Constants;
import team3164.simulator.physics.*;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Benchmarks all 4 actual auto modes (Do Nothing / Score+Collect+Climb / Quick Climb / Preload Only)
 * over many simulated matches and prints a detailed report.
 */
public class AutoModeBenchmark {

    // Actual 4 robot modes from AutoConstants
    private static final int[] BENCHMARK_MODES = {
        AutonomousController.AUTO_DO_NOTHING,       // 0
        AutonomousController.AUTO_SCORE_AND_COLLECT, // 1 = Score & Collect
        AutonomousController.AUTO_SCORE_ONLY,        // 2 = Score Only
        AutonomousController.AUTO_PRELOAD_ONLY       // 6 = Mode 3 (Preload Only)
    };
    private static final String[] BENCHMARK_MODE_NAMES = {
        "Do Nothing",
        "Score & Collect",
        "Score Only",
        "Preload Only"
    };

    public static class MatchResult {
        public int     matchNumber;
        public int     fuelScored;
        public int     fuelCollectedDuringAuto;
        public int     climbLevel;
        public boolean climbComplete;
        public double  totalDistance;
        public double  timeMoving;
        public double  timeIdle;
        public double  timeShooting;
        public double  timeIntaking;
        public String  alliance;
        public int     allianceScore;
        public int     opponentScore;
        public boolean allianceWon;
        public boolean autoWon;
        public int     autoFuelScored;
        public double  autoEndFuelScored;
        public double  autoEndX;
        public double  autoEndY;
        public String  autoEndPhase;
        public double  timeInShootingPhase;
        public double  timeInDrivingPhase;
        public double  timeInIntakePhase;
        public double  firstShotTime;
        public double  lastShotTime;
    }

    private final int    targetTeam;
    private final int    targetAutoMode;
    private final String autoModeName;

    public AutoModeBenchmark(int teamNumber, int autoMode) {
        this.targetTeam     = teamNumber;
        this.targetAutoMode = autoMode;
        this.autoModeName   = (autoMode < AutonomousController.AUTO_MODE_NAMES.length)
                              ? AutonomousController.AUTO_MODE_NAMES[autoMode] : "Mode " + autoMode;
    }

    public List<MatchResult> runBenchmark(int numMatches, String outputFile) {
        List<MatchResult> results = new ArrayList<>();
        System.out.printf("Benchmarking mode %d (%s) over %d matches...%n",
                targetAutoMode, autoModeName, numMatches);

        for (int i = 0; i < numMatches; i++) {
            results.add(runSingleMatch(i));
            int printEvery = Math.max(1, numMatches / 10);
            if ((i + 1) % printEvery == 0) {
                System.out.printf("  %d%% complete%n", (i + 1) * 100 / numMatches);
            }
        }

        printSummary(results);

        if (outputFile != null) {
            try { writeResultsToJson(results, outputFile); }
            catch (IOException e) { System.err.println("Could not write JSON: " + e.getMessage()); }
        }
        return results;
    }

    private MatchResult runSingleMatch(int matchNum) {
        MatchResult result = new MatchResult();
        result.matchNumber = matchNum;
        result.alliance    = "BLUE";

        MultiRobotManager manager = new MultiRobotManager();
        MatchState         match  = new MatchState();
        FuelState          fuel   = new FuelState();

        // Set player robot to target auto mode
        manager.getAIController(0).setAutoMode(targetAutoMode);
        manager.setPlayerRobot(0);

        match.startMatch();
        manager.startAuto();

        double dt     = Constants.Simulation.DT;
        double endTime = Constants.Match.AUTO_END;
        double prevFuel = 0;
        double firstShot = -1, lastShot = -1;

        while (match.matchTime < endTime) {
            match.matchTime += dt;
            match.updatePhase();

            InputState playerInput = new InputState();
            manager.update(playerInput, match, dt);

            // Physics
            RobotState[] robots = manager.getAllRobots();
            InputState[] inputs = manager.getAllInputs();
            for (int i = 0; i < robots.length; i++) {
                RobotState r = robots[i]; InputState inp = inputs[i];
                if (!r.isEnabled) continue;
                SwervePhysics.update(r, inp, dt);
                BumpPhysics.update(r, dt);
                CollisionPhysics.checkAndResolveCollisions(r);
                IntakePhysics.update(r, inp, fuel, dt);
                ShooterPhysics.update(r, inp, fuel, match, dt);
                TowerPhysics.update(r, inp, match, dt);
            }
            FuelPhysics.update(fuel, match, dt);
            manager.resolveRobotCollisions();

            // Track player robot stats
            RobotState player = manager.getPlayerRobot();
            double speed = player.getSpeed();
            if (speed > 0.1) result.timeMoving    += dt;
            if (player.intakeState == RobotState.IntakeState.SHOOTING) {
                result.timeShooting += dt;
                if (firstShot < 0) firstShot = match.matchTime;
                lastShot = match.matchTime;
            }
            if (player.intakeState == RobotState.IntakeState.INTAKING) result.timeIntaking += dt;
            else result.timeIdle += dt;

            result.totalDistance += speed * dt;

            // Track fuel scored
            if (match.blueFuelScored > prevFuel) {
                prevFuel = match.blueFuelScored;
            }
        }

        // Final state — track player robot (idx 0) stats only
        RobotState player = manager.getPlayerRobot();
        result.fuelScored   = player.fuelScored;  // only player robot's shots
        result.climbLevel   = player.climbLevel;
        result.climbComplete = player.climbComplete;
        result.autoEndX     = player.x;
        result.autoEndY     = player.y;
        result.autoEndFuelScored = player.fuelScored;
        result.firstShotTime = firstShot;
        result.lastShotTime  = lastShot;
        result.allianceScore = match.blueTotalScore;
        result.opponentScore = match.redTotalScore;
        result.allianceWon   = match.blueTotalScore >= match.redTotalScore;
        result.autoWon       = (match.autoWinner == null || match.autoWinner == MatchState.Alliance.BLUE);

        // Estimate climb points
        if (player.climbComplete) {
            result.allianceScore += TowerPhysics.getPointsForLevel(player.climbLevel, match);
        }

        return result;
    }

    private void printSummary(List<MatchResult> results) {
        System.out.println("\n" + "═".repeat(60));
        System.out.printf("BENCHMARK: %s  (%d simulations)%n", autoModeName, results.size());
        System.out.println("═".repeat(60));

        double avgFuel  = results.stream().mapToInt(r -> r.fuelScored).average().orElse(0);
        double avgScore = results.stream().mapToInt(r -> r.allianceScore).average().orElse(0);
        double avgDist  = results.stream().mapToDouble(r -> r.totalDistance).average().orElse(0);
        long   climbCount = results.stream().filter(r -> r.climbComplete).count();
        double climbPct   = climbCount * 100.0 / results.size();
        long   autoWins   = results.stream().filter(r -> r.autoWon).count();
        double autoWinPct = autoWins * 100.0 / results.size();

        System.out.printf("Avg FUEL scored:  %.2f%n", avgFuel);
        System.out.printf("Avg total score:  %.2f%n", avgScore);
        System.out.printf("Avg distance:     %.1f m%n", avgDist);
        System.out.printf("Climbs complete:  %d / %d (%.0f%%)%n", climbCount, results.size(), climbPct);
        System.out.printf("Auto wins:        %d / %d (%.0f%%)%n", autoWins, results.size(), autoWinPct);

        // FUEL distribution
        Map<Integer, Long> fuelDist = results.stream()
                .collect(Collectors.groupingBy(r -> r.fuelScored, Collectors.counting()));
        System.out.println("\nFUEL Distribution:");
        int maxFuel = fuelDist.keySet().stream().mapToInt(i -> i).max().orElse(0);
        for (int f = 0; f <= maxFuel; f++) {
            long cnt = fuelDist.getOrDefault(f, 0L);
            double pct = cnt * 100.0 / results.size();
            String bar = "█".repeat((int)(pct / 2));
            System.out.printf("  %2d FUEL: %s %.1f%% (%d)%n", f, bar, pct, cnt);
        }
        System.out.println("═".repeat(60));
    }

    private void writeResultsToJson(List<MatchResult> results, String outputFile) throws IOException {
        try (FileWriter fw = new FileWriter(outputFile)) {
            fw.write("{\"mode\":" + targetAutoMode + ",\"name\":\"" + autoModeName + "\",\"results\":[");
            for (int i = 0; i < results.size(); i++) {
                MatchResult r = results.get(i);
                if (i > 0) fw.write(",");
                fw.write(String.format(
                    "{\"match\":%d,\"fuel\":%d,\"climb\":%d,\"climbOk\":%b,\"score\":%d,\"autoWon\":%b,\"dist\":%.2f}",
                    r.matchNumber, r.fuelScored, r.climbLevel, r.climbComplete,
                    r.allianceScore, r.autoWon, r.totalDistance));
            }
            fw.write("]}");
        }
        System.out.println("Results written to " + outputFile);
    }

    /**
     * Main entry point: benchmark all 4 robot auto modes.
     */
    public static void main(String[] args) {
        int numMatches = 500; // fast default; use 5000 for full benchmark
        String outFile = "benchmark_results.json";

        if (args.length >= 1) {
            try { numMatches = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }
        if (args.length >= 2) outFile = args[1];

        System.out.println("TEAM 3164 AUTO MODE BENCHMARK");
        System.out.println("Simulating " + numMatches + " matches per mode");
        System.out.println();

        // Run each of the 4 actual auto modes
        int[] modes = { 0, 10, 2, 6 }; // DO_NOTHING, SCORE_COLLECT_CLIMB, QUICK_CLIMB, PRELOAD_ONLY
        String[] names = { "Do Nothing", "Score, Collect & Climb", "Quick Climb", "Preload Only" };

        Map<String, Double> avgFuels = new LinkedHashMap<>();
        for (int i = 0; i < modes.length; i++) {
            AutoModeBenchmark bench = new AutoModeBenchmark(3164, modes[i]);
            List<MatchResult> results = bench.runBenchmark(numMatches, null);
            double avg = results.stream().mapToInt(r -> r.fuelScored).average().orElse(0);
            avgFuels.put(names[i], avg);
        }

        // Summary table
        System.out.println("\n" + "═".repeat(60));
        System.out.println("SUMMARY — Average FUEL scored in AUTO (20 s)");
        System.out.println("═".repeat(60));
        avgFuels.forEach((name, avg) ->
            System.out.printf("  %-30s %5.2f FUEL%n", name, avg));
        System.out.println("═".repeat(60));
    }
}
