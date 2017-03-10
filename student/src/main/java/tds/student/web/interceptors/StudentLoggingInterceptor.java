package tds.student.web.interceptors;

import org.opentestsystem.delivery.logging.EventLogger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

import tds.student.tdslogger.StudentEventLogger;

public class StudentLoggingInterceptor extends HandlerInterceptorAdapter {

  @Override
  public boolean preHandle(HttpServletRequest request,
                           HttpServletResponse response, Object handler) throws Exception {
    long startTime = System.currentTimeMillis();
    request.setAttribute("startTime", startTime);

    StudentEventLogger.info(StudentEventLogger.APP, request.getPathInfo(),
      EventLogger.Checkpoint.ENTER.name(), request.getSession().getId(), "StudentLoggingInterceptor", null);
    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                         ModelAndView modelAndView) throws Exception {

    long elapsed = System.currentTimeMillis() - (Long) request.getAttribute("startTime");

    Map<EventLogger.IEventData, Object> fields = new HashMap<>();
    fields.put(EventLogger.EventData.ELAPSED_TIME, elapsed);
    fields.put(EventLogger.EventData.RESPONSE_CODE, response.getStatus());

    StudentEventLogger.info(StudentEventLogger.APP, request.getPathInfo(),
      EventLogger.Checkpoint.EXIT.name(), request.getSession().getId(), "StudentLoggingInterceptor", fields);
  }

}
