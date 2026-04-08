package frc.robot.subsystems.swerve;

import java.util.HashMap;

public class Config {
    protected boolean field_relative = false;
    protected boolean open_loop = false;

    public Config() {
    }

    // Override
    public Config(HashMap<String, Object> map) {
        map.forEach(
                (k, value) -> {
                    switch (k) {
                        case "field_relative":
                            field_relative = (value instanceof Boolean) ? (Boolean) value : field_relative;
                            break;

                        case "open_loop":
                            open_loop = (value instanceof Boolean) ? (Boolean) value : open_loop;
                            break;

                        // Fallback
                        default:
                            System.out.println("SwerveConfig: " + k + " DNE");
                            break;
                    }
                });
    }
}
