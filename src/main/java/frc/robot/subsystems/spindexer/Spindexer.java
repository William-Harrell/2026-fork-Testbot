package frc.robot.subsystems.spindexer;

import com.ctre.phoenix6.hardware.TalonFX;

import frc.robot.subsystems.spindexer.SpindexerState.spindexer_state;

public class Spindexer {
    private final SpindexerState state;
    private final Spinner spinner;

    public Spindexer() {
        state = new SpindexerState();
        spinner = new Spinner(new TalonFX(SpindexerConstants.ID));
    }

    public void startFeed() {
        spinner.setRPM(SpindexerConstants.FEEDINGRPM);
        state.set(spindexer_state.SPEEDING);
    }

    public void haltFeed() {
        spinner.setRPM(SpindexerConstants.OFFRPM);
        state.set(spindexer_state.SLOWING);
    }

    public void periodic() {
        boolean atSpeed = spinner.isAtSpeed();
        var cState = state.get();

        if (atSpeed && !(cState == spindexer_state.IDLE || cState == spindexer_state.FEEDING)) {
            if (cState == spindexer_state.SLOWING) {
                state.set(spindexer_state.IDLE);
            } else {
                state.set(spindexer_state.FEEDING);
            }
        }
    }
}