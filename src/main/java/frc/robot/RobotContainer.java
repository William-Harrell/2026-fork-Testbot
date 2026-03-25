// CHECK //
package frc.robot;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import frc.robot.OI.XboxDriver;
import frc.robot.OI.XboxTester;
import frc.robot.commands.SwerveCommands;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.ShooterConstants;
import frc.robot.subsystems.spindexer.Spindexer;
import frc.robot.subsystems.swerve.SwerveDrive;
import frc.robot.subsystems.vision.Vision;
import frc.robot.util.constants.DrivingConstants;

public class RobotContainer {

  private final XboxDriver driverJoystick;
  // private final XboxOperator operatorJoystick;
  private final XboxTester testerJoystick;

  // Instance vars
  private final SwerveDrive swerve;
  private final Vision vision;
  private final Shooter shooter;
  private final Intake intake;
  private final Spindexer spindexer;

  private final Superstructure superstructure;

  private final SendableChooser<Command> autoChooser;

  private int speedExponent = 1;

  public RobotContainer() {
    DriverStation.silenceJoystickConnectionWarning(true);

    driverJoystick = new XboxDriver(DrivingConstants.DRIVER_PORT);
    testerJoystick = new XboxTester(DrivingConstants.TESTER_PORT);

    autoChooser = new SendableChooser<>();
    SmartDashboard.putData("Auto Chooser", autoChooser);

    vision = new Vision();
    swerve = new SwerveDrive(vision);
    shooter = new Shooter(vision, swerve);
    spindexer = new Spindexer();
    intake = new Intake();

    superstructure = new Superstructure(swerve, shooter, intake, spindexer, vision);

    Command teleopDriveCommand = swerve.teleopCommand(
        () -> applySpeedCurve(driverJoystick.forward()),
        () -> applySpeedCurve(driverJoystick.strafe()),
        () -> applySpeedCurve(driverJoystick.turn()));
    swerve.setDefaultCommand(teleopDriveCommand);

    swerve.resetYaw(Rotation2d.fromDegrees(
        (DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue) == DriverStation.Alliance.Red)
            ? 180
            : 0));

    SmartDashboard.putData("TeleOp Command", teleopDriveCommand);

    configureButtonBindings();
    registerAutoRoutines();
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

    driverJoystick.resetGyroTo().onTrue(swerve.resetGyroCommandTo());
    driverJoystick.resetGyroAway().onTrue(swerve.resetGyroCommandAway());

    // driverJoystick.toggleFieldRelative().onTrue(new
    // InstantCommand(swerve::toggleFieldRelative));

    driverJoystick
        .skiStop()
        .onTrue(SwerveCommands.skiStopCommand(swerve).until(driverJoystick::isMovementCommanded));

    driverJoystick
        .toggleSpeed()
        .onTrue(new InstantCommand(() -> speedExponent = (speedExponent == 1) ? 2 : 1));

    /*
     * NEED CAMERAS CALIBRATED: TODO
     * operatorJoystick
     * .orientAndShoot()
     * .whileTrue(
     * Commands.parallel(
     * new SwerveCommands.OrientToHubCommand(
     * swerve,
     * shooter.getP(),
     * () -> applySpeedCurve(driverJoystick.forward()),
     * () -> applySpeedCurve(driverJoystick.strafe())),
     * ShooterCommands.shootCommand(
     * superstructure.getShooter(), superstructure.getIntake())));
     * 
     */

    testerJoystick.runFlywheel().whileTrue(
        Commands.startEnd(
            () -> shooter.getF().setFlywheelRPM(ShooterConstants.FLYWHEEL_SHOOT_RPM),
            shooter.getF()::stopFlywheel,
            shooter));

    testerJoystick.deployIntake().onTrue(
        Commands.startEnd(intake::deployIntakeMechanism, () -> {
        }, intake));

    // testerJoystick.retractIntake().onTrue(
    // Commands.startEnd(intake::retractIntakeMechanism, () -> {
    // }, intake));

    testerJoystick.runIntakeForward().whileTrue(
        Commands.startEnd(intake.getR()::runIntake, intake.getR()::stopRollers,
            intake));

    testerJoystick.runIntakeReverse().whileTrue(
        Commands.startEnd(() -> {
          intake.getR().runOuttake();
        }, intake.getR()::stopRollers,
            intake));

  }

  private void registerAutoRoutines() {
    // autoChooser.setDefaultOption(NAME, REF);
    // autoChooser.addOption(NAME, REF);
  }

  public Command getAutonomousCommand() {
    // if (USE_DIP_SWITCH) { // amazing addition. thank you. so much.
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
    // hasHomed = false;
  }

  // private boolean hasHomed = true;

  public void onEnabled() {

  }
}