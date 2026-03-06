package team3164.simulator.engine;

import team3164.simulator.Constants;

/**
 * Autonomous controller that implements the 4 auto modes from AutoRoutines.java:
 *  0 = Do Nothing
 *  1 = Score, Collect & Climb
 *  2 = Quick Climb  (shoot preload, then drive to tower and climb)
 *  3 = Preload Only (shoot preload, hold position)
 *
 * Additional simulation-only modes (4-19) are kept for compatibility with
 * the web frontend's auto selector.
 */
public class AutonomousController {

    // ── Mode IDs matching AutoConstants ────────────────────────────────────
    public static final int AUTO_DO_NOTHING       = 0;
    public static final int AUTO_SCORE_AND_COLLECT= 1;  // legacy alias
    public static final int AUTO_QUICK_CLIMB      = 2;
    public static final int AUTO_SCORE_THEN_CLIMB = 3;  // legacy alias
    public static final int AUTO_DEPOT_RAID       = 4;
    public static final int AUTO_FAR_NEUTRAL      = 5;
    public static final int AUTO_PRELOAD_ONLY     = 6;  // legacy slot 6
    public static final int AUTO_MAX_CYCLES       = 7;
    public static final int AUTO_CLIMB_SUPPORT    = 8;
    public static final int AUTO_WIN_AUTO         = 9;
    public static final int AUTO_SCORE_COLLECT_CLIMB = 10; // Mode 1 (actual robot)
    public static final int AUTO_FAST_CLIMB       = 11;
    public static final int AUTO_BALANCED         = 12;
    public static final int AUTO_DEPOT_CLIMB      = 13;
    public static final int AUTO_MAX_POINTS       = 14;
    public static final int AUTO_SAFE_CLIMB       = 15;
    public static final int AUTO_DUAL_CYCLE       = 16;
    public static final int AUTO_DENY_FUEL        = 17;
    public static final int AUTO_CENTER_CONTROL   = 18;
    public static final int AUTO_ALLIANCE_SUPPORT = 19;
    public static final int NUM_AUTO_MODES        = 20;

    public static final String[] AUTO_MODE_NAMES = {
        "0: Do Nothing",
        "1: Score, Collect & Climb",
        "2: Quick Climb",
        "3: Preload Only",
        "4: Depot Raid",
        "5: Far Neutral",
        "6: Preload Only",
        "7: Max Cycles",
        "8: Climb Support",
        "9: Win AUTO",
        "10: Score+Collect+Climb",
        "11: Fast Climb",
        "12: Balanced",
        "13: Depot+Climb OPTIMAL",
        "14: Max Points",
        "15: Safe Climb",
        "16: Dual Cycle",
        "17: Deny FUEL",
        "18: Center Control",
        "19: Alliance Support"
    };

    public enum AutoPhase {
        IDLE, POSITIONING_TO_SHOOT, SCORING_PRELOAD, DRIVING_TO_NEUTRAL,
        INTAKING, SCORING_COLLECTED, DRIVING_TO_TOWER, CLIMBING, HOLDING,
        DRIVING_TO_DEPOT, COLLECTING_FROM_DEPOT, DRIVING_TO_SCORE,
        DRIVING_TO_FAR_NEUTRAL, INTAKING_FAR, RETURNING_TO_SCORE,
        CYCLING, RAPID_SCORING, COMPLETE
    }

    // Timing constants (seconds)
    private static final double SHOOT_TIME_PER_FUEL   = 0.25;
    private static final double DRIVE_TO_NEUTRAL_TIME = 6.0;   // ~3.5m @ 2m/s + accel
    private static final double INTAKE_TIMEOUT        = 4.0;   // AutoConstants.INTAKE_TIMEOUT
    private static final double CLIMB_TIMEOUT         = 12.0;  // AutoConstants.CLIMB_TIMEOUT
    private static final double DRIVE_TO_TOWER_TIME   = 5.0;

    // Debug
    public static boolean DEBUG_DRIVING = false;
    private static double lastDebugTime = 0.0;

    // ── Per-instance state ─────────────────────────────────────────────────
    private int     selectedMode   = AUTO_DO_NOTHING;
    private boolean selectionLocked = false;
    private int     robotId;

    private AutoPhase currentPhase  = AutoPhase.IDLE;
    private double    phaseTimer    = 0.0;
    private double    totalAutoTime = 0.0;
    private double    targetX, targetY, targetHeading;

    // ── Public API ─────────────────────────────────────────────────────────
    public void setSelectedMode(int mode) {
        if (!selectionLocked) selectedMode = Math.max(0, Math.min(NUM_AUTO_MODES - 1, mode));
    }
    public int    getSelectedMode()     { return selectedMode; }
    public String getSelectedModeName() {
        return (selectedMode < AUTO_MODE_NAMES.length) ? AUTO_MODE_NAMES[selectedMode] : "Unknown";
    }
    public void lockSelection()   { selectionLocked = true; }
    public void unlockSelection() { selectionLocked = false; }
    public boolean isLocked()     { return selectionLocked; }
    public String getCurrentPhaseName() { return currentPhase.name(); }
    public double getAutoTime()         { return totalAutoTime; }

    public void startAuto(RobotState robot) {
        robotId = robot.robotId;
        reset();
        currentPhase = AutoPhase.IDLE;
        phaseTimer   = 0.0;
        totalAutoTime = 0.0;
        lockSelection();
    }

    public void reset() {
        currentPhase   = AutoPhase.IDLE;
        phaseTimer     = 0.0;
        totalAutoTime  = 0.0;
        targetX = targetY = targetHeading = 0.0;
    }

    /** Main update — called every sim tick during autonomous. */
    public void update(RobotState robot, InputState input, double dt) {
        if (!robot.isEnabled) return;
        clearInputs(input);
        totalAutoTime += dt;
        phaseTimer    += dt;

        switch (selectedMode) {
            case AUTO_DO_NOTHING:                         break; // stay still
            case AUTO_SCORE_AND_COLLECT:                  updateScoreCollectClimb(robot, input, dt); break;
            case AUTO_QUICK_CLIMB:                        updateQuickClimb(robot, input, dt); break;
            case AUTO_SCORE_THEN_CLIMB:                   updatePreloadOnly(robot, input, dt); break;
            case AUTO_DEPOT_RAID:                         updateDepotRaid(robot, input, dt); break;
            case AUTO_FAR_NEUTRAL:                        updateFarNeutral(robot, input, dt); break;
            case AUTO_PRELOAD_ONLY:                       updatePreloadOnly(robot, input, dt); break;
            case AUTO_MAX_CYCLES:                         updateMaxCycles(robot, input, dt); break;
            case AUTO_CLIMB_SUPPORT:                      updateClimbSupport(robot, input, dt); break;
            case AUTO_WIN_AUTO:                           updateWinAuto(robot, input, dt); break;
            case AUTO_SCORE_COLLECT_CLIMB:                updateScoreCollectClimb(robot, input, dt); break;
            case AUTO_FAST_CLIMB:                         updateFastClimb(robot, input, dt); break;
            case AUTO_BALANCED:                           updateBalanced(robot, input, dt); break;
            case AUTO_DEPOT_CLIMB:                        updateDepotClimb(robot, input, dt); break;
            case AUTO_MAX_POINTS:                         updateMaxPoints(robot, input, dt); break;
            case AUTO_SAFE_CLIMB:                         updateSafeClimb(robot, input, dt); break;
            case AUTO_DUAL_CYCLE:                         updateDualCycle(robot, input, dt); break;
            case AUTO_DENY_FUEL:                          updateDenyFuel(robot, input, dt); break;
            case AUTO_CENTER_CONTROL:                     updateCenterControl(robot, input, dt); break;
            case AUTO_ALLIANCE_SUPPORT:                   updateAllianceSupport(robot, input, dt); break;
            default:                                      break;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MODE 0 — DO NOTHING (implicit — clearInputs handles it)
    // ══════════════════════════════════════════════════════════════════════

    // ══════════════════════════════════════════════════════════════════════
    // MODE 1 — SCORE & COLLECT (sim alias; real robot = mode 10)
    // Shoot preload → drive to neutral → collect 4 fuel → return → shoot
    // ══════════════════════════════════════════════════════════════════════
    private void updateScoreAndCollect(RobotState robot, InputState input, double dt) {
        switch (currentPhase) {
            case IDLE:
                robot.currentCommand = "Mode1: Spin up shooter";
                input.spinUp = true;
                input.shooterPower = 1.0;
                if (phaseTimer > Constants.Shooter.SPINUP_TIME) transitionToPhase(AutoPhase.SCORING_PRELOAD);
                break;

            case SCORING_PRELOAD:
                robot.currentCommand = "Mode1: Shoot preload";
                input.spinUp = true;
                input.shoot  = true;
                input.shooterPower = 1.0;
                double shootTime = robot.fuelCount * SHOOT_TIME_PER_FUEL + 0.3;
                if (phaseTimer > shootTime || robot.fuelCount == 0) {
                    transitionToPhase(AutoPhase.DRIVING_TO_NEUTRAL);
                    double[] neutral = getNeutralPosition(robot);
                    targetX = neutral[0]; targetY = neutral[1];
                }
                break;

            case DRIVING_TO_NEUTRAL:
                robot.currentCommand = "Mode1: Drive to neutral";
                driveToTarget(robot, input, targetX, targetY);
                input.intake = true;
                if (isAtTarget(robot, targetX, targetY, 0.5) || phaseTimer > DRIVE_TO_NEUTRAL_TIME) {
                    transitionToPhase(AutoPhase.INTAKING);
                }
                break;

            case INTAKING:
                robot.currentCommand = "Mode1: Collect fuel";
                input.intake = true;
                // Drive slowly forward while collecting
                input.forward = 0.3;
                if (robot.fuelCount >= 4 || phaseTimer > INTAKE_TIMEOUT) {
                    double[] shootPos = getShootingPosition(robot);
                    targetX = shootPos[0]; targetY = shootPos[1];
                    transitionToPhase(AutoPhase.SCORING_COLLECTED);
                }
                break;

            case SCORING_COLLECTED:
                robot.currentCommand = "Mode1: Return and shoot";
                if (!isAtTarget(robot, targetX, targetY, 0.5)) {
                    driveToTarget(robot, input, targetX, targetY);
                } else {
                    input.spinUp = true;
                    input.shoot  = true;
                    input.shooterPower = 1.0;
                    if (robot.fuelCount == 0 || phaseTimer > 5.0) {
                        transitionToPhase(AutoPhase.COMPLETE);
                    }
                }
                break;

            case COMPLETE:
                robot.currentCommand = "Mode1: Complete";
                break;
            default:
                break;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MODE 2 — QUICK CLIMB
    // Shoot preload → drive to tower → climb
    // ══════════════════════════════════════════════════════════════════════
    private void updateQuickClimb(RobotState robot, InputState input, double dt) {
        switch (currentPhase) {
            case IDLE:
                robot.currentCommand = "Mode2: Spin up";
                input.spinUp = true;
                input.shooterPower = 1.0;
                if (phaseTimer > Constants.Shooter.SPINUP_TIME) transitionToPhase(AutoPhase.SCORING_PRELOAD);
                break;

            case SCORING_PRELOAD:
                robot.currentCommand = "Mode2: Shoot preload";
                input.spinUp = true;
                input.shoot  = true;
                input.shooterPower = 1.0;
                double tShoot = robot.fuelCount * SHOOT_TIME_PER_FUEL + 0.3;
                if (phaseTimer > tShoot || robot.fuelCount == 0) {
                    setTowerTarget(robot);
                    transitionToPhase(AutoPhase.DRIVING_TO_TOWER);
                }
                break;

            case DRIVING_TO_TOWER:
                robot.currentCommand = "Mode2: Drive to tower";
                driveToTarget(robot, input, targetX, targetY);
                if (isAtTarget(robot, targetX, targetY, 1.5) || phaseTimer > DRIVE_TO_TOWER_TIME) {
                    transitionToPhase(AutoPhase.CLIMBING);
                }
                break;

            case CLIMBING:
                robot.currentCommand = "Mode2: Climbing L1";
                input.level1    = true;
                input.climberUp = true;
                if (robot.climbComplete || phaseTimer > CLIMB_TIMEOUT) {
                    transitionToPhase(AutoPhase.HOLDING);
                }
                break;

            case HOLDING:
                robot.currentCommand = "Mode2: Holding";
                break;
            default:
                break;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MODE 3 — SCORE THEN CLIMB (old mode 3)
    // Shoot preload → drive toward tower → hold
    // ══════════════════════════════════════════════════════════════════════
    private void updateScoreThenClimb(RobotState robot, InputState input, double dt) {
        switch (currentPhase) {
            case IDLE:
                robot.currentCommand = "Mode3: Spin up";
                input.spinUp = true;
                if (phaseTimer > Constants.Shooter.SPINUP_TIME) transitionToPhase(AutoPhase.SCORING_PRELOAD);
                break;

            case SCORING_PRELOAD:
                robot.currentCommand = "Mode3: Shoot preload";
                input.shoot = true; input.spinUp = true;
                if (phaseTimer > robot.fuelCount * SHOOT_TIME_PER_FUEL + 0.3 || robot.fuelCount == 0) {
                    setTowerTarget(robot);
                    transitionToPhase(AutoPhase.DRIVING_TO_TOWER);
                }
                break;

            case DRIVING_TO_TOWER:
                robot.currentCommand = "Mode3: Drive to tower";
                driveToTarget(robot, input, targetX, targetY);
                if (isAtTarget(robot, targetX, targetY, 1.8) || phaseTimer > DRIVE_TO_TOWER_TIME) {
                    transitionToPhase(AutoPhase.HOLDING);
                }
                break;

            case HOLDING:
                robot.currentCommand = "Mode3: Holding at tower";
                break;
            default:
                break;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MODE 6 — PRELOAD ONLY (simple — matches AutoRoutines.preloadOnlyAuto)
    // Shoot preload → hold position
    // ══════════════════════════════════════════════════════════════════════
    private void updatePreloadOnly(RobotState robot, InputState input, double dt) {
        switch (currentPhase) {
            case IDLE:
                robot.currentCommand = "Mode6: Spin up";
                input.spinUp = true;
                if (phaseTimer > Constants.Shooter.SPINUP_TIME) transitionToPhase(AutoPhase.SCORING_PRELOAD);
                break;

            case SCORING_PRELOAD:
                robot.currentCommand = "Mode6: Shoot preload";
                input.shoot = true; input.spinUp = true;
                if (phaseTimer > robot.fuelCount * SHOOT_TIME_PER_FUEL + 0.3 || robot.fuelCount == 0) {
                    transitionToPhase(AutoPhase.HOLDING);
                }
                break;

            case HOLDING:
                robot.currentCommand = "Mode6: Holding";
                break;
            default:
                break;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // MODE 10 — SCORE, COLLECT & CLIMB (matches real robot Mode 1)
    // Shoot preload → neutral → collect → shoot → climb
    // ══════════════════════════════════════════════════════════════════════
    private void updateScoreCollectClimb(RobotState robot, InputState input, double dt) {
        switch (currentPhase) {
            case IDLE:
                robot.currentCommand = "SCC: Spin up";
                input.spinUp = true;
                if (phaseTimer > Constants.Shooter.SPINUP_TIME) transitionToPhase(AutoPhase.SCORING_PRELOAD);
                break;

            case SCORING_PRELOAD:
                robot.currentCommand = "SCC: Shoot preload";
                input.shoot = true; input.spinUp = true; input.shooterPower = 1.0;
                if (phaseTimer > robot.fuelCount * SHOOT_TIME_PER_FUEL + 0.3 || robot.fuelCount == 0) {
                    double[] n = getNeutralPosition(robot);
                    targetX = n[0]; targetY = n[1];
                    transitionToPhase(AutoPhase.DRIVING_TO_NEUTRAL);
                }
                break;

            case DRIVING_TO_NEUTRAL:
                robot.currentCommand = "SCC: Drive to neutral";
                driveToTarget(robot, input, targetX, targetY);
                input.intake = true;
                if (isAtTarget(robot, targetX, targetY, 0.5) || phaseTimer > DRIVE_TO_NEUTRAL_TIME) {
                    transitionToPhase(AutoPhase.INTAKING);
                }
                break;

            case INTAKING:
                robot.currentCommand = "SCC: Collecting";
                input.intake = true; input.forward = 0.3;
                if (robot.fuelCount >= 4 || phaseTimer > INTAKE_TIMEOUT) {
                    double[] sp = getShootingPosition(robot);
                    targetX = sp[0]; targetY = sp[1];
                    transitionToPhase(AutoPhase.SCORING_COLLECTED);
                }
                break;

            case SCORING_COLLECTED:
                robot.currentCommand = "SCC: Return+shoot";
                if (!isAtTarget(robot, targetX, targetY, 0.5)) {
                    driveToTarget(robot, input, targetX, targetY);
                } else {
                    input.shoot = true; input.spinUp = true; input.shooterPower = 1.0;
                    if (robot.fuelCount == 0 || phaseTimer > 5.0) {
                        setTowerTarget(robot);
                        transitionToPhase(AutoPhase.DRIVING_TO_TOWER);
                    }
                }
                break;

            case DRIVING_TO_TOWER:
                robot.currentCommand = "SCC: Drive to tower";
                driveToTarget(robot, input, targetX, targetY);
                if (isAtTarget(robot, targetX, targetY, 1.5) || phaseTimer > DRIVE_TO_TOWER_TIME) {
                    transitionToPhase(AutoPhase.CLIMBING);
                }
                break;

            case CLIMBING:
                robot.currentCommand = "SCC: Climbing";
                input.level1 = true; input.climberUp = true;
                if (robot.climbComplete || phaseTimer > CLIMB_TIMEOUT) {
                    transitionToPhase(AutoPhase.HOLDING);
                }
                break;

            case HOLDING:
                robot.currentCommand = "SCC: Holding";
                break;
            default:
                break;
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // Remaining modes — simplified implementations for simulation fidelity
    // ══════════════════════════════════════════════════════════════════════

    private void updateDepotRaid(RobotState robot, InputState input, double dt) {
        // Drive to depot → collect → score
        switch (currentPhase) {
            case IDLE:
                boolean isRed = robot.alliance == MatchState.Alliance.RED;
                targetX = isRed ? Constants.Field.RED_DEPOT_X - 1.0 : Constants.Field.BLUE_DEPOT_X + 1.0;
                targetY = isRed ? Constants.Field.RED_DEPOT_Y : Constants.Field.BLUE_DEPOT_Y;
                transitionToPhase(AutoPhase.DRIVING_TO_DEPOT);
                break;
            case DRIVING_TO_DEPOT:
                robot.currentCommand = "DepotRaid: Drive to depot";
                driveToTarget(robot, input, targetX, targetY);
                input.intake = true;
                if (isAtTarget(robot, targetX, targetY, 0.8) || phaseTimer > 9.0) {
                    transitionToPhase(AutoPhase.COLLECTING_FROM_DEPOT);
                }
                break;
            case COLLECTING_FROM_DEPOT:
                robot.currentCommand = "DepotRaid: Collecting";
                input.intake = true;
                if (robot.fuelCount >= 4 || phaseTimer > 3.0) {
                    double[] sp = getShootingPosition(robot);
                    targetX = sp[0]; targetY = sp[1];
                    transitionToPhase(AutoPhase.DRIVING_TO_SCORE);
                }
                break;
            case DRIVING_TO_SCORE:
                robot.currentCommand = "DepotRaid: Drive to score";
                driveToTarget(robot, input, targetX, targetY);
                if (isAtTarget(robot, targetX, targetY, 0.5) || phaseTimer > 8.0) {
                    transitionToPhase(AutoPhase.SCORING_COLLECTED);
                }
                break;
            case SCORING_COLLECTED:
                robot.currentCommand = "DepotRaid: Shoot";
                input.shoot = true; input.spinUp = true;
                if (robot.fuelCount == 0 || phaseTimer > 5.0) transitionToPhase(AutoPhase.COMPLETE);
                break;
            case COMPLETE:
                break;
            default:
                break;
        }
    }

    private void updateFarNeutral(RobotState robot, InputState input, double dt) {
        switch (currentPhase) {
            case IDLE:
                input.spinUp = true;
                if (phaseTimer > Constants.Shooter.SPINUP_TIME) transitionToPhase(AutoPhase.SCORING_PRELOAD);
                break;
            case SCORING_PRELOAD:
                robot.currentCommand = "FarNeutral: Shoot preload";
                input.shoot = true; input.spinUp = true;
                if (phaseTimer > robot.fuelCount * SHOOT_TIME_PER_FUEL + 0.3 || robot.fuelCount == 0) {
                    boolean isRed = robot.alliance == MatchState.Alliance.RED;
                    targetX = Constants.Field.CENTER_X + (isRed ? -Constants.Field.NEUTRAL_FAR_OFFSET : Constants.Field.NEUTRAL_FAR_OFFSET);
                    targetY = Constants.Field.CENTER_Y;
                    transitionToPhase(AutoPhase.DRIVING_TO_FAR_NEUTRAL);
                }
                break;
            case DRIVING_TO_FAR_NEUTRAL:
                robot.currentCommand = "FarNeutral: Drive far";
                driveToTarget(robot, input, targetX, targetY);
                input.intake = true;
                if (isAtTarget(robot, targetX, targetY, 0.6) || phaseTimer > 9.0) {
                    transitionToPhase(AutoPhase.INTAKING_FAR);
                }
                break;
            case INTAKING_FAR:
                robot.currentCommand = "FarNeutral: Intake";
                input.intake = true;
                if (robot.fuelCount >= 3 || phaseTimer > INTAKE_TIMEOUT) {
                    double[] sp = getShootingPosition(robot);
                    targetX = sp[0]; targetY = sp[1];
                    transitionToPhase(AutoPhase.RETURNING_TO_SCORE);
                }
                break;
            case RETURNING_TO_SCORE:
                robot.currentCommand = "FarNeutral: Return";
                driveToTarget(robot, input, targetX, targetY);
                if (isAtTarget(robot, targetX, targetY, 0.5) || phaseTimer > 8.0) {
                    transitionToPhase(AutoPhase.SCORING_COLLECTED);
                }
                break;
            case SCORING_COLLECTED:
                robot.currentCommand = "FarNeutral: Shoot";
                input.shoot = true; input.spinUp = true;
                if (robot.fuelCount == 0 || phaseTimer > 5.0) transitionToPhase(AutoPhase.COMPLETE);
                break;
            case COMPLETE:
                break;
            default:
                break;
        }
    }

    private void updateMaxCycles(RobotState robot, InputState input, double dt) {
        switch (currentPhase) {
            case IDLE:
                input.spinUp = true;
                if (phaseTimer > Constants.Shooter.SPINUP_TIME) transitionToPhase(AutoPhase.SCORING_PRELOAD);
                break;
            case SCORING_PRELOAD:
                robot.currentCommand = "MaxCycles: Shoot preload";
                input.shoot = true; input.spinUp = true;
                if (phaseTimer > robot.fuelCount * SHOOT_TIME_PER_FUEL + 0.2 || robot.fuelCount == 0) {
                    double[] n = getNeutralPosition(robot);
                    targetX = n[0]; targetY = n[1];
                    transitionToPhase(AutoPhase.CYCLING);
                }
                break;
            case CYCLING:
                robot.currentCommand = "MaxCycles: Cycle";
                if (!isAtTarget(robot, targetX, targetY, 0.5)) {
                    driveToTarget(robot, input, targetX, targetY);
                    input.intake = true;
                } else {
                    input.intake = true; input.forward = 0.3;
                }
                if (robot.fuelCount >= 3) {
                    double[] sp = getShootingPosition(robot);
                    targetX = sp[0]; targetY = sp[1];
                    transitionToPhase(AutoPhase.RAPID_SCORING);
                } else if (phaseTimer > 12.0) {
                    double[] sp = getShootingPosition(robot);
                    targetX = sp[0]; targetY = sp[1];
                    transitionToPhase(AutoPhase.SCORING_COLLECTED);
                }
                break;
            case RAPID_SCORING:
                robot.currentCommand = "MaxCycles: Rapid shoot";
                if (!isAtTarget(robot, targetX, targetY, 0.5)) {
                    driveToTarget(robot, input, targetX, targetY);
                } else {
                    input.shoot = true; input.spinUp = true;
                    if (robot.fuelCount == 0 || phaseTimer > 4.0) {
                        double[] n = getNeutralPosition(robot);
                        targetX = n[0]; targetY = n[1];
                        transitionToPhase(AutoPhase.CYCLING);
                    }
                }
                break;
            case SCORING_COLLECTED:
                robot.currentCommand = "MaxCycles: Final shoot";
                if (!isAtTarget(robot, targetX, targetY, 0.5)) driveToTarget(robot, input, targetX, targetY);
                else { input.shoot = true; input.spinUp = true; }
                if (robot.fuelCount == 0 || phaseTimer > 5.0) transitionToPhase(AutoPhase.COMPLETE);
                break;
            case COMPLETE:
                break;
            default:
                break;
        }
    }

    private void updateClimbSupport(RobotState robot, InputState input, double dt) {
        // Position near tower to assist alliance partners
        switch (currentPhase) {
            case IDLE:
                setTowerTarget(robot);
                // Offset slightly from tower
                boolean isRed = robot.alliance == MatchState.Alliance.RED;
                targetX += isRed ? -1.5 : 1.5;
                transitionToPhase(AutoPhase.DRIVING_TO_TOWER);
                break;
            case DRIVING_TO_TOWER:
                robot.currentCommand = "ClimbSupport: Position";
                driveToTarget(robot, input, targetX, targetY);
                if (isAtTarget(robot, targetX, targetY, 0.5) || phaseTimer > 8.0) {
                    transitionToPhase(AutoPhase.HOLDING);
                }
                break;
            case HOLDING:
                robot.currentCommand = "ClimbSupport: Ready";
                break;
            default:
                break;
        }
    }

    private void updateWinAuto(RobotState robot, InputState input, double dt) {
        updateScoreAndCollect(robot, input, dt); // same logic
    }

    private void updateFastClimb(RobotState robot, InputState input, double dt) {
        updateQuickClimb(robot, input, dt); // alias
    }

    private void updateBalanced(RobotState robot, InputState input, double dt) {
        updateScoreCollectClimb(robot, input, dt);
    }

    private void updateDepotClimb(RobotState robot, InputState input, double dt) {
        // Depot + climb combo
        switch (currentPhase) {
            case IDLE:
                boolean isRed = robot.alliance == MatchState.Alliance.RED;
                targetX = isRed ? Constants.Field.RED_DEPOT_X - 1.0 : Constants.Field.BLUE_DEPOT_X + 1.0;
                targetY = isRed ? Constants.Field.RED_DEPOT_Y : Constants.Field.BLUE_DEPOT_Y;
                transitionToPhase(AutoPhase.DRIVING_TO_DEPOT);
                break;
            case DRIVING_TO_DEPOT:
                robot.currentCommand = "DepotClimb: Drive to depot";
                driveToTarget(robot, input, targetX, targetY);
                input.intake = true;
                if (isAtTarget(robot, targetX, targetY, 0.8) || phaseTimer > 6.0) {
                    transitionToPhase(AutoPhase.COLLECTING_FROM_DEPOT);
                }
                break;
            case COLLECTING_FROM_DEPOT:
                robot.currentCommand = "DepotClimb: Collect";
                input.intake = true;
                if (robot.fuelCount >= 3 || phaseTimer > 2.0) {
                    double[] sp = getShootingPosition(robot);
                    targetX = sp[0]; targetY = sp[1];
                    transitionToPhase(AutoPhase.SCORING_COLLECTED);
                }
                break;
            case SCORING_COLLECTED:
                robot.currentCommand = "DepotClimb: Shoot";
                if (!isAtTarget(robot, targetX, targetY, 0.5)) driveToTarget(robot, input, targetX, targetY);
                else {
                    input.shoot = true; input.spinUp = true;
                    if (robot.fuelCount == 0 || phaseTimer > 5.0) {
                        setTowerTarget(robot);
                        transitionToPhase(AutoPhase.DRIVING_TO_TOWER);
                    }
                }
                break;
            case DRIVING_TO_TOWER:
                robot.currentCommand = "DepotClimb: Drive tower";
                driveToTarget(robot, input, targetX, targetY);
                if (isAtTarget(robot, targetX, targetY, 1.5) || phaseTimer > DRIVE_TO_TOWER_TIME) {
                    transitionToPhase(AutoPhase.CLIMBING);
                }
                break;
            case CLIMBING:
                robot.currentCommand = "DepotClimb: Climb";
                input.level1 = true; input.climberUp = true;
                if (robot.climbComplete || phaseTimer > CLIMB_TIMEOUT) transitionToPhase(AutoPhase.HOLDING);
                break;
            case HOLDING: break;
            default: break;
        }
    }

    private void updateMaxPoints(RobotState robot, InputState input, double dt) {
        updateScoreCollectClimb(robot, input, dt);
    }

    private void updateSafeClimb(RobotState robot, InputState input, double dt) {
        updateScoreThenClimb(robot, input, dt);
    }

    private void updateDualCycle(RobotState robot, InputState input, double dt) {
        updateMaxCycles(robot, input, dt);
    }

    private void updateDenyFuel(RobotState robot, InputState input, double dt) {
        // Drive to center line to deny neutral FUEL
        switch (currentPhase) {
            case IDLE:
                targetX = Constants.Field.CENTER_X;
                targetY = Constants.Field.CENTER_Y;
                transitionToPhase(AutoPhase.DRIVING_TO_NEUTRAL);
                break;
            case DRIVING_TO_NEUTRAL:
                robot.currentCommand = "DenyFuel: Block center";
                driveToTarget(robot, input, targetX, targetY);
                input.intake = true;
                if (isAtTarget(robot, targetX, targetY, 0.5) || phaseTimer > 6.0) {
                    transitionToPhase(AutoPhase.HOLDING);
                }
                break;
            case HOLDING:
                robot.currentCommand = "DenyFuel: Holding center";
                break;
            default: break;
        }
    }

    private void updateCenterControl(RobotState robot, InputState input, double dt) {
        updateDenyFuel(robot, input, dt);
    }

    private void updateAllianceSupport(RobotState robot, InputState input, double dt) {
        updateClimbSupport(robot, input, dt);
    }

    // ── Phase transition ────────────────────────────────────────────────────
    private void transitionToPhase(AutoPhase next) {
        currentPhase = next;
        phaseTimer   = 0.0;
    }

    // ── Input helpers ───────────────────────────────────────────────────────
    private void clearInputs(InputState input) {
        input.forward = 0; input.strafe = 0; input.turn = 0;
        input.shoot = false; input.intake = false; input.spinUp = false;
        input.climberUp = false; input.climberDown = false;
        input.level1 = false; input.level2 = false; input.level3 = false;
        input.shooterPower = 0;
    }

    // ── Navigation helpers ──────────────────────────────────────────────────
    private void driveToTarget(RobotState robot, InputState input, double tx, double ty) {
        driveTowardTarget(robot, input, tx, ty, 1.0);
    }

    private void driveTowardTarget(RobotState robot, InputState input, double tx, double ty, double speedFrac) {
        double dx = tx - robot.x;
        double dy = ty - robot.y;
        double dist = Math.hypot(dx, dy);
        if (dist < 0.05) return;

        // Speed profile: slow down when close
        double speed = Math.min(speedFrac, dist / 0.8);
        speed = Math.max(0.1, speed);

        // Field-space unit vector toward target
        double nx = dx / dist;
        double ny = dy / dist;

        // Project field direction into robot frame (inverse rotate by heading)
        // SwervePhysics uses field-relative: cmdVx = (fwd*cos - str*sin)*MAX_SPEED
        // We want cmdVx = nx*speed*MAX_SPEED, cmdVy = ny*speed*MAX_SPEED
        // So set fwd/str so that after field-relative rotation we get the right field velocity:
        //   fwd = nx*speed*cos(heading) + ny*speed*sin(heading)  ... wait, inverse rotation:
        // R(heading) * [fwd; str] = [nx*speed; ny*speed]
        // cos*fwd - sin*str = nx*speed  (where cos=cos(-h)=cos(h), sin=sin(-h)=-sin(h))
        // Actually SwervePhysics uses cos(-heading), sin(-heading):
        // cos(-h)*fwd - sin(-h)*str = nx*speed  =>  cos(h)*fwd + sin(h)*str = nx*speed
        // sin(-h)*fwd + cos(-h)*str = ny*speed  => -sin(h)*fwd + cos(h)*str = ny*speed
        // Solve: fwd = nx*speed*cos(h) - ny*speed*sin(h), str = nx*speed*sin(h) + ny*speed*cos(h)
        double cosH = Math.cos(robot.heading);
        double sinH = Math.sin(robot.heading);
        input.forward = nx * speed * cosH - ny * speed * sinH;
        input.strafe  = nx * speed * sinH + ny * speed * cosH;

        // Don't rotate the robot (let it strafe freely); just a slight correction
        // to face the target for shooting
        double targetAngle = Math.atan2(dy, dx);
        double headingError = normalizeAngle(targetAngle - robot.heading);
        // Small turn correction to avoid interfering with strafe
        input.turn = Math.max(-0.3, Math.min(0.3, headingError));
    }

    private boolean isAtTarget(RobotState robot, double tx, double ty, double tol) {
        return Math.hypot(robot.x - tx, robot.y - ty) < tol;
    }

    private double normalizeAngle(double angle) {
        while (angle >  Math.PI) angle -= 2 * Math.PI;
        while (angle < -Math.PI) angle += 2 * Math.PI;
        return angle;
    }

    private boolean isNearBump(double x, double y, MatchState.Alliance alliance) {
        if (alliance == MatchState.Alliance.RED) {
            return Math.hypot(x - Constants.Field.RED_BUMP_1_X, y - Constants.Field.RED_BUMP_1_Y) < 1.0
                || Math.hypot(x - Constants.Field.RED_BUMP_2_X, y - Constants.Field.RED_BUMP_2_Y) < 1.0;
        } else {
            return Math.hypot(x - Constants.Field.BLUE_BUMP_1_X, y - Constants.Field.BLUE_BUMP_1_Y) < 1.0
                || Math.hypot(x - Constants.Field.BLUE_BUMP_2_X, y - Constants.Field.BLUE_BUMP_2_Y) < 1.0;
        }
    }

    private double getSafeYPosition(RobotState robot, boolean avoidHigh) {
        double y = robot.y;
        // Prefer centre or low to avoid bumps
        if (Math.abs(y - Constants.Field.CENTER_Y) < 0.5) return y;
        return Constants.Field.CENTER_Y;
    }

    private void driveToPositionAvoidingBumps(RobotState robot, InputState input, double tx, double ty) {
        if (isNearBump(robot.x, robot.y, robot.alliance)) {
            // Detour: move to safe Y first
            double safeY = Constants.Field.CENTER_Y;
            if (!isAtTarget(robot, robot.x, safeY, 0.3)) {
                driveToTarget(robot, input, robot.x, safeY);
                return;
            }
        }
        driveToTarget(robot, input, tx, ty);
    }

    private boolean isInShootingPosition(RobotState robot) {
        double[] sp = getShootingPosition(robot);
        return isAtTarget(robot, sp[0], sp[1], 0.5);
    }

    private double[] getShootingPosition(RobotState robot) {
        boolean isRed = (robot.alliance == MatchState.Alliance.RED);
        double x = isRed ? Constants.Field.LENGTH - 2.5 : 2.5;
        return new double[]{ x, Constants.Field.CENTER_Y };
    }

    private double[] getNeutralPosition(RobotState robot) {
        boolean isRed = (robot.alliance == MatchState.Alliance.RED);
        // Each alliance drives to the neutral FUEL on their side of the field.
        // FUEL clusters are at CENTER_X ± ~1-2 m. Blue side is center-2=6.27m, Red side is center+2=10.27m.
        double x = Constants.Field.CENTER_X + (isRed ? Constants.Field.NEUTRAL_CLOSE_OFFSET : -Constants.Field.NEUTRAL_CLOSE_OFFSET);
        return new double[]{ x, Constants.Field.CENTER_Y };
    }

    private double[] calculateShotParams(RobotState robot) {
        double[] hub = getHubPosition(robot.alliance);
        double dx = hub[0] - robot.x;
        double dy = hub[1] - robot.y;
        double dist  = Math.hypot(dx, dy);
        double angle = 45.0; // fixed for sim
        return new double[]{ angle, 15.0 }; // angle, velocity
    }

    private boolean aimAtHub(RobotState robot, InputState input) {
        double[] hub = getHubPosition(robot.alliance);
        double targetAngle = Math.atan2(hub[1] - robot.y, hub[0] - robot.x);
        double err = normalizeAngle(targetAngle - robot.heading);
        input.turn = Math.max(-1, Math.min(1, err * 3.0));
        return Math.abs(err) < Math.toRadians(3.0);
    }

    private double[] getHubPosition(MatchState.Alliance alliance) {
        if (alliance == MatchState.Alliance.RED)
            return new double[]{ Constants.Field.RED_HUB_X, Constants.Field.RED_HUB_Y };
        return new double[]{ Constants.Field.BLUE_HUB_X, Constants.Field.BLUE_HUB_Y };
    }

    private void setTowerTarget(RobotState robot) {
        boolean isRed = (robot.alliance == MatchState.Alliance.RED);
        targetX = isRed ? Constants.Field.RED_TOWER_X  - 1.0 : Constants.Field.BLUE_TOWER_X + 1.0;
        targetY = isRed ? Constants.Field.RED_TOWER_Y       : Constants.Field.BLUE_TOWER_Y;
    }
}
