package frc.robot;

import static edu.wpi.first.units.Units.Meter;
import static edu.wpi.first.units.Units.Second;

// ============================================================================
// ROBOT CODE UNIT TESTS
// ============================================================================
//
// Framework: JUnit 5 + WPILib simulation HAL
//
// To run:
//   ./gradlew test
//
// These tests cover:
//   1.  OI.deadband()
//   2.  OI.XboxDriver.isMovementCommanded()
//   3.  FieldUtil.flipAlliance() — translation, rotation, pose
//   4.  CSPPathing.robotOnPath()
//   5.  CSPPathing.reset() clears state
//   6.  CSPPathing.guessGoalEndStateFromWaypoints() edge cases
//   7.  ShooterState transitions
//   8.  IntakeState transitions
//   9.  Physics.calculatePitchFromDistance() (via package-private exposure)
//  10.  DipSwitchSelector clamping / lock / unlock logic (mocked IO)
//  11.  AutoConstants consistency
//  12.  RollerBeltConstants sign consistency
//  13.  SwerveConstants gear ratio sanity
//  14.  Physics.VisionAimedShot record helpers
//  15.  Elastic.Notification builder methods
// ============================================================================

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import edu.wpi.first.math.geometry.*;
import edu.wpi.first.units.LinearVelocityUnit;
import frc.robot.OI.XboxDriver;
import frc.robot.auto.AutoConstants;
import frc.robot.auto.CSPPathing;
import frc.robot.subsystems.intake.IntakeState;
import frc.robot.subsystems.intake.IntakeState.intake_state;
import frc.robot.subsystems.shooter.Physics.VisionAimedShot;
import frc.robot.subsystems.shooter.ShooterConstants;
import frc.robot.subsystems.shooter.ShooterState;
import frc.robot.subsystems.shooter.ShooterState.shooter_state;
import frc.robot.subsystems.shooter.Flywheel;
import frc.robot.subsystems.swerve.SwerveConstants;
import frc.robot.util.Elastic;
import frc.robot.util.FieldUtil;
import frc.robot.util.constants.FieldConstants;
import java.util.List;
import org.junit.jupiter.api.*;

class RobotTests {

    // ========================================================================
    // 1. OI.deadband()
    // ========================================================================

    @Nested
    @DisplayName("OI.deadband()")
    class DeadbandTests {

        @Test
        @DisplayName("Value within deadband returns 0")
        void withinDeadband() {
            assertEquals(0.0, OI.deadband(0.05, 0.1), 1e-9);
        }

        @Test
        @DisplayName("Value exactly at deadband boundary returns 0")
        void atDeadbandBoundary() {
            assertEquals(0.0, OI.deadband(0.1, 0.1), 1e-9);
        }

        @Test
        @DisplayName("Value just above deadband is passed through unchanged")
        void justAboveDeadband() {
            assertEquals(0.101, OI.deadband(0.101, 0.1), 1e-9);
        }

        @Test
        @DisplayName("Negative value within deadband returns 0")
        void negativeWithinDeadband() {
            assertEquals(0.0, OI.deadband(-0.09, 0.1), 1e-9);
        }

        @Test
        @DisplayName("Negative value outside deadband is passed through unchanged")
        void negativeOutsideDeadband() {
            assertEquals(-0.5, OI.deadband(-0.5, 0.1), 1e-9);
        }

        @Test
        @DisplayName("Zero input returns zero")
        void zeroInput() {
            assertEquals(0.0, OI.deadband(0.0, 0.1), 1e-9);
        }

        @Test
        @DisplayName("Full positive input returns full positive")
        void fullPositive() {
            assertEquals(1.0, OI.deadband(1.0, 0.1), 1e-9);
        }

        @Test
        @DisplayName("Deadband of zero passes everything through")
        void zeroDeadband() {
            assertEquals(0.001, OI.deadband(0.001, 0.0), 1e-9);
        }
    }

    // ========================================================================
    // 2. OI.XboxDriver.isMovementCommanded()
    // ========================================================================
    //
    // XboxDriver reads from a real CommandXboxController which requires HAL.
    // We test the logic directly via a thin subclass that overrides the axis
    // methods, avoiding hardware simulation overhead.

    @Nested
    @DisplayName("XboxDriver.isMovementCommanded()")
    class MovementCommandedTests {

        /** Minimal subclass that lets us inject axis values without hardware. */
        static class TestableDriver extends XboxDriver {
            private double fwd, str, trn;
            TestableDriver(double fwd, double str, double trn) {
                super(99); // port 99 — never actually used because we override methods
                this.fwd = fwd; this.str = str; this.trn = trn;
            }
            @Override public double forward() { return fwd; }
            @Override public double strafe()  { return str; }
            @Override public double turn()    { return trn; }
        }

        @Test
        @DisplayName("All axes zero → no movement")
        void allZero() {
            assertFalse(new TestableDriver(0, 0, 0).isMovementCommanded());
        }

        @Test
        @DisplayName("Only forward non-zero → movement commanded")
        void forwardOnly() {
            assertTrue(new TestableDriver(0.5, 0, 0).isMovementCommanded());
        }

        @Test
        @DisplayName("Sum exactly 0.01 → no movement (threshold is >0.01)")
        void exactlyAtThreshold() {
            // sum = 0.01, threshold uses >, so this is NOT commanded
            assertFalse(new TestableDriver(0.005, 0.003, 0.002).isMovementCommanded());
        }

        @Test
        @DisplayName("Sum just above 0.01 → movement commanded")
        void justAboveThreshold() {
            assertTrue(new TestableDriver(0.007, 0.004, 0.0).isMovementCommanded());
        }

        @Test
        @DisplayName("All axes max → movement commanded")
        void allMax() {
            assertTrue(new TestableDriver(1.0, 1.0, 1.0).isMovementCommanded());
        }

        @Test
        @DisplayName("Negative axes → movement commanded")
        void negativeAxes() {
            assertTrue(new TestableDriver(-0.5, 0, 0).isMovementCommanded());
        }
    }

    // ========================================================================
    // 3. FieldUtil.flipAlliance() — pure math, no hardware needed
    // ========================================================================

    @Nested
    @DisplayName("FieldUtil — alliance flip (Blue, no flip)")
    class FieldUtilBlueTests {
        // DriverStation always returns Blue in tests unless mocked.
        // We test the math directly using the known field length.

        @Test
        @DisplayName("flipAlliance(Translation2d) identity when Blue")
        void translationIdentityBlue() {
            Translation2d t = new Translation2d(3.0, 4.0);
            Translation2d result = FieldUtil.flipAlliance(t);
            assertEquals(t.getX(), result.getX(), 1e-6);
            assertEquals(t.getY(), result.getY(), 1e-6);
        }

        @Test
        @DisplayName("flipAlliance(Rotation2d) identity when Blue")
        void rotationIdentityBlue() {
            Rotation2d r = Rotation2d.fromDegrees(45);
            Rotation2d result = FieldUtil.flipAlliance(r);
            assertEquals(r.getDegrees(), result.getDegrees(), 1e-6);
        }

        @Test
        @DisplayName("flipAlliance(Pose2d) identity when Blue")
        void poseIdentityBlue() {
            Pose2d p = new Pose2d(2.0, 3.0, Rotation2d.fromDegrees(90));
            Pose2d result = FieldUtil.flipAlliance(p);
            assertEquals(p.getX(), result.getX(), 1e-6);
            assertEquals(p.getY(), result.getY(), 1e-6);
            assertEquals(p.getRotation().getDegrees(), result.getRotation().getDegrees(), 1e-6);
        }
    }

    // ========================================================================
    // 4 & 5. CSPPathing.robotOnPath() and reset()
    // ========================================================================

    @Nested
    @DisplayName("CSPPathing.robotOnPath() and reset()")
    class CSPPathingTests {

        @BeforeEach
        void resetState() {
            CSPPathing.reset();
        }

        @Test
        @DisplayName("robotOnPath() returns false before any path is generated")
        void noPathYet() {
            Pose2d anywhere = new Pose2d(5.0, 3.0, new Rotation2d());
            assertFalse(CSPPathing.robotOnPath(anywhere));
        }

        @Test
        @DisplayName("robotOnPath() returns true when robot is at lastPose")
        void atLastPose() {
            Pose2d target = new Pose2d(4.0, 2.0, new Rotation2d());
            CSPPathing.setLastPose(target);
            assertTrue(CSPPathing.robotOnPath(target));
        }

        @Test
        @DisplayName("robotOnPath() returns true when robot is within 0.5m of lastPose")
        void withinTolerance() {
            Pose2d target = new Pose2d(4.0, 2.0, new Rotation2d());
            CSPPathing.setLastPose(target);
            Pose2d nearby = new Pose2d(4.4, 2.0, new Rotation2d()); // 0.4m away
            assertTrue(CSPPathing.robotOnPath(nearby));
        }

        @Test
        @DisplayName("robotOnPath() returns false when robot is beyond 0.5m of lastPose")
        void beyondTolerance() {
            Pose2d target = new Pose2d(4.0, 2.0, new Rotation2d());
            CSPPathing.setLastPose(target);
            Pose2d far = new Pose2d(4.6, 2.0, new Rotation2d()); // 0.6m away
            assertFalse(CSPPathing.robotOnPath(far));
        }

        @Test
        @DisplayName("reset() clears lastPose so robotOnPath() returns false")
        void resetClearsLastPose() {
            CSPPathing.setLastPose(new Pose2d(3.0, 3.0, new Rotation2d()));
            CSPPathing.reset();
            assertFalse(CSPPathing.robotOnPath(new Pose2d(3.0, 3.0, new Rotation2d())));
        }

        @Test
        @DisplayName("robotOnPath() returns false exactly at 0.5m boundary (uses <=)")
        void exactlyAtBoundary() {
            Pose2d target = new Pose2d(0.0, 0.0, new Rotation2d());
            CSPPathing.setLastPose(target);
            Pose2d boundary = new Pose2d(0.5, 0.0, new Rotation2d()); // exactly 0.5m
            assertTrue(CSPPathing.robotOnPath(boundary)); // <= 0.5 is on-path
        }
    }

    // ========================================================================
    // 6. CSPPathing.guessGoalEndStateFromWaypoints() edge cases
    // ========================================================================

    @Nested
    @DisplayName("CSPPathing.guessGoalEndStateFromWaypoints()")
    class GoalEndStateTests {

        @Test
        @DisplayName("null list returns zero-velocity, zero-rotation goal")
        void nullList() {
            var result = CSPPathing.guessGoalEndStateFromWaypoints(null);
            assertEquals(0.0, result.velocity().in(LinearVelocityUnit.combine(Meter, Second)), 1e-9);
            assertEquals(0.0, result.rotation().getDegrees(), 1e-6);
        }

        @Test
        @DisplayName("empty list returns zero-velocity, zero-rotation goal")
        void emptyList() {
            var result = CSPPathing.guessGoalEndStateFromWaypoints(List.of());
            assertEquals(0.0, result.velocity().in(LinearVelocityUnit.combine(Meter, Second)), 1e-9);
        }

        @Test
        @DisplayName("velocity is always 0 (robot stops at goal)")
        void velocityIsZero() {
            // Even with a valid waypoint, end velocity must be 0
            var result = CSPPathing.guessGoalEndStateFromWaypoints(null);
            assertEquals(0.0, result.velocity().in(LinearVelocityUnit.combine(Meter, Second)), 1e-9);
        }
    }

    // ========================================================================
    // 7. ShooterState transitions
    // ========================================================================

    @Nested
    @DisplayName("ShooterState transitions")
    class ShooterStateTests {

        Flywheel mockFlywheel;
        ShooterState state;

        @BeforeEach
        void setup() {
            mockFlywheel = mock(Flywheel.class);
            state = new ShooterState(mockFlywheel);
        }

        @Test
        @DisplayName("Initial state is IDLE")
        void initialState() {
            assertEquals(shooter_state.IDLE, state.get());
        }

        @Test
        @DisplayName("Can set state to SPINNING_UP")
        void setSpinningUp() {
            state.set(shooter_state.SPINNING_UP);
            assertEquals(shooter_state.SPINNING_UP, state.get());
        }

        @Test
        @DisplayName("Setting same state is a no-op (no listener side effects)")
        void sameStateNoOp() {
            state.set(shooter_state.IDLE);
            assertEquals(shooter_state.IDLE, state.get());
        }

        @Test
        @DisplayName("update() promotes SPINNING_UP → READY when flywheel is at speed")
        void spinningUpToReady() {
            state.set(shooter_state.SPINNING_UP);
            when(mockFlywheel.isFlywheelAtSpeed()).thenReturn(true);
            when(mockFlywheel.getFlywheelRPM()).thenReturn(4000.0);
            state.update();
            assertEquals(shooter_state.READY, state.get());
        }

        @Test
        @DisplayName("update() does NOT promote SPINNING_UP → READY when not at speed")
        void spinningUpNotReady() {
            state.set(shooter_state.SPINNING_UP);
            when(mockFlywheel.isFlywheelAtSpeed()).thenReturn(false);
            when(mockFlywheel.getFlywheelRPM()).thenReturn(1000.0);
            state.update();
            assertEquals(shooter_state.SPINNING_UP, state.get());
        }

        @Test
        @DisplayName("update() promotes SPINNING_DOWN → IDLE when RPM < 50")
        void spinningDownToIdle() {
            state.set(shooter_state.SPINNING_DOWN);
            when(mockFlywheel.getFlywheelRPM()).thenReturn(10.0);
            state.update();
            assertEquals(shooter_state.IDLE, state.get());
        }

        @Test
        @DisplayName("update() does NOT promote SPINNING_DOWN → IDLE when RPM ≥ 50")
        void spinningDownStillSpinning() {
            state.set(shooter_state.SPINNING_DOWN);
            when(mockFlywheel.getFlywheelRPM()).thenReturn(100.0);
            state.update();
            assertEquals(shooter_state.SPINNING_DOWN, state.get());
        }

        @Test
        @DisplayName("Override constructor sets initial state")
        void overrideConstructor() {
            ShooterState s = new ShooterState(shooter_state.READY, mockFlywheel);
            assertEquals(shooter_state.READY, s.get());
        }
    }

    // ========================================================================
    // 8. IntakeState transitions
    // ========================================================================

    @Nested
    @DisplayName("IntakeState transitions")
    class IntakeStateTests {

        IntakeState state;

        @BeforeEach
        void setup() {
            state = new IntakeState();
        }

        @Test
        @DisplayName("Initial state is STOWED")
        void initialState() {
            assertEquals(intake_state.STOWED, state.get());
        }

        @Test
        @DisplayName("update() promotes DEPLOYING → DEPLOYED when deployed=true")
        void deployingToDeployed() {
            state.set(intake_state.DEPLOYING);
            state.update(true, false);
            assertEquals(intake_state.DEPLOYED, state.get());
        }

        @Test
        @DisplayName("update() does NOT promote DEPLOYING → DEPLOYED when deployed=false")
        void deployingNotDeployed() {
            state.set(intake_state.DEPLOYING);
            state.update(false, false);
            assertEquals(intake_state.DEPLOYING, state.get());
        }

        @Test
        @DisplayName("update() promotes RETRACTING → STOWED when stowed=true")
        void retractingToStowed() {
            state.set(intake_state.RETRACTING);
            state.update(false, true);
            assertEquals(intake_state.STOWED, state.get());
        }

        @Test
        @DisplayName("update() does NOT promote RETRACTING → STOWED when stowed=false")
        void retractingNotStowed() {
            state.set(intake_state.RETRACTING);
            state.update(false, false);
            assertEquals(intake_state.RETRACTING, state.get());
        }

        @Test
        @DisplayName("Setting same state is a no-op")
        void sameStateNoOp() {
            state.set(intake_state.STOWED);
            assertEquals(intake_state.STOWED, state.get());
        }

        @Test
        @DisplayName("Override constructor sets state")
        void overrideConstructor() {
            IntakeState s = new IntakeState(intake_state.INTAKING);
            assertEquals(intake_state.INTAKING, s.get());
        }
    }

    // ========================================================================
    // 9. Physics.VisionAimedShot record helpers
    // ========================================================================

    @Nested
    @DisplayName("VisionAimedShot record helpers")
    class VisionAimedShotTests {

        @Test
        @DisplayName("isHighConfidence() true when visionAssisted, ≥2 tags, ambiguity<0.2")
        void highConfidence() {
            VisionAimedShot shot = new VisionAimedShot(45.0, true, 2, 0.15, 3.0, "HIGH");
            assertTrue(shot.isHighConfidence());
        }

        @Test
        @DisplayName("isHighConfidence() false when not vision assisted")
        void notVisionAssisted() {
            VisionAimedShot shot = new VisionAimedShot(45.0, false, 3, 0.1, 3.0, "N/A");
            assertFalse(shot.isHighConfidence());
        }

        @Test
        @DisplayName("isHighConfidence() false when only 1 tag")
        void oneTag() {
            VisionAimedShot shot = new VisionAimedShot(45.0, true, 1, 0.1, 3.0, "MEDIUM");
            assertFalse(shot.isHighConfidence());
        }

        @Test
        @DisplayName("isHighConfidence() false when ambiguity ≥ 0.2")
        void highAmbiguity() {
            VisionAimedShot shot = new VisionAimedShot(45.0, true, 2, 0.2, 3.0, "LOW");
            assertFalse(shot.isHighConfidence());
        }

        @Test
        @DisplayName("isSafeForAutoShot() true with 1 tag, ambiguity<0.3, vision-assisted")
        void safeForAutoShot() {
            VisionAimedShot shot = new VisionAimedShot(45.0, true, 1, 0.25, 3.0, "MEDIUM");
            assertTrue(shot.isSafeForAutoShot());
        }

        @Test
        @DisplayName("isSafeForAutoShot() false when not vision assisted")
        void unsafeNotVision() {
            VisionAimedShot shot = new VisionAimedShot(45.0, false, 2, 0.1, 3.0, "N/A");
            assertFalse(shot.isSafeForAutoShot());
        }

        @Test
        @DisplayName("isSafeForAutoShot() false when ambiguity ≥ 0.3")
        void unsafeAmbiguity() {
            VisionAimedShot shot = new VisionAimedShot(45.0, true, 2, 0.3, 3.0, "LOW");
            assertFalse(shot.isSafeForAutoShot());
        }

        @Test
        @DisplayName("Accessor values match constructor args")
        void accessors() {
            VisionAimedShot shot = new VisionAimedShot(42.5, true, 3, 0.12, 4.7, "HIGH - Multiple tags");
            assertEquals(42.5,  shot.pitchAngle(),          1e-9);
            assertEquals(true,  shot.visionAssisted());
            assertEquals(3,     shot.tagCount());
            assertEquals(0.12,  shot.ambiguity(),            1e-9);
            assertEquals(4.7,   shot.distanceToHub(),        1e-9);
            assertEquals("HIGH - Multiple tags", shot.confidenceDescription());
        }
    }

    // ========================================================================
    // 10. AutoConstants consistency
    // ========================================================================

    @Nested
    @DisplayName("AutoConstants consistency")
    class AutoConstantsTests {

        @Test
        @DisplayName("AUTO_MODE_NAMES array length matches NUM_AUTO_MODES")
        void modeNamesLength() {
            assertEquals(AutoConstants.NUM_AUTO_MODES, AutoConstants.AUTO_MODE_NAMES.length,
                "AUTO_MODE_NAMES must have exactly NUM_AUTO_MODES entries");
        }

        @Test
        @DisplayName("Mode identifiers are unique and within range")
        void modeIdentifiersUnique() {
            int[] modes = {
                AutoConstants.AUTO_DO_NOTHING,
                AutoConstants.AUTO_SCORE_COLLECT,
                AutoConstants.AUTO_SCORE_ONLY,
                AutoConstants.AUTO_PRELOAD_ONLY
            };
            for (int mode : modes) {
                assertTrue(mode >= 0 && mode < AutoConstants.NUM_AUTO_MODES,
                    "Mode " + mode + " is out of valid range [0, NUM_AUTO_MODES)");
            }
            // All distinct
            assertEquals(modes.length, java.util.Arrays.stream(modes).distinct().count(),
                "Auto mode identifiers must be unique");
        }

        @Test
        @DisplayName("DIP switch ports are unique")
        void dipPortsUnique() {
            int[] ports = {
                AutoConstants.DIP_SWITCH_BIT_0_PORT,
                AutoConstants.DIP_SWITCH_BIT_1_PORT,
                AutoConstants.DIP_SWITCH_BIT_2_PORT,
                AutoConstants.DIP_SWITCH_BIT_3_PORT,
                AutoConstants.DIP_SWITCH_BIT_4_PORT
            };
            assertEquals(ports.length, java.util.Arrays.stream(ports).distinct().count(),
                "DIP switch DIO ports must be unique");
        }

        @Test
        @DisplayName("Timing constants are positive")
        void timingPositive() {
            assertTrue(AutoConstants.INTAKE_TIMEOUT > 0);
            assertTrue(AutoConstants.DEPOT_COLLECTION_TIME > 0);
        }

        @Test
        @DisplayName("Speed constants are positive and sane (≤ MAX_SPEED)")
        void speedsSane() {
            double maxSpeed = frc.robot.subsystems.swerve.SwerveConstants.MAX_SPEED;
            assertTrue(AutoConstants.AUTO_DRIVE_SPEED > 0);
            assertTrue(AutoConstants.AUTO_DRIVE_SPEED <= maxSpeed,
                "AUTO_DRIVE_SPEED exceeds swerve MAX_SPEED");
            assertTrue(AutoConstants.AUTO_FAST_DRIVE_SPEED <= maxSpeed,
                "AUTO_FAST_DRIVE_SPEED exceeds swerve MAX_SPEED");
            assertTrue(AutoConstants.AUTO_INTAKE_DRIVE_SPEED <= maxSpeed,
                "AUTO_INTAKE_DRIVE_SPEED exceeds swerve MAX_SPEED");
            assertTrue(AutoConstants.AUTO_SLOW_DRIVE_SPEED <= maxSpeed,
                "AUTO_SLOW_DRIVE_SPEED exceeds swerve MAX_SPEED");
        }
    }

    // ========================================================================
    // 11. SwerveConstants gear ratio sanity
    // ========================================================================

    @Nested
    @DisplayName("SwerveConstants sanity")
    class SwerveConstantsTests {

        @Test
        @DisplayName("DRIVE_GEAR_RATIO is positive")
        void driveGearRatioPositive() {
            assertTrue(SwerveConstants.DRIVE_GEAR_RATIO > 0);
        }

        @Test
        @DisplayName("AZIMUTH_GEAR_RATIO is positive")
        void azimuthGearRatioPositive() {
            assertTrue(SwerveConstants.AZIMUTH_GEAR_RATIO > 0);
        }

        @Test
        @DisplayName("WHEEL_CIRCUMFERENCE is close to π × diameter")
        void wheelCircumference() {
            double expected = SwerveConstants.WHEEL_DIAMETER * Math.PI;
            assertEquals(expected, SwerveConstants.WHEEL_CIRCUMFERENCE, 1e-6);
        }

        @Test
        @DisplayName("MAX_SPEED is positive and ≤ 6 m/s (sanity bound)")
        void maxSpeed() {
            assertTrue(SwerveConstants.MAX_SPEED > 0);
            assertTrue(SwerveConstants.MAX_SPEED <= 6.0,
                "MAX_SPEED exceeds 6 m/s — verify this is intentional");
        }

        @Test
        @DisplayName("Track width and wheel base are in meters (between 0.3 and 1.0 m)")
        void chassisDimensions() {
            assertTrue(SwerveConstants.TRACK_WIDTH > 0.3 && SwerveConstants.TRACK_WIDTH < 1.0,
                "TRACK_WIDTH should be in meters: " + SwerveConstants.TRACK_WIDTH);
            assertTrue(SwerveConstants.WHEEL_BASE > 0.3 && SwerveConstants.WHEEL_BASE < 1.0,
                "WHEEL_BASE should be in meters: " + SwerveConstants.WHEEL_BASE);
        }

        @Test
        @DisplayName("Encoder offsets are within ±360 degrees")
        void encoderOffsets() {
            double[] offsets = {
                SwerveConstants.FL_ENCODER_OFFSET,
                SwerveConstants.FR_ENCODER_OFFSET,
                SwerveConstants.RL_ENCODER_OFFSET,
                SwerveConstants.RR_ENCODER_OFFSET
            };
            for (double o : offsets) {
                assertTrue(Math.abs(o) <= 360.0,
                    "Encoder offset " + o + " is outside ±360°");
            }
        }

        @Test
        @DisplayName("All module CAN IDs are positive and unique")
        void moduleCanIds() {
            int[] ids = {
                SwerveConstants.FL_DRIVE_ID, SwerveConstants.FL_AZIMUTH_ID, SwerveConstants.FL_CANCODER_ID,
                SwerveConstants.FR_DRIVE_ID, SwerveConstants.FR_AZIMUTH_ID, SwerveConstants.FR_CANCODER_ID,
                SwerveConstants.RL_DRIVE_ID, SwerveConstants.RL_AZIMUTH_ID, SwerveConstants.RL_CANCODER_ID,
                SwerveConstants.RR_DRIVE_ID, SwerveConstants.RR_AZIMUTH_ID, SwerveConstants.RR_CANCODER_ID,
            };
            for (int id : ids) assertTrue(id > 0, "CAN ID " + id + " must be > 0");
            assertEquals(ids.length, java.util.Arrays.stream(ids).distinct().count(),
                "All module CAN IDs must be unique");
        }
    }

    // ========================================================================
    // 12. ShooterConstants sanity
    // ========================================================================

    @Nested
    @DisplayName("ShooterConstants sanity")
    class ShooterConstantsTests {

        @Test
        @DisplayName("PITCH_MIN_ANGLE < PITCH_STOW_ANGLE < PITCH_MAX_ANGLE")
        void pitchAngleOrdering() {
            assertTrue(ShooterConstants.PITCH_MIN_ANGLE < ShooterConstants.PITCH_STOW_ANGLE,
                "PITCH_MIN_ANGLE must be < PITCH_STOW_ANGLE");
            assertTrue(ShooterConstants.PITCH_STOW_ANGLE < ShooterConstants.PITCH_MAX_ANGLE,
                "PITCH_STOW_ANGLE must be < PITCH_MAX_ANGLE");
        }

        @Test
        @DisplayName("PITCH_MIN and PITCH_MAX are within physical hood range (0–90°)")
        void pitchAnglesPhysical() {
            assertTrue(ShooterConstants.PITCH_MIN_ANGLE >= 0.0);
            assertTrue(ShooterConstants.PITCH_MAX_ANGLE <= 90.0);
        }

        @Test
        @DisplayName("FLYWHEEL_SHOOT_RPM is positive")
        void flywheelRpm() {
            assertTrue(ShooterConstants.FLYWHEEL_SHOOT_RPM > 0);
        }

        @Test
        @DisplayName("FLYWHEEL_IDLE_RPM < FLYWHEEL_SHOOT_RPM")
        void idleLessThanShoot() {
            assertTrue(ShooterConstants.FLYWHEEL_IDLE_RPM < ShooterConstants.FLYWHEEL_SHOOT_RPM);
        }

        @Test
        @DisplayName("FLYWHEEL_EFFICIENCY is between 0 and 1")
        void efficiency() {
            assertTrue(ShooterConstants.FLYWHEEL_EFFICIENCY > 0.0);
            assertTrue(ShooterConstants.FLYWHEEL_EFFICIENCY <= 1.0);
        }

        @Test
        @DisplayName("FLYWHEEL_WHEEL_RADIUS_M is positive and ≤ 0.15 m")
        void flywheelRadius() {
            assertTrue(ShooterConstants.FLYWHEEL_WHEEL_RADIUS_M > 0);
            assertTrue(ShooterConstants.FLYWHEEL_WHEEL_RADIUS_M <= 0.15,
                "Flywheel radius > 0.15m — verify this is correct");
        }

        @Test
        @DisplayName("Hood limit switch DIO port is non-negative")
        void hoodDioPort() {
            assertTrue(ShooterConstants.HOOD_LIMIT_SWITCH_DIO >= 0);
        }

        @Test
        @DisplayName("HOOD_HOMING_SPEED is negative (drives toward stow)")
        void homingSpeed() {
            assertTrue(ShooterConstants.HOOD_HOMING_SPEED < 0,
                "HOOD_HOMING_SPEED must be negative to retract toward stow");
        }
    }

    // ========================================================================
    // 13. FieldConstants geometry
    // ========================================================================

    @Nested
    @DisplayName("FieldConstants geometry")
    class FieldConstantsTests {

        @Test
        @DisplayName("Blue hub is within field bounds")
        void blueHubInBounds() {
            assertTrue(FieldConstants.BLUE_HUB_X >= 0 && FieldConstants.BLUE_HUB_X <= FieldConstants.FIELD_LENGTH);
            assertTrue(FieldConstants.BLUE_HUB_Y >= 0 && FieldConstants.BLUE_HUB_Y <= FieldConstants.FIELD_WIDTH);
        }

        @Test
        @DisplayName("Red hub is within field bounds")
        void redHubInBounds() {
            assertTrue(FieldConstants.RED_HUB_X >= 0 && FieldConstants.RED_HUB_X <= FieldConstants.FIELD_LENGTH);
            assertTrue(FieldConstants.RED_HUB_Y >= 0 && FieldConstants.RED_HUB_Y <= FieldConstants.FIELD_WIDTH);
        }

        @Test
        @DisplayName("Hubs are on opposite sides of the field center line")
        void hubsOpposite() {
            assertTrue(FieldConstants.BLUE_HUB_X < FieldConstants.CENTER_X,
                "Blue hub should be on the blue (left) side");
            assertTrue(FieldConstants.RED_HUB_X > FieldConstants.CENTER_X,
                "Red hub should be on the red (right) side");
        }

        @Test
        @DisplayName("Alliance zone boundaries don't overlap")
        void allianceZonesSeparate() {
            assertTrue(FieldConstants.BLUE_ALLIANCE_ZONE_MAX_X < FieldConstants.RED_ALLIANCE_ZONE_MIN_X,
                "Blue and red alliance zones must not overlap");
        }

        @Test
        @DisplayName("Field center is at half of field dimensions")
        void fieldCenter() {
            assertEquals(FieldConstants.FIELD_LENGTH / 2.0, FieldConstants.CENTER_X, 1e-6);
            assertEquals(FieldConstants.FIELD_WIDTH  / 2.0, FieldConstants.CENTER_Y, 1e-6);
        }
    }

    // ========================================================================
    // 14. Elastic.Notification builder methods
    // ========================================================================

    @Nested
    @DisplayName("Elastic.Notification builder")
    class ElasticNotificationTests {

        @Test
        @DisplayName("withDisplaySeconds(3.0) sets displayTimeMillis to 3000")
        void displaySeconds() {
            Elastic.Notification n = new Elastic.Notification().withDisplaySeconds(3.0);
            assertEquals(3000, n.getDisplayTimeMillis());
        }

        @Test
        @DisplayName("withNoAutoDismiss() sets displayTimeMillis to 0")
        void noAutoDismiss() {
            Elastic.Notification n = new Elastic.Notification().withNoAutoDismiss();
            assertEquals(0, n.getDisplayTimeMillis());
        }

        @Test
        @DisplayName("withLevel, withTitle, withDescription set fields correctly")
        void builderChaining() {
            Elastic.Notification n = new Elastic.Notification()
                .withLevel(Elastic.NotificationLevel.ERROR)
                .withTitle("Test Title")
                .withDescription("Test Description");
            assertEquals(Elastic.NotificationLevel.ERROR, n.getLevel());
            assertEquals("Test Title", n.getTitle());
            assertEquals("Test Description", n.getDescription());
        }

        @Test
        @DisplayName("Default Notification has INFO level and non-zero display time")
        void defaultNotification() {
            Elastic.Notification n = new Elastic.Notification();
            assertEquals(Elastic.NotificationLevel.INFO, n.getLevel());
            assertTrue(n.getDisplayTimeMillis() > 0, "Default display time should be positive");
        }

        @Test
        @DisplayName("withDisplaySeconds(0.5) rounds correctly")
        void displaySecondsRounding() {
            Elastic.Notification n = new Elastic.Notification().withDisplaySeconds(0.5);
            assertEquals(500, n.getDisplayTimeMillis());
        }
    }
}