// CHECK //

package frc.robot.subsystems.shooter;

public class ShooterState {
  private shooter_state state;
  private final Flywheel flywheel;

  public enum shooter_state {
    IDLE, // Flywheel stopped, servo at stow
    SPINNING_UP, // Flywheel accelerating to target speed
    READY, // Flywheel at speed, ready to shoot
    SHOOTING, // Actively feeding and shooting
    SPINNING_DOWN // Flywheel decelerating
  }

  public ShooterState(Flywheel myWheel) {
    state = shooter_state.IDLE;
    flywheel = myWheel;
  }

  // Just in case
  public ShooterState(shooter_state override, Flywheel myWheel) {
    state = override;
    flywheel = myWheel;
  }

  public void set(shooter_state newState) {
    if (state == newState) {
      return;
    }
    state = newState;
  }

  public shooter_state get() {
    return state;
  }

  public void update() {
    if (state == shooter_state.SPINNING_UP && flywheel.isFlywheelAtSpeed()) {
      state = shooter_state.READY;
    } else if (state == shooter_state.SPINNING_DOWN && flywheel.getFlywheelRPM() < 50) {
      state = shooter_state.IDLE;
    }
  }
}
