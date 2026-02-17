package frc.robot.subsystems.shooter;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.SparkMax;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkMaxConfig;

import edu.wpi.first.wpilibj.DriverStation.Alliance;
import edu.wpi.first.wpilibj.Servo;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.Constants.ShooterConstants;
import frc.robot.commands.ShooterCommands;
import frc.robot.subsystems.shooter.Physics.VisionAimedShot;
import frc.robot.subsystems.shooter.ShooterState.shooter_state;
import frc.robot.subsystems.swerve.SwerveDrive;
import frc.robot.subsystems.vision.Vision;

public class Shooter {
    // Sub-subsystems
    private Orientation orientation;
    private Physics physics;
    private ShooterState state_machine;
    private Flywheel flywheel;
    // private ShooterCommands commands;

    public Shooter(Vision vision, SwerveDrive swerve) {
        // Intra (w/ overload constructors)
        // Investigate what u don't know
        flywheel = new Flywheel(new SparkMax(ShooterConstants.FLYWHEEL_MOTOR_ID, MotorType.kBrushless));
        orientation = new Orientation(new Servo(ShooterConstants.PITCH_SERVO_CHANNEL));
        state_machine = new ShooterState(flywheel);
        physics = new Physics(vision);
    }

    private void updateDashboard() {
        // Main stats (Hover for Javadocs explanations)
        SmartDashboard.putString("Shooter/State", state_machine.get().toString());
        SmartDashboard.putNumber("Shooter/PitchAngle", targetPitchAngle);
        SmartDashboard.putNumber("Shooter/FlywheelRPM", flywheel.getFlywheelRPM());
        SmartDashboard.putNumber("Shooter/TargetRPM", targetFlywheelRPM);
        SmartDashboard.putBoolean("Shooter/AtSpeed", flywheel.isFlywheelAtSpeed());
        SmartDashboard.putBoolean("Shooter/ReadyToShoot", flywheel.isReadyToShoot());
        physics.hubDistance().ifPresent(d -> SmartDashboard.putNumber("Shooter/HubDistance", d));

        // Vision-related (can-shoot? & what the shot would look like)
        SmartDashboard.putBoolean("Shooter/VisionAvailable", physics.hasReliableVisionTarget());
        if (physics.hasReliableVisionTarget()) {
            VisionAimedShot visionShot = physics.calculateOptimalPitchWithVision();
            SmartDashboard.putNumber("Shooter/Vision/RecommendedPitch", visionShot.pitchAngle());
            SmartDashboard.putNumber("Shooter/Vision/TagCount", visionShot.tagCount());
            SmartDashboard.putNumber("Shooter/Vision/Ambiguity", visionShot.ambiguity());
            SmartDashboard.putNumber("Shooter/Vision/Distance", visionShot.distanceToHub());
            SmartDashboard.putBoolean("Shooter/Vision/HighConfidence", visionShot.isHighConfidence());
        }
    }

    // Runs every ~20 ms
    public void periodic() {
        // Ctrl + click (or symbol search) to see details
        state_machine.update();
        updateDashboard();
    }

    // Like a mini superstructure, if you will...
    // porbably all commands. will touch them later...
    public void prepareShot(double pitchDegrees, double flywheelRPM) {
        orientation.setPitchAngle(pitchDegrees);
        flywheel.setFlywheelRPM(flywheelRPM);
    }

    public void prepareDefaultShot() {
        prepareShot(ShooterConstants.LAUNCH_ANGLE, ShooterConstants.FLYWHEEL_SHOOT_RPM);
    }

    public void stop() { // no idea what this is for tbh. prolly cmds.
        flywheel.stopFlywheel();
        orientation.stowPitch();
        state_machine.set(shooter_state.IDLE);
    }
}
