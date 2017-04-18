package tds.student.tdslogger;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import static tds.student.tdslogger.StudentEventLogger.StudentLogEvent.APP_STARTUP;

@Component
public class ApplicationStartupLogger implements ApplicationListener<ContextRefreshedEvent> {

  private final StudentEventLogger logger;
  boolean logged = false;

  @Autowired
  public ApplicationStartupLogger(@Qualifier("integrationObjectMapper") final ObjectMapper objectMapper) {
    logger = new StudentEventLogger(objectMapper);
  }

  /**
   * We want to log an event to our centralized log service when the application server starts up.
   * This event is fired multiple times on startup, so we add logic to only log the first occurrence.
   */
  @Override
  public void onApplicationEvent(final ContextRefreshedEvent event) {
    // This event fires multiple times - we only want to log the first occurrence.
    if (!logged) {
      logged = true;
      logger.info(logger.getApp(), APP_STARTUP.name(), null, null, null);
    }
  }
}
