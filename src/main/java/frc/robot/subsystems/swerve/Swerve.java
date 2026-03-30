package frc.robot.subsystems.swerve;

import java.util.function.DoubleSupplier;

import com.ctre.phoenix6.hardware.Pigeon2;

import edu.wpi.first.math.VecBuilder;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.util.Units;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.RunCommand;
import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.vision.Vision;

public class Swerve extends SubsystemBase {
    // Dependencies
    private final Vision vision;

    // Sub-subsystems
    private final Config config;
    private final SwerveDriveKinematics kinematics;
    private final Hardware hardware;
    private final Dashboard dashboard;
    private final SwerveDrivePoseEstimator estimator;

    public Swerve(Vision vision) {
        this.vision = vision;

        // Sub-subsystems
        config = new Config();
        dashboard = new Dashboard();
        hardware = new Hardware(new Pigeon2(SwerveConstants.PIGEON_ID));
        kinematics = new SwerveDriveKinematics(hardware.getModuleLocations());
        estimator = new SwerveDrivePoseEstimator(
                kinematics,
                hardware.getYaw(),
                hardware.getPositions(),
                new Pose2d() // (0,0) & forward direction
        );
    }

    public void setFieldRelative(boolean b) {
        config.field_relative = b;
    }

    public void setOpenLoop(boolean b) {
        config.open_loop = b;
    }

    public Pose2d getPose() {
        return estimator.getEstimatedPosition();
    }

    public void resetPose(Pose2d pose) {
        estimator.resetPosition(hardware.getYaw(), hardware.getPositions(), pose);
    }

    public void addVisionMeasurement(Pose2d pose, double timestampSeconds) {
        estimator.addVisionMeasurement(pose, timestampSeconds);
    }

    public boolean isFieldRelative() {
        return config.field_relative;
    }

    public void skiStop() {
        hardware.skiStop(config.open_loop);
    }

    @Override
    public void periodic() {
        vision.getP().invalidateCache();

        vision.getBestVisionUpdateRaw(getPose()).ifPresent((update) -> {
            // Standard deviation for distance error
            double xyStdDev = 0.1 + (Math.pow(update.avgDistanceMeters(), 2) * 0.1);

            estimator.addVisionMeasurement(
                    update.pose2d(),
                    update.timestampSeconds(),
                    VecBuilder.fill(xyStdDev, xyStdDev, Units.degreesToRadians(90)));
        });

        vision.getBestVisionUpdateRaw(getPose())
                .ifPresent((update) -> addVisionMeasurement(update.pose2d(), update.timestampSeconds()));

        dashboard.updateLogs(getPose(), hardware.getStates());
    }

    // Driving methods
    public Command teleopCommand(DoubleSupplier forward, DoubleSupplier strafe, DoubleSupplier turn) {
        return new RunCommand(
                () -> {
                    double vx = forward.getAsDouble() * SwerveConstants.MAX_SPEED;
                    double vy = strafe.getAsDouble() * SwerveConstants.MAX_SPEED;
                    double omega = turn.getAsDouble() * SwerveConstants.MAX_ANGULAR_VELOCITY;

                    if (DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue) == DriverStation.Alliance.Red) {
                        vx = -vx;
                        vy = -vy;
                    }

                    drive(new Translation2d(vx, vy), omega);
                },
                this);
    }

    public void drive(ChassisSpeeds speeds) {
        speeds = ChassisSpeeds.discretize(speeds, 0.02);

        var states = kinematics.toSwerveModuleStates(speeds);
        var modules = hardware.getModules();

        SwerveDriveKinematics.desaturateWheelSpeeds(states, SwerveConstants.MAX_SPEED);

        for (int i = 0; i < modules.length; i++) {
            modules[i].setDesiredState(states[i], config.open_loop);
        }
    }

    public void drive(Translation2d translation, double rotation) {
        ChassisSpeeds speeds;

        // if (fieldRelative) {
        // // FIELD-RELATIVE: Adjust for robot's current heading
        // // "Forward" means toward the far end of the field, regardless of robot
        // // orientation
        // speeds = ChassisSpeeds.fromFieldRelativeSpeeds(
        // translation.getX(), // Field X velocity
        // translation.getY(), // Field Y velocity
        // rotation, // Rotation velocity
        // getYaw() // Current robot heading (to convert field->robot)
        // );
        // } else {
        // ROBOT-RELATIVE: No adjustment needed
        // "Forward" means wherever the robot is facing
        speeds = new ChassisSpeeds(translation.getX(), translation.getY(), rotation);
        // }

        drive(speeds);
    }

    // Sub-subsystem getters
    public Hardware getH() {
        return hardware;
    }

    public SwerveDriveKinematics getK() {
        return kinematics;
    }
}
