package tds.student.tdslogger;

import com.google.common.base.Optional;
import org.opentestsystem.delivery.logging.EventInfo;
import org.opentestsystem.delivery.logging.EventLogger;
import org.opentestsystem.delivery.logging.EventParser;
import org.opentestsystem.delivery.logging.EventParserFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import tds.student.tdslogger.StudentEventLogger.StudentEventData;
import tds.student.tdslogger.StudentEventLogger.StudentLogEvent;


public class StudentEventParserFactory extends EventParserFactory {

  private static Map<String, Class<? extends EventParser>> classMap = new HashMap<>();
  private static Map<String, EventLogger.LogEvent> eventMap = new HashMap<>();

  static {
    classMap.put("/MasterShell.axd/checkApproval", CheckApprovalParser.class);
    classMap.put("/MasterShell.axd/loginStudent", LoginParser.class);
    classMap.put("*", DefaultParser.class);

    eventMap.put("/MasterShell.axd/checkApproval", StudentLogEvent.APPROVAL_CHECK);
    eventMap.put("/MasterShell.axd/loginStudent", StudentLogEvent.STUDENT_LOGIN);
    eventMap.put("/MasterShell.axd/pauseTest", StudentLogEvent.PAUSE_EXAM);
  }

  @Override
  protected Map<String, Class<? extends EventParser>> getEventParserClassMap() {
    return classMap;
  }

  private static String getEventName(HttpServletRequest request) {
    String eventName = request.getPathInfo();
    if (eventMap.containsKey(request.getPathInfo())) {
      eventName = eventMap.get(request.getPathInfo()).name();
    }
    return eventName;
  }


  public static class DefaultParser extends EventParser {
    @Override
    public Optional<EventInfo> parsePreHandle(final HttpServletRequest request, EventLogger logger) {
      final Map<EventLogger.EventData, Object> fields = getEventDataFields(request);
      return Optional.of(EventInfo.create(getEventName(request), fields));
    }

    @Override
    public Optional<EventInfo> parsePostHandle(HttpServletRequest request, HttpServletResponse response,
                                               EventLogger logger) {
      final Map<EventLogger.EventData, Object> fields = getEventDataFields(request);
      return Optional.of(EventInfo.create(getEventName(request), fields));
    }
  }


  public static class LoginParser extends DefaultParser {
    @Override
    public Optional<EventInfo> parsePostHandle(HttpServletRequest request, HttpServletResponse response,
                                               EventLogger logger) {
      final Map<EventLogger.EventData, Object> fields = getEventDataFields(request);
      fields.put(EventLogger.BaseEventData.RESULT, "success".equals(logger.getField("login_status")));
      return Optional.of(EventInfo.create(getEventName(request), fields));
    }
  }


  public static class CheckApprovalParser extends EventParser {
    @Override
    public Optional<EventInfo> parsePreHandle(final HttpServletRequest request, EventLogger logger) {
      String studentId = "TODO: fake Student ID enter";//request.getParameter("studentID");
      final Map<EventLogger.EventData, Object> fields = getEventDataFields(request);
      fields.put(StudentEventData.STUDENT_ID, studentId);
      return Optional.of(EventInfo.create(getEventName(request), fields));
    }

    @Override
    public Optional<EventInfo> parsePostHandle(HttpServletRequest request, HttpServletResponse response,
                                               EventLogger logger) {
      String studentId = "TODO: fake Student ID exit";//request.getParameter("studentID");
      final Map<EventLogger.EventData, Object> fields = getEventDataFields(request);
      fields.put(StudentEventData.STUDENT_ID, studentId);
      return Optional.of(EventInfo.create(getEventName(request), fields));
    }
  }

}
