package tds.student.tdslogger;


import org.opentestsystem.delivery.logging.EventLogger;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.unmodifiableMap;

public class StudentEventLogger {

    private static final String APP = "student";
    private static final String EVENT_ENTRY_SUFFIX = "entry";
    public static final String SUB_EVENT = "sub_event";
    public enum LogEvent {
        LOGIN,
        LOGOUT,
        START_EXAM,
        READ_QUESTIONS,
        DISPLAY_PAGE_CONTENTS,
        DISPLAY_PREVIOUS_PAGE,
        ANSWER_QUESTIONS,
        DETERMINE_NEXT_QUESTION,
        SCORE_EXAM,
        SEGMENT_TRANSITION,
        PAUSE_EXAM,
        UNKNOWN
    }
    public enum EventData {
        SESSION_ID,
        STUDENT_ID,
        ASSESSMENTS,
        ACCOMMODATIONS,
        QUESTIONS,
        EXAMS
    }

    public static void info(final LogEvent logEvent, final String sessionId, final String message) {
        EventLogger.log(APP, formatMessage(logEvent, message), getFieldMap(sessionId, null));
    }

    public static void info(final LogEvent logEvent, final String sessionId, String message, Map<EventData, Object> data) {
        EventLogger.log(APP, formatMessage(logEvent, message), getFieldMap(sessionId, data));
    }

    public static void error(final LogEvent logEvent, final String sessionId, String message, final Exception e) {
        EventLogger.error(APP, formatMessage(logEvent, message), getFieldMap(sessionId, null), e);
    }

    public static void error(final LogEvent logEvent, final String sessionId, String message, Map<EventData, Object> data, final Exception e) {
        EventLogger.error(APP, formatMessage(logEvent, message), getFieldMap(sessionId, data), e);
    }

    private static Map<String, Object> getFieldMap(String sessionId, Map<EventData, Object> data) {
        Map<String, Object> fields = new HashMap<>();
        if(null != sessionId && !sessionId.isEmpty()) {
            fields.put(EventData.SESSION_ID.name().toLowerCase(), sessionId);
        }
        if(null == data) {
            return unmodifiableMap(fields);
        }
        for (Map.Entry<EventData, Object> entry : data.entrySet()) {
            fields.put(entry.getKey().name().toLowerCase(), entry.getValue());
        }
        return unmodifiableMap(fields);
    }

    private static String formatMessage(final LogEvent logEvent, final String message) {
        String result = logEvent.name().toLowerCase();
        if(null != message && !message.isEmpty()) {
            result = result + " - " + message;
        }
        return result;
    }

}

