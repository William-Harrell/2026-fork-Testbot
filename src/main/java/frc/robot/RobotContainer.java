// CHECK //
package frc.robot;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import edu.wpi.first.wpilibj2.command.RunCommand;
import frc.robot.OI.XboxDriver;
import frc.robot.OI.XboxOperator;
import frc.robot.commands.SwerveCommands;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.spindexer.Spindexer;
import frc.robot.subsystems.swerve.Swerve;
import frc.robot.subsystems.turret.Turret;
import frc.robot.subsystems.vision.Vision;
import frc.robot.util.constants.DrivingConstants;

public class RobotContainer {
  // Controllers
  private final XboxDriver driverJoystick;
  private final XboxOperator operatorJoystick;

  // Auto
  private final SendableChooser<Command> autoChooser;

  // Swerve
  private int speedExponent = 1;

  // Subsystems
  private final Swerve swerve;
  private final Vision vision;
  private final Turret turret;
  private final Intake intake;
  private final Spindexer spindexer;

  public RobotContainer() {
    // Controllers
    DriverStation.silenceJoystickConnectionWarning(true);
    driverJoystick = new XboxDriver(DrivingConstants.DRIVER_PORT);
    operatorJoystick = new XboxOperator(DrivingConstants.TESTER_PORT);

    // Auto
    autoChooser = new SendableChooser<>();
    SmartDashboard.putData("Auto Chooser", autoChooser);

    // Subsystems
    vision = new Vision();
    swerve = new Swerve(vision);
    turret = new Turret(vision);
    spindexer = new Spindexer();
    intake = new Intake();

    // Init methods
    setupDrive();
    configureButtonBindings();
    registerAutoRoutines();
  }

  // On container init
  private void setupDrive() {
    // Drive command
    Command teleopDriveCommand = swerve.teleopCommand(
        () -> applySpeedCurve(driverJoystick.forward()),
        () -> applySpeedCurve(driverJoystick.strafe()),
        () -> applySpeedCurve(driverJoystick.turn()));
    swerve.setDefaultCommand(teleopDriveCommand);

    // Gyro
    swerve.getH().setYaw(Rotation2d.fromDegrees(
        (DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue) == DriverStation.Alliance.Red)
            ? 180
            : 0));

    SmartDashboard.putData("TeleOp Command", teleopDriveCommand);
  }

  private double applySpeedCurve(double input) {
    if (input == 0.0) {
      return 0.0;
    }

    double magnitude = Math.pow(Math.abs(input), speedExponent);
    double sign = (input > 0) ? 1.0 : -1.0;
    return magnitude * sign;
  }

  private void configureButtonBindings() {
    // DRIVER

    // driverJoystick.toggleFieldRelative().onTrue(new
    // InstantCommand(swerve::toggleFieldRelative));

    driverJoystick
        .skiStop()
        .onTrue(SwerveCommands.SkiStop(swerve).until(driverJoystick::isMovementCommanded));

    driverJoystick
        .toggleSpeed()
        .onTrue(new InstantCommand(() -> speedExponent = (speedExponent == 1) ? 2 : 1));

    // OPERATOR
    operatorJoystick.runFlywheel().whileTrue(
        Commands.startEnd(
            turret.getF()::spinUp,
            turret.getF()::stop,
            turret));

    operatorJoystick.deployIntake().onTrue(
        Commands.startEnd(intake::deployIntakeMechanism, () -> {
        }, intake));

    // testerJoystick.retractIntake().onTrue(
    // Commands.startEnd(intake::retractIntakeMechanism, () -> {
    // }, intake));

    operatorJoystick.runIntakeForward().whileTrue(
        Commands.startEnd(intake.getR()::runIntake, intake.getR()::stopRollers,
            intake));

    operatorJoystick.runIntakeReverse().whileTrue(
        Commands.startEnd(() -> {
          intake.getR().runOuttake();
        }, intake.getR()::stopRollers,
            intake));

    operatorJoystick.chatClipThat().onTrue(
        new RunCommand(() -> {
          vision.getL().rewindRecord(5);
        }, vision));

  }

  private void registerAutoRoutines() {
    // autoChooser.setDefaultOption(NAME, REF);
    // autoChooser.addOption(NAME, REF);
  }

  public Command getAutonomousCommand() {
    // if (USE_DIP_SWITCH) {
    // dipSwitchSelector.lockSelection();
    // int selection = dipSwitchSelector.getSelection();
    // return AutoRoutines.getAutoFromSelection(
    // selection, swerve, intake, shooter, vision, null);
    // } else {
    return autoChooser.getSelected();
    // }
  }

  public void onDisabled() {
    // dipSwitchSelector.unlockSelection();
  }

  public void onEnabled() {

  }
}