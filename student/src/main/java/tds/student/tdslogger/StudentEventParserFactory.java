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


public class StudentEventParserFactory extends EventParserFactory {

  private static Map<String, Class<? extends EventParser>> classMap = new HashMap<>();

  static {
    classMap.put("/TestShell.axd/updateResponses", UpdateResponsesParser.class);
  }

  @Override
  protected Map<String, Class<? extends EventParser>> getEventParserClassMap() {
    return classMap;
  }

  public static class UpdateResponsesParser extends EventParser {
    @Override
    public Optional<EventInfo> parsePreHandle(final HttpServletRequest request, Object handler, EventLogger logger) {
      String studentId = "TODO: fake Student ID enter";//request.getParameter("studentID");
      final Map<EventLogger.EventData, Object> fields = getEventDataFields(request);
      fields.put(StudentEventData.STUDENT_ID, studentId);
      return Optional.of(EventInfo.create(request.getPathInfo(), fields));
    }

    @Override
    public Optional<EventInfo> parsePostHandle(HttpServletRequest request, HttpServletResponse response,
                                               Object handler, EventLogger logger) {
      String studentId = "TODO: fake Student ID exit";//request.getParameter("studentID");
      final Map<EventLogger.EventData, Object> fields = getEventDataFields(request);
      fields.put(StudentEventData.STUDENT_ID, studentId);
      return Optional.of(EventInfo.create(request.getPathInfo(), fields));
    }
  }
}
