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
import frc.robot.subsystems.swerve.SwerveDrive;
import frc.robot.subsystems.vision.Vision;

public class Shooter {
    private Orientation orientation;
    private Physics physics;
    private ShooterState stateMachine;
    private Flywheel flywheel;
    private ShooterCommands commands;
    private final Vision vision;
    private final SwerveDrive swerve;

    private double[] hub = new double[2];
    private Alliance alliance;

    public Shooter(Vision vision, SwerveDrive swerve) {
        this.vision = vision;
        this.swerve = swerve;

        // Initialize pitch servo
        pitchServo = new Servo(ShooterConstants.PITCH_SERVO_CHANNEL);

        // Initialize flywheel motor
        flywheelMotor = new SparkMax(ShooterConstants.FLYWHEEL_MOTOR_ID, MotorType.kBrushless);
        flywheelEncoder = flywheelMotor.getEncoder();
        flywheelController = flywheelMotor.getClosedLoopController();

        configureMotors();
        UpdateHubLocation();

        // Start at stow position
        setPitchAngle(ShooterConstants.PITCH_STOW_ANGLE);
    }

    // Configuration (setup)
    private void configureMotors() {
        SparkMaxConfig flywheelConfig = new SparkMaxConfig();
        flywheelConfig
                .idleMode(IdleMode.kCoast)
                .smartCurrentLimit(ShooterConstants.FLYWHEEL_CURRENT_LIMIT);

        flywheelConfig.closedLoop
                .p(ShooterConstants.FLYWHEEL_kP)
                .i(ShooterConstants.FLYWHEEL_kI)
                .d(ShooterConstants.FLYWHEEL_kD).feedForward
                .kV(ShooterConstants.FLYWHEEL_kFF);

        flywheelMotor.configure(
                flywheelConfig, ResetMode.kResetSafeParameters, PersistMode.kPersistParameters);
    }

    // Runs every ~20 ms
    public void periodic() {
        // Update state based on flywheel status
        if (state == ShooterState.SPINNING_UP && isFlywheelAtSpeed()) {
            state = ShooterState.READY;
        } else if (state == ShooterState.SPINNING_DOWN && getFlywheelRPM() < 50) {
            state = ShooterState.IDLE;
        }

        // Update SmartDashboard - Core shooter status
        SmartDashboard.putString("Shooter/State", state.toString());
        SmartDashboard.putNumber("Shooter/PitchAngle", targetPitchAngle);
        SmartDashboard.putNumber("Shooter/FlywheelRPM", getFlywheelRPM());
        SmartDashboard.putNumber("Shooter/TargetRPM", targetFlywheelRPM);
        SmartDashboard.putBoolean("Shooter/AtSpeed", isFlywheelAtSpeed());
        SmartDashboard.putBoolean("Shooter/ReadyToShoot", isReadyToShoot());

        // Show distance to hub if available (odometry-based)
        hubDistance().ifPresent(d -> SmartDashboard.putNumber("Shooter/HubDistance", d));

        // Vision targeting status
        SmartDashboard.putBoolean("Shooter/VisionAvailable", hasReliableVisionTarget());

        // If vision is available, show what the vision-aimed shot would look like
        if (hasReliableVisionTarget()) {
            VisionAimedShot visionShot = calculateOptimalPitchWithVision();
            SmartDashboard.putNumber("Shooter/Vision/RecommendedPitch", visionShot.pitchAngle());
            SmartDashboard.putNumber("Shooter/Vision/TagCount", visionShot.tagCount());
            SmartDashboard.putNumber("Shooter/Vision/Ambiguity", visionShot.ambiguity());
            SmartDashboard.putNumber("Shooter/Vision/Distance", visionShot.distanceToHub());
            SmartDashboard.putBoolean("Shooter/Vision/HighConfidence", visionShot.isHighConfidence());
        }
    }

    // Like a mini superstructure, if you will...
    public void prepareShot(double pitchDegrees, double flywheelRPM) {
        setPitchAngle(pitchDegrees);
        setFlywheelRPM(flywheelRPM);
    }

    public void prepareDefaultShot() {
        prepareShot(ShooterConstants.LAUNCH_ANGLE, ShooterConstants.FLYWHEEL_SHOOT_RPM);
    }

    public void stop() {
        stopFlywheel();
        stowPitch();
        state = ShooterState.IDLE;
    }
}
