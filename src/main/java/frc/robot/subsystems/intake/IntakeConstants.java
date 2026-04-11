package frc.robot.subsystems.intake;

public final class IntakeConstants {
  public static final int DEPLOY_MOTOR_ID = 15; // Vortex (SparkFlex)
  public static final int ROLLER_MOTOR_ID = 16; // Kraken X60 (TalonFX)
  public static final int KICKER_MOTOR_ID = 18; // SparkFlex
  
  public static final double DEPLOY_POS = 2.425;
  public static final double STOW_POS = 0.0;
  public static final double TOLERANCE = 0.05;

  public static final double DEPLOY_kP = 0.1;
  public static final double INTAKE_SPEED = -.95;
  public static final double OUTTAKE_SPEED = .7;

  // TODO: IMPLEMENT KICKER MOTOR, MAKE CONSTANTS FOR INTAKE MOTOR CURRENT LIMITS

  // public static final int DEPLOY_LIMIT_SWITCH_DIO = 0;
  // public static final double HOMING_SPEED = -0.15;
}