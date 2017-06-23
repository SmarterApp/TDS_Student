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
import org.opentestsystem.delivery.logging.EventLoggerBase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tds.student.services.data.ItemResponse;
import tds.student.services.data.NextItemGroupResult;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.web.handlers.TestResponseReader;

@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class StudentEventLogger extends EventLoggerBase {

  public String getApp() {
    return "student";
  }

  public enum StudentLogEvent implements LogEvent {
    APP_STARTUP,
    APP_SHUTDOWN,
    DETERMINE_NEXT_QUESTION,
  }

  public enum StudentEventData implements EventData {
    STUDENT_ID,
  }

  public StudentEventLogger() {
    super(new ObjectMapper());
  }

  @Autowired
  public StudentEventLogger(@Qualifier("integrationObjectMapper") final ObjectMapper objectMapper) {
    super(objectMapper);
  }

  public void putField(String key, TestResponseReader responseReader) {
    List<Map<String, String>> responses = new ArrayList<>();
    for (ItemResponseUpdate responseUpdate : responseReader.getResponses()) {
      Map<String, String> responseItems = new HashMap<>();
      responseItems.put("segment_id", responseUpdate.getSegmentID());
      responseItems.put("item_id", responseUpdate.getItemID());
      responseItems.put("value", responseUpdate.getValue());
      responseItems.put("valid", String.valueOf(responseUpdate.getIsValid()));
      responses.add(responseItems);
    }
    putField(key, responses);
  }

  @SuppressWarnings(value = "unchecked")
  public void putField(String key, NextItemGroupResult nextItemGroup) {
    List<Map<String, String>> prefetchedItemResponses;
    if (getField(key) == null) {
      prefetchedItemResponses = new ArrayList<>();
      putField(key, prefetchedItemResponses);
    } else {
      prefetchedItemResponses = (List<Map<String, String>>) getField("prefetched_item_responses");
    }
    for (ItemResponse itemResponse : nextItemGroup.getPage()) {
      Map<String, String> responseItems = new HashMap<>();
      responseItems.put("segment_id", itemResponse.getSegmentID());
      responseItems.put("item_id", itemResponse.getItemID());
      prefetchedItemResponses.add(responseItems);
    }
  }
}

