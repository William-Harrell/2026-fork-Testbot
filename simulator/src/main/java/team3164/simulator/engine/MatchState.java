package team3164.simulator.engine;

import team3164.simulator.Constants;

/**
 * Tracks full match state including scoring, hub active/inactive cycle,
 * climb levels, and ranking points.
 */
public class MatchState {

    // ── Enums ──────────────────────────────────────────────────────────────
    public enum Alliance { RED, BLUE }

    public enum HubStatus { ACTIVE, INACTIVE }

    public enum MatchPhase {
        PRE_MATCH, AUTO, TRANSITION,
        SHIFT_1, SHIFT_2, SHIFT_3, SHIFT_4,
        END_GAME, POST_MATCH
    }

    // ── Fields ─────────────────────────────────────────────────────────────
    public double       matchTime;
    public MatchPhase   currentPhase;
    public boolean      matchStarted;
    public boolean      matchEnded;

    /** FMS hub active/inactive status per alliance */
    public HubStatus redHubStatus;
    public HubStatus blueHubStatus;

    /** Which alliance won autonomous (null = tie) */
    public Alliance autoWinner;

    // Scores
    public int redAutoScore;
    public int blueAutoScore;
    public int redFuelScored;
    public int blueFuelScored;
    public int redFuelInHub;
    public int blueFuelInHub;
    public int redTowerPoints;
    public int blueTowerPoints;
    public int redTotalScore;
    public int blueTotalScore;

    // Ranking points
    public boolean redEnergized;
    public boolean redTraversal;
    public boolean blueEnergized;
    public boolean blueTraversal;

    // Climb levels for each robot (3 per alliance)
    public int[] redRobotClimbLevel;
    public int[] blueRobotClimbLevel;

    // Human player / outpost
    public int     redChuteCount;
    public int     blueChuteCount;
    public int     redCorralCount;
    public int     blueCorralCount;
    public boolean redChuteOpen;
    public boolean blueChuteOpen;

    // ── Constructor / reset ────────────────────────────────────────────────
    public MatchState() {
        redRobotClimbLevel  = new int[3];
        blueRobotClimbLevel = new int[3];
        reset();
    }

    public void reset() {
        matchTime    = 0.0;
        currentPhase = MatchPhase.PRE_MATCH;
        matchStarted = false;
        matchEnded   = false;

        redHubStatus  = HubStatus.ACTIVE;
        blueHubStatus = HubStatus.INACTIVE;
        autoWinner    = null;

        redAutoScore    = 0;
        blueAutoScore   = 0;
        redFuelScored   = 0;
        blueFuelScored  = 0;
        redFuelInHub    = 0;
        blueFuelInHub   = 0;
        redTowerPoints  = 0;
        blueTowerPoints = 0;
        redTotalScore   = 0;
        blueTotalScore  = 0;

        redEnergized  = false;
        redTraversal  = false;
        blueEnergized = false;
        blueTraversal = false;

        for (int i = 0; i < 3; i++) {
            redRobotClimbLevel[i]  = 0;
            blueRobotClimbLevel[i] = 0;
        }

        redChuteCount  = Constants.Fuel.CHUTE_START_COUNT;
        blueChuteCount = Constants.Fuel.CHUTE_START_COUNT;
        redCorralCount  = 0;
        blueCorralCount = 0;
        redChuteOpen  = false;
        blueChuteOpen = false;
    }

    public void startMatch() {
        matchStarted = true;
        matchEnded   = false;
        matchTime    = 0.0;
        currentPhase = MatchPhase.AUTO;

        // Red hub starts active, blue inactive — FMS alternates each shift
        redHubStatus  = HubStatus.ACTIVE;
        blueHubStatus = HubStatus.INACTIVE;
    }

    /** Called every sim tick to advance match time and update phase. */
    public void updatePhase() {
        if (!matchStarted || matchEnded) return;

        MatchPhase prev = currentPhase;

        if      (matchTime < Constants.Match.AUTO_END)       currentPhase = MatchPhase.AUTO;
        else if (matchTime < Constants.Match.TRANSITION_END) currentPhase = MatchPhase.TRANSITION;
        else if (matchTime < Constants.Match.SHIFT_1_END)    currentPhase = MatchPhase.SHIFT_1;
        else if (matchTime < Constants.Match.SHIFT_2_END)    currentPhase = MatchPhase.SHIFT_2;
        else if (matchTime < Constants.Match.SHIFT_3_END)    currentPhase = MatchPhase.SHIFT_3;
        else if (matchTime < Constants.Match.SHIFT_4_END)    currentPhase = MatchPhase.SHIFT_4;
        else if (matchTime < Constants.Match.TOTAL_DURATION) currentPhase = MatchPhase.END_GAME;
        else {
            currentPhase = MatchPhase.POST_MATCH;
            matchEnded   = true;
        }

        if (prev != currentPhase) {
            // Auto ended — record auto scores and determine auto winner
            if (prev == MatchPhase.AUTO) {
                redAutoScore  = redTotalScore;
                blueAutoScore = blueTotalScore;
                determineAutoWinner();
            }
            updateHubStatus();
        }
    }

    private void determineAutoWinner() {
        if      (redAutoScore  > blueAutoScore) autoWinner = Alliance.RED;
        else if (blueAutoScore > redAutoScore)  autoWinner = Alliance.BLUE;
        else                                    autoWinner = null;
    }

    /**
     * FMS alternates which hub is active each shift.
     * Red starts active; each new shift flips both.
     */
    private void updateHubStatus() {
        switch (currentPhase) {
            case SHIFT_1:
                // First teleop shift — flip from auto state
                redHubStatus  = HubStatus.INACTIVE;
                blueHubStatus = HubStatus.ACTIVE;
                break;
            case SHIFT_2:
                redHubStatus  = HubStatus.ACTIVE;
                blueHubStatus = HubStatus.INACTIVE;
                break;
            case SHIFT_3:
                redHubStatus  = HubStatus.INACTIVE;
                blueHubStatus = HubStatus.ACTIVE;
                break;
            case SHIFT_4:
            case END_GAME:
                redHubStatus  = HubStatus.ACTIVE;
                blueHubStatus = HubStatus.INACTIVE;
                break;
            default:
                break;
        }
    }

    /**
     * Score FUEL into a hub. Only scores if the hub is ACTIVE.
     * @param alliance  Which alliance scored the FUEL.
     * @param count     Number of FUEL cells to score.
     * @return          Points awarded (0 if hub inactive).
     */
    public int scoreFuel(Alliance alliance, int count) {
        if (count <= 0) return 0;
        if (!isHubActive(alliance)) return 0;

        int pts = count * Constants.Scoring.FUEL_ACTIVE_HUB;
        if (alliance == Alliance.RED) {
            redFuelScored += count;
            redFuelInHub  += count;
            redTotalScore  += pts;
        } else {
            blueFuelScored += count;
            blueFuelInHub  += count;
            blueTotalScore += pts;
        }
        updateRankingPoints();
        return pts;
    }

    /**
     * Record a tower climb for one robot.
     * @param alliance  Alliance of the climbing robot.
     * @param robotIdx  Robot index (0-2).
     * @param level     Climb level achieved (1, 2, or 3).
     * @return          Points awarded.
     */
    public int scoreTowerClimb(Alliance alliance, int robotIdx, int level) {
        int pts = getPointsForLevel(level);
        if (alliance == Alliance.RED) {
            redRobotClimbLevel[robotIdx] = level;
            redTowerPoints  += pts;
            redTotalScore   += pts;
        } else {
            blueRobotClimbLevel[robotIdx] = level;
            blueTowerPoints  += pts;
            blueTotalScore   += pts;
        }
        updateRankingPoints();
        return pts;
    }

    private int getPointsForLevel(int level) {
        boolean inAuto = (currentPhase == MatchPhase.AUTO);
        if (inAuto) {
            return level >= 1 ? Constants.Scoring.TOWER_L1_AUTO : 0;
        }
        switch (level) {
            case 1: return Constants.Scoring.TOWER_L1_TELEOP;
            case 2: return Constants.Scoring.TOWER_L2_TELEOP;
            case 3: return Constants.Scoring.TOWER_L3_TELEOP;
            default: return 0;
        }
    }

    private void updateRankingPoints() {
        redEnergized  = redFuelScored   >= Constants.Scoring.ENERGIZED_THRESHOLD;
        blueEnergized = blueFuelScored  >= Constants.Scoring.ENERGIZED_THRESHOLD;
        redTraversal  = redTowerPoints  >= Constants.Scoring.TRAVERSAL_THRESHOLD;
        blueTraversal = blueTowerPoints >= Constants.Scoring.TRAVERSAL_THRESHOLD;
    }

    /** Returns true if the given alliance's hub is currently ACTIVE.
     *  During AUTO, both hubs accept fuel (scoring is in-zone shots only). */
    public boolean isHubActive(Alliance alliance) {
        // In auto, both hubs are active
        if (currentPhase == MatchPhase.AUTO || currentPhase == MatchPhase.PRE_MATCH) return true;
        HubStatus s = (alliance == Alliance.RED) ? redHubStatus : blueHubStatus;
        return s == HubStatus.ACTIVE;
    }

    public double getRemainingTime() {
        return Math.max(0.0, Constants.Match.TOTAL_DURATION - matchTime);
    }

    public boolean isEndGame() {
        return matchTime >= Constants.Match.END_GAME_START && !matchEnded;
    }

    public boolean isAuto() {
        return currentPhase == MatchPhase.AUTO;
    }

    public String getFormattedTime() {
        int remaining = (int) Math.ceil(getRemainingTime());
        return String.format("%d:%02d", remaining / 60, remaining % 60);
    }

    public String getPhaseName() {
        switch (currentPhase) {
            case PRE_MATCH:  return "Pre-Match";
            case AUTO:       return "Autonomous";
            case TRANSITION: return "Transition";
            case SHIFT_1:    return "Shift 1";
            case SHIFT_2:    return "Shift 2";
            case SHIFT_3:    return "Shift 3";
            case SHIFT_4:    return "Shift 4";
            case END_GAME:   return "End Game";
            case POST_MATCH: return "Post-Match";
            default:         return "Unknown";
        }
    }
}
