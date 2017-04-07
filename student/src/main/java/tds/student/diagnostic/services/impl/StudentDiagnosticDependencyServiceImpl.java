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
import org.springframework.web.client.RestTemplate;
import tds.dll.common.diagnostic.domain.Level;
import tds.dll.common.diagnostic.domain.Providers;
import tds.dll.common.diagnostic.domain.Rating;
import tds.dll.common.diagnostic.domain.Status;
import tds.dll.common.diagnostic.services.impl.AbstractDiagnosticDependencyService;
import tds.student.diagnostic.HealthIndicatorClient;

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

    @Autowired
    private RestTemplate restTemplate;

    @Value("${tds.exam.remote.url}")
    private String examServiceURL;

    public Providers getProviders() {
        List<Status> statusList = new ArrayList<>();

        statusList.add(getArt());
        statusList.add(getProgman());
        statusList.add(getExamService());

        return new Providers(statusList);
    }

    private Status getExamService() {
        return new HealthIndicatorClient(restTemplate).getStatus("Exam Service", examServiceURL);
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
}
