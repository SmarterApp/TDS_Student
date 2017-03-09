package tds.student.tdslogger;


import org.opentestsystem.delivery.logging.EventLogger;

/**
 * Logs events to logstash for tracking application events.
 */
public class StudentEventLogger extends EventLogger {

  public static String APP = "student";

  public static String getApp() {
    return APP;
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
  }

  public enum StudentEventData implements IEventData {
    ACCOMMODATIONS,
    ASSESSMENTS,
    EXAMS,
    QUESTIONS,
    STUDENT_ID,
  }
}

