package team3164.simulator.engine;

import team3164.simulator.Constants;

/**
 * Mutable snapshot of one robot's full state.
 * Shared between the physics layer and the AI/autonomous controller.
 */
public class RobotState {

    public enum IntakeState { IDLE, INTAKING, TRANSFERRING, READY_TO_SHOOT, SHOOTING }

    // ── Identity ───────────────────────────────────────────────────────────
    public int                 robotId;
    public int                 teamNumber;
    public boolean             isPlayerControlled;
    public MatchState.Alliance alliance;

    // ── Drive pose ─────────────────────────────────────────────────────────
    public double x;
    public double y;
    public double heading;   // radians, 0 = field-positive-x
    public double vx;
    public double vy;
    public double omega;

    // ── Swerve module states ───────────────────────────────────────────────
    public double[] moduleAngles = new double[4];
    public double[] moduleSpeeds = new double[4];

    // ── Shooter ───────────────────────────────────────────────────────────
    public double  shooterAngle;       // degrees, current
    public double  shooterAngleGoal;
    public double  shooterVelocity;    // m/s exit speed
    public double  shooterVelocityGoal;
    public boolean shooterAtAngle;
    public boolean shooterAtSpeed;
    public boolean shooterSpinningUp;

    // ── Intake / FUEL ─────────────────────────────────────────────────────
    public IntakeState intakeState;
    public int         fuelCount;
    public double      intakeTimer;

    // ── Climber ───────────────────────────────────────────────────────────
    public double  climberPosition;  // rotations
    public double  climberVelocity;
    public int     climbLevel;       // 0 = none, 1/2/3
    public boolean isClimbing;
    public boolean climbComplete;
    public double  robotHeight;

    // ── Misc flags ────────────────────────────────────────────────────────
    public boolean trenchMode;
    public boolean onBump;
    public boolean fieldRelative;
    public boolean slowMode;
    public String  currentCommand;

    // ── Per-robot match stats (accumulated during a match) ─────────────────
    public int     fuelScored;
    public int     towerPoints;
    public boolean isEnabled;

    // ── Constructor / reset ────────────────────────────────────────────────
    public RobotState() {
        reset();
    }

    public void reset() {
        x = 0; y = 0; heading = 0;
        vx = 0; vy = 0; omega = 0;
        moduleAngles = new double[4];
        moduleSpeeds = new double[4];

        shooterAngle        = 45.0;
        shooterAngleGoal    = 45.0;
        shooterVelocity     = 0.0;
        shooterVelocityGoal = 0.0;
        shooterAtAngle      = true;
        shooterAtSpeed      = false;
        shooterSpinningUp   = false;

        intakeState  = IntakeState.IDLE;
        fuelCount    = 0;
        intakeTimer  = 0.0;

        climberPosition = 0.0;
        climberVelocity = 0.0;
        climbLevel      = 0;
        isClimbing      = false;
        climbComplete   = false;
        robotHeight     = 0.0;

        trenchMode    = false;
        onBump        = false;
        fieldRelative = true;
        slowMode      = false;
        currentCommand = "Idle";

        fuelScored  = 0;
        towerPoints = 0;
        isEnabled   = false;
    }

    /** Convenience init called when a robot is set up at match start. */
    public void initialize(int id, int team, MatchState.Alliance alliance, int startPos, boolean playerControlled) {
        reset();
        this.robotId             = id;
        this.teamNumber          = team;
        this.alliance            = alliance;
        this.isPlayerControlled  = playerControlled;
        this.isEnabled           = true;
        this.fuelCount           = Constants.Fuel.PRELOAD_PER_ROBOT;
        this.intakeState         = IntakeState.READY_TO_SHOOT; // preload loaded
        setStartingPosition(startPos);
    }

    public void setAlliance(MatchState.Alliance alliance) {
        this.alliance = alliance;
    }

    public void setStartingPosition(int startPos) {
        boolean isRed = (alliance == MatchState.Alliance.RED);
        double wallX = isRed ? Constants.Field.LENGTH - 0.5 : 0.5;

        // Three starting positions per alliance (Y offsets)
        double[] yPositions = { Constants.Field.CENTER_Y - 2.0, Constants.Field.CENTER_Y, Constants.Field.CENTER_Y + 2.0 };
        int idx = Math.max(0, Math.min(2, startPos));

        this.x       = wallX;
        this.y       = yPositions[idx];
        this.heading = isRed ? Math.PI : 0.0; // face inward
    }

    // ── Fuel helpers ───────────────────────────────────────────────────────
    public boolean canIntakeFuel() {
        return fuelCount < Constants.Intake.MAX_CAPACITY && intakeState == IntakeState.IDLE;
    }

    public boolean hasFuelToShoot() {
        return fuelCount > 0;
    }

    public boolean isReadyToShoot() {
        return hasFuelToShoot() && shooterAtAngle && shooterAtSpeed;
    }

    public boolean addFuel() {
        if (fuelCount >= Constants.Intake.MAX_CAPACITY) return false;
        fuelCount++;
        return true;
    }

    public boolean removeFuel() {
        if (fuelCount <= 0) return false;
        fuelCount--;
        return true;
    }

    public void recordFuelScored() {
        fuelScored++;
    }

    // ── Convenience getters ────────────────────────────────────────────────
    public double getShooterAngleRadians() {
        return Math.toRadians(shooterAngle);
    }

    public double getHeadingDegrees() {
        return Math.toDegrees(heading);
    }

    public double getSpeed() {
        return Math.hypot(vx, vy);
    }

    public boolean canPassTrench() {
        return trenchMode && robotHeight <= Constants.Robot.TRENCH_CONFIG_HEIGHT;
    }

    public String getClimbLevelString() {
        if (!climbComplete) return "None";
        switch (climbLevel) {
            case 1: return "L1";
            case 2: return "L2";
            case 3: return "L3";
            default: return "None";
        }
    }

    public double getTargetClimbHeight() {
        switch (climbLevel) {
            case 1: return Constants.Field.LOW_RUNG_HEIGHT;
            case 2: return Constants.Field.MID_RUNG_HEIGHT;
            case 3: return Constants.Field.HIGH_RUNG_HEIGHT;
            default: return 0.0;
        }
    }
}
