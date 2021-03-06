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

import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.SingleDataResultSet;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.dll.api.ICommonDLL;
import tds.dll.common.performance.exceptions.ReturnErrorException;
import tds.dll.common.performance.utils.LegacySqlConnection;
import tds.student.performance.services.LegacyErrorHandlerService;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.UUID;

@Service
public class LegacyErrorHandlerServiceImpl implements LegacyErrorHandlerService {
    private static final Logger logger = LoggerFactory.getLogger(LegacyErrorHandlerServiceImpl.class);

    @Autowired
    ICommonDLL commonDll;

    @Autowired
    LegacySqlConnection legacySqlConnection;

    @Override
    public void logDbError(String procName, String message, Long testee, String test, Integer opportunity, UUID key) {
        try (SQLConnection connection = legacySqlConnection.get()) {
            commonDll._LogDBError_SP(connection, procName, message, testee, test, opportunity, key);
        } catch (Exception e) {
            logger.error(String.format("Error logging error for %s, %s", procName, message), e);
        }
    }

    /**
     * Wrap a call to the {@code CommonDLL._ReturnError_SP} legacy method to get back the error message information the
     * legacy code expects.  The results of {@code CommonDLL._ReturnError_SP} are contained in a
     * {@link ReturnErrorException} which is thrown to the caller.
     *
     * @param client The client name.
     * @param procName The name of the method that failed/should throw a {@link ReturnErrorException}.
     * @param appkey The details (e.g. error or exception message) on why the method call failed.
     * @param argstring The arguments that were part of the method.
     * @param oppkey The key (opportunity key, session key) of the item was being operated on.
     * @param context The method within the procName method that failed.
     * @param status The status/state of the method (e.g. "denied" or "failed").
     * @throws ReturnErrorException
     * @throws SQLException
     * @throws ReturnStatusException
     */
    @Override
    public void throwReturnErrorException(String client, String procName, String appkey, String argstring, UUID oppkey, String context, String status) throws ReturnErrorException, SQLException, ReturnStatusException {
        try (SQLConnection connection = legacySqlConnection.get()) {
            SingleDataResultSet resultSet = commonDll._ReturnError_SP(connection, client, procName, appkey, argstring, oppkey, context, status);

            Iterator<DbResultRecord> records = resultSet.getRecords();

            if (records.hasNext()) {
                DbResultRecord record = records.next();

                throw new ReturnErrorException(
                        record.<String>get("status"),
                        record.<String>get("reason"),
                        record.<String>get("context"),
                        record.<String>get("appkey")
                );
            }
            else {
                throw new ReturnErrorException("ReturnError returned no results");
            }
        }

    }
}
