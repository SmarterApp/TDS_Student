package tds.student.tdslogger;

import com.google.common.base.Optional;
import org.opentestsystem.delivery.logging.EventInfo;
import org.opentestsystem.delivery.logging.EventLogger.IEventData;
import org.opentestsystem.delivery.logging.EventParser;
import org.opentestsystem.delivery.logging.EventParserFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class StudentEventParserFactory extends EventParserFactory {

  private static Map<String, Class<? extends EventParser>> classMap = new HashMap<>();

  static {
    classMap.put("/MasterShell.axd/loginStudent", StudentLoginParser.class);
    classMap.put("/MasterShell.axd/pauseTest", PauseExamParser.class);
  }

  @Override
  protected Map<String, Class<? extends EventParser>> getEventParserClassMap() {
    return classMap;
  }

  public static class StudentLoginParser extends EventParser {
    @Override
    public Optional<EventInfo> parsePreHandle(final HttpServletRequest request) {
      String studentId = "TODO: fake Student ID at login enter";//request.getParameter("studentID");
      final Map<IEventData, Object> fields = getEventDataFields(request);
      fields.put(StudentEventLogger.StudentEventData.STUDENT_ID, studentId);
      return Optional.of(EventInfo.create(StudentEventLogger.StudentLogEvent.STUDENT_LOGIN.name(), fields));
    }

    @Override
    public Optional<EventInfo> parsePostHandle(HttpServletRequest request, HttpServletResponse response) {
      String studentId = "TODO: fake Student ID at login exit";//request.getParameter("studentID");
      final Map<IEventData, Object> fields = getEventDataFields(request);
      fields.put(StudentEventLogger.StudentEventData.STUDENT_ID, studentId);
      return Optional.of(EventInfo.create(StudentEventLogger.StudentLogEvent.STUDENT_LOGIN.name(), fields));
    }
  }

  public static class PauseExamParser extends EventParser {
    @Override
    public Optional<EventInfo> parsePreHandle(final HttpServletRequest request) {
      String studentId = "TODO: fake Student ID pausing test enter";//request.getParameter("studentID");
      final Map<IEventData, Object> fields = getEventDataFields(request);
      fields.put(StudentEventLogger.StudentEventData.STUDENT_ID, studentId);
      return Optional.of(EventInfo.create(StudentEventLogger.StudentLogEvent.PAUSE_EXAM.name(), fields));
    }

    @Override
    public Optional<EventInfo> parsePostHandle(HttpServletRequest request, HttpServletResponse response) {
      String studentId = "TODO: fake Student ID pausing test exit";//request.getParameter("studentID");
      final Map<IEventData, Object> fields = getEventDataFields(request);
      fields.put(StudentEventLogger.StudentEventData.STUDENT_ID, studentId);
      return Optional.of(EventInfo.create(StudentEventLogger.StudentLogEvent.PAUSE_EXAM.name(), fields));
    }
  }
}
