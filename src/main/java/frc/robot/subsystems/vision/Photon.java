package frc.robot.subsystems.vision;

import edu.wpi.first.apriltag.AprilTagFieldLayout;
import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
import frc.robot.subsystems.vision.Config.CameraConfig;
import frc.robot.subsystems.vision.Vision.VisionUpdate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.photonvision.EstimatedRobotPose;
import org.photonvision.PhotonCamera;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.targeting.PhotonPipelineResult;
import org.photonvision.targeting.PhotonTrackedTarget;

// Another QoL organization thing, but this one is for the photon image handling
public class Photon {
  private final List<PhotonCamera> cameras = new ArrayList<>();
  private final List<PhotonPoseEstimator> estimators = new ArrayList<>();
  private AprilTagFieldLayout field;
  private Map<String, Optional<VisionUpdate>> updateCache = new HashMap<>();

  public Photon(Optional<AprilTagFieldLayout> layout) {
    // Initialize cameras & estimators w/ config data
    for (CameraConfig cfg : Config.cameraConfigs) {
      PhotonCamera cam = new PhotonCamera(cfg.name());
      field = layout.orElse(null); // honestly dk what to do here if we can't get the tags
      PhotonPoseEstimator estimator = new PhotonPoseEstimator(field, cfg.strategy(), cfg.robotToCamera());

      cameras.add(cam);
      estimators.add(estimator);
    }
  }

  // SOLVED C-02
  public void clearUpdateCache() {
    updateCache.clear();
  }

  /** Makes a vision update record based on the specified camera */
  private Optional<VisionUpdate> getSingleCameraUpdate(PhotonCamera camera, PhotonPoseEstimator estimator,
      Pose2d robotPose) {

    // SOLVED C-02
    Optional<VisionUpdate> temp = updateCache.get(camera.getName());

    if (temp.isPresent()) {
      return temp;
    }

    List<PhotonPipelineResult> results = camera.getAllUnreadResults();
    if (results.isEmpty()) {
      return Optional.empty();
    }

    PhotonPipelineResult result = results.get(results.size() - 1);

    /* FILTERS */
    // Is this result stale?
    double age = Timer.getFPGATimestamp() - result.getTimestampSeconds();
    if (age > VisionConstants.MAX_FRAME_AGE) {
      return Optional.empty();
    }

    // Are there any tags?
    if (!result.hasTargets()) {
      return Optional.empty();
    }

    List<PhotonTrackedTarget> targets = result.getTargets();
    int tagCount = targets.size();
    double avgAmbiguity = targets.stream().mapToDouble(PhotonTrackedTarget::getPoseAmbiguity).average().orElse(1.0);

    // Can't validate tags without a field layout
    if (field == null) {
      return Optional.empty();
    }

    // Is this tag legal?
    for (PhotonTrackedTarget t : targets) {
      if (field.getTagPose(t.getFiducialId()).isEmpty()) {
        return Optional.empty();
      }
    }

    // Is this tag close enough for our liking?
    if (targets.stream().anyMatch(t -> t.getArea() < VisionConstants.MIN_AREA)) {
      return Optional.empty();
    }

    // Are there enough tags for us to make a good guess?
    if (tagCount < VisionConstants.MIN_TAG_COUNT)
      return Optional.empty();

    // Does data meet our custom, personal standards?
    if (avgAmbiguity > VisionConstants.AMBIGUITY_THRESHOLD) {
      return Optional.empty();
    }

    estimator.setReferencePose(robotPose);

    Optional<EstimatedRobotPose> estOpt = estimator.update(result);

    // Did the estimator get a result?
    if (estOpt.isEmpty()) {
      return Optional.empty();
    }

    EstimatedRobotPose est = estOpt.get();
    Pose2d pose2d = est.estimatedPose.toPose2d();

    // Did we do a crazy change from our last position?
    if (pose2d.getTranslation().getDistance(robotPose.getTranslation()) > VisionConstants.MAX_POSE_DIFFERENCE) {
      return Optional.empty();
    }

    double avgDistance = targets.stream()
        .mapToDouble(t -> t.getBestCameraToTarget().getTranslation().getNorm())
        .average()
        .orElse(VisionConstants.MAX_TAG_DISTANCE);
    if (avgDistance > VisionConstants.MAX_TAG_DISTANCE)
      return Optional.empty();

    SmartDashboard.putNumber("Vision/" + camera.getName() + "/TagCount", tagCount);
    SmartDashboard.putNumber("Vision/" + camera.getName() + "/AvgAmbiguity", avgAmbiguity);
    SmartDashboard.putNumber("Vision/" + camera.getName() + "/AvgDistance", avgDistance);
    SmartDashboard.putNumberArray(
        "Vision/" + camera.getName() + "/Pose2d",
        new double[] { pose2d.getX(), pose2d.getY(), pose2d.getRotation().getDegrees() });

    var t2 = Optional.of(
        new VisionUpdate(
            est.estimatedPose,
            est.estimatedPose.toPose2d(),
            est.timestampSeconds,
            tagCount,
            avgDistance,
            avgAmbiguity));

    updateCache.put("String", t2);

    return t2;
  }

  /** Returns the best one based on our "custom" ranking routine */
  public Optional<VisionUpdate> getBestVisionUpdate(Pose2d robotPose) {
    VisionUpdate best = null;
    double bestScore = Double.NEGATIVE_INFINITY;

    // Rank estimations from each camera
    for (int i = 0; i < estimators.size(); i++) {
      PhotonCamera cam = cameras.get(i);
      PhotonPoseEstimator est = estimators.get(i);

      Optional<VisionUpdate> updateOpt = getSingleCameraUpdate(cam, est, robotPose);
      if (updateOpt.isEmpty()) {
        continue;
      }

      VisionUpdate update = updateOpt.get();

      double score = 0.0;
      score += 2.0 * update.tagCount(); // more tags is good
      score += 1.5 * (1.0 / (update.avgDistanceMeters() + 0.1)); // less distance is good
      score += 1.0 * (1.0 - Math.min(update.avgAmbiguity(), 1.0)); // certainty is good

      double odomDistance = update.pose2d().getTranslation().getDistance(robotPose.getTranslation());
      score -= odomDistance / 2; // Difference when compared to 'official' odometry results

      if (score > bestScore) {
        bestScore = score;
        best = update;
      }
    }

    return Optional.ofNullable(best);
  }
}
