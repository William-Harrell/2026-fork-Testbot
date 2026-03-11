package frc.robot.util;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.wpilibj.DriverStation;
import frc.robot.util.constants.FieldConstants;

// MIGHT use this. It's probably not ready at the moment
public final class FieldUtil {
  private FieldUtil() {
    // Utility class - prevent instantiation
  }

  // ========================================================================
  // ALLIANCE FLIPPING
  // ========================================================================
  //
  // [WHY WE FLIP]
  // All our autonomous paths and positions are defined for Blue alliance.
  // When we're on Red alliance, we flip everything to the mirrored position.
  //
  // [THE MATH]
  // X: Flip across center of field -> newX = FIELD_LENGTH - oldX
  // Y: Flip across center of field -> newY = FIELD_WIDTH - oldY
  // Rotation: Flip direction -> rotate 180 deg (negate cos and sin)
  //
  // ========================================================================

  /**
   * Flip a translation (X, Y position) for Red alliance.
   *
   * <p>Convention used in this codebase: only X is mirrored (reflected across the vertical
   * center line). Y is left unchanged. This matches how AutoRoutines and SwerveDrive compute
   * Red-alliance positions. A full 180° flip (mirroring both X and Y) would be correct for a
   * rotation, but this field uses a simple X-mirror convention — do not flip Y here.
   *
   * @param translation The position (Blue alliance origin)
   * @return The flipped position if Red, original if Blue
   */
  public static Translation2d flipAlliance(Translation2d translation) {
    boolean shouldFlip =
        DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue)
            == DriverStation.Alliance.Red;

    if (shouldFlip) {
      // Mirror X across the field center line; Y is unchanged.
      return new Translation2d(
          FieldConstants.FIELD_LENGTH - translation.getX(),
          translation.getY());
    }
    return translation;
  }

  /**
   * Flip a rotation for Red alliance.
   *
   * <p>[THE MATH] To rotate 180 deg, we negate both cos and sin components. This effectively points
   * the robot the opposite direction.
   *
   * @param rotation The rotation (Blue alliance reference)
   * @return The flipped rotation if Red, original if Blue
   */
  public static Rotation2d flipAlliance(Rotation2d rotation) {
    boolean shouldFlip =
        DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue)
            == DriverStation.Alliance.Red;

    if (shouldFlip) {
      // Rotate 180 deg by negating cos and sin
      return new Rotation2d(-rotation.getCos(), -rotation.getSin());
    }
    return rotation;
  }

  /**
   * Flip a complete pose (position + rotation) for Red alliance.
   * Only X is mirrored; Y is left unchanged. Rotation is flipped 180°.
   *
   * @param pose The pose (Blue alliance reference)
   * @return The flipped pose if Red, original if Blue
   */
  public static Pose2d flipAlliance(Pose2d pose) {
    boolean shouldFlip =
        DriverStation.getAlliance().orElse(DriverStation.Alliance.Blue)
            == DriverStation.Alliance.Red;

    if (shouldFlip) {
      return new Pose2d(
          new Translation2d(
              FieldConstants.FIELD_LENGTH - pose.getX(),
              pose.getY()),  // Y unchanged — matches X-mirror convention
          new Rotation2d(-pose.getRotation().getCos(), -pose.getRotation().getSin()));
    }
    return pose;
  }
}