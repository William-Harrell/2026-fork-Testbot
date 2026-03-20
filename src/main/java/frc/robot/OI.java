// CHECK //

package frc.robot;

import edu.wpi.first.wpilibj2.command.button.CommandXboxController;
import edu.wpi.first.wpilibj2.command.button.Trigger;
import frc.robot.util.constants.OIConstants;

public final class OI {
  private OI() {
  }

  public static double deadband(double value, double band) {
    return Math.abs(value) > band ? value : 0;
  }

  public interface DriverActionSet {

    double forward();

    double strafe();

    double turn();

    Trigger resetGyro();

    Trigger toggleSpeed();

    Trigger toggleFieldRelative();

    Trigger skiStop();

    boolean isMovementCommanded();
  }

  public interface OperatorActionSet {
    Trigger toggleIntakeOutake();

    // Trigger toggleDeploy();

    Trigger maintainDeployed();

    Trigger orientAndShoot(); // UNTESTED

    Trigger reverseFeeder(); // UNIMPLEMENTED

    Trigger runFlywheel();

    Trigger setPitchMax(); // UNTESTED
    Trigger setPitchMin(); // UNTESTED
    Trigger setPitchStow(); // UNTESTED
  }

  public interface TesterActionSet {
    Trigger runFlywheel();

    Trigger deployIntake();

    Trigger retractIntake();

    Trigger runIntakeForward();

    Trigger runIntakeReverse();

    Trigger setPitchMax();

    Trigger setPitchMin();
  }

  public static class XboxDriver implements DriverActionSet {
    private final CommandXboxController stick;

    public XboxDriver(int port) {
      this.stick = new CommandXboxController(port);
    }

    @Override
    public double forward() {
      return deadband(-stick.getLeftY(), OIConstants.JOYSTICK_DEADBAND);
    }

    @Override
    public double strafe() {
      return deadband(-stick.getLeftX(), OIConstants.JOYSTICK_DEADBAND);
    }

    @Override
    public double turn() {
      return deadband(-stick.getRightX(), OIConstants.JOYSTICK_DEADBAND) * 0.4;
    }

    @Override
    public Trigger toggleSpeed() {
      return stick.b();
    }

    @Override
    public Trigger resetGyro() {
      return stick.povUp();
    }

    @Override
    public Trigger toggleFieldRelative() {
      return stick.povDown();
    }

    @Override
    public Trigger skiStop() {
      return stick.rightBumper();
    }

    @Override
    public boolean isMovementCommanded() {
      return Math.abs(forward()) + Math.abs(strafe()) + Math.abs(turn()) > 0.01;
    }
  }

  public static class XboxOperator implements OperatorActionSet {
    private final CommandXboxController stick;

    public XboxOperator(int port) {
      this.stick = new CommandXboxController(port);
    }

    @Override
    public Trigger runFlywheel() {
      return stick.x();
    }

    @Override
    public Trigger toggleIntakeOutake() {
      return stick.leftBumper();
    }

    // @Override
    // public Trigger toggleDeploy() {
    // return stick.a();
    // }

    @Override
    public Trigger maintainDeployed() {
      return stick.rightTrigger();
    }

    @Override // IMPLEMENTED/UNTESTED
    public Trigger orientAndShoot() {
      return stick.b();
    }

    @Override // UNIMPLEMENTED
    public Trigger reverseFeeder() {
      return stick.y();
    }

    @Override
    public Trigger setPitchMax() {
      return stick.povUp();
    }

    @Override
    public Trigger setPitchMin() {
      return stick.povDown();
    }

    @Override
    public Trigger setPitchStow() {
      return stick.povLeft();
    }
  }

  public static class XboxTester implements TesterActionSet {
    private final CommandXboxController stick;

    public XboxTester(int port) {
      stick = new CommandXboxController(port);
    }

    @Override
    public Trigger runFlywheel() {
      return stick.x();
    }

    @Override
    public Trigger deployIntake() {
      return stick.leftBumper();
    }

    @Override
    public Trigger retractIntake() {
      return stick.rightBumper();
    }

    @Override
    public Trigger runIntakeForward() {
      return stick.leftTrigger();
    }

    @Override
    public Trigger runIntakeReverse() {
      return stick.rightTrigger();
    }

        @Override
    public Trigger setPitchMax() {
      return stick.y();
    }

    @Override
    public Trigger setPitchMin() {
      return stick.a();
    }
  }
}