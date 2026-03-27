package frc.robot.subsystems.turret;

import com.ctre.phoenix6.hardware.TalonFX;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.SparkLowLevel.MotorType;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.turret.TurretState.turret_state;
import frc.robot.subsystems.vision.Limelight;
import frc.robot.subsystems.vision.Vision;

public class Turret extends SubsystemBase {
    private final Limelight limelight;
    private final Vision vision;

    // Sub-subsystems
    private final Flywheel flywheel;
    private final TurretState state;
    private final Pitch pitch;
    private final Yaw yaw;

    private double pitch_goal; // Degrees
    private double yaw_goal; // Radians

    public Turret(Vision vision) {
        this.vision = vision;
        this.limelight = vision.getL();

        flywheel = new Flywheel(new TalonFX(TurretConstants.FLYWHEEL_MOTOR_1_ID));

        state = new TurretState();

        pitch = new Pitch(
                new TalonFX(TurretConstants.HOOD_MOTOR_1_ID),
                null // TODO: Implement encoder logic
        );

        yaw = new Yaw(new SparkMax(TurretConstants.TURN_MOTOR_ID, MotorType.kBrushless));

        pitch.reset();
        yaw.reset();
    }

    // Start shooting
    public void start() {
        flywheel.spinUp();
        state.set(turret_state.SPINNING_UP);
    }

    // Stop shooting
    public void stop() {
        flywheel.stop();
        state.set(turret_state.SPINNING_DOWN);
    }

    public boolean canShoot() {
        return (state.get() == turret_state.READY);
    }

    public double getYaw() {
        return yaw.get();
    }

    public double getPitch() {
        return pitch.get();
    }

    public void FindHub() {
        // Turn while probing limelight
        // If not found by the time a limit is reached, turn in the opposite direction
        // Run every heartbeat or so
        // If found, maintain current Yaw orientation and optimize pitch
    }

    public void periodic() {
        // Trust pitch & yaw to auto-update, since they change slightly every loop
        if (state.get() == turret_state.SPINNING_UP && flywheel.atTargetRPM()) {
            state.set(turret_state.READY);

        } else if (state.get() == turret_state.SPINNING_DOWN
                && flywheel.getRPM() < TurretConstants.FLYWHEEL_RPM_TOLERANCE) {
            state.set(turret_state.IDLE);

        }

        // Update angles & encoders here
    }

    public Flywheel getF() {
        return flywheel;
    }

    public turret_state getState() {
        return state.get();
    }
}
