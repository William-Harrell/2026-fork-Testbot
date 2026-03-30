package frc.robot.subsystems.spindexer;

public class SpindexerState {
  public enum spindexer_state {
    IDLE,
    SPEEDING,
    FEEDING,
    SLOWING
  }

  private spindexer_state state;

  public SpindexerState() {
    state = spindexer_state.IDLE;
  }

  // Just in case
  public SpindexerState(spindexer_state override) {
    state = override;
  }

  public void set(spindexer_state newState) {
    if (state == newState) {
      return;
    }
    
    state = newState;
  }

  public spindexer_state get() {
    return state;
  }
}
