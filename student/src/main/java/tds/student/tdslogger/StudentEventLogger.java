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

  public enum StudentLogEvent implements ILogEvent {
    ANSWER_QUESTIONS,
    DETERMINE_NEXT_QUESTION,
    DISPLAY_PAGE_CONTENTS,
    DISPLAY_PREVIOUS_PAGE,
    PAUSE_EXAM,
    READ_QUESTIONS,
    SCORE_EXAM,
    SEGMENT_TRANSITION,
    START_EXAM,
    STUDENT_LOGIN,
    STUDENT_LOGOUT,
  }

  public enum StudentEventData implements IEventData {
    ACCOMMODATIONS,
    ASSESSMENTS,
    EXAMS,
    QUESTIONS,
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

