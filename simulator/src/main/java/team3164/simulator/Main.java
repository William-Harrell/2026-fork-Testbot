package team3164.simulator;

import team3164.simulator.engine.*;
import team3164.simulator.web.SimulatorServer;

import java.awt.Desktop;
import java.net.URI;

/**
 * Entry point for the REBUILT 2026 simulator.
 *
 * Usage:
 *   java -jar reefscape-sim.jar                      → web server on port 8080
 *   java -jar reefscape-sim.jar --headless           → run one headless match
 *   java -jar reefscape-sim.jar --benchmark [N]      → benchmark all 4 auto modes (N matches each)
 *   java -jar reefscape-sim.jar --port 9090          → web server on custom port
 *   java -jar reefscape-sim.jar --help               → show usage
 */
public class Main {

    private static final String BANNER =
        "╔══════════════════════════════════════════════╗\n" +
        "║   REBUILT 2026 SIMULATOR — TEAM 3164         ║\n" +
        "║   Field: 16.54m × 8.07m  |  Auto: 20s       ║\n" +
        "║   Modes: Do Nothing / Score+Collect+Climb    ║\n" +
        "║          Quick Climb / Preload Only          ║\n" +
        "╚══════════════════════════════════════════════╝";

    private static final int DEFAULT_PORT = 8080;

    public static void main(String[] args) {
        System.out.println(BANNER);
        System.out.println();

        boolean headless   = false;
        boolean benchmark  = false;
        boolean showHelp   = false;
        int     port       = DEFAULT_PORT;
        int     numMatches = 500;

        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--headless":  headless  = true; break;
                case "--benchmark": benchmark = true;
                    if (i + 1 < args.length) {
                        try { numMatches = Integer.parseInt(args[++i]); } catch (NumberFormatException ignored) { i--; }
                    }
                    break;
                case "--port":
                    if (i + 1 < args.length) {
                        try { port = Integer.parseInt(args[++i]); } catch (NumberFormatException ignored) { i--; }
                    }
                    break;
                case "--help": case "-h": showHelp = true; break;
            }
        }

        if (showHelp) { printHelp(); return; }

        if (headless || benchmark) {
            runHeadlessMode(args);
            return;
        }

        // Web server mode
        SimulationEngine engine = new SimulationEngine();
        engine.start();

        SimulatorServer server = new SimulatorServer(engine, port);
        server.start();

        System.out.println("Open your browser at: http://localhost:" + port);
        openBrowser("http://localhost:" + port);

        // Keep alive until interrupted
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nShutting down...");
            engine.stop();
            server.stop();
        }));

        try {
            Thread.currentThread().join();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private static void openBrowser(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            }
        } catch (Exception ignored) {}
    }

    private static void runHeadlessMode(String[] args) {
        boolean benchmark = false;
        int numMatches = 500;

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("--benchmark")) {
                benchmark = true;
                if (i + 1 < args.length) {
                    try { numMatches = Integer.parseInt(args[++i]); } catch (NumberFormatException ignored) { i--; }
                }
            }
        }

        if (benchmark) {
            System.out.println("Running benchmark with " + numMatches + " simulations per mode...\n");
            AutoModeBenchmark.main(new String[]{ String.valueOf(numMatches), "benchmark_results.json" });
        } else {
            // Single headless match
            HeadlessMatchRunner runner = new HeadlessMatchRunner();
            System.out.println(runner.runMatch(true));
        }
    }

    private static void printHelp() {
        System.out.println("REBUILT 2026 Simulator — Usage:");
        System.out.println("  (no args)              Start web server on port 8080");
        System.out.println("  --port <N>             Start web server on port N");
        System.out.println("  --headless             Run one headless auto match and exit");
        System.out.println("  --benchmark [N]        Benchmark all 4 auto modes (N sims each, default 500)");
        System.out.println("  --help                 Show this help");
        System.out.println();
        System.out.println("Auto Modes (from AutoConstants.java):");
        System.out.println("  0: Do Nothing");
        System.out.println("  1: Score, Collect & Climb  (shoots preload + collects neutral + climbs L1)");
        System.out.println("  2: Quick Climb             (shoots preload + drives to tower + climbs L1)");
        System.out.println("  3: Preload Only            (shoots preload + holds position)");
    }
}
