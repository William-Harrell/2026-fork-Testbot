package team3164.simulator.physics;

import team3164.simulator.Constants;
import team3164.simulator.engine.FuelState;
import team3164.simulator.engine.InputState;
import team3164.simulator.engine.MatchState;
import team3164.simulator.engine.RobotState;

/**
 * Human-player outpost / chute mechanics.
 */
public class OutpostPhysics {

    private static final double CHUTE_RELEASE_INTERVAL = 2.0;  // seconds per FUEL release
    private static final double CHUTE_SLOPE_ANGLE      = 20.0; // degrees
    private static final double CORRAL_PICKUP_RANGE    = 1.0;

    private static double redChuteTimer  = 0.0;
    private static double blueChuteTimer = 0.0;

    public static void update(FuelState fuelState, MatchState match, InputState input, double dt) {
        redChuteTimer  += dt;
        blueChuteTimer += dt;

        if (input.redChuteRelease  && redChuteTimer  >= CHUTE_RELEASE_INTERVAL) {
            releaseFuelFromChute(fuelState, match, MatchState.Alliance.RED);
            redChuteTimer = 0;
        }
        if (input.blueChuteRelease && blueChuteTimer >= CHUTE_RELEASE_INTERVAL) {
            releaseFuelFromChute(fuelState, match, MatchState.Alliance.BLUE);
            blueChuteTimer = 0;
        }
        if (input.redCorralTransfer)  transferCorralToChute(fuelState, match, MatchState.Alliance.RED);
        if (input.blueCorralTransfer) transferCorralToChute(fuelState, match, MatchState.Alliance.BLUE);
    }

    public static FuelState.Fuel releaseFuelFromChute(FuelState fuelState, MatchState match, MatchState.Alliance alliance) {
        FuelState.Fuel f = fuelState.releaseFuelFromChute(alliance);
        if (f == null) return null;
        double[] pos   = getChutePosition(alliance);
        double[] vel   = calculateChuteRelease(alliance);
        f.x = pos[0]; f.y = pos[1]; f.z = 0.1;
        f.vx = vel[0]; f.vy = vel[1]; f.vz = 0;
        f.isMoving = true;
        return f;
    }

    public static boolean transferCorralToChute(FuelState fuelState, MatchState match, MatchState.Alliance alliance) {
        return fuelState.transferCorralToChute(alliance);
    }

    public static boolean isNearOutpost(RobotState robot) {
        double[] rp = getCorralPosition(robot.alliance);
        return Math.hypot(robot.x - rp[0], robot.y - rp[1]) < CORRAL_PICKUP_RANGE + 0.5;
    }

    public static boolean isNearCorral(RobotState robot) {
        double[] rp = getCorralPosition(robot.alliance);
        return Math.hypot(robot.x - rp[0], robot.y - rp[1]) < CORRAL_PICKUP_RANGE;
    }

    public static double[] getChutePosition(MatchState.Alliance alliance) {
        if (alliance == MatchState.Alliance.RED)
            return new double[]{ Constants.Field.RED_OUTPOST_X, Constants.Field.RED_OUTPOST_Y };
        return new double[]{ Constants.Field.BLUE_OUTPOST_X, Constants.Field.BLUE_OUTPOST_Y };
    }

    public static double[] getCorralPosition(MatchState.Alliance alliance) {
        // Corral is near the chute
        double[] chute = getChutePosition(alliance);
        boolean isRed  = (alliance == MatchState.Alliance.RED);
        return new double[]{ chute[0] + (isRed ? -0.5 : 0.5), chute[1] };
    }

    public static double[] calculateChuteRelease(MatchState.Alliance alliance) {
        // Chute slope pushes FUEL toward the field
        double angle = Math.toRadians(CHUTE_SLOPE_ANGLE);
        double speed = 1.5;
        boolean isRed = (alliance == MatchState.Alliance.RED);
        double vx = (isRed ? -1 : 1) * speed * Math.cos(angle);
        return new double[]{ vx, 0 };
    }

    public static int getChuteCount(FuelState fuelState, MatchState.Alliance alliance) {
        return alliance == MatchState.Alliance.RED
               ? fuelState.getRedChuteFuel().size()
               : fuelState.getBlueChuteFuel().size();
    }

    public static int getCorralCount(FuelState fuelState, MatchState.Alliance alliance) {
        return alliance == MatchState.Alliance.RED
               ? fuelState.getRedCorralFuel().size()
               : fuelState.getBlueCorralFuel().size();
    }

    public static boolean canRelease(FuelState fuelState, MatchState.Alliance alliance) {
        return getChuteCount(fuelState, alliance) > 0;
    }

    public static boolean canTransfer(FuelState fuelState, MatchState.Alliance alliance) {
        return getCorralCount(fuelState, alliance) > 0;
    }
}
