package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Transform3d;
import java.util.List;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;

// chiikawa
public class Config {
  public record CameraConfig(String name, Transform3d robotToCamera, PoseStrategy strategy) {}

  public static final List<CameraConfig> cameraConfigs =
      List.of(
          new CameraConfig(
              "front_cam",
              VisionConstants.FRONT_LEFT_CAMERA_TRANSFORM,
              PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR),
          new CameraConfig(
              "front_right",
              VisionConstants.FRONT_RIGHT_CAMERA_TRANSFORM,
              PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR),
          new CameraConfig(
              "back_left_cam",
              VisionConstants.BACK_LEFT_CAMERA_TRANSFORM,
              PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR),
          new CameraConfig(
              "back_right_cam",
              VisionConstants.BACK_RIGHT_CAMERA_TRANSFORM,
              PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR));
}
