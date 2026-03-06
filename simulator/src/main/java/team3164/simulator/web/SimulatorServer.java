package team3164.simulator.web;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import io.javalin.Javalin;
import io.javalin.websocket.WsContext;
import team3164.simulator.Constants;
import team3164.simulator.engine.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Javalin-based HTTP + WebSocket server.
 * Serves the static web frontend and streams simulation state to browsers.
 */
public class SimulatorServer {

    private final SimulationEngine engine;
    private final Gson             gson;
    private final int              port;
    private Javalin                app;
    private final Map<String, WsContext> clients = new ConcurrentHashMap<>();
    private ScheduledExecutorService broadcastExecutor;

    public SimulatorServer(SimulationEngine engine, int port) {
        this.engine = engine;
        this.gson   = new Gson();
        this.port   = port;
    }

    public void start() {
        app = Javalin.create(config -> {
            config.staticFiles.add(staticFiles -> {
                staticFiles.hostedPath = "/";
                staticFiles.directory  = "/web";
                staticFiles.location   = io.javalin.http.staticfiles.Location.CLASSPATH;
            });
        });

        // REST endpoints
        app.get("/api/state", ctx -> ctx.json(buildFullStateJson()));
        app.get("/api/modes",  ctx -> {
            JsonArray arr = new JsonArray();
            for (int i = 0; i < AutonomousController.NUM_AUTO_MODES; i++) {
                JsonObject m = new JsonObject();
                m.addProperty("id",   i);
                m.addProperty("name", AutonomousController.AUTO_MODE_NAMES[i]);
                arr.add(m);
            }
            ctx.result(gson.toJson(arr));
        });
        app.post("/api/start", ctx -> {
            engine.startMatch();
            ctx.result("{\"ok\":true}");
        });
        app.post("/api/reset", ctx -> {
            // handled via WS message too
            ctx.result("{\"ok\":true}");
        });
        app.post("/api/automode", ctx -> {
            String body = ctx.body();
            try {
                JsonObject obj = gson.fromJson(body, JsonObject.class);
                int mode = obj.get("mode").getAsInt();
                engine.setAutoMode(mode);
                ctx.result("{\"ok\":true,\"mode\":" + mode + "}");
            } catch (Exception e) {
                ctx.status(400).result("{\"error\":\"" + e.getMessage() + "\"}");
            }
        });

        // WebSocket
        app.ws("/ws", ws -> {
            ws.onConnect(ctx -> {
                clients.put(ctx.sessionId(), ctx);
                sendFullState(ctx);
            });
            ws.onMessage(ctx -> handleMessage(ctx.message()));
            ws.onClose(ctx  -> clients.remove(ctx.sessionId()));
            ws.onError(ctx  -> clients.remove(ctx.sessionId()));
        });

        app.start(port);
        startBroadcastLoop();
        System.out.println("[WEB] Server running at http://localhost:" + port);
    }

    public void stop() {
        if (broadcastExecutor != null) broadcastExecutor.shutdownNow();
        if (app != null) app.stop();
    }

    private void startBroadcastLoop() {
        long intervalMs = (long)(1000.0 / Constants.Simulation.TICK_RATE
                * Constants.Simulation.BROADCAST_RATE);
        broadcastExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "ws-broadcast");
            t.setDaemon(true);
            return t;
        });
        broadcastExecutor.scheduleAtFixedRate(this::broadcastState,
                0, intervalMs, TimeUnit.MILLISECONDS);
    }

    private void broadcastState() {
        if (clients.isEmpty()) return;
        String json = buildFullStateJson();
        for (WsContext ctx : clients.values()) {
            try { ctx.send(json); } catch (Exception ignored) {}
        }
    }

    private void sendFullState(WsContext ctx) {
        try { ctx.send(buildFullStateJson()); } catch (Exception ignored) {}
    }

    private String buildFullStateJson() {
        JsonObject root = new JsonObject();
        root.add("robots", buildAllRobotsJson());
        root.add("player", buildRobotJson(engine.getState()));
        root.add("match",  buildMatchJson(engine.getMatchState()));
        root.add("fuel",   buildFuelJson(engine.getFuelState()));
        root.add("control", buildControlJson(engine.getState()));
        root.addProperty("autoMode",     engine.getAutoMode());
        root.addProperty("autoModeName", engine.getAutoModeName());
        return gson.toJson(root);
    }

    private JsonArray buildAllRobotsJson() {
        JsonArray arr = new JsonArray();
        for (RobotState r : engine.getAllRobots()) {
            arr.add(buildRobotJson(r));
        }
        return arr;
    }

    private JsonObject buildRobotJson(RobotState r) {
        JsonObject o = new JsonObject();
        o.addProperty("id",       r.robotId);
        o.addProperty("team",     r.teamNumber);
        o.addProperty("alliance", r.alliance != null ? r.alliance.name() : "NONE");
        o.addProperty("player",   r.isPlayerControlled);
        o.addProperty("x",        round(r.x, 3));
        o.addProperty("y",        round(r.y, 3));
        o.addProperty("heading",  round(Math.toDegrees(r.heading), 1));
        o.addProperty("speed",    round(r.getSpeed(), 2));
        o.addProperty("fuel",     r.fuelCount);
        o.addProperty("fuelScored", r.fuelScored);
        o.addProperty("cmd",      r.currentCommand);
        o.add("shooter",  buildShooterJson(r));
        o.add("climber",  buildClimberJson(r));
        o.add("swerve",   buildSwerveJson(r));
        return o;
    }

    private JsonObject buildSwerveJson(RobotState r) {
        JsonObject o = new JsonObject();
        JsonArray angles = new JsonArray();
        JsonArray speeds = new JsonArray();
        for (int i = 0; i < 4; i++) {
            angles.add(round(Math.toDegrees(r.moduleAngles[i]), 1));
            speeds.add(round(r.moduleSpeeds[i], 2));
        }
        o.add("angles", angles);
        o.add("speeds", speeds);
        o.addProperty("vx",    round(r.vx, 2));
        o.addProperty("vy",    round(r.vy, 2));
        o.addProperty("omega", round(r.omega, 2));
        return o;
    }

    private JsonObject buildShooterJson(RobotState r) {
        JsonObject o = new JsonObject();
        o.addProperty("angle",    round(r.shooterAngle, 1));
        o.addProperty("angleGoal",round(r.shooterAngleGoal, 1));
        o.addProperty("velocity", round(r.shooterVelocity, 1));
        o.addProperty("atAngle",  r.shooterAtAngle);
        o.addProperty("atSpeed",  r.shooterAtSpeed);
        o.addProperty("spinUp",   r.shooterSpinningUp);
        o.addProperty("state",    r.intakeState.name());
        return o;
    }

    private JsonObject buildClimberJson(RobotState r) {
        JsonObject o = new JsonObject();
        o.addProperty("position", round(r.climberPosition, 2));
        o.addProperty("level",    r.climbLevel);
        o.addProperty("complete", r.climbComplete);
        o.addProperty("climbing", r.isClimbing);
        o.addProperty("height",   round(r.robotHeight, 3));
        o.addProperty("points",   r.towerPoints);
        return o;
    }

    private JsonObject buildMatchJson(MatchState m) {
        JsonObject o = new JsonObject();
        o.addProperty("time",        round(m.matchTime, 2));
        o.addProperty("remaining",   round(m.getRemainingTime(), 1));
        o.addProperty("formatted",   m.getFormattedTime());
        o.addProperty("phase",       m.getPhaseName());
        o.addProperty("started",     m.matchStarted);
        o.addProperty("ended",       m.matchEnded);
        o.addProperty("redScore",    m.redTotalScore);
        o.addProperty("blueScore",   m.blueTotalScore);
        o.addProperty("redFuel",     m.redFuelScored);
        o.addProperty("blueFuel",    m.blueFuelScored);
        o.addProperty("redTower",    m.redTowerPoints);
        o.addProperty("blueTower",   m.blueTowerPoints);
        o.addProperty("redHubActive",  m.redHubStatus  == MatchState.HubStatus.ACTIVE);
        o.addProperty("blueHubActive", m.blueHubStatus == MatchState.HubStatus.ACTIVE);
        o.addProperty("redEnergized",  m.redEnergized);
        o.addProperty("blueEnergized", m.blueEnergized);
        o.addProperty("redTraversal",  m.redTraversal);
        o.addProperty("blueTraversal", m.blueTraversal);
        // Climb levels
        JsonArray redClimb  = new JsonArray();
        JsonArray blueClimb = new JsonArray();
        for (int l : m.redRobotClimbLevel)  redClimb.add(l);
        for (int l : m.blueRobotClimbLevel) blueClimb.add(l);
        o.add("redClimb",  redClimb);
        o.add("blueClimb", blueClimb);
        return o;
    }

    private JsonObject buildFuelJson(FuelState fs) {
        JsonObject o = new JsonObject();
        JsonArray field  = new JsonArray();
        JsonArray flight = new JsonArray();
        for (FuelState.Fuel f : fs.getFieldFuel()) {
            JsonObject fo = new JsonObject();
            fo.addProperty("id", f.id);
            fo.addProperty("x",  round(f.x, 2));
            fo.addProperty("y",  round(f.y, 2));
            fo.addProperty("z",  round(f.z, 2));
            field.add(fo);
        }
        for (FuelState.Fuel f : fs.getFlightFuel()) {
            JsonObject fo = new JsonObject();
            fo.addProperty("id", f.id);
            fo.addProperty("x",  round(f.x, 2));
            fo.addProperty("y",  round(f.y, 2));
            fo.addProperty("z",  round(f.z, 2));
            fo.addProperty("vx", round(f.vx, 2));
            fo.addProperty("vy", round(f.vy, 2));
            fo.addProperty("vz", round(f.vz, 2));
            flight.add(fo);
        }
        o.add("field",  field);
        o.add("flight", flight);
        o.addProperty("redChute",  fs.getRedChuteFuel().size());
        o.addProperty("blueChute", fs.getBlueChuteFuel().size());
        o.addProperty("redDepot",  fs.getRedDepotFuel().size());
        o.addProperty("blueDepot", fs.getBlueDepotFuel().size());
        return o;
    }

    private JsonObject buildControlJson(RobotState r) {
        JsonObject o = new JsonObject();
        o.addProperty("fieldRelative", r.fieldRelative);
        o.addProperty("slowMode",      r.slowMode);
        o.addProperty("trenchMode",    r.trenchMode);
        o.addProperty("onBump",        r.onBump);
        return o;
    }

    private void handleMessage(String msg) {
        try {
            JsonObject obj = gson.fromJson(msg, JsonObject.class);
            String type = obj.has("type") ? obj.get("type").getAsString() : "";

            switch (type) {
                case "input":
                    engine.updateInput(parseInput(obj));
                    break;
                case "startMatch":
                    engine.startMatch();
                    break;
                case "reset":
                    InputState resetInput = new InputState();
                    resetInput.resetRobot = true;
                    engine.updateInput(resetInput);
                    break;
                case "setAutoMode":
                    if (obj.has("mode")) engine.setAutoMode(obj.get("mode").getAsInt());
                    break;
                default:
                    // Try parsing as input directly
                    if (obj.has("forward") || obj.has("shoot")) {
                        engine.updateInput(parseInput(obj));
                    }
                    break;
            }
        } catch (Exception e) {
            // Ignore malformed messages
        }
    }

    private InputState parseInput(JsonObject obj) {
        InputState inp = new InputState();
        if (obj.has("forward"))  inp.forward  = obj.get("forward").getAsDouble();
        if (obj.has("strafe"))   inp.strafe   = obj.get("strafe").getAsDouble();
        if (obj.has("turn"))     inp.turn     = obj.get("turn").getAsDouble();
        if (obj.has("shoot"))    inp.shoot    = obj.get("shoot").getAsBoolean();
        if (obj.has("intake"))   inp.intake   = obj.get("intake").getAsBoolean();
        if (obj.has("spinUp"))   inp.spinUp   = obj.get("spinUp").getAsBoolean();
        if (obj.has("climberUp"))   inp.climberUp   = obj.get("climberUp").getAsBoolean();
        if (obj.has("climberDown")) inp.climberDown = obj.get("climberDown").getAsBoolean();
        if (obj.has("level1"))   inp.level1   = obj.get("level1").getAsBoolean();
        if (obj.has("level2"))   inp.level2   = obj.get("level2").getAsBoolean();
        if (obj.has("level3"))   inp.level3   = obj.get("level3").getAsBoolean();
        if (obj.has("toggleTrenchMode")) inp.toggleTrenchMode = obj.get("toggleTrenchMode").getAsBoolean();
        if (obj.has("toggleSpeed"))      inp.toggleSpeed      = obj.get("toggleSpeed").getAsBoolean();
        if (obj.has("toggleFieldRel"))   inp.toggleFieldRel   = obj.get("toggleFieldRel").getAsBoolean();
        if (obj.has("resetGyro"))        inp.resetGyro        = obj.get("resetGyro").getAsBoolean();
        if (obj.has("startMatch"))       inp.startMatch       = obj.get("startMatch").getAsBoolean();
        if (obj.has("resetRobot"))       inp.resetRobot       = obj.get("resetRobot").getAsBoolean();
        if (obj.has("redChuteRelease"))  inp.redChuteRelease  = obj.get("redChuteRelease").getAsBoolean();
        if (obj.has("blueChuteRelease")) inp.blueChuteRelease = obj.get("blueChuteRelease").getAsBoolean();
        if (obj.has("shooterAngle"))     inp.shooterAngle     = obj.get("shooterAngle").getAsDouble();
        if (obj.has("shooterPower"))     inp.shooterPower     = obj.get("shooterPower").getAsDouble();
        return inp;
    }

    private double round(double v, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(v * factor) / factor;
    }

    public int getPort() { return port; }
}
