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

package tds.student.diagnostic.services.impl;

import org.opentestsystem.shared.progman.client.ProgManClient;
import org.opentestsystem.shared.progman.client.domain.TenantType;
import org.opentestsystem.shared.trapi.ITrClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tds.dll.common.diagnostic.domain.Level;
import tds.dll.common.diagnostic.domain.Providers;
import tds.dll.common.diagnostic.domain.Rating;
import tds.dll.common.diagnostic.domain.Status;
import tds.dll.common.diagnostic.services.impl.AbstractDiagnosticDependencyService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Service
public class StudentDiagnosticDependencyServiceImpl extends AbstractDiagnosticDependencyService {

    private static final Logger logger = LoggerFactory.getLogger(StudentDiagnosticDependencyServiceImpl.class);

    @Autowired
    private ProgManClient progManClient;

    @Autowired
    private ITrClient _trClient;

    public Providers getProviders() {
        List<Status> statusList = new ArrayList<>();

        statusList.add(getArt());
        statusList.add(getProgman());
        statusList.add(getMathScoring());
        statusList.addAll(getStudentReportProcessor());

        return new Providers(statusList);
    }

    protected List<String> getProcesses(String grepValue) {
        List<String> processes = new ArrayList<>();

        try {
            String line;
            Process p = Runtime.getRuntime().exec("ps -ax"); // for some reason | grep is not working as expected
            BufferedReader input =
                    new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((line = input.readLine()) != null) {
                if (line.contains(grepValue))
                    processes.add(line);
            }
            input.close();
        } catch (Exception e) {
            logger.error("Error getting process list", e);
        }

        return processes;
    }

    protected Status getArt() {

        final String unit = "ART";
        try {
            String clients = _trClient.getForObject("clients");
            logger.debug("Client from ART {}", clients);
            return new Status(unit, Level.LEVEL_0, new Date());

        } catch (Exception e) {
            logger.error("Diagnostic error with dependency ART ", e);
            Status errorStatus = new Status(unit, Level.LEVEL_0, new Date());
            errorStatus.setRating(Rating.FAILED);
            errorStatus.setError("Diagnostic error with dependency ART");
            return errorStatus;
        }
    }

    protected Status getProgman() {
        final String unit = "Progman";
        try {
            String progmanClassName = progManClient.getClass().getSimpleName();

            if (progmanClassName.equalsIgnoreCase("ProgramManagementNullClient")) {
                logger.info("Using null client {} ", progmanClassName);
            }

            List<TenantType> tenantTypes = progManClient.getTenantTypes();
            logger.debug("Tenant Types from progman: {}", tenantTypes);

            return new Status(unit, Level.LEVEL_0, new Date());

        } catch (Exception e) {
            logger.error("Diagnostic error with dependency Progman ", e);
            Status errorStatus = new Status(unit, Level.LEVEL_0, new Date());
            errorStatus.setRating(Rating.FAILED);
            errorStatus.setError("Diagnostic error with dependency Progman");
            return errorStatus;
        }

    }

    protected Status getMathScoring() {
        final String unit = "EquationScoring";
        try {
            List<String> ps = getProcesses("EqScoringWebService.py");

            if (ps.size() == 0) {
                logger.warn("Diagnostic warning with dependency EqScoringWebService.  It does not appear to be running.");
                Status errorStatus = new Status(unit, Level.LEVEL_0, new Date());
                errorStatus.setRating(Rating.WARNING);
                errorStatus.setWarning("EqScoringWebService.py service is not running.");
                return errorStatus;
            }

            return new Status(unit, Level.LEVEL_0, new Date());
        } catch (Exception e) {
            logger.error("Diagnostic warning with dependency EqScoringWebService.", e);
            Status errorStatus = new Status(unit, Level.LEVEL_0, new Date());
            errorStatus.setRating(Rating.FAILED);
            errorStatus.setError("Diagnostic error with dependency EqScoringWebService.");
            return errorStatus;
        }

    }

    // checks that StudentReportProcessor is running
    //  if it is, then it checks that TIS is available via a ping
    // NOTE: if the student report processor background task is not running or we can't connect with TIS
    //      it is a WARNING since the system still works and tests can be scored and taken as needed
    //      Then when these are back up all of the results will be sent over and nothing will be lost
    protected List<Status> getStudentReportProcessor() {
        List<Status> results = new ArrayList<>();
        final String unit = "StudentReportProcessor";
        String processEntry = null;

        try {
            List<String> ps = getProcesses("org.opentestsystem.delivery.studentreportprocessor.StudentReportProcessor");

            if (ps.size() == 0) {
                logger.warn("Diagnostic warning with dependency StudentReportProcessor.  It does not appear to be running.");
                Status errorStatus = new Status(unit, Level.LEVEL_0, new Date());
                errorStatus.setRating(Rating.WARNING);
                errorStatus.setWarning("StudentReportProcessor service is not running.");
                results.add(errorStatus);
            }
            else {
                // get the line from ps -ax to use for pinging TIS below
                processEntry = ps.get(0);
                results.add(new Status(unit, Level.LEVEL_0, new Date()));
            }

        } catch (Exception e) {
            logger.error("Diagnostic warning with dependency StudentReportProcessor.", e);
            Status errorStatus = new Status(unit, Level.LEVEL_0, new Date());
            errorStatus.setRating(Rating.WARNING);
            errorStatus.setError("Diagnostic error with dependency StudentReportProcessor.");

            results.add(errorStatus);
        }

        final String tisUnit = "TIS";
        if (processEntry == null) {
            // not found so put in a warning for TIS
            Status errorStatus = new Status(tisUnit, Level.LEVEL_0, new Date());
            errorStatus.setRating(Rating.WARNING);
            errorStatus.setWarning("Could not retrieve TIS url since StudentReportProcessor is not running.");

            results.add(errorStatus);
        } else {
            Pattern pattern = Pattern.compile("-DreportingDLL.tisUrl=(http[^ ]*) -D");
            Matcher matcher = pattern.matcher(processEntry);

            if (!matcher.find()) {
                // not found so put in a warning for TIS
                Status errorStatus = new Status(tisUnit, Level.LEVEL_0, new Date());
                errorStatus.setRating(Rating.WARNING);
                errorStatus.setWarning("Could not retrieve TIS url since StudentReportProcessor is not running.");

                results.add(errorStatus);
            }
            else {
                String tisUrl = matcher.group(1) + "?statusCallback=fakeurl";
                Integer tisResponseCode = httpResponseCode(tisUrl, "POST");

                // POST to TIS should give a 401 not authorized since we haven't authenticated
                //  but this is enough to know it is up and running
                if (tisResponseCode != 401 && tisResponseCode != 411) {
                    // not found so put in a warning for TIS
                    Status errorStatus = new Status(tisUnit, Level.LEVEL_0, new Date());
                    errorStatus.setRating(Rating.WARNING);
                    errorStatus.setWarning("Could not ping TIS at " + tisUrl);

                    results.add(errorStatus);
                }
                else {
                    results.add(new Status(tisUnit, Level.LEVEL_0, new Date()));
                }
            }
        }


        return results;
    }
}
