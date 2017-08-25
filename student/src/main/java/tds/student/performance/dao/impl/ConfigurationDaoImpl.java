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
package tds.student.performance.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import tds.dll.common.performance.caching.CacheType;
import tds.dll.common.performance.utils.LegacyDbNameUtility;
import tds.student.performance.dao.ConfigurationDao;
import tds.student.performance.domain.*;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Data Access Object for interacting with the {@code configs} database.
 * <p>
 *     This class could potentially be used for all interaction with the {@code configs} database.  Normally the DAO
 *     pattern tends to define one DAO object per table.  In this case, it might make more sense to just have one
 *     general DAO that allows us to talk to the {@code configs} database and get whatever we need.
 * </p>
 */
@Repository
public class ConfigurationDaoImpl extends tds.dll.common.performance.dao.impl.ConfigurationDaoImpl implements ConfigurationDao {

    /**
     * Get the {@code ClientTestProperty} record from {@code configs.client_testproperties} for the specified client name
     * and test id.
     * <p>
     *     <strong>NOTE:</strong> Candidate for caching.
     * </p>
     *
     * @param clientName The client name for which the {@code ClientTestProperty} records should be fetched.
     * @param testId The ID (which is the name) of the test for which the {@code ClientTestProperty} records should be fetched.
     * @return A {@code ClientTestProperty} for the specified client name and test id.
     */
    @Override
    @Cacheable(CacheType.LongTerm)
    public ClientTestProperty getClientTestProperty(String clientName, String testId) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("clientName", clientName);
        parameters.put("testId", testId);


        final String SQL =
                "SELECT\n" +
                    "clientname AS clientName,\n" +
                    "testid AS testId,\n" +
                    "maxopportunities AS maxOpportunities,\n" +
                    "handscoreproject AS handScoreProject,\n" +
                    "prefetch AS prefetch,\n" +
                    "datechanged AS dateChanged,\n" +
                    "isprintable AS isPrintable,\n" +
                    "isselectable AS isSelectable,\n" +
                    "label AS label,\n" +
                    "printitemtypes AS printItemTypes,\n" +
                    "scorebytds AS scoreByTds,\n" +
                    "batchmodereport AS batchModeReport,\n" +
                    "subjectname AS subjectName,\n" +
                    "origin AS origin,\n" +
                    "source AS source,\n" +
                    "maskitemsbysubject AS maskItemsBySubject,\n" +
                    "initialabilitybysubject AS initialAbilityBySubject,\n" +
                    "startdate AS startDate,\n" +
                    "enddate AS endDate,\n" +
                    "ftstartdate AS ftStartDate,\n" +
                    "ftenddate AS ftEndDate,\n" +
                    "accommodationfamily AS accommodationFamily,\n" +
                    "sortorder AS sortOrder,\n" +
                    "rtsformfield AS rtsFormField,\n" +
                    "rtswindowfield AS rtsWindowField,\n" +
                    "windowtideselectable AS windowTideSelectable,\n" +
                    "requirertswindow AS requireRtsWindow,\n" +
                    "reportinginstrument AS reportingInstrument,\n" +
                    "tide_id AS tideId,\n" +
                    "forcecomplete AS forceComplete,\n" +
                    "rtsmodefield AS rtsModeField,\n" +
                    "modetideselectable AS modeTideSelectable,\n" +
                    "requirertsmode AS requireRtsMode,\n" +
                    "requirertsmodewindow AS requireRtsModeWindow,\n" +
                    "deleteunanswereditems AS deleteUnansweredItems,\n" +
                    "abilityslope AS abilitySlope,\n" +
                    "abilityintercept AS abilityIntercept,\n" +
                    "validatecompleteness AS validateCompleteness,\n" +
                    "gradetext AS gradeText,\n" +
                    "initialabilitytestid AS initialAbilityTestId,\n" +
                    "proctoreligibility AS proctorEligibility,\n" +
                    "category AS category\n" +
                "FROM\n" +
                    "${configdb}.client_testproperties\n" +
                "WHERE\n" +
                    "clientname = :clientName\n" +
                    "AND testid = :testId";

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    dbNameUtility.setDatabaseNames(SQL),
                    parameters,
                    new BeanPropertyRowMapper<>(ClientTestProperty.class));
        } catch (EmptyResultDataAccessException e) {
            logger.warn(String.format("%s did not return results for clientName = %s, testId = %s", SQL, clientName, testId));
            return null;
        }

    }

    @Override
    @Cacheable(CacheType.LongTerm)
    public List<StudentLoginField> getStudentLoginFields(String clientName) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("clientName", clientName);

        final String SQL = "SELECT \n" +
                "    cta.TDS_ID as tdsId,\n" +
                "    cta.RTSName as rtsName,\n" +
                "    cta.type as fieldType,\n" +
                "    cta.atLogin as atLogin,\n" +
                "    cta.Label as label,\n" +
                "    cta.SortOrder as sortOrder\n" +
                "FROM\n" +
                "    ${configdb}.client_testeeattribute cta\n" +
                "WHERE\n" +
                "    cta.clientname = :clientName\n" +
                "        AND atLogin is not null\n" +
                "ORDER BY SortOrder";


        return namedParameterJdbcTemplate.query(
                dbNameUtility.setDatabaseNames(SQL),
                parameters,
                new BeanPropertyRowMapper<>(StudentLoginField.class));

    }

    @Override
    @Cacheable(CacheType.LongTerm)
    public ConfigTestToolType getTestToolType(String clientName, String toolName, String context, String contextType) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientName", clientName);
        parameters.put("contextType", contextType);
        parameters.put("context", context);
        parameters.put("toolName", toolName);

        final String SQL =
            "SELECT\n" +
                "clientname AS clientName,\n" +
                "toolname AS toolName,\n" +
                "rtsfieldname AS rtsFieldName,\n" +
                "source,\n" +
                "contexttype AS contextType,\n" +
                "context,\n" +
                "testmode AS testMode\n" +
            "FROM\n" +
                "${configdb}.client_testtooltype\n" +
            "WHERE\n" +
                "clientname = :clientName\n" +
                "AND contexttype = :contextType\n" +
                "AND context = :context\n" +
                "AND toolname = :toolName";

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    dbNameUtility.setDatabaseNames(SQL),
                    parameters,
                    new BeanPropertyRowMapper<>(ConfigTestToolType.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Determine if a {@code TestConfig} should have its {@code scoreByTds} property set.
     * <p>
     *     This method emulates the logic contained within {@code CommonDLL.ScoreByTDS_FN}.  The
     *     {@code CommonDLL.ScoreByTDS_FN} is called within {@code StudentDLL.T_StartTestOpportunity_SP} and ultimately
     *     passed onto the {@code TestConfig}.
     * </p>
     *
     * @param clientName The client name.
     * @param testId The test ID (i.e. the name of the test).
     * @return {@code true} if the {@code scoreByTDS} should be set; otherwise {@code false}.
     */
    @Override
    @Cacheable(CacheType.LongTerm)
    public Boolean isSetForScoreByTDS(String clientName, String testId) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("clientName", clientName);
        parameters.put("testId", testId);

        final String SQL =
                "SELECT\n" +
                    "COUNT(clientname)\n" +
                "FROM\n" +
                    "${configdb}.client_testscorefeatures\n" +
                "WHERE\n" +
                    "clientname = :clientName\n" +
                    "AND TestID = :testId\n" +
                    "AND (ReportToStudent = 1\n" +
                        "OR ReportToProctor = 1\n" +
                        "OR ReportToParticipation = 1\n" +
                        "OR UseForAbility = 1)\n" +
                "LIMIT 1";

        Integer recordCount = namedParameterJdbcTemplate.queryForObject(dbNameUtility.setDatabaseNames(SQL), parameters, Integer.class);

        return recordCount > 0;
    }

    /**
     * Determine if a {@code TestConfig} should have its {@code isMsb} property set.
     *
     * @param clientName The client name.
     * @param testId The test ID (i.e. the name of the test).
     * @return {@code true} if the {@code isMsb} should be set; otherwise {@code false}.
     */
    @Override
    public boolean isMsb(String clientName, String testId) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("clientName", clientName);
        parameters.put("testId", testId);

        final String SQL =
                "SELECT\n" +
                        "COUNT(clientname)\n" +
                        "FROM\n" +
                        "${configdb}.client_testproperties\n" +
                        "WHERE\n" +
                        "clientname = :clientName\n" +
                        "AND TestID = :testId\n" +
                        "AND msb = 1\n" +
                        "LIMIT 1";

        Integer recordCount = namedParameterJdbcTemplate.queryForObject(dbNameUtility.setDatabaseNames(SQL), parameters, Integer.class);

        return recordCount > 0;
    }

}
