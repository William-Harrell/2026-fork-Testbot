package frc.robot.subsystems.vision;

import edu.wpi.first.math.geometry.Transform3d;
import frc.robot.util.constants.VisionConstants;
import java.util.List;
import org.photonvision.PhotonPoseEstimator;
import org.photonvision.PhotonPoseEstimator.PoseStrategy;

// chiikawa
public class Config {
  public record CameraConfig(String name, Transform3d robotToCamera, PoseStrategy strategy) {}

  public static final List<CameraConfig> cameraConfigs =
      List.of(
          new CameraConfig(
              "example_cam_1",
              VisionConstants.EXAMPLE_CAMERA_TRANSFORM_1,
              PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR),
          new CameraConfig(
              "example_cam_2",
              VisionConstants.EXAMPLE_CAMERA_TRANSFORM_2,
              PhotonPoseEstimator.PoseStrategy.MULTI_TAG_PNP_ON_COPROCESSOR));
}
