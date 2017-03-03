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

    public static void info(final LogEvent logEvent, final String sessionId, String message) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(EventData.SESSION_ID.name().toLowerCase(), sessionId);
        EventLogger.log(APP, String.format("%s: %s", logEvent.name(), message), unmodifiableMap(fields));
    }

    public static void info(final LogEvent logEvent, final String sessionId, String message, Map<EventData, Object> data) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(EventData.SESSION_ID.name().toLowerCase(), sessionId);
        putAllEventData(fields, data);
        EventLogger.log(APP, String.format("%s: %s", logEvent.name(), message), unmodifiableMap(fields));
    }

    public static void error(final LogEvent logEvent, final String sessionId, String message, final Exception e) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(EventData.SESSION_ID.name().toLowerCase(), sessionId);
        EventLogger.error(APP, String.format("%s: %s", logEvent.name(), message), unmodifiableMap(fields), e);
    }

    public static void error(final LogEvent logEvent, final String sessionId, String message, Map<EventData, Object> data, final Exception e) {
        Map<String, Object> fields = new HashMap<>();
        fields.put(EventData.SESSION_ID.name().toLowerCase(), sessionId);
        putAllEventData(fields, data);
        EventLogger.error(APP, String.format("%s: %s", logEvent.name(), message), unmodifiableMap(fields), e);
    }

    private static void putAllEventData(Map<String, Object> fields, Map<EventData, Object> eventData) {
        for (Map.Entry<EventData, Object> entry : eventData.entrySet()) {
            fields.put(entry.getKey().name().toLowerCase(), entry.getValue());
        }
    }

}

