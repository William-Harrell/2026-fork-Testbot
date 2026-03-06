package team3164.simulator.engine;

/** Represents one frame of control input (player keyboard or AI). */
public class InputState {

    // Drive
    public double  forward;
    public double  strafe;
    public double  turn;

    // Shooter
    public double  shooterAngle;
    public double  shooterPower;
    public boolean intake;
    public boolean shoot;
    public boolean spinUp;

    // Climber
    public boolean climberUp;
    public boolean climberDown;
    public boolean level1;
    public boolean level2;
    public boolean level3;

    // Mode toggles
    public boolean toggleTrenchMode;
    public boolean toggleSpeed;
    public boolean toggleFieldRel;
    public boolean resetGyro;
    public boolean skiStop;
    public boolean resetRobot;

    // Human player (outpost)
    public boolean redChuteRelease;
    public boolean blueChuteRelease;
    public boolean redCorralTransfer;
    public boolean blueCorralTransfer;

    // Match control
    public boolean startMatch;
    public boolean pauseMatch;

    public InputState() {
        reset();
    }

    public void reset() {
        forward  = 0; strafe   = 0; turn     = 0;
        shooterAngle = 0; shooterPower = 0;
        intake       = false; shoot    = false; spinUp  = false;
        climberUp    = false; climberDown = false;
        level1       = false; level2   = false; level3  = false;
        toggleTrenchMode = false; toggleSpeed = false;
        toggleFieldRel   = false; resetGyro   = false;
        skiStop          = false; resetRobot  = false;
        redChuteRelease  = false; blueChuteRelease  = false;
        redCorralTransfer = false; blueCorralTransfer = false;
        startMatch = false; pauseMatch = false;
    }

    public int getRequestedClimbLevel() {
        if (level3) return 3;
        if (level2) return 2;
        if (level1) return 1;
        return 0;
    }

    public boolean hasDriveInput() {
        return Math.abs(forward) > 0.05 || Math.abs(strafe) > 0.05 || Math.abs(turn) > 0.05;
    }

    public boolean hasShooterInput() {
        return shoot || intake || spinUp || Math.abs(shooterAngle) > 0.01 || Math.abs(shooterPower) > 0.01;
    }

    public boolean hasHPInput() {
        return redChuteRelease || blueChuteRelease || redCorralTransfer || blueCorralTransfer;
    }

    @Override
    public String toString() {
        return String.format("Input[fwd=%.2f str=%.2f turn=%.2f shoot=%b intake=%b]",
                forward, strafe, turn, shoot, intake);
    }
}
