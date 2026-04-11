package frc.robot.subsystems.intake;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.NeutralModeValue;
import frc.robot.subsystems.intake.IntakeState.intake_state;

public class Roller {
  // Dependencies bc poor design lol
  private final IntakeState state_machine;
  private final Deploy deploy;

  // Actual subsystem-specific stuff
  private final TalonFX motor;

  public Roller(TalonFX motor, IntakeState state_machine, Deploy deploy) {
    this.motor = motor;
    this.state_machine = state_machine;
    this.deploy = deploy;

    TalonFXConfiguration config = new TalonFXConfiguration();
    config.MotorOutput.NeutralMode = NeutralModeValue.Coast;
    this.motor.getConfigurator().apply(config);
  }

  public void runIntake() {
    motor.set(IntakeConstants.INTAKE_SPEED);
    if (deploy.isDeployed()) {
      state_machine.set(intake_state.INTAKING);
    }
  }

  public void feed() {
    motor.set(IntakeConstants.INTAKE_SPEED);
    state_machine.set(intake_state.INTAKING);
  }

  public void stop() {
    motor.set(0);
    if (deploy.isDeployed()) {
      state_machine.set(intake_state.DEPLOYED);
    } else if (deploy.isStowed()) {
      state_machine.set(intake_state.STOWED);
    } else {
      state_machine.set(intake_state.DEPLOYED);
    }
  }
}