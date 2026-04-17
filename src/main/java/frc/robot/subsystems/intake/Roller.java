package frc.robot.subsystems.intake;

import com.ctre.phoenix6.configs.TalonFXConfiguration;
import com.ctre.phoenix6.hardware.TalonFX;
import com.ctre.phoenix6.signals.InvertedValue;
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
    config.MotorOutput.NeutralMode = IntakeConstants.ROLLER_COAST ? 
        NeutralModeValue.Coast : NeutralModeValue.Brake;
    config.CurrentLimits.StatorCurrentLimit = 
      IntakeConstants.ROLLER_STATOR_CURRENT_LIMIT;
    config.CurrentLimits.StatorCurrentLimitEnable = 
      IntakeConstants.ROLLER_STATOR_CURRENT_LIMIT_ENABLE;
    config.CurrentLimits.SupplyCurrentLimit = 
      IntakeConstants.ROLLER_SUPPLY_CURRENT_LIMIT;
    config.CurrentLimits.SupplyCurrentLimitEnable = 
      IntakeConstants.ROLLER_SUPPLY_CURRENT_LIMIT_ENABLE;
    config.MotorOutput.Inverted = InvertedValue.Clockwise_Positive;

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