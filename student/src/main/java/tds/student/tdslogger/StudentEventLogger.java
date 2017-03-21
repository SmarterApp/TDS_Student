package tds.student.tdslogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.opentestsystem.delivery.logging.EventLoggerBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class StudentEventLogger extends EventLoggerBase {

  protected String getApp() {
    return "student";
  }

  public enum StudentLogEvent implements LogEvent {
    DETERMINE_NEXT_QUESTION,
  }

  public enum StudentEventData implements EventData {
    STUDENT_ID,
  }

  public StudentEventLogger() {
    super(new ObjectMapper());
  }

  @Autowired
  public StudentEventLogger(final ObjectMapper objectMapper) {
    super(objectMapper);
  }
}

