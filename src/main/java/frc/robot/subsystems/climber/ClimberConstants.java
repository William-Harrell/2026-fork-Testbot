package frc.robot.subsystems.climber;

public final class ClimberConstants {
  // Left arm
  public static final int LEFT_LEADER_ID = 22; // TODO: set correct CAN ID
  public static final int LEFT_FOLLOWER_ID = 23; // TODO: set correct CAN ID

  // Right arm
  public static final int RIGHT_LEADER_ID = 24; // TODO: set correct CAN ID
  public static final int RIGHT_FOLLOWER_ID = 25; // TODO: set correct CAN ID

  public static final double CLIMB_SPEED = 0.8;
  public static final double LOWER_SPEED = -0.5;

  public static final int CURRENT_LIMIT = 40; // Amps

  // Hex encoder (REV Through Bore, 8192 CPR)
  public static final int ENCODER_CPR = 8192;

  public static final double RETRACTED_POSITION = 0.0; // rotations
  public static final double EXTENDED_POSITION = 50.0; // rotations — TODO: measure on real robot
  public static final double POSITION_TOLERANCE = 1.0; // rotations

  public static final double HOOD_kP = 0.1; // TODO: tune
  public static final double HOOD_kI = 0.0;
  public static final double HOOD_kD = 0.0;
}
