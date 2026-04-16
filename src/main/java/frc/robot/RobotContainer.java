// CHECK //
package frc.robot;

import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.wpilibj.DriverStation;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
// import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
// import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import edu.wpi.first.wpilibj2.command.Command;
import edu.wpi.first.wpilibj2.command.Commands;
import edu.wpi.first.wpilibj2.command.InstantCommand;
import frc.robot.OI.XboxDriver;
import frc.robot.OI.XboxOperator;
// import frc.robot.commands.SwerveCommands;
import frc.robot.subsystems.intake.Intake;
import frc.robot.subsystems.spindexer.Spindexer;
import frc.robot.subsystems.swerve.Swerve;
import frc.robot.subsystems.turret.Turret;
import frc.robot.subsystems.turret.TurretConstants;
import frc.robot.subsystems.vision.Vision;
import frc.robot.util.constants.DrivingConstants;

public class RobotContainer {
  // Controllers
  private final XboxDriver driverJoystick;
  private final XboxOperator operatorJoystick;

  // Auto
  // private final SendableChooser<Command> autoChooser;

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
    operatorJoystick = new XboxOperator(DrivingConstants.OPERATOR_PORT);

    // Auto
    // autoChooser = new SendableChooser<>();
    // SmartDashboard.putData("Auto Chooser", autoChooser);

    // Subsystems
    // swerve = new Swerve(vision);
    swerve = new Swerve();
    vision = new Vision();
    turret = new Turret(); // should be passing through vision
    // turret = new Turret(vision);
    spindexer = new Spindexer();
    intake = new Intake();

    // Init methods
    setupDrive();
    configureButtonBindings();
    // registerAutoRoutines();
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
    swerve.getH().setYaw(new Rotation2d());
    swerve.zeroHeading();

    // .setYaw(Rotation2d.fromDegrees(
    // (DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue) ==
    // DriverStation.Alliance.Red)
    // ? 180
    // : 0));

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
    driverJoystick.toggleFieldRelative().onTrue(new InstantCommand(() -> {
      swerve.setFieldRelative(!swerve.isFieldRelative());
    }));

    driverJoystick
        .toggleSpeedExponent()
        .onTrue(new InstantCommand(() -> {
          speedExponent = (speedExponent == 1) ? 2 : 1;
        }));

    driverJoystick.resetGyro().onTrue(new InstantCommand(() -> swerve.zeroGyro()));

    // driverJoystick
    // .skiStop()
    // .onTrue(SwerveCommands.SkiStop(swerve).until(driverJoystick::isMovementCommanded));

    // OPERATOR
    operatorJoystick.runSpindexer().whileTrue(Commands.startEnd(spindexer::startFeed, spindexer::stopFeed, spindexer));

    operatorJoystick.deployIntake().onTrue(new InstantCommand(intake::deployIntakeMechanism));

    operatorJoystick.runIntake().whileTrue(
        Commands.startEnd(intake.getR()::runIntake, intake.getR()::stop,
            intake));

    // Shoot
    operatorJoystick.runFlywheel().whileTrue(
        Commands.sequence(
            Commands.runOnce(turret.getFlywheel()::spinStart, turret),
            Commands.waitUntil(() -> {
              return turret.getFlywheel().atTargetRPM();
            }),
            Commands.run(turret.getFlywheel()::spinFull, turret))
            .finallyDo(() -> turret.getFlywheel().stop()));

    // Toggle auto aim
    operatorJoystick.toggleAutoAim().onTrue(new InstantCommand(turret::toggleAutoAimEnabled));

    // Pitch
    operatorJoystick.posPitch().onTrue(
        new InstantCommand(() -> {
          if (turret.getAutoAimEnabled()) {
            turret.turnPitchTo(turret.getPitch() + DrivingConstants.PITCH_INCREMENT_MAGNITUDE);
          }
        }));

    operatorJoystick.negPitch().onTrue(
        new InstantCommand(() -> {
          if (turret.getAutoAimEnabled()) {
            turret.turnPitchTo(turret.getPitch() - DrivingConstants.PITCH_INCREMENT_MAGNITUDE);
          }
        }));

    // Yaw
    operatorJoystick.posYaw().onTrue(
        new InstantCommand(() -> {
          if (turret.getAutoAimEnabled()) {
            turret.moveYawTo(turret.getYaw() + DrivingConstants.YAW_INCREMENT_MAGNITUDE);
          }
        }));

    operatorJoystick.negYaw().onTrue(
        new InstantCommand(() -> {
          if (turret.getAutoAimEnabled()) {
            turret.moveYawTo(turret.getYaw() - DrivingConstants.YAW_INCREMENT_MAGNITUDE);
          }
        }));

    operatorJoystick.zeroYaw().onTrue(
      new InstantCommand(() -> {
        if (turret.getAutoAimEnabled()) {
          turret.zeroTurretYaw();
        }
      }));

    // gone but not forgotten :( fly high chatClipThat command
    // operatorJoystick.chatClipThat().onTrue(
    // new RunCommand(() -> {
    // vision.getL().rewindRecord(5);
    // }, vision));

  }

  // private void registerAutoRoutines() {
  // // autoChooser.setDefaultOption(NAME, REF);
  // // autoChooser.addOption(NAME, REF);
  // }

  // public Command getAutonomousCommand() {
  // return autoChooser.getSelected();
  // }

  public void onDisabled() {
  }

  public void onEnabled() {
    swerve.zeroHeading();
  }
}