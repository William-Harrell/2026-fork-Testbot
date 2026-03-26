package frc.robot.subsystems.turret;

import com.revrobotics.RelativeEncoder;
import com.revrobotics.spark.SparkMax;

public class Yaw {
    private final SparkMax motor;
    private final RelativeEncoder encoder;

    public Yaw(SparkMax motor1) {
        motor = motor1;
        encoder = motor.getEncoder();
    }

    public double get() {
        return encoder.getPosition();
    }

    public void setSpeed(double goal) {
        motor.set(goal);
    }

    public void reset() {
        
    }
}
