// CHECK //

package frc.robot;

// import com.pathplanner.lib.path.PathPlannerPath;

import edu.wpi.first.wpilibj.DataLogManager;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
// import frc.robot.auto.AutoSetup;
// import frc.robot.auto.CSPPathing;

public class Robot extends TimedRobot {

  private Command autonomousCommand;
  private RobotContainer robotContainer;

  @Override
  public void robotInit() {
    DataLogManager.start();
    robotContainer = new RobotContainer();
  }

  @Override
  public void robotPeriodic() {
    CommandScheduler.getInstance().run();
    robotContainer.logData();
  }

  @Override
  public void disabledInit() {
    robotContainer.onDisabled();
  }

  @Override
  public void disabledPeriodic() {
  }

  @Override
  public void autonomousInit() {
    robotContainer.onEnabled();
    // AutoSetup.configure();
    // CSPPathing.reset();
    try {
      // PathPlannerPath testPath = PathPlannerPath.fromPathFile("src/main/deploy/paths/Juggernaut_Collect.path");
    } catch (Exception e) {
      System.out.println("Error loading path: " + e.getMessage());
    }
    autonomousCommand = robotContainer.getAutonomousCommand();

    if (autonomousCommand != null) {
      CommandScheduler.getInstance().schedule(autonomousCommand);
    }
  }

  @Override
  public void autonomousPeriodic() {
  }

  @Override
  public void teleopInit() {
    robotContainer.onEnabled();

    if (autonomousCommand != null) {
      autonomousCommand.cancel();
    }
  }

  @Override
  public void teleopPeriodic() {
  }

  @Override
  public void testInit() {
    CommandScheduler.getInstance().cancelAll();
  }

  @Override
  public void testPeriodic() {
  }

  @Override
  public void simulationInit() {
  }

  @Override
  public void simulationPeriodic() {
  }
}