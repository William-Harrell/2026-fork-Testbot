package frc.robot.subsystems.intake;

public final class IntakeConstants {
  
  public static final double DEPLOY_POS = 26.382;
  public static final double STOW_POS = 0.0;
  public static final double TOLERANCE = 0.05;

  public static final double DEPLOY_kP = 0.05;
  public static final double INTAKE_SPEED = -.45; //in duty cycle
  public static final double OUTTAKE_SPEED = .7; //in duty cycle


    // Deploy Motor Configs
  public static final int DEPLOY_MOTOR_ID = 15; // Vortex (SparkFlex)

  public static final int DEPLOY_STATOR_CURRENT_LIMIT = 50;
  public static final int DEPLOY_SUPPLY_CURRENT_LIMIT = 40;
      // true = coast, false = brake
  public static final boolean DEPLOY_COAST = true;

    // Roller Motor Configs
  public static final int ROLLER_MOTOR_ID = 16; // Kraken X60 (TalonFX)

  public static final int ROLLER_STATOR_CURRENT_LIMIT = 60;
  public static final boolean ROLLER_STATOR_CURRENT_LIMIT_ENABLE = true;
  public static final int ROLLER_SUPPLY_CURRENT_LIMIT = 40;
  public static final boolean ROLLER_SUPPLY_CURRENT_LIMIT_ENABLE = true;
      // true = coast, false = brake
  public static final boolean ROLLER_COAST = true;



  // public static final int DEPLOY_LIMIT_SWITCH_DIO = 0;
  // public static final double HOMING_SPEED = -0.15;
}