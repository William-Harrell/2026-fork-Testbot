// CHECK //
package frc.robot;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.CommandScheduler;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import frc.robot.OI.XboxDriver;
import frc.robot.OI.XboxOperator;
import frc.robot.OI.XboxTester;
import frc.robot.auto.AutoRoutines;
import frc.robot.commands.IntakeCommands;
import frc.robot.commands.SwerveCommands;
// import frc.robot.subsystems.hopper.Hopper;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.shooter.Shooter;
import frc.robot.subsystems.shooter.ShooterConstants;
import frc.robot.subsystems.swerve.SwerveDrive;
import frc.robot.subsystems.vision.Vision;
import frc.robot.util.DipSwitchSelector;
import frc.robot.util.Elastic;
import frc.robot.util.constants.DrivingConstants;

public class RobotContainer {

  private final XboxDriver driverJoystick;
  private final XboxOperator operatorJoystick;
  private final XboxTester testerJoystick;

  // Instance vars
  private final SwerveDrive swerve;
  private final Vision vision;
  private final Shooter shooter;
  private final Intake intake;

  private final Superstructure superstructure;

  private final SendableChooser<Command> autoChooser;

  private final DipSwitchSelector dipSwitchSelector;

  private static final boolean USE_DIP_SWITCH = false;

  private int speedExponent = 2;

  public RobotContainer() {
    DriverStation.silenceJoystickConnectionWarning(true);

    driverJoystick = new XboxDriver(DrivingConstants.DRIVER_PORT);
    operatorJoystick = new XboxOperator(DrivingConstants.OPERATOR_PORT);
    testerJoystick = new XboxTester(DrivingConstants.TESTER_PORT);

    autoChooser = new SendableChooser<>();
    SmartDashboard.putData("Auto Chooser", autoChooser);

    dipSwitchSelector = new DipSwitchSelector(); // for choosing auto routine

    vision = new Vision();
    swerve = new SwerveDrive(vision);
    shooter = new Shooter(vision, swerve);
    intake = new Intake();

    superstructure = new Superstructure(swerve, shooter, intake, vision);

    Command teleopDriveCommand = swerve.teleopCommand(
        () -> applySpeedCurve(driverJoystick.forward()),
        () -> applySpeedCurve(driverJoystick.strafe()),
        () -> applySpeedCurve(driverJoystick.turn()));
    swerve.setDefaultCommand(teleopDriveCommand);

    boolean isRed = DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue) == DriverStation.Alliance.Red;
    swerve.resetYaw(Rotation2d.fromDegrees(isRed ? 0 : 180));

    SmartDashboard.putData("TeleOp Command", teleopDriveCommand);

    configureButtonBindings();
    registerAutoRoutines();
  }

  private double applySpeedCurve(double input) {
    if (input == 0.0) {
      return 0.0;
    }

    double magnitude = Math.pow(Math.abs(input), speedExponent);
    double sign = (input > 0) ? 1.0 : -1.0; // a famous function
    return magnitude * sign;
  }

  private void configureButtonBindings() {

    driverJoystick.resetGyro().onTrue(swerve.resetGyroCommand());

    driverJoystick.toggleFieldRelative().onTrue(new InstantCommand(swerve::toggleFieldRelative));

    driverJoystick
        .skiStop()
        .onTrue(SwerveCommands.skiStopCommand(swerve).until(driverJoystick::isMovementCommanded));

    driverJoystick
        .toggleSpeed()
        .onTrue(new InstantCommand(() -> speedExponent = (speedExponent == 1) ? 2 : 1));

    // operatorJoystick
    // .toggleDeploy()
    // .onTrue(IntakeCommands.toggleDeployCommand(intake));

    operatorJoystick
        .toggleIntakeOutake()
        .onTrue(IntakeCommands.toggleDirection(intake));

    operatorJoystick
        .maintainDeployed()
        .whileTrue(
            Commands.parallel(
                IntakeCommands.holdToIntakeCommand(intake)));

    operatorJoystick.runFlywheel().whileTrue(
        Commands.startEnd(
            () -> shooter.getF().setFlywheelRPM(ShooterConstants.FLYWHEEL_SHOOT_RPM),
            shooter.getF()::stopFlywheel,
            shooter));

    operatorJoystick.setPitchMax().onTrue(
      Commands.run(() -> {shooter.getO().setPitchAngle(ShooterConstants.PITCH_MAX_ANGLE);}, shooter)
    );

    operatorJoystick.setPitchMin().onTrue(
      Commands.run(() -> {shooter.getO().setPitchAngle(ShooterConstants.PITCH_MIN_ANGLE);}, shooter)
    );

    operatorJoystick.setPitchStow().onTrue(
      Commands.run(() -> {shooter.getO().setPitchAngle(ShooterConstants.PITCH_STOW_ANGLE);}, shooter)
    );
    /*
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
     * superstructure.getShooter(), superstructure.getIntake()),
     * Commands.startEnd(hopper::feed, hopper::stop, hopper)));
     * 
     */
    // operatorJoystick
    // .reverseFeeder()
    // .whileTrue(
    // Commands.parallel(
    // Commands.startEnd(hopper::reverse, hopper::stop, shooter)));

    testerJoystick.runFlywheel().whileTrue(
        Commands.startEnd(
            () -> shooter.getF().setFlywheelRPM(ShooterConstants.FLYWHEEL_SHOOT_RPM),
            shooter.getF()::stopFlywheel,
            shooter));

    testerJoystick.deployIntake().onTrue(
        Commands.startEnd(intake::deployIntakeMechanism, () -> {
        }, intake));

    testerJoystick.retractIntake().onTrue(
        Commands.startEnd(intake::retractIntakeMechanism, () -> {
        }, intake));

    testerJoystick.runIntakeForward().whileTrue(
        Commands.startEnd(intake.getR()::runIntake, intake.getR()::stopRollers, intake));

    testerJoystick.runIntakeReverse().whileTrue(
        Commands.startEnd(intake.getR()::runOuttake, intake.getR()::stopRollers, intake));
  }

  private void registerAutoRoutines() {
    // TODO - verify auto routines are reliable
    AutoRoutines.seedPoseFromVision(swerve, vision);
    autoChooser.setDefaultOption("0: Do Nothing", AutoRoutines.doNothing());
    /*
     * autoChooser.addOption(
     * "1: Score & Collect",
     * Commands.sequence(
     * AutoRoutines.seedPoseFromVision(swerve, vision),
     * AutoRoutines.scoreCollectAuto(swerve, intake, shooter, hopper)));
     * autoChooser.addOption(
     * "2: Score Only",
     * Commands.sequence(
     * AutoRoutines.seedPoseFromVision(swerve, vision),
     * AutoRoutines.scoreOnlyAuto(swerve, intake, shooter, hopper)));
     * autoChooser.addOption(
     * "3: Preload Only",
     * Commands.sequence(
     * AutoRoutines.seedPoseFromVision(swerve, vision),
     * AutoRoutines.preloadOnlyAuto(swerve, intake, shooter, hopper)));
     */
  }

  public Command getAutonomousCommand() {
    if (USE_DIP_SWITCH) { // amazing addition. thank you. so much.
      dipSwitchSelector.lockSelection();
      int selection = dipSwitchSelector.getSelection();
      return AutoRoutines.getAutoFromSelection(
          selection, swerve, intake, shooter, vision, null);
    } else {
      return autoChooser.getSelected();
    }
  }

  public void onDisabled() {
    dipSwitchSelector.unlockSelection();
    hasHomed = false;
  }

  private boolean hasHomed = false;

  public void onEnabled() {
    if (hasHomed)
      return;
    hasHomed = true;

    CommandScheduler.getInstance().schedule(
        Commands.parallel(intake.getD().homeCommand(intake), shooter.getO().homeCommand(shooter))
            .withTimeout(3.0)
            .andThen(
                Commands.runOnce(
                    () -> {
                      boolean intakeOk = intake.getD().isHomed();
                      boolean hoodOk = shooter.getO().isHomed();
                      if (intakeOk && hoodOk) {
                        Elastic.sendNotification(
                            new Elastic.Notification()
                                .withLevel(Elastic.NotificationLevel.INFO)
                                .withTitle("Homing Complete")
                                .withDescription("Intake and hood encoders zeroed")
                                .withDisplaySeconds(3.0));
                      } else {
                        Elastic.sendNotification(
                            new Elastic.Notification()
                                .withLevel(Elastic.NotificationLevel.ERROR)
                                .withTitle("Homing Failed")
                                .withDescription(
                                    (!intakeOk ? "Intake " : "")
                                        + (!hoodOk ? "Hood " : "")
                                        + "limit switch not triggered")
                                .withNoAutoDismiss());
                      }
                    }))
            .withName("Home Mechanisms"));
  }

  public void logData() {
    SmartDashboard.putBoolean("Slow Speed", speedExponent == 2);
    SmartDashboard.putBoolean("Auto/Using DIP Switch", USE_DIP_SWITCH);
    dipSwitchSelector.updateDashboard();
  }
}