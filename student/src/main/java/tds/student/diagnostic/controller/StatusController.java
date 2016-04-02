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

import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import tds.student.diagnostic.dao.ReadTestDao;
import tds.student.diagnostic.dao.WriteTestDao;
import tds.student.diagnostic.domain.Level;
import tds.student.diagnostic.domain.Rating;
import tds.student.diagnostic.domain.Status;
import tds.student.diagnostic.domain.Summary;
import tds.student.diagnostic.services.DiagnosticConfigurationService;
import tds.student.diagnostic.services.DiagnosticDependencyService;
import tds.student.diagnostic.services.DiagnosticSystemService;
import tds.student.diagnostic.services.impl.DiagnosticDatabaseServiceImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
public class StatusController {

    //private static final Logger logger = LoggerFactory.getLogger(StatusController.class);

    @Autowired
    ReadTestDao readTestDao;

    @Autowired
    WriteTestDao writeTestDao;

    @Autowired
    DiagnosticSystemService diagnosticSystemService;

    @Autowired
    DiagnosticConfigurationService diagnosticConfigurationService;

    @Autowired
    DiagnosticDatabaseServiceImpl diagnosticDatabaseService;

    @Autowired
    private DiagnosticDependencyService diagnosticDependencyService;


    @RequestMapping(value = "/status", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Status getStatus(@RequestParam(value = "level", required = false, defaultValue = "0") Integer level,
                            @RequestParam(value = "single", required = false, defaultValue = "0") Boolean single) {

        return processLevel(level, single);
    }

    @RequestMapping(value = "/status/summary", method = RequestMethod.GET)
    @ResponseStatus(HttpStatus.OK)
    @ResponseBody
    public Summary getSummary(@RequestParam(value = "level", required = false, defaultValue = "0") Integer level,
                              @RequestParam(value = "single", required = false, defaultValue = "0") Boolean single) {

        return new Summary(processLevel(level, single));
    }

    private Status processLevel(Integer level, Boolean single) {
        switch (level) {
            case 1:
                return levelLocalSystem();
            case 2:
                return levelConfig(single);
            case 3:
                return levelReadData(single);
            case 4:
                return levelWriteData(single);
            case 5:
                return levelDependencies(single);
            case 0:
            default:
                return new Status("Student", Level.LEVEL_0, Rating.IDEAL, new DateTime());
        }
    }

    // Local System Level 1
    private Status levelLocalSystem() {

        Status status = new Status("Student", Level.LEVEL_1, new DateTime());
        status.setLocalSystem(diagnosticSystemService.getSystem());
        return status;
    }

    // Config Level 2
    private Status levelConfig(Boolean single) {

        Status status = new Status("Student", Level.LEVEL_2, new DateTime());
        if ( !single ) {
            status.setLocalSystem(diagnosticSystemService.getSystem());
        }
        status.setConfiguration(diagnosticConfigurationService.getConfiguration(configPropertyWhitelist));
        return status;
    }

    // Read Level 3
    private Status levelReadData(Boolean single) {

        Status status = new Status("Student", Level.LEVEL_3, new DateTime());
        if ( !single ) {
            status.setLocalSystem(diagnosticSystemService.getSystem());
            status.setConfiguration(diagnosticConfigurationService.getConfiguration(configPropertyWhitelist));
        }
        status.setDatabase(diagnosticDatabaseService.readLevelTest());
        return status;
    }

    // Write Level 4
    private Status levelWriteData(Boolean single) {

        Status status = new Status("Student", Level.LEVEL_4, new DateTime());
        if ( !single ) {
            status.setLocalSystem(diagnosticSystemService.getSystem());
            status.setConfiguration(diagnosticConfigurationService.getConfiguration(configPropertyWhitelist));
        }
        status.setDatabase(diagnosticDatabaseService.writeLevelTest());
        return status;
    }

    // Dependencies Level 5
    private Status levelDependencies(Boolean single) {

        Status status = new Status("Student", Level.LEVEL_5, new DateTime());
        if ( !single ) {
            status.setLocalSystem(diagnosticSystemService.getSystem());
            status.setConfiguration(diagnosticConfigurationService.getConfiguration(configPropertyWhitelist));
            status.setDatabase(diagnosticDatabaseService.writeLevelTest());
        }
        status.setProviders(diagnosticDependencyService.getProviders());
        return status;
    }

    final private List<String> configPropertyWhitelist = Arrays.asList(
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
    );

}