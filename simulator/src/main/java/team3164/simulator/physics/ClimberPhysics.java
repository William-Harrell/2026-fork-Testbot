package team3164.simulator.physics;

import team3164.simulator.Constants;
import team3164.simulator.engine.InputState;
import team3164.simulator.engine.RobotState;

/**
 * Simulates the climber arm extending/retracting.
 */
public class ClimberPhysics {

    public static void update(RobotState robot, InputState input, double dt) {
        int requestedLevel = input.getRequestedClimbLevel();
        if (requestedLevel > 0 && !robot.climbComplete) {
            robot.climbLevel = requestedLevel;
        }

        // Compute target rung height for the requested level
        double targetHeight = robot.getTargetClimbHeight(); // 0 if no level set

        // Motion — but stop when we reach the target rung height
        if (input.climberUp && !robot.climbComplete) {
            if (targetHeight > 0 && robot.robotHeight >= targetHeight - 0.02) {
                // Close enough to target — stop climbing
                robot.climberVelocity = 0;
            } else {
                robot.climberVelocity = Math.min(
                        robot.climberVelocity + Constants.Climber.MAX_ACCELERATION * dt,
                        Constants.Climber.MAX_VELOCITY);
            }
        } else if (input.climberDown) {
            robot.climberVelocity = Math.max(
                    robot.climberVelocity - Constants.Climber.MAX_ACCELERATION * dt,
                    -Constants.Climber.MAX_VELOCITY);
        } else {
            // Coast to stop
            double decel = Constants.Climber.MAX_ACCELERATION * dt;
            if (Math.abs(robot.climberVelocity) <= decel) robot.climberVelocity = 0;
            else robot.climberVelocity -= Math.signum(robot.climberVelocity) * decel;
        }

        robot.climberPosition = Math.max(Constants.Climber.MIN_POSITION,
                Math.min(Constants.Climber.MAX_POSITION,
                         robot.climberPosition + robot.climberVelocity * dt));
        robot.isClimbing = input.climberUp || input.climberDown;

        // Update robot height proportional to climber extension
        robot.robotHeight = (robot.climberPosition / Constants.Climber.MAX_POSITION)
                * Constants.Field.HIGH_RUNG_HEIGHT;
    }
}
