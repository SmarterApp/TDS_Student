package tds.student.performance.services.impl;

import AIR.Common.DB.*;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers.CaseInsensitiveMap;
import AIR.Common.Helpers._Ref;
import AIR.Common.Sql.AbstractDateUtilDll;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.dll.api.ICommonDLL;
import tds.dll.api.IRtsDLL;
import tds.student.performance.dao.ConfigurationDao;
import tds.student.performance.domain.ConfigTestToolType;
import tds.student.performance.domain.StudentLoginField;
import tds.student.performance.services.ConfigurationService;
import tds.student.performance.services.DbLatencyService;
import tds.student.performance.services.StudentLoginService;
import tds.student.performance.utils.DateUtility;

import java.util.*;

/**
 * A service for interacting with a {@code TestOpportunity}.
 */
@Service
public class StudentLoginServiceImpl extends AbstractDLL implements StudentLoginService {
    private static final Logger logger = LoggerFactory.getLogger(StudentLoginServiceImpl.class);

    private static final String UNKNOWN_ATTRIBUTE_VALUE = "UNKNOWN";
    private static final String ID_FIELD_NAME = "ID";
    private static final String AT_LOGIN_VERIFY = "VERIFY";
    private static final String AT_LOGIN_REQUIRE = "REQUIRE";

    @Autowired
    private AbstractDateUtilDll _dateUtil = null;

    @Autowired
    private ICommonDLL _commonDll = null;

    @Autowired
    private IRtsDLL _rtsDll = null;

    @Autowired
    private ConfigurationDao configurationDao;

    // TODO: access if both the DAO and Service are needed.  The service holds the convenience method to determine if a flag is on
    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private DateUtility dateUtility;

    @Autowired
    private DbLatencyService dbLatencyService;

    @Override
    public MultiDataResultSet login(SQLConnection connection, String clientname, Map<String, String> keyValues, String sessionId)
            throws ReturnStatusException {

        List<SingleDataResultSet> resultsSets = new ArrayList<SingleDataResultSet>();
        Date startTime = _dateUtil.getDateWRetStatus(connection);
        String ssId;
        Long studentKey;

        // todo: Remove this for now Note _maxtestopps inserts only if there is existing rows to select from, or so it seems.
        // START: Accounting: how many open test opportunities are there currently for this client?

        // Get list of fields from config
        Map<String, StudentFieldValue> fieldValueMap = createFieldMap(configurationDao.getStudentLoginFields(clientname));

        // *new* Copy inputs to map.
        for (Map.Entry<String, String> pair : keyValues.entrySet()) {
            if (fieldValueMap.containsKey(pair.getKey())) {
                StudentFieldValue studentFieldValue = fieldValueMap.get(pair.getKey());
                studentFieldValue.inValue = pair.getValue();
            }
        }
        // *new* Look for ID
        if (fieldValueMap.containsKey(ID_FIELD_NAME)) {
            ssId = fieldValueMap.get(ID_FIELD_NAME).inValue;
        }   else {
            resultsSets.add(_commonDll._ReturnError_SP(connection, clientname, "T_Login", "No match for student ID"));
            return (new MultiDataResultSet(resultsSets));
        }
        // todo: remove guest session logic for now
        // START: Open guest session, if requested and permitted by client configuration
        // START: Handle request for anonymous login for practice test (assumes GUEST session?)

        // START: Handle request for validated login.
        // -- This 'may' be a real student. Attempt to validate that
        _Ref<String> errorRef = new _Ref<>();
        _Ref<Long> entityRef = new _Ref<>();
        _T_ValidateTesteeLogin_SP(connection, clientname, ssId, sessionId, errorRef, entityRef);
        studentKey = entityRef.get();
        if (errorRef.get() != null) {
            resultsSets.add(_commonDll._ReturnError_SP(connection, clientname, "T_Login", errorRef.get()));
            //connection.dropTemporaryTable(valsTbl);
            return (new MultiDataResultSet(resultsSets));
        }

        // *new* -- Student has been validated against the proctor and a valid RTS Key returned. Now check required attributes
        if (fieldValueMap.containsKey(ID_FIELD_NAME)) {
            fieldValueMap.get(ID_FIELD_NAME).outValue = fieldValueMap.get(ID_FIELD_NAME).inValue;
        }

        // *new* Replace temp tables
        // todo -- check for login failure due to bad required input data
        collectAndVerifyFields(connection, studentKey, clientname, fieldValueMap);

        SingleDataResultSet resultSetEntity = createResultEntity(studentKey);
        resultsSets.add(resultSetEntity);

        SingleDataResultSet resultSetInputs = createResultFields(fieldValueMap);
        resultsSets.add(resultSetInputs);

        _commonDll._LogDBLatency_SP(connection, "T_Login", startTime, studentKey, true, null, null, null, clientname, null);
        return (new MultiDataResultSet(resultsSets));
    }


    private void _T_ValidateTesteeLogin_SP(SQLConnection connection, String clientname, String testeeId, String sessionId,
                                          _Ref<String> reasonRef, _Ref<Long> testeeKeyRef) throws ReturnStatusException {
        Date startTime = dateUtility.getLocalDate();

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

                _commonDll._LogDBLatency_SP(connection, "_T_ValidateTesteeLogin", startTime, testeeKeyRef.get(), true,
                        null, null, null, clientname, null);
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
                _commonDll._LogDBLatency_SP(connection, "_T_ValidateTesteeLogin", startTime, testeeKeyRef.get(), true,
                        null, null, null, clientname, null);
                return;
            }
            // -- proctor key is the USERKEY in RTS, NOT the Entity key
            Long proctorKey = null;
            final String SQL_QUERY3 = "select _efk_Proctor from session where clientname = ${clientname} and sessionID = ${sessionID} "
                    + " and status = 'open' and ${now} between DateBegin and DateEnd";
            SqlParametersMaps parms3 = (new SqlParametersMaps()).put("clientname", clientname).put("sessionID", sessionId).put("now", startTime);
            result = executeStatement(connection, SQL_QUERY3, parms3, false).getResultSets().next();
            record = (result.getCount() > 0 ? result.getRecords().next() : null);
            if (record != null) {
                proctorKey = record.<Long>get("_efk_Proctor");
            }
            if (proctorKey == null) {
                reasonRef.set("The session is not available for testing");
                _commonDll._LogDBLatency_SP(connection, "_T_ValidateTesteeLogin", startTime, testeeKeyRef.get(), true,
                        null, null, null, clientname, null);
                return;
            }
            _Ref<String> schoolKeyRef = new _Ref<>();

            _rtsDll._ValidateInstitutionMatch_SP(connection, clientname, testeeKeyRef.get(), proctorKey, schoolKeyRef);
            if (schoolKeyRef.get() == null) {
                reasonRef.set("You must test in a session in your own school");
                _commonDll._LogDBLatency_SP(connection, "_T_ValidateTesteeLogin", startTime, testeeKeyRef.get(), true,
                        null, null, null, clientname, null);
                return;
            }
        }

        dbLatencyService.logLatency("_T_ValidateTesteeLogin", startTime, testeeKeyRef.get(), clientname);
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

    private void collectAndVerifyFields(SQLConnection connection, Long studentKey, String clientName, Map<String, StudentFieldValue> fieldValueMap)
            throws ReturnStatusException {

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

            // Required fields (expect ID). Check that the in matches the out
            if (fieldValue.loginField.getAtLogin().equals(AT_LOGIN_REQUIRE) && !fieldValue.loginField.getTdsId().equals(ID_FIELD_NAME)) {
                // todo must throw error if in not equal to out.
                // select  _key from ${valsTblName} where _key <> 'ID' and action = 'REQUIRE' and (outval is null or inval is null or inval <> outval ) limit 1
                // resultsSets.add(_commonDll._ReturnError_SP(connection, clientname, "T_Login", "No match"));
                if (!fieldValue.inValue.equalsIgnoreCase(fieldValue.outValue)) {
                    throw new ReturnStatusException("Invalid Login");
                }
            }
        }
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


}
