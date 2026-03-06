package team3164.simulator.engine;

import team3164.simulator.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks all FUEL on the field (field, in-robot, in-flight, in-hub, in-depot, in-chute, in-corral, scored).
 */
public class FuelState {

    public enum FuelLocation {
        ON_FIELD, IN_ROBOT, IN_FLIGHT, IN_HUB, IN_CHUTE, IN_CORRAL, IN_DEPOT, SCORED
    }

    public static class Fuel {
        public int          id;
        public FuelLocation location;
        public MatchState.Alliance alliance;
        public double x, y, z;
        public double vx, vy, vz;
        public boolean isMoving;
        public double  restTimer;
        public int     owningRobotIndex; // -1 if not owned

        public Fuel(int id) {
            this.id               = id;
            this.location         = FuelLocation.ON_FIELD;
            this.owningRobotIndex = -1;
        }

        public double getSpeed() {
            return Math.sqrt(vx*vx + vy*vy + vz*vz);
        }

        public boolean isAtRest() {
            return !isMoving && restTimer <= 0;
        }
    }

    // ── Storage lists ──────────────────────────────────────────────────────
    private final List<Fuel> allFuel         = new ArrayList<>();
    private final List<Fuel> fieldFuel       = new ArrayList<>();
    private final List<Fuel> flightFuel      = new ArrayList<>();
    private final List<Fuel> redChuteFuel    = new ArrayList<>();
    private final List<Fuel> blueChuteFuel   = new ArrayList<>();
    private final List<Fuel> redCorralFuel   = new ArrayList<>();
    private final List<Fuel> blueCorralFuel  = new ArrayList<>();
    private final List<Fuel> redDepotFuel    = new ArrayList<>();
    private final List<Fuel> blueDepotFuel   = new ArrayList<>();
    private int nextFuelId = 0;

    public FuelState() {
        reset();
    }

    public void reset() {
        allFuel.clear(); fieldFuel.clear(); flightFuel.clear();
        redChuteFuel.clear(); blueChuteFuel.clear();
        redCorralFuel.clear(); blueCorralFuel.clear();
        redDepotFuel.clear(); blueDepotFuel.clear();
        nextFuelId = 0;
        initializeFieldFuel();
    }

    private void initializeFieldFuel() {
        // Scatter FUEL across the neutral zone
        double cx = Constants.Field.CENTER_X;
        double cy = Constants.Field.CENTER_Y;
        // Place 20 field FUEL in the neutral zone
        double[][] positions = {
            {cx - 1.0, cy - 1.5}, {cx, cy - 1.5}, {cx + 1.0, cy - 1.5},
            {cx - 1.0, cy},       {cx, cy},        {cx + 1.0, cy},
            {cx - 1.0, cy + 1.5}, {cx, cy + 1.5},  {cx + 1.0, cy + 1.5},
            {cx - 2.0, cy - 1.0}, {cx - 2.0, cy},  {cx - 2.0, cy + 1.0},
            {cx + 2.0, cy - 1.0}, {cx + 2.0, cy},  {cx + 2.0, cy + 1.0},
            {cx - 0.5, cy - 2.5}, {cx + 0.5, cy - 2.5},
            {cx - 0.5, cy + 2.5}, {cx + 0.5, cy + 2.5},
            {cx, cy - 3.0}
        };

        for (double[] pos : positions) {
            Fuel f = createFuel();
            f.x = pos[0];
            f.y = pos[1];
            f.z = Constants.Fuel.RADIUS;
            fieldFuel.add(f);
            allFuel.add(f);
        }

        // Depot FUEL
        for (int i = 0; i < 10; i++) {
            Fuel f = createFuel();
            f.location = FuelLocation.IN_DEPOT;
            f.alliance = MatchState.Alliance.RED;
            f.x = Constants.Field.RED_DEPOT_X;
            f.y = Constants.Field.RED_DEPOT_Y + (i - 5) * 0.15;
            redDepotFuel.add(f);
            allFuel.add(f);

            Fuel f2 = createFuel();
            f2.location = FuelLocation.IN_DEPOT;
            f2.alliance = MatchState.Alliance.BLUE;
            f2.x = Constants.Field.BLUE_DEPOT_X;
            f2.y = Constants.Field.BLUE_DEPOT_Y + (i - 5) * 0.15;
            blueDepotFuel.add(f2);
            allFuel.add(f2);
        }

        // Chute FUEL
        for (int i = 0; i < Constants.Fuel.CHUTE_START_COUNT; i++) {
            Fuel fr = createFuel();
            fr.location = FuelLocation.IN_CHUTE;
            fr.alliance = MatchState.Alliance.RED;
            redChuteFuel.add(fr);
            allFuel.add(fr);

            Fuel fb = createFuel();
            fb.location = FuelLocation.IN_CHUTE;
            fb.alliance = MatchState.Alliance.BLUE;
            blueChuteFuel.add(fb);
            allFuel.add(fb);
        }
    }

    private Fuel createFuel() {
        Fuel f = new Fuel(nextFuelId++);
        f.location = FuelLocation.ON_FIELD;
        return f;
    }

    // ── Public accessors ───────────────────────────────────────────────────
    public List<Fuel> getAllFuel()        { return allFuel; }
    public List<Fuel> getFieldFuel()     { return fieldFuel; }
    public List<Fuel> getFlightFuel()    { return flightFuel; }
    public List<Fuel> getRedChuteFuel()  { return redChuteFuel; }
    public List<Fuel> getBlueChuteFuel() { return blueChuteFuel; }
    public List<Fuel> getRedCorralFuel() { return redCorralFuel; }
    public List<Fuel> getBlueCorralFuel(){ return blueCorralFuel; }
    public List<Fuel> getRedDepotFuel()  { return redDepotFuel; }
    public List<Fuel> getBlueDepotFuel() { return blueDepotFuel; }

    public int getTotalFuelCount() { return allFuel.size(); }

    public List<Fuel> getActiveFuel() {
        List<Fuel> active = new ArrayList<>();
        for (Fuel f : allFuel) {
            if (f.location == FuelLocation.ON_FIELD || f.location == FuelLocation.IN_FLIGHT) {
                active.add(f);
            }
        }
        return active;
    }

    public int getCountByLocation(FuelLocation loc) {
        int count = 0;
        for (Fuel f : allFuel) if (f.location == loc) count++;
        return count;
    }

    // ── Field pickup ───────────────────────────────────────────────────────
    public Fuel pickupFromField(double x, double y, double radius) {
        Fuel nearest = null;
        double minDist = radius;
        for (Fuel f : fieldFuel) {
            if (f.location != FuelLocation.ON_FIELD) continue;
            double d = Math.hypot(f.x - x, f.y - y);
            if (d < minDist) { minDist = d; nearest = f; }
        }
        if (nearest != null) {
            fieldFuel.remove(nearest);
            nearest.location = FuelLocation.IN_ROBOT;
        }
        return nearest;
    }

    public boolean isNearDepot(double x, double y, MatchState.Alliance alliance) {
        double dx = alliance == MatchState.Alliance.RED ? Constants.Field.RED_DEPOT_X : Constants.Field.BLUE_DEPOT_X;
        double dy = alliance == MatchState.Alliance.RED ? Constants.Field.RED_DEPOT_Y : Constants.Field.BLUE_DEPOT_Y;
        return Math.hypot(x - dx, y - dy) < 1.5;
    }

    public Fuel pickupFromDepot(double x, double y, MatchState.Alliance alliance, double radius) {
        List<Fuel> depot = (alliance == MatchState.Alliance.RED) ? redDepotFuel : blueDepotFuel;
        if (depot.isEmpty()) return null;
        Fuel f = depot.remove(0);
        f.location = FuelLocation.IN_ROBOT;
        return f;
    }

    // ── Chute / corral ────────────────────────────────────────────────────
    public Fuel releaseFuelFromChute(MatchState.Alliance alliance) {
        List<Fuel> chute = (alliance == MatchState.Alliance.RED) ? redChuteFuel : blueChuteFuel;
        if (chute.isEmpty()) return null;
        Fuel f = chute.remove(0);
        f.location = FuelLocation.ON_FIELD;
        fieldFuel.add(f);
        return f;
    }

    public boolean transferCorralToChute(MatchState.Alliance alliance) {
        List<Fuel> corral = (alliance == MatchState.Alliance.RED) ? redCorralFuel : blueCorralFuel;
        List<Fuel> chute  = (alliance == MatchState.Alliance.RED) ? redChuteFuel  : blueChuteFuel;
        if (corral.isEmpty()) return false;
        Fuel f = corral.remove(0);
        f.location = FuelLocation.IN_CHUTE;
        chute.add(f);
        return true;
    }

    public void addToCorral(Fuel f, MatchState.Alliance alliance) {
        f.location = FuelLocation.IN_CORRAL;
        if (alliance == MatchState.Alliance.RED) redCorralFuel.add(f);
        else blueCorralFuel.add(f);
    }

    // ── Launch / flight ───────────────────────────────────────────────────
    public Fuel launchFuel(double x, double y, double z, double vx, double vy, double vz) {
        Fuel f = createFuel();
        f.location  = FuelLocation.IN_FLIGHT;
        f.x = x; f.y = y; f.z = z;
        f.vx = vx; f.vy = vy; f.vz = vz;
        f.isMoving  = true;
        flightFuel.add(f);
        allFuel.add(f);
        return f;
    }

    public void landOnField(Fuel f) {
        flightFuel.remove(f);
        f.location = FuelLocation.ON_FIELD;
        f.vz = 0; f.isMoving = true;
        fieldFuel.add(f);
    }

    public void enterHub(Fuel f, MatchState.Alliance alliance) {
        flightFuel.remove(f);
        f.location  = FuelLocation.IN_HUB;
        f.alliance  = alliance;
    }

    public void markScored(Fuel f) {
        f.location = FuelLocation.SCORED;
    }
}
