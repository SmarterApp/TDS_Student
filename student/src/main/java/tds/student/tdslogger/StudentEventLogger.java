package tds.student.tdslogger;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.opentestsystem.delivery.logging.EventLogger;
import org.opentestsystem.delivery.logging.EventLoggerBase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Logs events to logstash for tracking application events.
 */
public class StudentEventLogger extends EventLoggerBase {
  protected String getApp() {
    return "student";
  }

  public enum StudentLogEvent implements EventLogger.ILogEvent {
    ANSWER_QUESTIONS,
    DETERMINE_NEXT_QUESTION,
    DISPLAY_PAGE_CONTENTS,
    DISPLAY_PREVIOUS_PAGE,
    PAUSE_EXAM,
    READ_QUESTIONS,
    SCORE_EXAM,
    SEGMENT_TRANSITION,
    START_EXAM,
  }

  public enum StudentEventData implements EventLogger.IEventData {
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

