package frc.robot.subsystems.turret;

import com.ctre.phoenix6.hardware.TalonFX;
import com.revrobotics.AbsoluteEncoder;

public class Pitch {
    private final TalonFX motor;
    private final AbsoluteEncoder encoder;

    public Pitch(TalonFX motor1, AbsoluteEncoder encoder1) {
        motor = motor1;
        encoder = encoder1;
    }

    public double get() {
        return encoder.getPosition();
    }

    public void turnTo(double goal) {

    }

    public void reset() {

    }
}
