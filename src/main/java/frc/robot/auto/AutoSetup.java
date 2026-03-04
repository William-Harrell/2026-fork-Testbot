package frc.robot.auto;

import com.pathplanner.lib.config.ModuleConfig;
import com.pathplanner.lib.config.RobotConfig;
import com.pathplanner.lib.path.PathConstraints;
import frc.robot.util.constants;

public class AutoSetup {
  public AutoSetup() {
    CSPPathing.setRobotDimensions(1 / 2);

CSPPathing.addObstacle(cornerPose, nonAdjacentCornerPose);

CSPPathing.configureConstraints(pathConstraints, robotConfig);
  }
// template pathconstraints
public PathConstraints pathConstraints = new PathConstraints(
DRIVE_MAX_VEL, DRIVE_MAX_ACC,
TURN_MAX_VEL, TURN_MAX_ACC);

public RobotConfig robotConfig = new RobotConfig(MASS, MOI,
new ModuleConfig(WHEELRADIUS, MAX_VEL, COEFFICIENT_OF_FRICTION,
driveMotor, CURRENT_LIMIT, NUM_OF_MOTORS),
moduleOffsets);

CSPPathing.configureNodes(
  CSPPathing.NavNode(idString1, nodePose1, List.of(idString2, idString3)),
  CSPPathing.NavNode(idString2, nodePose2, List.of(idString1, idString3)),
  CSPPathing.NavNode(idString3, nodePose3, List.of(idString1, idString2)),
  ...
);
}
