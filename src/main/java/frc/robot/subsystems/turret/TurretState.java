package frc.robot.subsystems.turret;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class TurretState {
  public enum turret_state {
    IDLE,
    SPINNING_UP,
    READY,
    SHOOTING,
    SPINNING_DOWN,
    TURNING,
    AIMING
  }

  private turret_state state;

  public TurretState() {
    state = turret_state.IDLE;
  }

  // Just in case
  public TurretState(turret_state override) {
    state = override;
  }

  public void set(turret_state newState) {
    if (state == newState) {
      return;
    }

    state = newState;
  }

  public turret_state get() {
    return state;
  }

}
