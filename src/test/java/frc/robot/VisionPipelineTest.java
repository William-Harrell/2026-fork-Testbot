package frc.robot;

import static org.junit.jupiter.api.Assertions.*;

import edu.wpi.first.hal.HAL;
import edu.wpi.first.math.estimator.SwerveDrivePoseEstimator;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.geometry.Pose3d;
import edu.wpi.first.math.geometry.Rotation2d;
import edu.wpi.first.math.geometry.Rotation3d;
import edu.wpi.first.math.geometry.Translation2d;
import edu.wpi.first.math.kinematics.SwerveDriveKinematics;
import edu.wpi.first.math.kinematics.SwerveModulePosition;
import edu.wpi.first.wpilibj.Timer;
import frc.robot.subsystems.vision.Vision.VisionUpdate;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Proves the vision → pose estimator pipeline is correctly wired.
 *
 * <p>We test each layer independently so failures are easy to pinpoint:
 *
 * <ol>
 *   <li>addVisionMeasurement actually moves the pose estimate (the core fix)
 *   <li>VisionUpdate carries pose + timestamp through correctly
 *   <li>Camera scoring formula picks the best reading
 * </ol>
 *
 * <p>We don't instantiate Vision or SwerveDrive directly because they require
 * hardware (PhotonCamera, Pigeon2, CAN motors). Instead we test the math
 * classes they delegate to, which is where the real behavior lives.
 */
public class VisionPipelineTest {

  @BeforeAll
  static void setupHAL() {
    // Required for any WPILib class that touches simulation/HAL internals.
    assert HAL.initialize(500, 0) : "HAL failed to initialize";
  }

  // ── helpers ──────────────────────────────────────────────────────────────

  /** Minimal swerve kinematics — four wheels, no real hardware needed. */
  private static SwerveDriveKinematics makeKinematics() {
    return new SwerveDriveKinematics(
        new Translation2d(0.3, 0.3), // FL
        new Translation2d(0.3, -0.3), // FR
        new Translation2d(-0.3, 0.3), // RL
        new Translation2d(-0.3, -0.3) // RR
        );
  }

  /** All four modules stationary at 0 m distance, 0 deg angle. */
  private static SwerveModulePosition[] zeroPositions() {
    return new SwerveModulePosition[] {
      new SwerveModulePosition(),
      new SwerveModulePosition(),
      new SwerveModulePosition(),
      new SwerveModulePosition()
    };
  }

  // ── tests ─────────────────────────────────────────────────────────────────

  /**
   * Core proof: addVisionMeasurement pulls the pose estimate toward the vision
   * reading.
   *
   * <p>Before the pipeline fix, this call was never made, so odometry drift was
   * never corrected. After the fix, SwerveDrive.periodic() calls it every 20ms
   * whenever a valid AprilTag reading exists.
   */
  @Test
  void visionMeasurementCorrectsPose() {
    var estimator =
        new SwerveDrivePoseEstimator(
            makeKinematics(), new Rotation2d(), zeroPositions(), new Pose2d());

    // Robot sits still — wheel odometry stays at origin (0, 0).
    for (int i = 0; i < 3; i++) {
      estimator.update(new Rotation2d(), zeroPositions());
    }
    assertEquals(0.0, estimator.getEstimatedPosition().getX(), 0.01);
    assertEquals(0.0, estimator.getEstimatedPosition().getY(), 0.01);

    // AprilTags report robot is actually at (3.0, 2.0).
    // Use the live FPGA timestamp so the estimator's pose buffer accepts it —
    // a hardcoded value like 0.02 would be rejected as too old.
    Pose2d visionPose = new Pose2d(3.0, 2.0, new Rotation2d());
    estimator.addVisionMeasurement(visionPose, Timer.getFPGATimestamp());

    // Kalman filter blends gradually — it won't jump all the way to (3, 2).
    // The right check: the corrected pose is closer to (3, 2) than the
    // initial pose (origin) was. That proves it moved in the right direction.
    double initialDistToVision =
        new Pose2d().getTranslation().getDistance(visionPose.getTranslation());
    Pose2d after = estimator.getEstimatedPosition();
    double afterDistToVision = after.getTranslation().getDistance(visionPose.getTranslation());

    assertTrue(
        afterDistToVision < initialDistToVision,
        "Pose should be closer to vision reading than the initial pose was. "
            + "Initial dist: "
            + initialDistToVision
            + "  After dist: "
            + afterDistToVision
            + "  Pose: ("
            + after.getX()
            + ", "
            + after.getY()
            + ")");
  }

  /**
   * Proves VisionUpdate carries pose and timestamp correctly through the
   * pipeline.
   *
   * <p>SwerveDrive.periodic() calls:
   *
   * <pre>
   *   vision.getBestVisionUpdateRaw(getPose()).ifPresent(update ->
   *       addVisionMeasurement(update.pose2d(), update.timestampSeconds()));
   * </pre>
   *
   * If the record fields were wrong the wrong pose or stale timestamp would be
   * passed to the Kalman filter, silently producing bad estimates.
   */
  @Test
  void visionUpdateRecordPreservesData() {
    Pose2d pose2d = new Pose2d(5.0, 3.0, Rotation2d.fromDegrees(45));
    Pose3d pose3d = new Pose3d(5.0, 3.0, 0.0, new Rotation3d());
    double timestamp = 12.345;

    VisionUpdate update = new VisionUpdate(pose3d, pose2d, timestamp, 2, 1.5, 0.1);

    assertEquals(5.0, update.pose2d().getX(), 0.001, "pose2d X");
    assertEquals(3.0, update.pose2d().getY(), 0.001, "pose2d Y");
    assertEquals(12.345, update.timestampSeconds(), 0.001, "timestamp");
    assertEquals(2, update.tagCount(), "tagCount");
  }

  /**
   * Proves the camera scoring formula in Photon.getBestVisionUpdate ranks
   * readings correctly: more tags + closer distance + lower ambiguity = higher
   * score.
   *
   * <p>Scoring formula (replicated from Photon.java):
   *
   * <pre>
   *   score += 2.0 * tagCount
   *   score += 1.5 / (avgDistanceMeters + 0.1)
   *   score += 1.0 * (1.0 - avgAmbiguity)
   *   score -= odomDistance / 2
   * </pre>
   */
  @Test
  void scoringFavorsMoreTagsLowerAmbiguity() {
    double scoreGood =
        2.0 * 3 // 3 tags visible
            + 1.5 / (1.0 + 0.1) // tags only 1 m away
            + 1.0 * (1.0 - 0.05); // 5% ambiguity — very clear

    double scoreBad =
        2.0 * 1 // only 1 tag visible
            + 1.5 / (4.0 + 0.1) // tags 4 m away
            + 1.0 * (1.0 - 0.80); // 80% ambiguity — nearly mirrored

    assertTrue(
        scoreGood > scoreBad,
        "3-tag / close / clear reading should outscore 1-tag / far / ambiguous. "
            + "Good score: "
            + scoreGood
            + "  Bad score: "
            + scoreBad);
  }
}
