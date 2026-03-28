package frc.robot.subsystems.swerve;

import edu.wpi.first.math.geometry.Pose2d;
import edu.wpi.first.math.kinematics.SwerveModuleState;
import edu.wpi.first.networktables.NetworkTableInstance;
import edu.wpi.first.networktables.StructArrayPublisher;
import edu.wpi.first.networktables.StructPublisher;
import edu.wpi.first.wpilibj.smartdashboard.Field2d;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

public class Dashboard {
    private final Field2d field;
    private final StructPublisher<Pose2d> posePublisher;
    private final StructArrayPublisher<SwerveModuleState> statePublisher;

    public Dashboard() {
        field = new Field2d();
        SmartDashboard.putData("Field", field);

        var table = NetworkTableInstance.getDefault().getTable("Swerve");
        posePublisher = table.getStructTopic("Pose", Pose2d.struct).publish();
        statePublisher = table.getStructArrayTopic("ModuleStates", SwerveModuleState.struct).publish();

    }

    private void writeVisuals(Pose2d pose) {
        field.setRobotPose(pose);
    }

    private void writeText(Pose2d pose, SwerveModuleState[] states) {
        // Publish to NetworkTables for AdvantageScope
        posePublisher.set(pose);
        statePublisher.set(states);
    }

    protected void updateLogs(Pose2d pose, SwerveModuleState[] states) {
        writeText(pose, states);
        writeVisuals(pose);
    }
}
