package tds.student.tdslogger;


import org.opentestsystem.delivery.logging.EventLogger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

public class StudentEventLogger {

  public enum LogEvent {
    ANSWER_QUESTIONS,
    DETERMINE_NEXT_QUESTION,
    DISPLAY_PAGE_CONTENTS,
    DISPLAY_PREVIOUS_PAGE,
    LOGIN,
    LOGOUT,
    PAUSE_EXAM,
    READ_QUESTIONS,
    SCORE_EXAM,
    SEGMENT_TRANSITION,
    START_EXAM,
    UNKNOWN,
  }

  public enum EventData {
    ACCOMMODATIONS,
    ASSESSMENTS,
    ELAPSED_TIME,
    EXAMS,
    CHECKPOINT,
    QUESTIONS,
    SESSION_ID,
    STUDENT_ID,
    SUB_EVENT,
  }

  public enum Checkpoint {
    NONE,
    ENTER,
    EXIT,
  }

  private static final String APP = "student";
  private static Map<LogEvent, Long> eventTimes = new HashMap<>();

  public static void info(final LogEvent logEvent, final String checkpoint, final String sessionId, String message,
                          final Map<EventData, Object> data) {
    EventLogger.log(APP, formatMessage(logEvent, message), getFieldMap(logEvent, checkpoint, sessionId, data));
  }

  public static void error(final LogEvent logEvent, final String checkpoint, final String sessionId, String message,
                           final Map<EventData, Object> data, final Exception e) {
    EventLogger.error(APP, formatMessage(logEvent, message), getFieldMap(logEvent, checkpoint, sessionId, data), e);
  }

  private static void addTimerMetrics(final LogEvent event, final String checkpoint, Map<EventData, Object> fields) {
    Long eventTime = eventTimes.get(event);
    switch (checkpoint) {
      case "ENTER":
        // entry point - (re)set eventTime and emit nothing
        eventTimes.put(event, System.currentTimeMillis());
        break;
      case "EXIT":
        // exit point - remove timestamp. falls through to default to emit metric.
        eventTimes.remove(event);
        // no break;
      default:
        // non entry/exit or missing position marker - emit elapsed metric
        if (null != eventTime) {
          fields.put(EventData.ELAPSED_TIME, System.currentTimeMillis() - eventTime);
        }
        break;
    }
  }

  private static Map<String, Object> getFieldMap(final LogEvent event, final String checkpoint, final String
    sessionId, final Map<EventData, Object> data) {
    Map<EventData, Object> fields = new HashMap<>();
    fields.put(EventData.CHECKPOINT, null == checkpoint ? Checkpoint.NONE : checkpoint);
    if (null != sessionId && !sessionId.isEmpty()) {
      fields.put(EventData.SESSION_ID, sessionId);
    }
    if (null != data) {
      fields.putAll(data);
    }
    // TODO: Move this to EventLogger so all events get timed.
    addTimerMetrics(event, checkpoint, fields);
    // TODO: Move this to output step in EventLogger. For now, stay compatible.
    Map<String, Object> outputMap = new HashMap();
    for (Map.Entry<EventData, Object> entry : fields.entrySet()) {
      outputMap.put(entry.getKey().name().toLowerCase(), entry.getValue());
    }
    return unmodifiableMap(outputMap);
  }

  private static String formatMessage(final LogEvent logEvent, final String message) {
    String result = logEvent.name();
    if (null != message && !message.isEmpty()) {
      result = result + " - " + message;
    }
    return result;
  }

}

