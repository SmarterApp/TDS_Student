/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/
package tds.student.performance.services;

import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestSession;

import java.util.Date;
import java.util.UUID;

public interface DbLatencyService extends tds.dll.common.performance.services.DbLatencyService {

    void logLatency(String procname, Date startTime, Long userKey, Integer n, TestOpportunity testOpportunity, String comment);
    void logLatency(String procname, Date startTime, Long userKey, Integer n, TestSession testSession, String comment);

    void logLatency(String procname, Date startTime, Long userKey, TestOpportunity testOpportunity, String comment);
    void logLatency(String procname, Date startTime, Long userKey, TestSession testSession, String comment);

    void logLatency(String procname, Date startTime, Long userKey, TestOpportunity testOpportunity);
    void logLatency(String procname, Date startTime, Long userKey, TestSession testSession);

    void logLatency(String procname, Date startTime, Long userKey, Integer n, UUID testoppKey, UUID sessionKey, String clientName, String comment);
}
