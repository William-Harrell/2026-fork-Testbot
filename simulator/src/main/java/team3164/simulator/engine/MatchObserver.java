package team3164.simulator.engine;

import java.util.*;

/**
 * Records match events and per-robot statistics.
 * Used by HeadlessMatchRunner and AutoModeBenchmark for analysis.
 */
public class MatchObserver {

    public enum EventType {
        MATCH_START, PHASE_CHANGE, FUEL_SCORED, FUEL_COLLECTED, FUEL_SHOT,
        CLIMB_START, CLIMB_COMPLETE, ROBOT_MOVEMENT, MATCH_END
    }

    public static class MatchEvent {
        public final double    time;
        public final EventType type;
        public final int       robotId;
        public final String    alliance;
        public final String    description;
        public final Map<String, Object> data;

        public MatchEvent(double time, EventType type, int robotId, String alliance, String description) {
            this.time        = time;
            this.type        = type;
            this.robotId     = robotId;
            this.alliance    = alliance;
            this.description = description;
            this.data        = new LinkedHashMap<>();
        }

        public MatchEvent withData(String key, Object value) {
            data.put(key, value);
            return this;
        }

        @Override
        public String toString() {
            return String.format("[%5.2f] %-18s R%d %-4s %s", time, type, robotId, alliance, description);
        }
    }

    public static class RobotStats {
        public int     robotId;
        public String  alliance;
        public int     teamNumber;
        public boolean isPlayer;

        public int     fuelCollected;
        public int     fuelShot;
        public int     fuelScored;
        public int     climbLevel;
        public boolean climbComplete;

        public double  totalDistance;
        public double  lastX, lastY;
        public boolean positionInitialized;

        public double  timeMoving;
        public double  timeShooting;
        public double  timeIntaking;
        public double  timeIdle;

        public String  autoMode;
        public String  teleopBehavior;
    }

    private final List<MatchEvent>           events    = new ArrayList<>();
    private final Map<Integer, RobotStats>   robotStats = new LinkedHashMap<>();
    private double lastSampleTime = 0.0;
    private static final double SAMPLE_INTERVAL = 0.5;
    private MatchState.MatchPhase lastPhase = MatchState.MatchPhase.PRE_MATCH;

    public void initialize(RobotState[] robots, MultiRobotManager manager) {
        events.clear();
        robotStats.clear();
        lastSampleTime = 0.0;

        for (RobotState r : robots) {
            RobotStats s = new RobotStats();
            s.robotId   = r.robotId;
            s.alliance  = r.alliance != null ? r.alliance.name() : "NONE";
            s.teamNumber = r.teamNumber;
            s.isPlayer   = r.isPlayerControlled;
            robotStats.put(r.robotId, s);
        }
    }

    public void onMatchStart(MatchState match) {
        events.add(new MatchEvent(0.0, EventType.MATCH_START, -1, "ALL", "Match started"));
    }

    public void update(RobotState[] robots, MatchState match, double dt) {
        double t = match.matchTime;

        // Phase change
        if (match.currentPhase != lastPhase) {
            events.add(new MatchEvent(t, EventType.PHASE_CHANGE, -1, "ALL",
                    "Phase: " + match.getPhaseName()));
            lastPhase = match.currentPhase;
        }

        // Sample robot positions at intervals
        if (t - lastSampleTime >= SAMPLE_INTERVAL) {
            lastSampleTime = t;
            for (RobotState r : robots) {
                RobotStats s = robotStats.get(r.robotId);
                if (s == null) continue;

                if (s.positionInitialized) {
                    s.totalDistance += Math.hypot(r.x - s.lastX, r.y - s.lastY);
                }
                s.lastX = r.x; s.lastY = r.y;
                s.positionInitialized = true;

                // Time tracking
                double speed = r.getSpeed();
                if (speed > 0.1)                           s.timeMoving   += SAMPLE_INTERVAL;
                else if (r.intakeState == RobotState.IntakeState.SHOOTING)  s.timeShooting += SAMPLE_INTERVAL;
                else if (r.intakeState == RobotState.IntakeState.INTAKING)  s.timeIntaking += SAMPLE_INTERVAL;
                else                                       s.timeIdle     += SAMPLE_INTERVAL;

                // Climb state sync
                s.climbLevel    = r.climbLevel;
                s.climbComplete = r.climbComplete;
                s.fuelScored    = r.fuelScored;
            }
        }
    }

    public void onMatchEnd(MatchState match) {
        events.add(new MatchEvent(match.matchTime, EventType.MATCH_END, -1, "ALL",
                String.format("Final — RED %d  BLUE %d", match.redTotalScore, match.blueTotalScore)));
    }

    public void recordFuelScored(int robotId, double time, String alliance, int count) {
        events.add(new MatchEvent(time, EventType.FUEL_SCORED, robotId, alliance,
                count + " FUEL scored"));
        RobotStats s = robotStats.get(robotId);
        if (s != null) s.fuelScored += count;
    }

    public void recordClimbComplete(int robotId, double time, String alliance, int level) {
        events.add(new MatchEvent(time, EventType.CLIMB_COMPLETE, robotId, alliance,
                "Climb L" + level + " complete"));
        RobotStats s = robotStats.get(robotId);
        if (s != null) { s.climbLevel = level; s.climbComplete = true; }
    }

    public String generateSummary(MatchState match) {
        StringBuilder sb = new StringBuilder();
        sb.append("=== MATCH SUMMARY ===\n");
        sb.append(String.format("RED  total: %d pts  (fuel=%d, tower=%d)\n",
                match.redTotalScore, match.redFuelScored, match.redTowerPoints));
        sb.append(String.format("BLUE total: %d pts  (fuel=%d, tower=%d)\n",
                match.blueTotalScore, match.blueFuelScored, match.blueTowerPoints));
        if (match.autoWinner != null) sb.append("Auto winner: ").append(match.autoWinner).append("\n");
        else sb.append("Auto: Tied\n");
        sb.append("\n--- ROBOT STATS ---\n");
        for (RobotStats s : robotStats.values()) {
            sb.append(String.format("  [%s T%d] fuel=%d climb=L%d dist=%.1fm moving=%.1fs\n",
                    s.alliance, s.teamNumber, s.fuelScored, s.climbLevel,
                    s.totalDistance, s.timeMoving));
        }
        return sb.toString();
    }

    public String generateJsonSummary(MatchState match) {
        StringBuilder sb = new StringBuilder();
        sb.append("{");
        sb.append("\"redScore\":").append(match.redTotalScore).append(",");
        sb.append("\"blueScore\":").append(match.blueTotalScore).append(",");
        sb.append("\"redFuel\":").append(match.redFuelScored).append(",");
        sb.append("\"blueFuel\":").append(match.blueFuelScored).append(",");
        sb.append("\"redTower\":").append(match.redTowerPoints).append(",");
        sb.append("\"blueTower\":").append(match.blueTowerPoints).append(",");
        sb.append("\"autoWinner\":\"").append(match.autoWinner != null ? match.autoWinner : "TIE").append("\",");

        sb.append("\"robots\":[");
        boolean first = true;
        for (RobotStats s : robotStats.values()) {
            if (!first) sb.append(",");
            first = false;
            sb.append("{");
            sb.append("\"id\":").append(s.robotId).append(",");
            sb.append("\"team\":").append(s.teamNumber).append(",");
            sb.append("\"alliance\":\"").append(s.alliance).append("\",");
            sb.append("\"fuelScored\":").append(s.fuelScored).append(",");
            sb.append("\"climbLevel\":").append(s.climbLevel).append(",");
            sb.append("\"climbComplete\":").append(s.climbComplete).append(",");
            sb.append("\"distance\":").append(String.format("%.2f", s.totalDistance));
            sb.append("}");
        }
        sb.append("],");

        sb.append("\"events\":[");
        first = true;
        for (MatchEvent e : events) {
            if (e.type == EventType.ROBOT_MOVEMENT) continue;
            if (!first) sb.append(",");
            first = false;
            sb.append("{\"t\":").append(String.format("%.2f", e.time))
              .append(",\"type\":\"").append(e.type)
              .append("\",\"desc\":\"").append(e.description.replace("\"", "'")).append("\"}");
        }
        sb.append("]}");
        return sb.toString();
    }

    public List<MatchEvent> getEvents() { return events; }
    public Map<Integer, RobotStats> getRobotStats() { return robotStats; }

    private String truncate(String s, int max) {
        return s.length() <= max ? s : s.substring(0, max - 3) + "...";
    }
}
