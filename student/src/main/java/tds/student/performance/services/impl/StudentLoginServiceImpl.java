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

import AIR.Common.DB.*;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers.CaseInsensitiveMap;
import AIR.Common.Helpers._Ref;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import tds.dll.api.ICommonDLL;
import tds.dll.api.IRtsDLL;
import tds.dll.api.IStudentDLL;
import tds.dll.common.performance.services.ConfigurationService;
import tds.dll.common.performance.utils.DateUtility;
import tds.student.performance.dao.ConfigurationDao;
import tds.student.performance.domain.ConfigTestToolType;
import tds.student.performance.domain.StudentLoginField;
import tds.student.performance.services.DbLatencyService;
import tds.student.performance.services.StudentLoginService;
import tds.student.securebrowser.enums.LoginKeys;

import java.util.*;

/**
 * Service to contain a new version of StudentDLL.T_Login_SP.
 */
@Service
public class StudentLoginServiceImpl extends AbstractDLL implements StudentLoginService {
    private static final Logger logger = LoggerFactory.getLogger(StudentLoginServiceImpl.class);

    private static final String UNKNOWN_ATTRIBUTE_VALUE = "UNKNOWN";
    private static final String LAST_NAME_FIELD_NAME = "LastName";
    private static final String FIRST_NAME_FIELD_NAME = "FirstName";
    private static final String AT_LOGIN_REQUIRE = "REQUIRE";
    private static final String GUEST_SESSION_NAME = "GUEST Session";
    private static final String GUEST_USER_NAME = "GUEST";


    @Value("${performance.logMaxTestOpportunities.enabled:false}")
    private Boolean logMaxTestOpportunities;

    @Autowired
    private IStudentDLL _studentDll = null;

    @Autowired
    private ICommonDLL _commonDll = null;

    @Autowired
    private IRtsDLL _rtsDll = null;

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private DateUtility dateUtility;

    @Autowired
    private DbLatencyService dbLatencyService;

    /**
     * A new implementation of StudentDLL.T_Login_SP.  The objective of this implementation is to reduce round
     * trips to the database by replacing temporary tables with data structures.
     *
     * @param connection Database connection.
     * @param clientName The client name.
     * @param keyValues A map of login fields and their input values.
     * @param sessionId An id for the session the student is logging into.
     * @return A {@link MultiDataResultSet} with student data or error information.
     * @throws ReturnStatusException
     */
    @Override
    public MultiDataResultSet login(SQLConnection connection, String clientName, Map<String, String> keyValues, String sessionId)
            throws ReturnStatusException {

        List<SingleDataResultSet> resultsSets = new ArrayList<>();
        Date startTime = dateUtility.getDbDate();
        Date dbLatencyTime = dateUtility.getLocalDate();

        String ssId;
        boolean isSBLaunchProtocolLogin = keyValues.containsKey(LoginKeys.SECURE_BROWSER_LAUNCH_PROTOCOL.getKeyName());
        // Accounting: how many open test opportunities currently for this client?
        logger.debug("Property performance.logMaxTestOpportunities.enabled {} ", logMaxTestOpportunities);
        if ( logMaxTestOpportunities ) {
            recordMaxTestOpportunities(connection, clientName, startTime);
        }

        // Get list of fields from config database
        Map<String, StudentFieldValue> fieldValueMap = createFieldMap(configurationDao.getStudentLoginFields(clientName));

        // Copy keyValues from input form to config database. Map inValue by field key.
        for (Map.Entry<String, String> pair : keyValues.entrySet()) {
            if (fieldValueMap.containsKey(pair.getKey())) {
                StudentFieldValue studentFieldValue = fieldValueMap.get(pair.getKey());
                studentFieldValue.inValue = pair.getValue();
            }
        }

        // Look for the field that is the ID
        if (fieldValueMap.containsKey(LoginKeys.STUDENTID.getKeyName())) {
            ssId = fieldValueMap.get(LoginKeys.STUDENTID.getKeyName()).inValue;

        } else {
            resultsSets.add(_commonDll._ReturnError_SP(connection, clientName, "T_Login", "No match for student ID"));
            return (new MultiDataResultSet(resultsSets));
        }

        // Open guest session, if requested and permitted by client configuration
        if (DbComparator.isEqual (sessionId, GUEST_SESSION_NAME)) {
            if (_studentDll._AllowProctorlessSessions_FN(connection, clientName)) {
                _Ref<UUID> sessionKeyRef = new _Ref<> ();
                _Ref<String> sessionIdRef = new _Ref<> ();
                _studentDll._SetupProctorlessSession_SP(connection, clientName, sessionKeyRef, sessionIdRef);
                sessionId = sessionIdRef.get();
                //UUID sessionKey = sessionKeyRef.get();
            } else {
                resultsSets.add (_commonDll._ReturnError_SP (connection, clientName, "T_Login", "You are not allowed to log in without a Test Administrator"));
                return (new MultiDataResultSet (resultsSets));
            }
        }

        // Return a guest login
        if (DbComparator.isEqual(ssId, GUEST_USER_NAME) && _studentDll._AllowAnonymousTestee_FN(connection, clientName)) {
            return  handleGuestLogin(connection, clientName, startTime, dbLatencyTime, fieldValueMap );
        }

        // Return a User login
        // The dbLatencyTime is passed in since the startTime in handleUserLogin is only used to record latency/performance metrics.
        return handleUserLogin( connection,  clientName,  dbLatencyTime, fieldValueMap,  ssId,  sessionId, isSBLaunchProtocolLogin);
    }


    private MultiDataResultSet handleUserLogin(SQLConnection connection, String clientName, Date dbLatencyTime, Map<String, StudentFieldValue> fieldValueMap, String ssId, String sessionId, boolean isSBLaunchProtocolLogin)
            throws ReturnStatusException {
        logger.debug("Handle a User Login");

        List<SingleDataResultSet> resultsSets = new ArrayList<>();
        Long studentKey;

        _Ref<String> errorRef = new _Ref<>();
        _Ref<Long> entityRef = new _Ref<>();
        _T_ValidateTesteeLogin_SP(connection, clientName, ssId, sessionId, errorRef, entityRef);
        studentKey = entityRef.get();
        if (errorRef.get() != null) {
            resultsSets.add(_commonDll._ReturnError_SP(connection, clientName, "T_Login", errorRef.get()));
            return (new MultiDataResultSet(resultsSets));
        }

        // Student has been validated against the proctor and a valid RTS Key returned. Now check required attributes
        if (fieldValueMap.containsKey(LoginKeys.STUDENTID.getKeyName())) {
            fieldValueMap.get(LoginKeys.STUDENTID.getKeyName()).outValue = fieldValueMap.get(LoginKeys.STUDENTID.getKeyName()).inValue;
        }

        List<SingleDataResultSet> errorSets = collectAndVerifyFields(connection, studentKey, clientName, fieldValueMap, isSBLaunchProtocolLogin);
        if ( errorSets != null ) {
            logger.error("Login error in collectAndVerifyFields");
            return (new MultiDataResultSet(errorSets));
        }

        SingleDataResultSet resultSetEntity = createResultEntity(studentKey);
        resultsSets.add(resultSetEntity);

        SingleDataResultSet resultSetInputs = createResultFields(fieldValueMap);
        resultsSets.add(resultSetInputs);

        dbLatencyService.logLatency("T_Login", dbLatencyTime, studentKey, clientName);
        return (new MultiDataResultSet(resultsSets));
    }


    private MultiDataResultSet handleGuestLogin(SQLConnection connection, String clientName, Date startTime, Date dbLatencyTime, Map<String, StudentFieldValue> fieldValueMap)
            throws ReturnStatusException {

        logger.debug("Handle a Guest Login");

        List<SingleDataResultSet> resultsSets = new ArrayList<>();
        Long guestKey = 0L;

        final String SQL_QUERY6 = "insert into _anonymoustestee (dateCreated, clientname) values (${now}, ${clientname})";
        SqlParametersMaps params6 = (new SqlParametersMaps()).put("now", startTime).put("clientname", clientName);
        executeStatement(connection, SQL_QUERY6, params6, false).getUpdateCount();

        // to get auto_increment _key column, we use SELECT LAST_INSERT_ID()
        final String SQL_QUERY7 = "select cast(LAST_INSERT_ID() as SIGNED) as maxkey";
        SingleDataResultSet result = executeStatement(connection, SQL_QUERY7, null, false).getResultSets().next();
        DbResultRecord record = (result.getCount() > 0 ? result.getRecords().next() : null);
        if (record != null) {
            guestKey = record.<Long>get("maxKey");
            guestKey = (guestKey == null ? 0 : (-1 * guestKey));
        }

        // Set the ID to the generated gKey.
        StudentFieldValue guestIdField = fieldValueMap.get(LoginKeys.STUDENTID.getKeyName());
        if (guestIdField != null) {
            guestIdField.outValue = String.format("GUEST %d", guestKey);
        }

        StudentFieldValue lastNameField = fieldValueMap.get(LAST_NAME_FIELD_NAME);
        if (lastNameField != null) {
            lastNameField.outValue = "GUEST";
        }

        StudentFieldValue firstNameField = fieldValueMap.get(FIRST_NAME_FIELD_NAME);
        if (firstNameField != null) {
            firstNameField.outValue = "GUEST";
        }

        // Convert relationship fields such as School to GUESTSchool
        for (Map.Entry<String, StudentFieldValue> entry : fieldValueMap.entrySet()) {
            StudentFieldValue fieldValue = entry.getValue();
            if (fieldValue.loginField != null && fieldValue.loginField.getFieldType().equalsIgnoreCase("relationship")) {
                fieldValue.outValue = "GUEST" + fieldValue.loginField.getTdsId();
            }
        }

        List<CaseInsensitiveMap<Object>> resultList = new ArrayList<>();
        CaseInsensitiveMap<Object> rcd = new CaseInsensitiveMap<>();
        rcd.put("status", "success");
        rcd.put("entityKey", guestKey);
        rcd.put("accommodations", null);
        resultList.add(rcd);

        SingleDataResultSet rs1 = new SingleDataResultSet();
        rs1.addColumn("status", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs1.addColumn("entityKey", SQL_TYPE_To_JAVA_TYPE.BIGINT);
        rs1.addColumn("accommodations", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs1.addRecords(resultList);

        resultsSets.add(rs1);

        SingleDataResultSet resultSetInputsGuest = createResultFields(fieldValueMap);
        resultsSets.add(resultSetInputsGuest);

        dbLatencyService.logLatency("T_Login", dbLatencyTime, guestKey, clientName);
        return (new MultiDataResultSet(resultsSets));
    }

    /**
     *  A new version of StudentDLL._T_ValidateTesteeLogin_SP
     */
    private void _T_ValidateTesteeLogin_SP(SQLConnection connection, String clientname, String testeeId, String sessionId,
                                          _Ref<String> reasonRef, _Ref<Long> testeeKeyRef) throws ReturnStatusException {
        Date dbLatencyTime = dateUtility.getLocalDate();

        // START: Get internal key for student with official ID testeeId
        _rtsDll._GetRTSEntity_SP(connection, clientname, testeeId, "STUDENT", testeeKeyRef);
        if (testeeKeyRef.get() == null) {
            reasonRef.set("No match for student ID");
            return;
        }
        // END: Get internal key for student with official ID testeId

        // START: Block login if "parent exempt"
        ConfigTestToolType parentExemptToolType = configurationDao.getTestToolType(clientname, "Parent Exempt", "TEST", "*");

        // -- check for parent exemption from all tests (no point in logging in)
        _Ref<String> rtsValueRef = new _Ref<>();
        if (parentExemptToolType != null && parentExemptToolType.getRtsFieldName() != null) {

            _rtsDll._GetRTSAttribute_SP(connection, clientname, testeeKeyRef.get(), parentExemptToolType.getRtsFieldName(), rtsValueRef);
            if (DbComparator.isEqual(rtsValueRef.get(), "TDS_ParentExempt1")) {
                reasonRef.set("parent exempt");

                dbLatencyService.logLatency("_T_ValidateTesteeLogin", dbLatencyTime, testeeKeyRef.get(), clientname);
                return;
            }
        }
        // END: Block login if "parent exempt"


        SingleDataResultSet result;
        DbResultRecord record;

        if (configurationService.isFlagOn(clientname, "MatchTesteeProctorSchool")) {
            if (sessionId == null) {
                // -- this is an internal system error
                _commonDll._RecordSystemError_SP(connection, "T_GetRTSTestee", "Missing session ID");
                reasonRef.set("Session ID required");
                dbLatencyService.logLatency("_T_ValidateTesteeLogin", dbLatencyTime, testeeKeyRef.get(), clientname);
                return;
            }
            // -- proctor key is the USERKEY in RTS, NOT the Entity key
            Long proctorKey = null;
            final String SQL_QUERY3 = "select _efk_Proctor from session where clientName = ${clientName} and sessionID = ${sessionID} "
                    + " and status = 'open' and ${now} between DateBegin and DateEnd";
            SqlParametersMaps parms3 = (new SqlParametersMaps()).put("clientName", clientname).put("sessionID", sessionId).put("now", dateUtility.getDbDate());
            result = executeStatement(connection, SQL_QUERY3, parms3, false).getResultSets().next();
            record = (result.getCount() > 0 ? result.getRecords().next() : null);
            if (record != null) {
                proctorKey = record.<Long>get("_efk_Proctor");
            }
            if (proctorKey == null) {
                reasonRef.set("The session is not available for testing");
                dbLatencyService.logLatency("_T_ValidateTesteeLogin", dbLatencyTime, testeeKeyRef.get(), clientname);
                return;
            }
            _Ref<String> schoolKeyRef = new _Ref<>();

            _rtsDll._ValidateInstitutionMatch_SP(connection, clientname, testeeKeyRef.get(), proctorKey, schoolKeyRef);
            if (schoolKeyRef.get() == null) {
                reasonRef.set("You must test in a session in your own school");
                dbLatencyService.logLatency("_T_ValidateTesteeLogin", dbLatencyTime, testeeKeyRef.get(), clientname);
                return;
            }
        }

        dbLatencyService.logLatency("_T_ValidateTesteeLogin", dbLatencyTime, testeeKeyRef.get(), clientname);
    }

    private SingleDataResultSet createResultEntity(Long studentKey)
            throws ReturnStatusException {

        // select 'success' as status, @entity as entityKey;
        List<CaseInsensitiveMap<Object>> resultList = new ArrayList<>();
        CaseInsensitiveMap<Object> rcd = new CaseInsensitiveMap<>();
        rcd.put("status", "success");
        rcd.put("entityKey", studentKey);
        resultList.add(rcd);

        SingleDataResultSet resultSet = new SingleDataResultSet();
        resultSet.addColumn("status", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        resultSet.addColumn("entityKey", SQL_TYPE_To_JAVA_TYPE.BIGINT);
        resultSet.addRecords(resultList);

        return resultSet;
    }


    private SingleDataResultSet createResultFields(Map<String, StudentFieldValue> fieldValueMap)
            throws ReturnStatusException {

        SingleDataResultSet resultSet = new SingleDataResultSet();
        resultSet.addColumn("tds_id", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        resultSet.addColumn("value", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        resultSet.addColumn("label", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        resultSet.addColumn("sortorder", SQL_TYPE_To_JAVA_TYPE.INT);
        resultSet.addColumn("atlogin", SQL_TYPE_To_JAVA_TYPE.VARCHAR);

        for (Map.Entry<String, StudentFieldValue> entry : fieldValueMap.entrySet()) {
            StudentFieldValue fieldValue = entry.getValue();

            List<CaseInsensitiveMap<Object>> resultList = new ArrayList<>();

            CaseInsensitiveMap<Object> map = new CaseInsensitiveMap<>();
            map.put("tds_id", fieldValue.loginField.getTdsId());
            map.put("value", fieldValue.outValue);
            map.put("label", fieldValue.loginField.getLabel());
            map.put("sortorder", new Integer(fieldValue.loginField.getSortOrder()));
            map.put("atlogin", fieldValue.loginField.getAtLogin());

            resultList.add(map);
            resultSet.addRecords(resultList);
        }

        return resultSet;
    }

    private List<SingleDataResultSet> collectAndVerifyFields(SQLConnection connection, Long studentKey, String clientName, Map<String,
            StudentFieldValue> fieldValueMap, boolean isSecureBrowserLaunchProtocol) throws ReturnStatusException {
        for (Map.Entry<String, StudentFieldValue> entry : fieldValueMap.entrySet()) {
            StudentFieldValue fieldValue = entry.getValue();

            if (fieldValue.outValue == null) {
                _Ref<String> valueRef = new _Ref<>();

                if (fieldValue.loginField.getFieldType().equals("attribute")) {
                    _rtsDll._GetRTSAttribute_SP(connection, clientName, studentKey, fieldValue.loginField.getRtsName(), valueRef);
                } else if (fieldValue.loginField.getFieldType().equals("relationship")) {
                    _Ref<Long> relatedEntityRef = new _Ref<>();
                    _Ref<String> relatedIdRef = new _Ref<>();
                    _rtsDll._GetRTSRelationship_SP(connection, clientName, studentKey, fieldValue.loginField.getRtsName(), relatedEntityRef, relatedIdRef, valueRef);
                }
                fieldValue.outValue = (valueRef.get() == null ? UNKNOWN_ATTRIBUTE_VALUE : valueRef.get());
            }

            // Required fields (expect ID). Check that the in matches the out. If this is SBLP, only sessionId and SSID need validation
            if (!isSecureBrowserLaunchProtocol && fieldValue.loginField.getAtLogin().equals(AT_LOGIN_REQUIRE)
                    && !fieldValue.loginField.getTdsId().equals(LoginKeys.STUDENTID.getKeyName())) {

                // Replaces Logic:  select _key from ${valsTblName} where _key <> 'ID' and action = 'REQUIRE' and (outval is null or inval is null or inval <> outval ) limit 1
                if (!fieldValue.inValue.equalsIgnoreCase(fieldValue.outValue)) {
                    List<SingleDataResultSet> errorSets = new ArrayList<>();
                    errorSets.add(_commonDll._ReturnError_SP(connection, clientName, "T_Login", "No match"));
                    return errorSets;
                }
            }
        }
        return null;
    }

    private Map<String, StudentFieldValue> createFieldMap(List<StudentLoginField> studentLoginFields) {

        Map<String, StudentFieldValue> fieldValueMap = new LinkedHashMap<>();

        for (StudentLoginField studentLoginField : studentLoginFields) {
            fieldValueMap.put(studentLoginField.getTdsId(), new StudentFieldValue(studentLoginField));
        }
        return fieldValueMap;
    }

    private static class StudentFieldValue {

        StudentLoginField loginField;
        String inValue;
        String outValue;

        public StudentFieldValue(StudentLoginField loginField) {
            this.loginField = loginField;
            this.inValue = null;
            this.outValue = null;
        }

    }

    private void recordMaxTestOpportunities(SQLConnection connection, String clientName, Date startTime)
            throws ReturnStatusException {

        logger.debug("Record current test opportunities to _maxtestopps for client {}", clientName);

        try {

            //Interval for logging into the _maxtestopps table
            final int maxTestOppsIntervalInMinutes = 10;
            Date lastupdate = null;
            final String SQL_QUERY1 = "select max(_time) as lastupdate from _maxtestopps where clientName = ${clientName}";
            SqlParametersMaps parms1 = (new SqlParametersMaps()).put("clientName", clientName);
            SingleDataResultSet result = executeStatement(connection, SQL_QUERY1, parms1, false).getResultSets().next();
            DbResultRecord record = (result.getCount() > 0 ? result.getRecords().next() : null);
            if (record != null) {
                lastupdate = record.get("lastupdate");
            }

            if (lastupdate == null || DbComparator.greaterOrEqual(minutesDiff(lastupdate, startTime), maxTestOppsIntervalInMinutes)) {
                final String SQL_QUERY2 = "insert into _maxtestopps(numopps,_time,clientName) "
                        + " SELECT -1,now(3), ${clientName} FROM dual WHERE"
                        + " 1 = (select (time_to_sec(TIMEDIFF(now(3),max(_time)))/60) >=${maxTestOppsIntervalInMinutes} as diff_minutes "
                        + " from _maxtestopps where clientName =  ${clientName}) ";
                SqlParametersMaps parms2 = (new SqlParametersMaps()).put("clientName", clientName).put("maxTestOppsIntervalInMinutes", maxTestOppsIntervalInMinutes);
                int insertCount = executeStatement(connection, SQL_QUERY2, parms2, false).getUpdateCount();

                if (insertCount > 0) {
                    Integer numOpps = _studentDll._ActiveOpps_FN(connection, clientName);
                    final String SQL_QUERY3 = "update _maxtestopps set numopps =  ${numopps},_time = now(3) where numopps= -1 and  clientName = ${clientName}";
                    SqlParametersMaps parms3 = (new SqlParametersMaps()).put("numopps", numOpps).put("clientName", clientName);
                    executeStatement(connection, SQL_QUERY3, parms3, false).getUpdateCount();
                }
            }
        } catch (ReturnStatusException re) {
            // Original comment here mentions the external ID is not known.
            // Changed to log the client instead of user externalID on error.
            String error = String.format(" for testee ID %s: %s", clientName, re.getMessage());
            _commonDll._LogDBError_SP(connection, "T_Login", error, null, null, null, null, clientName, null);
        }
    }

    private Long minutesDiff (Date from, Date to) {

        if (from == null || to == null)
            return null;
        return (to.getTime () - from.getTime ()) / 1000 / 60;
    }


}
