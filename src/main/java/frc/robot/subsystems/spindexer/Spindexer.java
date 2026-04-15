package frc.robot.subsystems.spindexer;

import com.revrobotics.PersistMode;
import com.revrobotics.ResetMode;
import com.revrobotics.spark.FeedbackSensor;
import com.revrobotics.spark.SparkFlex;
import com.revrobotics.spark.SparkLowLevel.MotorType;
import com.revrobotics.spark.config.SparkBaseConfig.IdleMode;
import com.revrobotics.spark.config.SparkFlexConfig;

import edu.wpi.first.wpilibj2.command.SubsystemBase;
import frc.robot.subsystems.intake.IntakeConstants;
import frc.robot.subsystems.spindexer.SpindexerState.spindexer_state;

public class Spindexer extends SubsystemBase{
    private final SpindexerState state;
    private final Spinner spinner;

    public Spindexer() {
        state = new SpindexerState();
        spinner = new Spinner(new SparkFlex(SpindexerConstants.SPINDEXER_ID, MotorType.kBrushless));

    SparkFlexConfig config = new SparkFlexConfig();
      config.idleMode(SpindexerConstants.SPIN_COAST ? 
        IdleMode.kCoast : IdleMode.kBrake)
        .smartCurrentLimit(
          SpindexerConstants.SPIN_STATOR_CURRENT_LIMIT, 
          SpindexerConstants.SPIN_SUPPLY_CURRENT_LIMIT);

        spinner.configure(config, ResetMode.kNoResetSafeParameters, PersistMode.kNoPersistParameters);
        spinner.

    }

    public void startFeed() {
        spinner.setRPM(SpindexerConstants.FEED_RPM);
        state.set(spindexer_state.SPEEDING);
    }

    public void stopFeed() {
        spinner.setRPM(SpindexerConstants.OFFRPM);
        state.set(spindexer_state.SLOWING);
    }

    @Override
    public void periodic() {
        boolean atSpeed = spinner.isAtSpeed();
        var cState = state.get();

        if (atSpeed && !(cState == spindexer_state.IDLE || cState == spindexer_state.FEEDING)) {
            if (cState == spindexer_state.SLOWING) {
                state.set(spindexer_state.IDLE);
            } else {
                state.set(spindexer_state.FEEDING);
            }
        }
    }

    public Spinner getS() {
        return spinner;
    }

    public spindexer_state getState() {
        return state.get();
    }
}