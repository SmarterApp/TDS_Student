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
package tds.student.performance.services.impl;

import org.springframework.stereotype.Service;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestSession;
import tds.student.performance.services.DbLatencyService;

import java.util.Date;

@Service
public class DbLatencyServiceImpl extends tds.dll.common.performance.services.impl.DbLatencyServiceImpl implements DbLatencyService {

    @Override
    public void logLatency(String procname, Date startTime, Long userKey, Integer n, TestOpportunity testOpportunity, String comment) {
        logLatency(procname, startTime, userKey, n, testOpportunity.getKey(), testOpportunity.getSessionKey(), testOpportunity.getClientName(), comment);
    }

    @Override
    public void logLatency(String procname, Date startTime, Long userKey, Integer n, TestSession testSession, String comment) {
        logLatency(procname, startTime, userKey, n, null, testSession.getKey(), testSession.getClientName(), comment);
    }

    @Override
    public void logLatency(String procname, Date startTime, Long userKey, TestOpportunity testOpportunity, String comment) {
        logLatency(procname, startTime, userKey, null, testOpportunity, comment);
    }

    @Override
    public void logLatency(String procname, Date startTime, Long userKey, TestSession testSession, String comment) {
        logLatency(procname, startTime, userKey, null, testSession, comment);
    }

    @Override
    public void logLatency(String procname, Date startTime, Long userKey, TestOpportunity testOpportunity) {
        logLatency(procname, startTime, userKey, null, testOpportunity, null);
    }

    @Override
    public void logLatency(String procname, Date startTime, Long userKey, TestSession testSession) {
        logLatency(procname, startTime, userKey, null, testSession, null);
    }
}
