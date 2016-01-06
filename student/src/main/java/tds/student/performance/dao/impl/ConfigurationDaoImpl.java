package tds.student.performance.dao.impl;

import org.apache.commons.lang3.tuple.MutablePair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.caching.CacheType;
import tds.student.performance.dao.ConfigurationDao;
import tds.student.performance.dao.mappers.ClientTestModeMapper;
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
public class ConfigurationDaoImpl implements ConfigurationDao {
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private static final Logger logger = LoggerFactory.getLogger(ConfigurationDaoImpl.class);

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    /**
     * Get all the {@code ClientSystemFlag} records from the {@code configs.client_systemflags} database for the
     * specified client name.
     * <p>
     *     <strong>NOTE:</strong> Candidate for caching.
     * </p>
     * <p>
     *     The {@code JOIN} to the {@code session.externs} view came from looking at the SQL contained in the
     *     {@code CommonDLL.selectIsOnByAuditObject} method.
     * </p>
     *
     * @param clientName The client name for which the {@code ClientSystemFlag} records should be fetched.
     * @return A collection of {@code ClientSystemFlag} records for the specified client name.
     */
    @Override
    @Transactional
    @Cacheable(CacheType.LongTerm)
    public List<ClientSystemFlag> getSystemFlags(String clientName) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("clientName", clientName);

        final String SQL =
                "SELECT\n" +
                    "s.auditobject AS auditObject,\n" +
                    "s.clientname AS clientName,\n" +
                    "s.ispracticetest AS isPracticeTest,\n" +
                    "s.ison AS isOn,\n" +
                    "s.description AS description,\n" +
                    "s.datechanged AS dateChanged,\n" +
                    "s.datepublished AS datePublished\n" +
                "FROM\n" +
                    "configs.client_systemflags s\n" +
                "JOIN\n" +
                    "session.externs e\n" +
                    "ON (e.clientname = s.clientname\n" +
                    "AND e.ispracticetest = s.ispracticetest)\n" +
                "WHERE\n" +
                    "e.clientname = :clientName";

        try {
            return namedParameterJdbcTemplate.query(
                    SQL,
                    parameters,
                    new BeanPropertyRowMapper<>(ClientSystemFlag.class));
        } catch(EmptyResultDataAccessException e) {
            logger.warn(String.format("%s did not return results for clientName = %s", SQL, clientName));
            return null;
        }
    }

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
    @Transactional
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
                    "configs.client_testproperties\n" +
                "WHERE\n" +
                    "clientname = :clientName\n" +
                    "AND testid = :testId";

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    SQL,
                    parameters,
                    new BeanPropertyRowMapper<>(ClientTestProperty.class));
        } catch (EmptyResultDataAccessException e) {
            logger.warn(String.format("%s did not return results for clientName = %s, testId = %s", SQL, clientName, testId));
            return null;
        }

    }

    @Override
    @Transactional
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
                "    configs.client_testeeattribute cta\n" +
                "WHERE\n" +
                "    cta.clientname = :clientName\n" +
                "        AND atLogin is not null\n" +
                "ORDER BY SortOrder";


        return namedParameterJdbcTemplate.query(
                SQL,
                parameters,
                new BeanPropertyRowMapper<>(StudentLoginField.class));

    }

    @Override
    @Transactional
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
                "configs.client_testtooltype\n" +
            "WHERE\n" +
                "clientname = :clientName\n" +
                "AND contexttype = :contextType\n" +
                "AND context = :context\n" +
                "AND toolname = :toolName";

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    SQL,
                    parameters,
                    new BeanPropertyRowMapper<>(ConfigTestToolType.class));
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    /**
     * Determine if a {@link tds.student.performance.domain.TestConfiguration} should have its {@code scoreByTds}
     * property set.
     * <p>
     *     This method emulates the logic contained within {@code CommonDLL.ScoreByTDS_FN}.  The
     *     {@code CommonDLL.ScoreByTDS_FN} is called within {@code StudentDLL.T_StartTestOpportunity_SP} and ultimately
     *     passed onto the {@link tds.student.sql.data.TestConfig}.
     * </p>
     *
     * @param clientName The client name.
     * @param testId The test ID (i.e. the name of the test).
     * @return {@code true} if the {@code scoreByTDS} should be set; otherwise {@code false}.
     */
    @Override
    @Transactional
    @Cacheable(CacheType.LongTerm)
    public Boolean isSetForScoreByTDS(String clientName, String testId) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("clientName", clientName);
        parameters.put("testId", testId);

        final String SQL =
                "SELECT\n" +
                    "COUNT(clientname)\n" +
                "FROM\n" +
                    "configs.client_testscorefeatures\n" +
                "WHERE\n" +
                    "clientname = :clientName\n" +
                    "AND TestID = :testId\n" +
                    "AND (ReportToStudent = 1\n" +
                        "OR ReportToProctor = 1\n" +
                        "OR ReportToParticipation = 1\n" +
                        "OR UseForAbility = 1)\n" +
                "LIMIT 1";

        Integer recordCount = namedParameterJdbcTemplate.queryForInt(SQL, parameters);

        return recordCount > 0;
    }

    /**
     * Get the {@link ClientTestMode} from the {@code configs.client_testmode} table for the specified
     * {@link TestOpportunity}.
     *
     * @param testOpportunity The {@code TestOpportunity}.
     * @return  A {@code ClientTestMode} for the {@code TestOpportunity}.
     */
    @Override
    @Transactional
    @Cacheable(CacheType.LongTerm)
    public ClientTestMode getClientTestMode(TestOpportunity testOpportunity) {
        Map<String, String> parameters = new HashMap<>();
        parameters.put("parentTest", testOpportunity.getTestKey());

        final String SQL =
                "SELECT\n" +
                    "_key AS `key`,\n" +
                    "clientname AS clientName,\n" +
                    "testid AS testId,\n" +
                    "mode AS mode,\n" +
                    "algorithm AS algorithm,\n" +
                    "formtideselectable AS formTideSelectable,\n" +
                    "issegmented AS isSegmented,\n" +
                    "maxopps AS maxOpps,\n" +
                    "requirertsform AS requireRtsForm,\n" +
                    "requirertsformwindow AS requireRtsFormWindow,\n" +
                    "requirertsformifexists AS requireRtsFormIfExists,\n" +
                    "sessiontype AS sessionType,\n" +
                    "testkey AS testKey\n" +
                "FROM\n" +
                    "configs.client_testmode\n" +
                "WHERE\n" +
                    "testkey = :parentTest";

        try {
            return namedParameterJdbcTemplate.queryForObject(
                    SQL,
                    parameters,
                    new ClientTestModeMapper());
        } catch (EmptyResultDataAccessException e) {
            logger.warn(String.format("%s did not return results for parentTest = %s", SQL, parameters.get("parentTest")));
            return null;
        }
    }

    @Override
    public List<TestFormWindow> getTestFormWindows(TestOpportunity testOpportunity, TestSession testSession) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("clientName", testOpportunity.getClientName());
        parameters.put("testId", testOpportunity.getTestId());
        parameters.put("sessionType", testSession.getSessionType());

        final String SQL =
                "SELECT\n" +
                    "windowid AS windowId,\n" +
                    "W.numopps AS windowMax,\n" +
                    "M.maxopps AS modeMax,\n" +
                    "CASE\n" +
                        "WHEN W.startdate IS NULL THEN NOW(3)\n" +
                        "ELSE (W.startdate + INTERVAL shiftwindowstart DAY)\n" +
                    "END AS startDate\n," +
                    "CASE\n" +
                        "WHEN W.enddate IS NULL THEN NOW(3)\n" +
                        "ELSE (W.enddate + INTERVAL shiftwindowend DAY)\n" +
                    "END AS endDate,\n" +
                    "CASE\n" +
                        "WHEN F.startDate IS NULL THEN NOW(3)\n" +
                        "ELSE (F.startdate + INTERVAL shiftformstart DAY)\n" +
                    "END AS formStart,\n" +
                    "CASE\n" +
                        "WHEN F.enddate IS NULL THEN NOW(3)\n" +
                        "ELSE (F.enddate + INTERVAL shiftformend DAY)\n" +
                    "END AS formEnd,\n" +
                    "_efk_TestForm AS formKey,\n" +
                    "formid AS formId,\n" +
                    "F.language AS `language`,\n" +
                    "M.mode AS mode,\n" +
                    "M.testkey AS testKey,\n" +
                    "W.sessionType AS windowSession\n," +
                    "M.sessionType AS modeSession\n" +
                "FROM\n" +
                    "configs.client_testwindow W,\n" +
                    "configs.client_testformproperties F,\n" +
                    "configs.client_testmode M,\n" +
                    "itembank.tblsetofadminsubjects BANK,\n" +
                    "session._externs E\n" +
                "WHERE\n" +
                    "F.clientname = :clientName\n" +
                    "AND F.testID = :testId\n" +
                    "AND M.testkey = F.testkey\n" +
                    "AND M.testkey = BANK._key\n" +
                    "AND M.clientname = :clientName\n" +
                    "AND M.testID = :testId\n" +
                    "AND (M.sessionType = -1 OR M.sessionType = :sessionType)\n" +
                    "AND E.clientname = :clientName\n" +
                    "AND NOW(3) BETWEEN CASE\n" +
                            "WHEN F.startDate IS NULL THEN NOW(3)\n" +
                            "ELSE (F.startdate + INTERVAL shiftformstart DAY)\n" +
                        "END\n" +
                    "AND CASE\n" +
                            "WHEN F.enddate IS NULL THEN NOW(3)\n" +
                            "ELSE (F.enddate + INTERVAL shiftformend DAY)\n" +
                        "END\n" +
                    "AND NOW(3) BETWEEN CASE\n" +
                            "WHEN W.startDate IS NULL THEN NOW(3)\n" +
                            "ELSE (W.startDate + INTERVAL shiftwindowstart DAY)\n" +
                        "END\n" +
                    "AND CASE\n" +
                            "WHEN W.endDate IS NULL THEN NOW(3)\n" +
                            "ELSE (W.endDate + INTERVAL shiftwindowend DAY )\n" +
                        "END\n" +
                    "AND W.clientname = :clientName\n" +
                    "AND W.testID = :testId\n" +
                    "AND (W.sessionType = -1 OR W.sessiontype = :sessionType)\n" +
                "UNION\n" +
                "SELECT\n" +
                    "windowid AS windowId,\n" +
                    "W.numopps AS windowMax,\n" +
                    "M.maxopps AS modeMax,\n" +
                    "CASE\n" +
                        "WHEN W.startDate IS NULL THEN NOW(3)\n" +
                        "ELSE (W.startDate + INTERVAL shiftwindowstart DAY)\n" +
                    "END AS startDate,\n" +
                    "CASE\n" +
                        "WHEN W.endDate IS NULL THEN NOW(3)\n" +
                        "ELSE (W.endDate + INTERVAL shiftwindowend DAY)\n" +
                    "END AS endDate,\n" +
                    "CASE\n" +
                        "WHEN F.startDate IS NULL THEN NOW(3)\n" +
                        "ELSE (F.startdate + INTERVAL shiftformstart DAY)\n" +
                    "END AS formStart,\n" +
                    "CASE\n" +
                        "WHEN F.enddate IS NULL THEN NOW(3)\n" +
                        "ELSE (F.enddate + INTERVAL shiftformend DAY)\n" +
                    "END AS formEnd,\n" +
                    "_efk_testform AS formKey,\n" +
                    "formid AS formId,\n" +
                    "F.language AS `language`,\n" +
                    "M.mode AS mode,\n" +
                    "M.testkey AS testKey,\n" +
                    "W.sessiontype AS windowSession,\n" +
                    "M.sessiontype AS modeSession\n" +
                "FROM\n" +
                    "configs.client_testwindow W,\n" +
                    "configs.client_testformproperties F,\n" +
                    "configs.client_segmentproperties S,\n" +
                    "configs.client_testmode M,\n" +
                    "itembank.tblsetofadminsubjects BANK,\n" +
                    "session._externs E\n" +
                "WHERE\n" +
                    "S.clientname = :clientName\n" +
                    "AND F.clientname = :clientName}\n" +
                    "AND F.testkey = BANK._key\n" +
                    "AND S.parenttest = :testId\n" +
                    "AND M.clientname = :clientName\n" +
                    "AND M.testid = :testId\n" +
                    "AND (M.sessiontype = -1 OR M.sessiontype = :sessionType)\n" +
                    "AND S.modekey = M.testkey\n" +
                    "AND S.segmentid = BANK.testid\n" +
                    "AND E.clientname = :clientName\n" +
                    "AND NOW(3) BETWEEN CASE\n" +
                        "WHEN F.startdate IS NULL THEN NOW(3)\n" +
                        "ELSE (F.startdate + INTERVAL shiftformstart DAY)\n" +
                    "END\n" +
                    "AND CASE\n" +
                        "WHEN F.enddate IS NULL THEN NOW(3)\n" +
                        "ELSE (F.enddate + INTERVAL shiftformend DAY)\n" +
                    "END\n" +
                    "AND NOW(3) BETWEEN CASE\n" +
                        "WHEN W.startdate IS NULL THEN NOW(3)\n" +
                        "ELSE (W.startdate + INTERVAL shiftwindowstart DAY)\n" +
                    "END\n" +
                    "AND CASE\n" +
                        "WHEN W.enddate IS NULL THEN NOW(3)\n" +
                        "ELSE (W.enddate + INTERVAL shiftwindowend DAY)\n" +
                    "END\n" +
                    "AND W.clientname = ${clientname}\n" +
                    "AND W.testid = S.parenttest\n" +
                    "AND (W.sessiontype = -1 OR W.sessiontype = :sessionType)";

        return namedParameterJdbcTemplate.query(
                SQL,
                parameters,
                new BeanPropertyRowMapper<TestFormWindow>());
    }
}
