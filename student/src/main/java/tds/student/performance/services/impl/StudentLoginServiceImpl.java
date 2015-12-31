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
        Date starttime = _dateUtil.getDateWRetStatus(connection);
        String externalId = null;
        Long entity = null;
        String inval = null, type = null, field = null;


        List<StudentLoginField> studentLoginFields = configurationDao.getStudentLoginFields(clientname);

        // todo: Remove this for now
        // Note _maxtestopps inserts only if there is existing rows to select from, or so it seems.
        // START: Accounting: how many open test opportunities are there currently for this client?

        // START: Retrieve a list of fields that this client expects at login

        // declare @vals table (_key varchar(50), inval varchar(200), outval
        // varchar(200), field varchar(100), atttype varchar(50), action
        // varchar(50));
        DataBaseTable valsTbl = getDataBaseTable("valsTable").addColumn("_key", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 50).
                addColumn("inval", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 200).addColumn("outval", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 200).addColumn("field", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 100).
                addColumn("atttype", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 50).addColumn("action", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 50);
        connection.createTemporaryTable(valsTbl);

        final String SQL_QUERY3 = "insert into ${valsTblName} (_key, field, atttype, action) "
                + " select TDS_ID, RTSName, type, atLogin from ${ConfigDB}.client_testeeattribute "
                + " where clientname = ${clientname} and atLogin is not null";
        SqlParametersMaps parms3 = (new SqlParametersMaps()).put("clientname", clientname);
        Map<String, String> unquotedParms3 = new HashMap<>();
        unquotedParms3.put("valsTblName", valsTbl.getTableName());
        final String query3 = fixDataBaseNames(SQL_QUERY3);
        executeStatement(connection, fixDataBaseNames(query3, unquotedParms3), parms3, false).getUpdateCount();
        // END: Retrieve a list of fields that this client expects at login

        // START: Update list of fields with the values supplied from the login form
        // (form values were passed into fn as argument)
        for (Map.Entry<String, String> pair : keyValues.entrySet()) {
            final String SQL_QUERY4 = "update ${valsTblName} set inval = ${theRight} where _key = ${theLeft}";
            Map<String, String> unquotedParms4 = unquotedParms3;
            SqlParametersMaps parms4 = (new SqlParametersMaps()).put("theRight", pair.getValue()).put("theLeft", pair.getKey());
            executeStatement(connection, fixDataBaseNames(SQL_QUERY4, unquotedParms4), parms4, false).getUpdateCount();
        }
        // END: Update list of fields with the values supplied from the login form
        // (form values were passed into fn as argument)

        // START: Retrieve the student ID
        // -- ID is ALWAYS REQUIRED
        final String SQL_QUERY5 = "select  inval, atttype  as type from ${valsTblName} where _key = 'ID'";
        Map<String, String> unquotedParms5 = unquotedParms3;
        SingleDataResultSet result = executeStatement(connection, fixDataBaseNames(SQL_QUERY5, unquotedParms5), parms3, false).getResultSets().next();
        DbResultRecord record = (result.getCount() > 0 ? result.getRecords().next() : null);
        if (record != null) {
            inval = record.<String>get("inval");
            type = record.<String>get("type");
        }
        if (inval == null) {
            //SB-797
            //resultsSets.add (_commonDll._ReturnError_SP (connection, clientname, "T_Login", "No ID"));
            resultsSets.add(_commonDll._ReturnError_SP(connection, clientname, "T_Login", "No match for student ID"));

            // TODO Elena: check it out: entity is not set up at this point
            _commonDll._LogDBLatency_SP(connection, "T_Login", starttime, entity, true, null, null, null, clientname, null);
            connection.dropTemporaryTable(valsTbl);
            return (new MultiDataResultSet(resultsSets));
        }

        // The value of the student ID provided on the login form.
        externalId = inval;
        // END: Retrieve the student ID

        _Ref<UUID> sessionKeyRef = new _Ref<>();
        UUID sessionKey = null;

        // todo: remove guest session logic for now
        // START: Open guest session, if requested and permitted by client
        // configuration
        // END: Open guest session, if requested and permitted by client
        // configuration
        // START: Handle request for anonymous login for practice test (assumes
        // GUEST session?)
        // END: Handle request for anonymous login for practice test

        // START: Handle request for validated login.
        // -- This 'may' be a real student. Attempt to validate that
        _Ref<String> errorRef = new _Ref<>();
        _Ref<Long> entityRef = new _Ref<>();
        _T_ValidateTesteeLogin_SP(connection, clientname, externalId, sessionId, errorRef, entityRef);
        entity = entityRef.get();
        if (errorRef.get() != null) {

            resultsSets.add(_commonDll._ReturnError_SP(connection, clientname, "T_Login", errorRef.get()));
            _commonDll._LogDBLatency_SP(connection, "T_Login", starttime, entity, true, null, null, null, clientname, null);
            connection.dropTemporaryTable(valsTbl);

            return (new MultiDataResultSet(resultsSets));
        }

        // -- Student has been validated against the proctor and a valid RTS Key
        // returned. Now check required attributes
        final String SQL_QUERY12 = "update ${valsTblName} set outval = inval where _Key = 'ID'";
        Map<String, String> unquotedParms12 = unquotedParms3;
        executeStatement(connection, fixDataBaseNames(SQL_QUERY12, unquotedParms12), null, false).getUpdateCount();

        final String SQL_QUERY13 = "select  action from ${valsTblName} where outval is null and action = 'REQUIRE' limit 1";

        final String SQL_QUERY14 = " select  field,  atttype as type from ${valsTblName} where outval is null and action = 'REQUIRE' limit 1";

        final String SQL_QUERY15 = "update ${valsTblName} set outval = ${value}  where field = ${field}";

        LoginHelper loginHelper = new LoginHelper();
        loginHelper.setClientname(clientname);
        loginHelper.setEntity(entity);
        loginHelper.setTbl(valsTbl);
        loginHelper.setQueryExists(SQL_QUERY13);
        loginHelper.setQueryExec(SQL_QUERY14);
        loginHelper.setQueryUpdate(SQL_QUERY15);

        loginHelper.doIt(connection);

        // -- check for login failure due to bad required input data
        final String SQL_QUERY16 = "select  _key from ${valsTblName} where _key <> 'ID' and action = 'REQUIRE' and (outval is null or inval is null or inval <> outval ) limit 1";

        Map<String, String> unquotedParms16 = unquotedParms3;
        if (exists(executeStatement(connection, fixDataBaseNames(SQL_QUERY16, unquotedParms16), null, false))) {

            resultsSets.add(_commonDll._ReturnError_SP(connection, clientname, "T_Login", "No match"));
            _commonDll._LogDBLatency_SP(connection, "T_Login", starttime, entity, true, null, null, null, clientname, null);
            connection.dropTemporaryTable(valsTbl);

            return (new MultiDataResultSet(resultsSets));
        }

        // -- get the remaining verify data and data required by student app for
        // practice tests
        final String SQL_QUERY17 = "select  _key from ${valsTblName} where outval is null  limit 1";

        final String SQL_QUERY18 = " select  field,  atttype as type from ${valsTblName} where outval is null  limit 1";

        loginHelper.setQueryExists(SQL_QUERY17);
        loginHelper.setQueryExec(SQL_QUERY18);
        loginHelper.setQueryUpdate(SQL_QUERY15); // this one stays the same

        loginHelper.doIt(connection);

        // select 'success' as status, @entity as entityKey;
        List<CaseInsensitiveMap<Object>> resultList = new ArrayList<CaseInsensitiveMap<Object>>();
        CaseInsensitiveMap<Object> rcd = new CaseInsensitiveMap<Object>();
        rcd.put("status", "success");
        rcd.put("entityKey", entity);
        resultList.add(rcd);

        SingleDataResultSet rs1 = new SingleDataResultSet();
        rs1.addColumn("status", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs1.addColumn("entityKey", SQL_TYPE_To_JAVA_TYPE.BIGINT);
        rs1.addRecords(resultList);

        resultsSets.add(rs1);

        final String SQL_QUERY19 = "select TDS_ID, outval as Value, Label, SortOrder, atLogin from ${valsTblName}, ${ConfigDB}.client_testeeattribute "
                + " where clientname = ${clientname} and _Key = TDS_ID and atLogin in ('REQUIRE', 'VERIFY') order by SortOrder";
        Map<String, String> unquotedParms19 = unquotedParms3;
        SqlParametersMaps parms19 = (new SqlParametersMaps()).put("clientname", clientname);
        final String query19 = fixDataBaseNames(SQL_QUERY19);
        SingleDataResultSet rs2 = executeStatement(connection, fixDataBaseNames(query19, unquotedParms19), parms19, false).getResultSets().next();
        resultsSets.add(rs2);

        _commonDll._LogDBLatency_SP(connection, "T_Login", starttime, entity, true, null, null, null, clientname, null);
        connection.dropTemporaryTable(valsTbl);

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


    // This class is used only to run three specific queries having one unquoted parameter - temp table name
    class LoginHelper {
        private DataBaseTable tbl;
        private String queryExists;
        private String queryExec;
        private String queryUpdate;
        private String clientname;
        private Long entity;

        public void setTbl(DataBaseTable tbl) {
            this.tbl = tbl;
        }

        public void setQueryExists(String queryExists) {
            this.queryExists = queryExists;
        }

        public void setQueryExec(String queryExec) {
            this.queryExec = queryExec;
        }

        public void setQueryUpdate(String queryUpdate) {
            this.queryUpdate = queryUpdate;
        }

        public void setClientname(String clientname) {
            this.clientname = clientname;
        }

        public void setEntity(Long entity) {
            this.entity = entity;
        }

        public void doIt(SQLConnection connection) throws ReturnStatusException {

            Map<String, String> unquotedParms = new HashMap<>();
            unquotedParms.put("valsTblName", this.tbl.getTableName());

            _Ref<String> valueRef = new _Ref<>();
            _Ref<Long> relatedEntityRef = new _Ref<>();
            _Ref<String> relatedIdRef = new _Ref<>();

            while (exists(executeStatement(connection, fixDataBaseNames(this.queryExists, unquotedParms), null, false))) {
                String type = null;
                String field = null;
                SingleDataResultSet result = executeStatement(connection, fixDataBaseNames(this.queryExec, unquotedParms), null, false).getResultSets().next();
                DbResultRecord record = (result.getCount() > 0 ? result.getRecords().next() : null);
                if (record != null) {
                    field = record.<String>get("field");
                    type = record.<String>get("type");
                }

                valueRef.set(null);
                relatedEntityRef.set(null);
                relatedIdRef.set(null);
                if (DbComparator.isEqual(type, "attribute"))
                    _rtsDll._GetRTSAttribute_SP(connection, clientname, entity, field, valueRef);
                else if (DbComparator.isEqual(type, "relationship"))
                    _rtsDll._GetRTSRelationship_SP(connection, clientname, entity, field, relatedEntityRef, relatedIdRef, valueRef);

                String value = (valueRef.get() == null ? UNKNOWN_ATTRIBUTE_VALUE : valueRef.get());
                SqlParametersMaps parms = (new SqlParametersMaps()).put("value", value).put("field", field);
                executeStatement(connection, fixDataBaseNames(queryUpdate, unquotedParms), parms, false).getUpdateCount();
            }
        }
    }
}
