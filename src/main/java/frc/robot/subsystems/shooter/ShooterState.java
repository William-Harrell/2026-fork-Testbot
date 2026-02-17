package frc.robot.subsystems.shooter;

public class ShooterState {
    private shooter_state state;

    public enum shooter_state {
        IDLE, // Flywheel stopped, servo at stow
        SPINNING_UP, // Flywheel accelerating to target speed
        READY, // Flywheel at speed, ready to shoot
        SHOOTING, // Actively feeding and shooting
        SPINNING_DOWN // Flywheel decelerating
    }

    public ShooterState() {
        state = shooter_state.IDLE;
    }
    
    // Just in case
    public ShooterState(shooter_state override) {
        state = override;
    }

    public void set(shooter_state newState) {
        if (state.equals(newState)) {return;}
        state = newState;
    }

    public shooter_state get() {
        return state;
    }
}
