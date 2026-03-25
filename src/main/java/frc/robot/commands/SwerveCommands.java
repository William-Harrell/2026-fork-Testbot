package frc.robot.commands;

// Comments should explain *why*, not narrate the obvious.
// Most of the previous commentary was noise.

/*
 * Swerve commands live here instead of bloating the drive subsystem.
 * Keep logic separated so the drivetrain stays dumb and predictable.
 *
 * Simple commands: static factories.
 * Stateful commands: real classes.
 */

import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.pathplanner.lib.trajectory.PathPlannerTrajectoryState;
import edu.wpi.first.math.controller.PIDController;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.subsystems.shooter.Physics;
import frc.robot.subsystems.swerve.SwerveConstants;
import frc.robot.subsystems.swerve.SwerveDrive;
import frc.robot.util.constants.DrivingConstants;
import java.util.Set;
import java.util.function.DoubleSupplier;

/**
 * Static helpers for swerve commands.
 * No state, no instances, no nonsense.
 */
public final class SwerveCommands {

  /** Prevent instantiation. If you need an object, you’re doing it wrong. */
  private SwerveCommands() {
    // nothing
  }

  // Lock wheels in an X. This is just brute-force resistance to motion.
  public static Command skiStopCommand(SwerveDrive swerve) {
    return Commands.run(swerve::setX, swerve).withName("SkiStop");
  }

  /**
   * Drives to a pose using PathPlanner’s controller.
   * We reuse their math instead of reinventing a worse version.
   */
  public static class DriveToPoseCommand extends Command {

    private final SwerveDrive swerve;

    // PathPlanner wants a trajectory state, so we wrap the pose.
    private final PathPlannerTrajectoryState target;

    // Handles XY + rotation control. That’s its entire job.
    private final PPHolonomicDriveController controller;

    public DriveToPoseCommand(SwerveDrive swerve, Pose2d targetPose) {
      this.swerve = swerve;

      this.target = new PathPlannerTrajectoryState();
      this.target.pose = targetPose;

      this.controller =
          new PPHolonomicDriveController(
              new PIDConstants(SwerveConstants.AUTO_XY_kP),
              new PIDConstants(SwerveConstants.AUTO_THETA_kP));

      setName("DriveToPose");
    }

    @Override
    public void initialize() {
      // Reset so we don’t carry stale error from previous runs.
      controller.reset(swerve.getPose(), new ChassisSpeeds());
    }

    @Override
    public void execute() {
      // Compute speeds to reduce pose error.
      ChassisSpeeds output =
          controller.calculateRobotRelativeSpeeds(swerve.getPose(), target);

      swerve.drive(output, DrivingConstants.OPEN_LOOP);
    }

    @Override
    public void end(boolean interrupted) {
      // Stop means zero. Not “kind of zero”.
      swerve.drive(new ChassisSpeeds(), DrivingConstants.OPEN_LOOP);
    }

    @Override
    public Set<Subsystem> getRequirements() {
      return Set.of(swerve);
    }
  }

  /**
   * Drives a fixed distance from the starting point.
   * This is intentionally dumb: constant speed, no correction.
   */
  public static class DriveDistanceCommand extends Command {

    private final SwerveDrive swerve;

    // Direction and magnitude bundled together.
    private final Translation2d speeds;

    private final double distance;

    // Captured at start. Used to measure progress.
    private Translation2d initialTranslation;

    public DriveDistanceCommand(SwerveDrive swerve, double xSpeed, double ySpeed, double distance) {
      this.swerve = swerve;
      this.speeds = new Translation2d(xSpeed, ySpeed);
      this.distance = distance;
      setName("DriveDistance");
    }

    @Override
    public void initialize() {
      initialTranslation = swerve.getPose().getTranslation();
    }

    @Override
    public void execute() {
      // Just drive. No feedback, no correction.
      swerve.drive(speeds, 0, false, false);
    }

    @Override
    public void end(boolean interrupted) {
      swerve.drive(new Translation2d(), 0, false, false);
    }

    @Override
    public boolean isFinished() {
      // Straight-line distance. Good enough.
      double distanceTraveled =
          swerve.getPose().getTranslation().getDistance(initialTranslation);
      return distanceTraveled >= distance;
    }

    @Override
    public Set<Subsystem> getRequirements() {
      return Set.of(swerve);
    }
  }

  /**
   * Rotates toward the hub using vision.
   * Translation stays under driver control.
   *
   * If vision disappears, rotation stops instead of guessing.
   */
  public static class OrientToHubCommand extends Command {

    private final SwerveDrive swerve;
    private final Physics physics;
    private final DoubleSupplier forward;
    private final DoubleSupplier strafe;
    private final PIDController thetaController;

    public OrientToHubCommand(
        SwerveDrive swerve, Physics physics, DoubleSupplier forward, DoubleSupplier strafe) {
      this.swerve = swerve;
      this.physics = physics;
      this.forward = forward;
      this.strafe = strafe;

      thetaController =
          new PIDController(
              SwerveConstants.AIM_kP,
              SwerveConstants.AIM_kI,
              SwerveConstants.AIM_kD);

      // We want zero error. Anything else is wrong.
      thetaController.setSetpoint(0.0);
      thetaController.setTolerance(SwerveConstants.AIM_TOLERANCE_DEG);
      thetaController.enableContinuousInput(-180.0, 180.0);

      setName("OrientToHub");
    }

    @Override
    public void initialize() {
      thetaController.reset();
    }

    @Override
    public void execute() {
      double omega = 0.0;
      double errorDeg = 0.0;

      if (physics.hasReliableVisionTarget()) {
        errorDeg = physics.robotToHub().get();
        omega = thetaController.calculate(errorDeg);
      }

      Translation2d translation =
          new Translation2d(
              forward.getAsDouble() * SwerveConstants.MAX_SPEED,
              strafe.getAsDouble() * SwerveConstants.MAX_SPEED);

      swerve.drive(translation, omega, true, DrivingConstants.OPEN_LOOP);

      // Debug output. If this spams too much, that’s a separate problem.
      SmartDashboard.putBoolean("Aim/Locked", thetaController.atSetpoint());
      SmartDashboard.putBoolean("Aim/VisionAvailable", physics.hasReliableVisionTarget());
      SmartDashboard.putNumber("Aim/ErrorDeg", errorDeg);
    }

    @Override
    public void end(boolean interrupted) {
      swerve.drive(new Translation2d(), 0, true, DrivingConstants.OPEN_LOOP);
    }

    @Override
    public boolean isFinished() {
      return false; // runs until externally stopped
    }

    @Override
    public Set<Subsystem> getRequirements() {
      return Set.of(swerve);
    }
  }

} // End of SwerveCommands