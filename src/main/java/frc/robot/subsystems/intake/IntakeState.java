package frc.robot.subsystems.intake;

import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class IntakeState {
  public enum intake_state {
    STOWED,
    DEPLOYING,
    DEPLOYED,
    RETRACTING,
    INTAKING,
    OUTTAKING
  }

  private intake_state state;

  public IntakeState() {
    state = intake_state.STOWED;
  }

  // Just in case
  public IntakeState(intake_state override) {
    state = override;
  }

  public void set(intake_state newState) {
    if (state == newState) {
      return;
    }
    state = newState;
  }

  public intake_state get() {
    return state;
  }

  public void update(boolean deployed, boolean stowed) {
    if (state == intake_state.DEPLOYING && deployed) {
      state = intake_state.DEPLOYED;
    } else if (state == intake_state.RETRACTING && stowed) {
      state = intake_state.STOWED;
    }

    SmartDashboard.putString("Intake/State", state.toString());
  }
}
