package tds.student.tdslogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.stereotype.Component;

import static tds.student.tdslogger.StudentEventLogger.StudentLogEvent.APP_SHUTDOWN;

@Component
public class ApplicationShutdownLogger implements ApplicationListener<ContextClosedEvent> {

  private final StudentEventLogger logger;
  boolean logged = false;

  @Autowired
  public ApplicationShutdownLogger(@Qualifier("integrationObjectMapper") final ObjectMapper objectMapper) {
    logger = new StudentEventLogger(objectMapper);
  }

  /**
   * We want to log an event to our centralized log service when the application server shuts down.
   * This event is fired multiple times on shutdown, so we add logic to only log the first occurrence.
   */
  @Override
  public void onApplicationEvent(final ContextClosedEvent event) {
    // This event fires multiple times - we only want to log the first occurrence.
    if (!logged) {
      logged = true;
      logger.trace(logger.getApp(), APP_SHUTDOWN.name(), null, null, null);
    }
  }
}
