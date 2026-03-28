package frc.robot.commands;

import com.pathplanner.lib.config.PIDConstants;
import com.pathplanner.lib.controllers.PPHolonomicDriveController;
import com.pathplanner.lib.trajectory.PathPlannerTrajectoryState;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.Subsystem;
import frc.robot.subsystems.swerve.SwerveConstants;
import frc.robot.subsystems.swerve.Swerve;
import java.util.Set;

public final class SwerveCommands {

  private SwerveCommands() {
  }

  public static Command SkiStop(Swerve swerve) {
    return Commands.run(swerve::skiStop, swerve).withName("SkiStop");
  }

  public static class FollowPath extends Command {

    private final Swerve swerve;

    // PathPlanner wants a trajectory state, so we wrap the pose.
    private final PathPlannerTrajectoryState target;

    // Handles XY + rotation control. That’s its entire job.
    private final PPHolonomicDriveController controller;

    public FollowPath(Swerve swerve, Pose2d targetPose) {
      this.swerve = swerve;

      this.target = new PathPlannerTrajectoryState();
      this.target.pose = targetPose;

      this.controller = new PPHolonomicDriveController(
          new PIDConstants(SwerveConstants.AUTO_XY_kP),
          new PIDConstants(SwerveConstants.AUTO_THETA_kP));

      setName("DriveToPose");
    }

    @Override
    public void initialize() {
      controller.reset(swerve.getPose(), new ChassisSpeeds());
    }

    @Override
    public void execute() {
      swerve.drive(controller.calculateRobotRelativeSpeeds(swerve.getPose(), target));
    }

    @Override
    public void end(boolean interrupted) {
      swerve.drive(new ChassisSpeeds());
    }

    @Override
    public Set<Subsystem> getRequirements() {
      return Set.of(swerve);
    }
  }
}