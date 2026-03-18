package frc.robot.subsystems.intake;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import frc.robot.subsystems.intake.IntakeState.intake_state;

public class Roller {
  /** Motor that spins the rollers to intake/outtake FUEL */
  private final TalonFX rollerMotor;

  private final IntakeState state_machine;
  private final Deploy deploy;

  /** {@code myRM} is the roller motor */
  public Roller(TalonFX myRM, IntakeState mySM, Deploy myD) {
    rollerMotor = myRM;
    state_machine = mySM;
    deploy = myD;

    TalonFXConfiguration rollerConfig = new TalonFXConfiguration();
    rollerConfig.MotorOutput.NeutralMode = NeutralModeValue.Coast;
    rollerMotor.getConfigurator().apply(rollerConfig);
  }

  /**
   * @deprecated Do NOT call set() directly on the returned controller — doing so bypasses the
   *     state machine and will desync physical and logical state. Use the methods on {@link Roller}
   *     or {@link IntakeCommands} instead. This accessor exists only for legacy telemetry reads.
   */
  @Deprecated
  public TalonFX get() {
    return rollerMotor;
  }

  /** Run intake rollers to collect FUEL */
  public void runIntake() {
    rollerMotor.set(IntakeConstants.INTAKE_SPEED);
    if (deploy.isDeployed()) {
      state_machine.set(intake_state.INTAKING);
    }
  }

  /** Reverse rollers to eject FUEL */
  public void runOuttake() {
    rollerMotor.set(IntakeConstants.OUTTAKE_SPEED);
    state_machine.set(intake_state.OUTTAKING);
  }

  /** Feed FUEL to the shooter mechanism */
  public void feedToShooter() {
    rollerMotor.set(IntakeConstants.INTAKE_SPEED);
    state_machine.set(intake_state.FEEDING);
  }

  /**
   * Stop the roller motors and update the state machine.
   *
   * <p>FIX: Previously, if the arm was mid-travel (neither isDeployed() nor isStowed()),
   * state.set() was never called, leaving the state as DEPLOYING or RETRACTING. This caused the
   * next intake command to read stale state. The else branch now resolves ambiguous mid-travel
   * state to DEPLOYED so the arm finishes its motion cleanly before the next command runs.
   */
  public void stopRollers() {
    rollerMotor.set(0);
    if (deploy.isDeployed()) {
      state_machine.set(intake_state.DEPLOYED);
    } else if (deploy.isStowed()) {
      state_machine.set(intake_state.STOWED);
    } else {
      // Arm is mid-travel (DEPLOYING or RETRACTING). Settle to DEPLOYED so the
      // next command doesn't act on stale DEPLOYING/RETRACTING state.
      // Intake.periodic() will auto-advance to STOWED once isStowed() becomes true.
      state_machine.set(intake_state.DEPLOYED);
    }
  }

  public void toggleIntakeOutake() {
    if (!deploy.isDeployed()) return;

    if (state_machine.get() == intake_state.INTAKING) {
      runOuttake();
    } else {
      runIntake();
    }
  }
}