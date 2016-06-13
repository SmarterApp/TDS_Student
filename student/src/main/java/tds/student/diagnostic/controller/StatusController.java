/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 * <p/>
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 * <p/>
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/

package tds.student.diagnostic.controller;

import org.springframework.stereotype.Controller;
import tds.dll.common.diagnostic.controller.AbstractStatusController;

import java.util.Arrays;

@Controller
public class StatusController extends AbstractStatusController {

    public StatusController() {
        super("Student", Arrays.asList(
                "component.name",

                "datasource.acquireRetryAttempts",
                "datasource.minPoolSize",
                "datasource.testConnectionOnCheckout",
                "datasource.maxPoolSize",
                "datasource.numHelperThreads",
                "datasource.maxStatements",
                "datasource.idleConnectionTestPeriod",
                "datasource.testConnectionOnCheckin",
                "datasource.url", // TODO: is this safe?
                "datasource.checkoutTimeout",
                "datasource.maxStatementsPerConnection",
                "datasource.driverClassName",
                "datasource.acquireIncrement",
                "datasource.maxConnectionAge",

                "dbLockRetrySleepInterval",
                "dbLockRetryAttemptMax",

                "iris.ContentPath",
                "itemscoring.qti.sympyTimeoutMillis",
                "itemscoring.qti.sympyServiceUrl",
                "itemscoring.qti.sympyMaxTries",
                "itemScoring.callbackUrl",

                "logLatencyInterval",
                "logLatencyMaxTime",

                "mnaNodeName",
                "mnaServerName",
                "mna.mnaUrl",
                "mna.logger.level",

                "opportunity.isScoredByTDS",

                "performance.logMaxTestOpportunities.enabled",
                "performance.logLatency.enabled",
                "performance.datasource.maxPoolSize",
                "performance.datasource.minPoolSize",

                "permission.uri",
                "permission.security.profile",

                "proctor.AppName",
                "proctor.Appkey",
                "proctor.StateCode",
                "proctor.TestRegistrationApplicationUrl",
                "proctor.ItembankDBName",
                "proctor.IsCheckinSite",
                "proctor.TDSSessionDBName",
                "proctor.ClientQueryString",
                "proctor.ClientName",
                "proctor.TDSArchiveDBName",
                "proctor.security.dir",
                "proctor.webapp.saml.metadata.filename",
                "proctor.SessionType",
                "proctor.security.idp", // TODO: is this safe?
                "proctor.Debug.AllowFTP",
                "proctor.DBJndiName",
                "proctor.RecordSystemClient",
                "proctor.TDSConfigsDBName",
                "proctor.SqlCommandTimeout",

                "student.AppName",
                "student.ItembankDBName",
                "student.testScoring.logDebug",
                "student.TDSConfigsDBName",
                "student.RecordSystemClient",
                "student.Debug.AllowFTP",
                "student.TDSReportsRootDirectory",
                "student.StateCode",
                "student.SqlCommandTimeout",
                "student.StudentMaxOpps", // TODO: is this used anywhere?
                "student.TDSSessionDBName",
                "student.ClientCookie",
                "student.IsCheckinSite",
                "student.ClientQueryString",
                "student.TDSArchiveDBName",
                "student.Appkey",
                "student.SessionType",
                "student.DBDialect",
                "student.testScoring.logError",
                "student.ClientName",
                "student.TestRegistrationApplicationUrl"
        ));
    }
}