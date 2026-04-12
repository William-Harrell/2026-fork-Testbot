package frc.robot.subsystems.spindexer;

public final class SpindexerConstants {
    public static final int OFFRPM = 0;
    public static final int RPMTOLERANCE = 50; //TODO placeholder TBD
    public static final int RAMPRATE = 2; //seconds to full power

    // SpinDexer Velo: (Find Max Velocity at Motor 100% -> put that in RPM MAX)
    
    public static final int FEEDINGRPM = 1000; //TODO placeholder TBD
    public static final int RPMMAX = 1000;

    // SpinDexer Motor Configs
    public static final int SPINDEXER_ID = 17; // Vortex (Spark Flex)

    public static final int SPIN_STATOR_CURRENT_LIMIT = 120;
    public static final int SPIN_SUPPLY_CURRENT_LIMIT = 80;
        // true = coast, false = brake
    public static final boolean SPIN_COAST = true;
}
