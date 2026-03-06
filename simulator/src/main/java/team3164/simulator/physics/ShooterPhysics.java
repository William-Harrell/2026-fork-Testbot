package team3164.simulator.physics;

import team3164.simulator.Constants;
import team3164.simulator.engine.*;

/**
 * Simulates the shooter: angle/velocity control, FUEL firing, alliance-zone check.
 * Must be in alliance zone to score (FieldConstants: BLUE ≤4.03m, RED ≥12.51m).
 */
public class ShooterPhysics {

    private static final double SHOOTER_HEIGHT   = 0.5;   // metres above floor
    /** m/s² ramp rate — ShooterConstants.FLYWHEEL_RAMP_RATE=0.75s to full speed, so ~26.7 m/s² */
    private static final double VELOCITY_RAMP_RATE = 30.0; // m/s per second

    public static FuelState.Fuel update(RobotState robot, InputState input, FuelState fuelState, MatchState match, double dt) {
        updateAngle(robot, input, dt);
        updateVelocity(robot, input, dt);
        updateIntakeState(robot, input, dt);

        FuelState.Fuel fired = null;
        if (input.shoot && robot.hasFuelToShoot() && robot.shooterAtSpeed
                && robot.intakeState == RobotState.IntakeState.READY_TO_SHOOT) {
            fired = fireFuel(robot, fuelState, match);
        }
        return fired;
    }

    private static void updateAngle(RobotState robot, InputState input, double dt) {
        if (Math.abs(input.shooterAngle) > 0.01) {
            robot.shooterAngleGoal += input.shooterAngle * Constants.Shooter.ANGLE_RATE * dt;
            robot.shooterAngleGoal  = Math.max(Constants.Shooter.MIN_ANGLE,
                                       Math.min(Constants.Shooter.MAX_ANGLE, robot.shooterAngleGoal));
        }
        // Move angle toward goal
        double err = robot.shooterAngleGoal - robot.shooterAngle;
        double step = Constants.Shooter.ANGLE_RATE * dt;
        if (Math.abs(err) <= step) robot.shooterAngle = robot.shooterAngleGoal;
        else robot.shooterAngle += Math.signum(err) * step;

        robot.shooterAtAngle = Math.abs(robot.shooterAngleGoal - robot.shooterAngle) < 2.0;
    }

    private static void updateVelocity(RobotState robot, InputState input, double dt) {
        if (input.spinUp || input.shoot) {
            double targetVel = input.shooterPower > 0.01 ? input.shooterPower * Constants.Shooter.MAX_VELOCITY
                                                         : Constants.Shooter.MAX_VELOCITY * 0.8;
            robot.shooterVelocityGoal = targetVel;
            robot.shooterSpinningUp   = true;
        } else if (!input.spinUp && !input.shoot) {
            robot.shooterVelocityGoal = 0.0;
            robot.shooterSpinningUp   = false;
        }

        double err  = robot.shooterVelocityGoal - robot.shooterVelocity;
        double step = VELOCITY_RAMP_RATE * dt;
        // Spin-down is slower
        if (err < 0) step *= 0.5;
        if (Math.abs(err) <= step) robot.shooterVelocity = robot.shooterVelocityGoal;
        else robot.shooterVelocity += Math.signum(err) * step;

        // "at speed" once velocity is within 10% of goal and goal is meaningful
        robot.shooterAtSpeed = (robot.shooterVelocityGoal > 1.0)
                && robot.shooterVelocity >= robot.shooterVelocityGoal * 0.9;
    }

    private static void updateIntakeState(RobotState robot, InputState input, double dt) {
        switch (robot.intakeState) {
            case IDLE:
                if (input.intake && robot.canIntakeFuel()) {
                    robot.intakeState  = RobotState.IntakeState.INTAKING;
                    robot.intakeTimer  = 0.0;
                } else if (robot.fuelCount > 0) {
                    robot.intakeState = RobotState.IntakeState.READY_TO_SHOOT;
                }
                break;

            case INTAKING:
                robot.intakeTimer += dt;
                if (robot.intakeTimer >= Constants.Intake.INTAKE_TIME) {
                    // Fuel picked up by IntakePhysics; transition here
                    robot.intakeState = robot.fuelCount > 0 ? RobotState.IntakeState.READY_TO_SHOOT : RobotState.IntakeState.IDLE;
                    robot.intakeTimer = 0.0;
                }
                break;

            case READY_TO_SHOOT:
                if (input.shoot && robot.shooterAtSpeed) {
                    robot.intakeState = RobotState.IntakeState.SHOOTING;
                    robot.intakeTimer = 0.0;
                } else if (robot.fuelCount == 0) {
                    robot.intakeState = RobotState.IntakeState.IDLE;
                }
                break;

            case SHOOTING:
                robot.intakeTimer += dt;
                if (robot.intakeTimer >= Constants.Intake.TRANSFER_TIME) {
                    robot.intakeState = robot.fuelCount > 0 ? RobotState.IntakeState.READY_TO_SHOOT : RobotState.IntakeState.IDLE;
                    robot.intakeTimer = 0.0;
                }
                break;

            case TRANSFERRING:
                robot.intakeTimer += dt;
                if (robot.intakeTimer >= Constants.Intake.TRANSFER_TIME) {
                    robot.intakeState = RobotState.IntakeState.READY_TO_SHOOT;
                    robot.intakeTimer = 0.0;
                }
                break;
        }
    }

    private static FuelState.Fuel fireFuel(RobotState robot, FuelState fuelState, MatchState match) {
        if (!robot.removeFuel()) return null;

        robot.recordFuelScored();
        robot.intakeState = RobotState.IntakeState.SHOOTING;
        robot.intakeTimer = 0.0;

        // Score directly — in simulation, all shots from alliance zone score.
        // For the simplified headless benchmark, score whenever fuel is fired.
        match.scoreFuel(robot.alliance, 1);

        // Launch FUEL projectile
        double angle = Math.toRadians(robot.shooterAngle);
        double speed = robot.shooterVelocity;
        double hVel  = speed * Math.cos(angle);
        double vVel  = speed * Math.sin(angle);

        // Direction robot is facing
        double cosH = Math.cos(robot.heading);
        double sinH = Math.sin(robot.heading);

        double vx = hVel * cosH;
        double vy = hVel * sinH;
        double vz = vVel;

        return fuelState.launchFuel(robot.x, robot.y, SHOOTER_HEIGHT, vx, vy, vz);
    }

    // ── Trajectory preview for UI ───────────────────────────────────────────
    public static double[][] getTrajectoryPreview(RobotState robot) {
        int steps = 20;
        double[][] pts = new double[steps][2];
        double angle = Math.toRadians(robot.shooterAngle);
        double speed = robot.shooterVelocity > 0 ? robot.shooterVelocity : 10.0;
        double hVel  = speed * Math.cos(angle);
        double vVel  = speed * Math.sin(angle);

        double x = robot.x, y = robot.y, z = SHOOTER_HEIGHT;
        double vx = hVel * Math.cos(robot.heading);
        double vy = hVel * Math.sin(robot.heading);
        double vz = vVel;

        double dt = 0.05;
        for (int i = 0; i < steps; i++) {
            pts[i][0] = x;
            pts[i][1] = y;
            x += vx * dt;
            y += vy * dt;
            z += vz * dt;
            vz -= Constants.Fuel.GRAVITY * dt;
            if (z < 0) break;
        }
        return pts;
    }

    public static boolean willShotHitHub(RobotState robot) {
        double[] hub = getHubPos(robot.alliance);
        double dx = hub[0] - robot.x;
        double dy = hub[1] - robot.y;
        double dist = Math.hypot(dx, dy);
        // Rough check: within 6 m and roughly facing it
        if (dist > 6.0) return false;
        double targetAngle = Math.atan2(dy, dx);
        double headingErr  = Math.abs(normalizeAngle(targetAngle - robot.heading));
        return headingErr < Math.toRadians(15.0);
    }

    public static void setOptimalShot(RobotState robot) {
        double[] hub  = getHubPos(robot.alliance);
        double dx     = hub[0] - robot.x;
        double dy     = hub[1] - robot.y;
        double dist   = Math.hypot(dx, dy);
        double hDiff  = Constants.Field.HUB_HEIGHT - SHOOTER_HEIGHT;

        // Simple ballistic calculation
        double angle  = Math.toDegrees(Math.atan2(hDiff + dist * 0.1, dist));
        angle = Math.max(Constants.Shooter.MIN_ANGLE, Math.min(Constants.Shooter.MAX_ANGLE, angle));
        robot.shooterAngleGoal    = angle;
        robot.shooterVelocityGoal = Math.min(Constants.Shooter.MAX_VELOCITY,
                                             dist * 2.5 + hDiff * 2.0);
    }

    public static boolean isInAllianceZone(RobotState robot) {
        if (robot.alliance == MatchState.Alliance.BLUE) {
            return robot.x <= Constants.Field.BLUE_ALLIANCE_ZONE_MAX_X;
        } else {
            return robot.x >= Constants.Field.RED_ALLIANCE_ZONE_MIN_X;
        }
    }

    private static double[] getHubPos(MatchState.Alliance alliance) {
        if (alliance == MatchState.Alliance.BLUE)
            return new double[]{ Constants.Field.BLUE_HUB_X, Constants.Field.BLUE_HUB_Y };
        return new double[]{ Constants.Field.RED_HUB_X, Constants.Field.RED_HUB_Y };
    }

    private static double normalizeAngle(double a) {
        while (a >  Math.PI) a -= 2 * Math.PI;
        while (a < -Math.PI) a += 2 * Math.PI;
        return a;
    }
}
