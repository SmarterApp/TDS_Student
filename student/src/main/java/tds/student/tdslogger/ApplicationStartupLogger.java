/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

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
      logger.trace(logger.getApp(), APP_STARTUP.name(), null, null, null);
    }
  }
}
