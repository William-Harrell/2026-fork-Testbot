package frc.robot.subsystems.intake;

public final class IntakeConstants {
  public static final int DEPLOY_MOTOR_ID = 9;   // Vortex (SparkFlex)
  public static final int DEPLOY_MOTOR_2_ID = 14; // Vortex (SparkFlex) — assign actual CAN ID
  public static final int ROLLER_MOTOR_ID = 10;   // NEO (SparkMax)

  // Hex encoder (REV Through Bore, 8192 CPR)
  public static final int ENCODER_CPR = 8192;

  public static final double DEPLOYED_POSITION = 1.0;
  public static final double STOWED_POSITION = 0.0;
  public static final double POSITION_TOLERANCE = 0.05;

  public static final double DEPLOY_kP = 0.1;
  public static final double INTAKE_SPEED = 0.8;
  public static final double OUTTAKE_SPEED = -0.5;
}
