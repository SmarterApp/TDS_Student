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
import tds.dll.api.IStudentDLL;
import tds.student.performance.dao.ConfigurationDao;
import tds.student.performance.services.ConfigurationService;
import tds.student.performance.services.DbLatencyService;
import tds.student.performance.services.StudentInsertItemsService;
import tds.student.performance.utils.DateUtility;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;


/**
 * A service to replace T_InsertItems_SP  in StudentDLL
 */
@Service
public class StudentInsertItemsImpl extends AbstractDLL implements StudentInsertItemsService {
    private static final Logger logger = LoggerFactory.getLogger(StudentInsertItemsImpl.class);


    @Autowired
    private AbstractDateUtilDll _dateUtil = null;

    @Autowired
    private ICommonDLL _commonDll = null;

    @Autowired
    private IStudentDLL _studentDll = null;

    @Autowired
    private ConfigurationDao configurationDao;

    @Autowired
    private ConfigurationService configurationService;

    @Autowired
    private DateUtility dateUtility;

    @Autowired
    private DbLatencyService dbLatencyService;

    // replace T_InsertItems_SP  in StudentDLL
    public MultiDataResultSet insertItems(SQLConnection connection,
                                          UUID oppKey,
                                          UUID sessionKey,
                                          UUID browserId,
                                          Integer segment,
                                          String segmentId,
                                          Integer page,
                                          String groupId,
                                          String itemKeys,
                                          Character delimiter,
                                          Integer groupItemsRequired,
                                          Float groupB,
                                          Integer debug,
                                          Boolean noinsert)
            throws ReturnStatusException {

        List<SingleDataResultSet> resultsSets = new ArrayList<SingleDataResultSet>();
        Date starttime = _dateUtil.getDateWRetStatus(connection);
        String localhostname = _commonDll.getLocalhostName();
        _Ref<String> error = new _Ref<>();
        Long testee = null;

        _studentDll._ValidateTesteeAccessProc_SP(connection, oppKey, sessionKey, browserId, false, error);
        if (error.get() != null) {
            resultsSets.add(_commonDll._ReturnError_SP(connection, null, "T_InsertItems", error.get(), null, oppKey, "_ValidateTesteeAccesss", "denied"));
            return (new MultiDataResultSet(resultsSets));
        }

        Integer count = null;
        Integer opprestart = null;
        String status = null;
        String clientname = null;
        String environment = null;
        String language = null;
        String item = null;
        String testkey = null;
        Integer lastPosition = null;
        String msg = null;
        String segmentKey = null;
        String formKey = null;
        String algorithm = null;
        String argstring = null;

        DataBaseTable itemsTable = getDataBaseTable("items").addColumn("p", SQL_TYPE_To_JAVA_TYPE.INT).addColumn("itemkey", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 50);
        connection.createTemporaryTable(itemsTable);
        Map<String, String> unquotedParms = new HashMap<String, String>();
        unquotedParms.put("itemsTableName", itemsTable.getTableName());

        if (itemKeys != null) {
            DataBaseTable buildTable = _commonDll._BuildTable_FN(connection, "_BuildTable", itemKeys, delimiter.toString());
            final String SQL_INSERT1 = "insert into ${itemsTableName} (p, itemkey) select idx, record from ${temporaryTableName};";
            Map<String, String> unquotedParms1 = new HashMap<String, String>();
            unquotedParms1.put("itemsTableName", itemsTable.getTableName());
            unquotedParms1.put("temporaryTableName", buildTable.getTableName());
            executeStatement(connection, fixDataBaseNames(SQL_INSERT1, unquotedParms1), null, false).getUpdateCount();
            // System.err.println (insertedCnt); // for testing
            connection.dropTemporaryTable(buildTable);
        }
        final String SQL_QUERY1 = "select clientname, _efk_AdminSUbject as testkey, Restart as opprestart, status, environment, _efk_Testee as testee  from testopportunity where _key = ${oppkey};";
        SqlParametersMaps parms1 = new SqlParametersMaps().put("oppkey", oppKey);
        SingleDataResultSet result = executeStatement(connection, SQL_QUERY1, parms1, false).getResultSets().next();
        DbResultRecord record = (result.getCount() > 0 ? result.getRecords().next() : null);
        if (record != null) {
            clientname = record.<String>get("clientname");
            testkey = record.<String>get("testkey");
            opprestart = record.<Integer>get("opprestart");
            status = record.<String>get("status");
            environment = record.<String>get("environment");
            testee = record.<Long>get("testee");
        }

        if (DbComparator.notEqual(environment, "production")) {
            // / set @trace = 'TRACING ' + @groupID + ':' + @itemkeys;
            String trace = String.format("TRACING groupID = %s : itemkeys = %s", groupId, itemKeys);
        }
        if (DbComparator.notEqual(status, "started")) {
            resultsSets.add(_commonDll._ReturnError_SP(connection, clientname, "T_InsertItems",
                    "Your test opportunity has been interrupted. Please check with your Test Administrator to resume your test.", null,
                    oppKey, "T_InsertItems_2009", "denied"));
            return (new MultiDataResultSet(resultsSets));
        }
        final String SQL_QUERY2 = "select _efk_Segment as segmentKey, formKey, algorithm from testopportunitysegment where _fk_TestOpportunity = ${oppkey} and SegmentPosition = ${segment};";
        SqlParametersMaps parms2 = new SqlParametersMaps().put("oppkey", oppKey).put("segment", segment);
        result = executeStatement(connection, SQL_QUERY2, parms2, false).getResultSets().next();
        record = (result.getCount() > 0 ? result.getRecords().next() : null);
        if (record != null) {
            segmentKey = record.<String>get("segmentKey");
            formKey = record.<String>get("formKey");
            algorithm = record.<String>get("algorithm");
        }

        if (segmentKey == null) {
            argstring = segment.toString().trim();
            msg = String.format("Unknown test segment %s", argstring);
            _commonDll._LogDBError_SP(connection, "T_InsertItems", msg, null, null, null, oppKey, clientname, sessionKey);
            resultsSets.add(_commonDll._ReturnError_SP(connection, clientname, "T_InsertItems", "Unknown test segment", null, oppKey, null));
            return (new MultiDataResultSet(resultsSets));
        }
        _Ref<String> msgRef = new _Ref<>();

        _studentDll._ValidateItemInsert_SP(connection, oppKey, page, segment, segmentId, groupId, msgRef);

        if (msgRef.get() != null) {
            _commonDll._LogDBError_SP(connection, "T_InsertItems", msgRef.get(), null, null, null, oppKey, clientname, sessionKey);
            resultsSets.add(_commonDll._ReturnError_SP(connection, clientname, "T_InsertItems", "Database record insertion failed for new test items: ", msgRef.get(), oppKey, null));
            logger.debug("_ValidateItemInsert_SP: oppKey = " + oppKey);
            logger.debug("Message: " + msgRef.get());
            return (new MultiDataResultSet(resultsSets));
        }
        final String SQL_QUERY3 = "select AccCode as language from testeeaccommodations where _fk_TestOpportunity = ${oppkey} and AccType = ${Language};";
        SqlParametersMaps parms3 = new SqlParametersMaps().put("oppkey", oppKey).put("Language", "Language");
        result = executeStatement(connection, SQL_QUERY3, parms3, false).getResultSets().next();
        record = (result.getCount() > 0 ? result.getRecords().next() : null);
        if (record != null) {
            language = record.<String>get("language");
        }
        Integer minpos = null;
        Integer maxpos = null;
        Integer insertcnt = null;
        Integer lastSegment = null;
        Integer lastpage = null;
        Integer lastpos = null;

   /*final String SQL_QUERY4 = "select max(position) as lastPosition from testeeresponse where _fk_TestOpportunity = ${oppkey} and _efk_ITSItem is not null;";
   SqlParametersMaps parms4 = parms1;
   result = executeStatement (connection, SQL_QUERY4, parms4, false).getResultSets ().next ();
   record = (result.getCount () > 0 ? result.getRecords ().next () : null);
   if (record != null) {
     lastPosition = record.<Integer> get ("lastPosition");
   }*/
//   final String SQL_QUERY5 = "select segment as lastSegment, page as lastPage from testeeresponse where _fk_TestOpportunity = ${oppkey} and position = ${lastPosition};";
        final String SQL_QUERY5 = "select segment as lastSegment, page as lastPage, position as lastPosition from testeeresponse where _fk_TestOpportunity = ${oppkey} and position = (select max(position) as lastPosition from testeeresponse where _fk_TestOpportunity = ${oppkey} and _efk_ITSItem is not null);";
        SqlParametersMaps parms5 = new SqlParametersMaps().put("oppkey", oppKey)/*.put ("lastPosition", lastPosition)*/;
        result = executeStatement(connection, SQL_QUERY5, parms5, false).getResultSets().next();
        record = (result.getCount() > 0 ? result.getRecords().next() : null);
        if (record != null) {
            lastSegment = record.<Integer>get("lastSegment");
            lastpage = record.<Integer>get("lastPage");
            lastPosition = record.<Integer>get("lastPosition");
        }

        // Get item data from the itembank, filtering by the items that were chosen
        // by the selection algorithm (some may have been excluded)
        DataBaseTable insertsTable = getDataBaseTable("inserts").addColumn("bankitemkey", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 50).addColumn("relativePosition", SQL_TYPE_To_JAVA_TYPE.INT)
                .addColumn("formPosition", SQL_TYPE_To_JAVA_TYPE.INT).addColumn("Position", SQL_TYPE_To_JAVA_TYPE.INT).addColumn("answer", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 10)
                .addColumn("b", SQL_TYPE_To_JAVA_TYPE.FLOAT).addColumn("bankkey", SQL_TYPE_To_JAVA_TYPE.BIGINT).addColumn("_efk_ITSItem", SQL_TYPE_To_JAVA_TYPE.BIGINT)
                .addColumn("Scorepoint", SQL_TYPE_To_JAVA_TYPE.INT).addColumn("contentLevel", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 200).addColumn("Format", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 50)
                .addColumn("IsFieldTest", SQL_TYPE_To_JAVA_TYPE.BIT).addColumn("IsRequired", SQL_TYPE_To_JAVA_TYPE.BIT);
        connection.createTemporaryTable(insertsTable);
        // DataBaseTable TestItemgroupDataTable = ITEMBANK_TestItemGroupData_FN
        // (connection, testkey, groupId, language, formKey);
        // final String SQL_INSERT2 =
        // "insert into ${insertsTableName} (bankitemkey, relativePosition, bankkey, _efk_ITSItem, b, Scorepoint,  format, isFieldTest, IsRequired, contentLevel, formPosition, answer)"
        // +
        // "select bankitemkey, itemposition, bankkey, itemkey, IRT_b, scorepoint, itemType, IsFieldTest, IsRequired, ContentLevel, FormPosition, answerKey from ${TestItemgroupDataTableName} order by itemposition;";

        final String SQL_INSERT2 = "insert into ${insertsTableName} (bankitemkey, relativePosition, bankkey, _efk_ITSItem, b, Scorepoint,  format, isFieldTest, IsRequired, contentLevel, formPosition, answer)"
                + " select  A._fk_Item as bankitemkey, ItemPosition, _efk_ItemBank as bankkey, _efk_Item as itemkey,  IRT_b,"
                + " ScorePoint, ItemType, IsFieldTest, IsRequired, _fk_Strand as ContentLevel, "
                + " (select FormPosition from ${ItemBankDB}.testformitem F where F._fk_Item = A._fk_Item and _fk_TestForm = ${formkey} and F._fk_AdminSubject = ${segmentKey}) as FormPosition, "
                + " Answer as answerKey "
                + " from ${ItemBankDB}.tblsetofadminitems A, ${ItemBankDB}.tblitem I, ${ItemBankDB}.tblitemprops P where A._fk_AdminSubject = ${segmentKey} and A.groupID = ${groupID} and A._fk_ITem = I._Key"
                + " and P._fk_AdminSubject = ${segmentKey} and P._fk_Item = A._fk_Item and P.Propname = 'Language' and P.Propvalue = ${language} "
                + " order by itemposition";

        // + " groupKey, GroupID,  P.IsActive, BlockID,     "
        // + "  ContentSize,   "
        // + " strandName, "
        // +
        // " (select concat(C.Homepath, B.HomePath, B.ItemPath, I.FilePath, I.FileName) "
        // +
        // " from ${ItemBankDB}.tblitembank B, ${ItemBankDB}.tblclient C, ${ItemBankDB}.tblitem I"
        // +
        // " where B._efk_Itembank = bankkey and B._fk_Client = C._Key and I._Key = concat(bankkey, '-', itemkey) limit 1) as itemFile "

        Map<String, String> unquotedParms3 = new HashMap<String, String>();
        unquotedParms3.put("insertsTableName", insertsTable.getTableName());

        SqlParametersMaps params = (new SqlParametersMaps()).put("formkey", formKey).put("testkey", testkey).put("groupid", groupId).put("language", language).put("segmentKey", segmentKey);
        String query = fixDataBaseNames(SQL_INSERT2); // to substitute
        // ${ItemBankDB}
        executeStatement(connection, fixDataBaseNames(query, unquotedParms3), params, false).getUpdateCount();
        if (itemKeys != null) {
            final String SQL_DELETE1 = "delete from  ${insertsTableName} where bankitemkey not in (select itemkey from ${itemsTableName});";
            Map<String, String> unquotedParms4 = new HashMap<String, String>();
            unquotedParms4.put("insertsTableName", insertsTable.getTableName());
            unquotedParms4.put("itemsTableName", itemsTable.getTableName());
            executeStatement(connection, fixDataBaseNames(SQL_DELETE1, unquotedParms4), null, false).getUpdateCount();
            // System.err.println (deletedCnt); // for testing
        }
        final String SQL_QUERY6 = "select  bankitemkey from ${insertsTableName} where formPosition is null limit 1";
        if (DbComparator.isEqual(algorithm, "fixedform") && (exists(executeStatement(connection, fixDataBaseNames(SQL_QUERY6, unquotedParms3), null, false)))) {
            // set @msg = 'Item(s) not on form: ' + @groupID + '; items: ' +
            // @itemkeys;
            msg = String.format("Item(s) not on form: groupID = %s; items: = %s ", groupId, itemKeys);
            _commonDll._LogDBError_SP(connection, "T_InsertItems", msg, null, null, null, oppKey, clientname, sessionKey);
            resultsSets.add(_commonDll._ReturnError_SP(connection, clientname, "T_InsertItems", "Database record insertion failed for new test items", null, oppKey, null));
            return (new MultiDataResultSet(resultsSets));
        }
        final String SQL_QUERY7 = "select  bankitemkey from ${insertsTableName} limit 1";
        if (!exists(executeStatement(connection, fixDataBaseNames(SQL_QUERY7, unquotedParms3), null, false))) {
            // set @msg = 'Item group does not exist: ' + @groupID + '; items: ' +
            // @itemkeys;
            msg = String.format("Item group does not exist: groupID = %s; items: = %s ", groupId, itemKeys);
            _commonDll._LogDBError_SP(connection, "T_InsertItems", msg, null, null, null, oppKey, clientname, sessionKey);
            resultsSets.add(_commonDll._ReturnError_SP(connection, clientname, "T_InsertItems", "Database record insertion failed for new test items", null, oppKey, null));
            return (new MultiDataResultSet(resultsSets));
        }

        if (DbComparator.isEqual(algorithm, "fixedform")) {
            Integer formStart = null;
            final String SQL_QUERY8 = "select min(formPosition) as formStart from ${insertsTableName};";
            result = executeStatement(connection, fixDataBaseNames(SQL_QUERY8, unquotedParms3), null, false).getResultSets().next();
            record = (result.getCount() > 0 ? result.getRecords().next() : null);
            if (record != null) {
                formStart = record.<Integer>get("formStart");
            }
            final String SQL_UPDATE1 = "update ${insertsTableName} set relativePosition = formPosition - ${formStart};";
            SqlParametersMaps parms6 = new SqlParametersMaps().put("formStart", formStart);
            executeStatement(connection, fixDataBaseNames(SQL_UPDATE1, unquotedParms3), parms6, false).getUpdateCount();
            // System.err.println (updatedCnt); // for testing
        }
        final String SQL_QUERY9 = "select relativePosition from ${insertsTableName} group by relativePosition having count(*) > 1";
        final String SQL_QUERY10 = "select  bankitemkey from ${insertsTableName} where relativePosition is null limit 1";

        if ((exists(executeStatement(connection, fixDataBaseNames(SQL_QUERY9, unquotedParms3), null, false)))
                || (exists(executeStatement(connection, fixDataBaseNames(SQL_QUERY10, unquotedParms3), null, false)))) {
            msg = String.format("Ambiguous item positions in item group %s", groupId);
            _commonDll._LogDBError_SP(connection, "T_InsertItems", msg, null, null, null, oppKey, clientname, sessionKey);
            resultsSets.add(_commonDll._ReturnError_SP(connection, clientname, "T_InsertItems", "Database record insertion failed for new test items"));
            return (new MultiDataResultSet(resultsSets));
        }
        lastpos = lastPosition;
        if (lastpos == null)
            lastpos = 0;

        final String SQL_QUERY11 = "select  bankitemkey from ${insertsTableName} where position is null limit 1";
        while (exists(executeStatement(connection, fixDataBaseNames(SQL_QUERY11, unquotedParms3), null, false))) {
            final String SQL_QUERY12 = "select min(relativePosition) as minpos from ${insertsTableName} where position is null;";
            result = executeStatement(connection, fixDataBaseNames(SQL_QUERY12, unquotedParms3), null, false).getResultSets().next();
            record = (result.getCount() > 0 ? result.getRecords().next() : null);
            if (record != null) {
                minpos = record.<Integer>get("minpos");
            }
            final String SQL_UPDATE2 = "update ${insertsTableName} set position = ${lastpos} + 1 where relativePosition = ${minpos};";
            SqlParametersMaps parms7 = new SqlParametersMaps().put("lastpos", lastpos).put("minpos", minpos);
            executeStatement(connection, fixDataBaseNames(SQL_UPDATE2, unquotedParms3), parms7, false).getUpdateCount();
            // System.err.println (updatedCnt); // for testing

            lastpos += 1;
        }
        final String SQL_QUERY13 = "select count(*) as count from ${insertsTableName};";
        result = executeStatement(connection, fixDataBaseNames(SQL_QUERY13, unquotedParms3), null, false).getResultSets().next();
        record = (result.getCount() > 0 ? result.getRecords().next() : null);
        if (record != null) {
            count = record.<Long>get("count").intValue();
        }
        if (debug == 1) {
            final String SQL_QUERY14 = "select * from ${insertsTableName} ;";
            result = executeStatement(connection, fixDataBaseNames(SQL_QUERY14, unquotedParms3), null, false).getResultSets().next();
            resultsSets.add(result);
        }
        final String SQL_QUERY15 = "select  bankitemkey from testeeresponse, ${insertsTableName} where _fk_TestOpportunity = ${oppkey} and _efk_Itemkey = bankitemkey limit 1";
        SqlParametersMaps parms8 = parms1;
        if (exists(executeStatement(connection, fixDataBaseNames(SQL_QUERY15, unquotedParms3), parms8, false))) {
            msg = String.format("Attempt to duplicate existing item: %s", itemKeys);
            _commonDll._LogDBError_SP(connection, "T_InsertItems", msg, null, null, null, oppKey, clientname, sessionKey);
            resultsSets.add(_commonDll._ReturnError_SP(connection, clientname, "T_InsertItems", "Database record insertion failed for new test items", null, oppKey, null));
            return (new MultiDataResultSet(resultsSets));
        }
        if (noinsert == true) {
            return (new MultiDataResultSet(resultsSets));
        }
        String errmsg = null;
        Integer itemcnt = null;
        try {
            boolean preexistingAutoCommitMode = connection.getAutoCommit();
            connection.setAutoCommit(false);

            final String SQL_INSERT3 = "insert into testeeresponse (_fk_TestOpportunity, Position) select ${oppkey}, R.position from ${insertsTableName} R " +
                    " where not exists (select * from testeeresponse where _fk_TestOpportunity = ${oppkey} and position = R.position);";
            SqlParametersMaps parms9 = parms1;
            executeStatement(connection, fixDataBaseNames(SQL_INSERT3, unquotedParms3), parms9, false).getUpdateCount();
            // TODO Elena: this is temporary solution while Sai further researches
            // mysql capabilities around this and
            // see if it is even possible to this and the next statement as 1
            // statement call
            final String SQL_EXISTS1 = "select page from testeeresponse T,  ${insertsTableName} R where T._fk_TestOpportunity = ${oppkey} and "
                    + " (T.page = ${page} or (T._efk_ITSBank = R.bankkey and T._efk_ITSItem = R._efk_ITSItem))";
            SqlParametersMaps prm = (new SqlParametersMaps()).put("oppkey", oppKey).put("page", page);
            if (exists(executeStatement(connection, fixDataBaseNames(SQL_EXISTS1, unquotedParms3), prm, false)) == false) {
                final String SQL_UPDATE3 = "Update testeeresponse T, ${insertsTableName} R  set T.isRequired = R.IsRequired, T._efk_ITSItem = R._efk_ITSItem, T._efk_ITSBank = R.bankkey, "
                        + " T.response = null, T.OpportunityRestart = ${opprestart}, T.Page = ${page}, T.Answer = R.Answer, T.ScorePoint = R.ScorePoint, T.DateGenerated = ${today},"
                        + " T._fk_Session = ${session}, T.Format = R.format, T.isFieldTest = R.isFieldTest, T.Hostname = ${hostname}, T.GroupID = ${groupID}, T.groupItemsRequired = ${groupItemsRequired},"
                        + " T._efk_Itemkey = R.bankitemkey, T.segment = ${segment}, T.segmentID = ${segmentID}, T.groupB = ${groupB}, T.itemB = b   "
                        + " where  _fk_TestOpportunity = ${oppkey} and T.position = R.position and T._efk_ITSItem is null ";
                // +
                // " and not exists (select * from testeeresponse T where T._fk_TestOpportunity = ${oppkey} and (T.page = ${page} or (T._efk_ITSBank = R.bankkey and T._efk_ITSItem = R._efk_ITSItem)));";
                SqlParametersMaps parms10 = new SqlParametersMaps();
                parms10.put("oppkey", oppKey);
                parms10.put("opprestart", opprestart);
                parms10.put("page", page);
                parms10.put("today", starttime);
                parms10.put("session", sessionKey);
                parms10.put("hostname", localhostname);
                parms10.put("groupID", groupId);
                parms10.put("groupItemsRequired", groupItemsRequired);
                parms10.put("segment", segment);
                parms10.put("segmentID", segmentId);
                parms10.put("groupB", groupB);

                executeStatement(connection, fixDataBaseNames(SQL_UPDATE3, unquotedParms3), parms10, false).getUpdateCount();
            }
            // check for successful insertion of ALL and ONLY the items in the group
            // given here
            final String SQL_QUERY16 = "select count(*) as itemcnt from testeeresponse where _fk_TestOpportunity = ${oppkey} and GroupID = ${groupID} and DateGenerated = ${today};";
            SqlParametersMaps parms11 = new SqlParametersMaps().put("oppkey", oppKey).put("groupID", groupId).put("today", starttime);
            result = executeStatement(connection, SQL_QUERY16, parms11, false).getResultSets().next();
            record = (result.getCount() > 0 ? result.getRecords().next() : null);
            if (record != null) {
                itemcnt = record.<Long>get("itemcnt").intValue();
            }
            if (itemcnt != null && count != null && DbComparator.notEqual(itemcnt, count)) {
                connection.rollback();
                connection.setAutoCommit(preexistingAutoCommitMode);
                errmsg = String.format("Item insertion failed for group %s", groupId);
                _commonDll._LogDBError_SP(connection, "T_InsertItems", errmsg, null, null, null, oppKey, clientname, sessionKey);
                resultsSets.add(_commonDll._ReturnError_SP(connection, clientname, "T_InsertItems", "Database record insertion failed for new test items", null, oppKey, null));
                return (new MultiDataResultSet(resultsSets));
            }
            final String SQL_QUERY17 = "select  bankitemkey from ${insertsTableName} where isFIeldTest = 1 limit 1";
            if (exists(executeStatement(connection, fixDataBaseNames(SQL_QUERY17, unquotedParms3), null, false))) {
                Integer minFTpos = null;
                final String SQL_QUERY18 = "select min(position) as minFTpos from ${insertsTableName}";
                result = executeStatement(connection, fixDataBaseNames(SQL_QUERY18, unquotedParms3), null, false).getResultSets().next();
                record = (result.getCount() > 0 ? result.getRecords().next() : null);
                if (record != null) {
                    minFTpos = record.<Integer>get("minFTpos");
                }
                final String SQL_UPDATE4 = "update ft_opportunityitem set dateAdministered = ${now}, positionAdministered = ${minFTpos} where _fk_TestOpportunity = ${oppkey} " +
                        "and segment = ${segment} and groupID = ${groupID};";
                SqlParametersMaps parms12 = new SqlParametersMaps().put("oppkey", oppKey).put("groupID", groupId).put("now", starttime).put("minFTpos", minFTpos)
                        .put("segment", segment);
                executeStatement(connection, SQL_UPDATE4, parms12, false).getUpdateCount();
            }
            if ( _studentDll._AA_IsSegmentSatisfied_FN(connection, oppKey, segment) ) {
                final String SQL_UPDATE5 = "update testopportunitysegment set IsSatisfied = 1 where _fk_TestOpportunity = ${oppkey} and segmentPosition = ${segment};";
                SqlParametersMaps parms13 = new SqlParametersMaps().put("oppkey", oppKey).put("segment", segment);
                executeStatement(connection, SQL_UPDATE5, parms13, false).getUpdateCount();
            }
            connection.commit();
            connection.setAutoCommit(preexistingAutoCommitMode);
        } catch (Exception e) {
            try {
                connection.rollback();
            } catch (SQLException se) {
                logger.error("Failed rollback transaction");
            }
            errmsg = String.format("Item insertion failed: %s", e.getMessage());
            _commonDll._LogDBError_SP(connection, "T_InsertItems", errmsg, null, null, null, oppKey, clientname, sessionKey);
            resultsSets.add(_commonDll._ReturnError_SP(connection, clientname, "T_InsertItems", "Database record insertion failed for new test items", null, oppKey, null));
            return (new MultiDataResultSet(resultsSets));
        }
        try {
            final String SQL_QUERY19 = "select count(*) as itemcnt from testeeresponse where _fk_TestOpportunity = ${oppkey} and dateGenerated is not null;";
            SqlParametersMaps parms14 = parms1;
            result = executeStatement(connection, SQL_QUERY19, parms14, false).getResultSets().next();
            record = (result.getCount() > 0 ? result.getRecords().next() : null);
            if (record != null) {
                itemcnt = record.<Long>get("itemcnt").intValue();
            }
            final String SQL_UPDATE6 = "update testopportunity set inSegment = ${segment}, numitems = ${itemcnt} where _Key = ${oppkey};";
            SqlParametersMaps parms15 = new SqlParametersMaps().put("oppkey", oppKey).put("segment", segment).put("itemcnt", itemcnt);
            executeStatement(connection, SQL_UPDATE6, parms15, false).getUpdateCount();
            // System.err.println (updateCnt); // for testing

        } catch (ReturnStatusException re) {
            errmsg = re.getMessage();
            _commonDll._LogDBError_SP(connection, "T_InsertItems", errmsg, null, null, null, oppKey, clientname, sessionKey);
        }
        String starttimeStr = new SimpleDateFormat(AbstractDateUtilDll.DB_DATETIME_FORMAT_MS_PRECISION).format(starttime);
        List<CaseInsensitiveMap<Object>> resultList = new ArrayList<CaseInsensitiveMap<Object>>();
        CaseInsensitiveMap<Object> rcd = new CaseInsensitiveMap<Object>();
        rcd.put("status", "inserted");
        rcd.put("number", count);
        rcd.put("reason", null);
        rcd.put("dateCreated", starttimeStr);
        resultList.add(rcd);

        SingleDataResultSet rs1 = new SingleDataResultSet();
        rs1.addColumn("status", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs1.addColumn("number", SQL_TYPE_To_JAVA_TYPE.INT);
        rs1.addColumn("reason", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        rs1.addColumn("dateCreated", SQL_TYPE_To_JAVA_TYPE.VARCHAR);

        rs1.addRecords(resultList);
        resultsSets.add(rs1);

        final String SQL_QUERY21 = "select bankitemkey, bankkey, _efk_ITSItem as itemkey, bigtoint(${page}) as page, position," +
                " format from ${insertsTableName} order by position;";
        SqlParametersMaps parms17 = new SqlParametersMaps().put("page", page);
        SingleDataResultSet rs2 = executeStatement(connection, fixDataBaseNames(SQL_QUERY21, unquotedParms3), parms17, false).getResultSets().next();
        resultsSets.add(rs2);

        connection.dropTemporaryTable(insertsTable);
        connection.dropTemporaryTable(itemsTable);
        _commonDll._LogDBLatency_SP(connection, "T_InsertItems", starttime, null, true, page, oppKey, sessionKey, clientname, null);
        return (new MultiDataResultSet(resultsSets));
    }

}
