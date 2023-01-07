import com.swrobotics.robot.subsystems.StatusLogging;

// Interface for methods that want
public interface StatusLoggable {
    public StatusLogging logger;
    public void initLogging(StatusLogging logger);
}
