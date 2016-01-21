package tds.student.performance.services.impl;

import AIR.Common.DB.*;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers.CaseInsensitiveMap;
import AIR.Common.Helpers._Ref;
import AIR.Common.Sql.AbstractDateUtilDll;
import TDS.Shared.Exceptions.ReturnStatusException;
import com.google.common.base.Splitter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.dll.api.ICommonDLL;
import tds.dll.api.IStudentDLL;
import tds.student.performance.dao.OpportunitySegmentDao;
import tds.student.performance.domain.InsertTesteeResponse;
import tds.student.performance.domain.ItemForTesteeResponse;
import tds.student.performance.domain.OpportunitySegment;
import tds.student.performance.services.StudentInsertItemsService;
import tds.student.performance.utils.TesteeResponseHelper;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Service to contain a new version of StudentDLL.T_InsertItems_SP
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
    private OpportunitySegmentDao opportunitySegmentDao;

    /**
     * A new implementation of StudentDLL.T_InsertItems_SP.  The objective of this implementation is to reduce round
     * trips to the database by replacing temporary tables with data structures, using cached queries and combining
     * queries where possible.
     *
     * Note: noinsert and debug parameters have been removed. These values are hard coded
     * to false in ResponseRepository.insertItems.  Debug log statements have been added and
     * integration tests exist to exercise logic.
     *
     * @param connection A database connection.
     * @param oppKey The testee opportunity key.
     * @param sessionKey The testee session key.
     * @param browserId The testee browser id.
     * @param segment The segment number.
     * @param segmentId The segment id.
     * @param page The page number.
     * @param groupId The group id.
     * @param itemKeys The itemKeys as a delimited string
     * @param delimiter The delimiter used in item keys.
     * @param groupItemsRequired Indicator for grouping required.
     * @param groupB Used in the join to update testeeresponses
     * @return A {@link MultiDataResultSet} with data on updated testeeresponses or error information.
     * @throws ReturnStatusException
     */
    @Override
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
                                          Float groupB)
            throws ReturnStatusException {

        List<SingleDataResultSet> resultsSets = new ArrayList<>();
        Date starttime = _dateUtil.getDateWRetStatus(connection);
        String localhostname = _commonDll.getLocalhostName();
        _Ref<String> error = new _Ref<>();

        logger.debug("In insertItems : oppKey: {} ", oppKey.toString() );

        if ( itemKeys == null ) {
            itemKeys = "";
        }
        final List<String> itemKeyList = Splitter.on(delimiter).omitEmptyStrings().trimResults().splitToList(itemKeys);

        _studentDll._ValidateTesteeAccessProc_SP(connection, oppKey, sessionKey, browserId, false, error);
        if (error.get() != null) {
            resultsSets.add(_commonDll._ReturnError_SP(connection, null, "T_InsertItems", error.get(), null, oppKey, "_ValidateTesteeAccess", "denied"));
            return (new MultiDataResultSet(resultsSets));
        }

        Integer count;
        Integer lastPosition = null;
        String msg;

        // Query the opportunity segment and return error if not found
        OpportunitySegment oppSeg = opportunitySegmentDao.getOpportunitySegmentAccommodation(oppKey, segment);
        if (oppSeg == null) {
            resultsSets.add(_commonDll._ReturnError_SP(connection, "Empty", "T_InsertItems", "Segment not found"));
            return (new MultiDataResultSet(resultsSets));
        }
        SqlParametersMaps parms1 = new SqlParametersMaps().put("oppkey", oppKey);

        if (DbComparator.notEqual(oppSeg.getStatus(), "started")) {
            resultsSets.add(_commonDll._ReturnError_SP(connection, oppSeg.getClientName(), "T_InsertItems",
                    "Your test opportunity has been interrupted. Please check with your Test Administrator to resume your test.", null,
                    oppKey, "T_InsertItems_2009", "denied"));
            return (new MultiDataResultSet(resultsSets));
        }

        if (oppSeg.getSegmentKey() == null) {
            msg = String.format("Unknown test segment %s", segment.toString().trim());
            _commonDll._LogDBError_SP(connection, "T_InsertItems", msg, null, null, null, oppKey, oppSeg.getClientName(), sessionKey);
            resultsSets.add(_commonDll._ReturnError_SP(connection, oppSeg.getClientName(), "T_InsertItems", "Unknown test segment", null, oppKey, null));
            return (new MultiDataResultSet(resultsSets));
        }

        _Ref<String> msgRef = new _Ref<>();
        _studentDll._ValidateItemInsert_SP(connection, oppKey, page, segment, segmentId, groupId, msgRef);
        if (msgRef.get() != null) {
            _commonDll._LogDBError_SP(connection, "T_InsertItems", msgRef.get(), null, null, null, oppKey, oppSeg.getClientName(), sessionKey);
            resultsSets.add(_commonDll._ReturnError_SP(connection, oppSeg.getClientName(), "T_InsertItems", "Database record insertion failed for new test items: ", msgRef.get(), oppKey, null));
            logger.debug("_ValidateItemInsert_SP: oppKey = " + oppKey);
            logger.debug("Message: " + msgRef.get());
            return (new MultiDataResultSet(resultsSets));
        }

        // Get the new item insert list from the database
        List<ItemForTesteeResponse> itemInsertListDB = opportunitySegmentDao.getItemForTesteeResponse(
                oppSeg.getSegmentKey(),
                oppSeg.getFormKey(),
                groupId,
                oppSeg.getLanguage());

        // Make a copy that filters out items not in itemList
        List<InsertTesteeResponse> itemInsertList = TesteeResponseHelper.createInsertsList(itemInsertListDB, itemKeys, delimiter);
        dumpInsertList(itemInsertList);

        final String SQL_QUERY5 = "select segment as lastSegment, page as lastPage, position as lastPosition from testeeresponse where _fk_TestOpportunity = ${oppkey} and position = (select max(position) as lastPosition from testeeresponse where _fk_TestOpportunity = ${oppkey} and _efk_ITSItem is not null);";
        SqlParametersMaps parms5 = new SqlParametersMaps().put("oppkey", oppKey)/*.put ("lastPosition", lastPosition)*/;
        SingleDataResultSet result = executeStatement(connection, SQL_QUERY5, parms5, false).getResultSets().next();
        DbResultRecord record = (result.getCount() > 0 ? result.getRecords().next() : null);
        if (record != null) {
            lastPosition = record.<Integer>get("lastPosition");
        }

        // Make sure fixed form has not null form position items. SQL_QUERY6
        if (DbComparator.isEqual(oppSeg.getAlgorithm(), "fixedform") && TesteeResponseHelper.nullFormPositionList(itemInsertList).size() > 0 ) {
            _commonDll._LogDBError_SP(connection, "T_InsertItems", String.format("Item(s) not on form: groupID = %s; items: = %s ", groupId, itemKeys), null, null, null, oppKey, oppSeg.getClientName(), sessionKey);
            resultsSets.add(_commonDll._ReturnError_SP(connection, oppSeg.getClientName(), "T_InsertItems", "Database record insertion failed for new test items", null, oppKey, null));
            return (new MultiDataResultSet(resultsSets));
        }

        // Check list has items replaces SQL_QUERY7
        if ( itemInsertList == null || itemInsertList.size() <= 0 ) {
            _commonDll._LogDBError_SP(connection, "T_InsertItems", String.format("Item group does not exist: groupID = %s; items: = %s ", groupId, itemKeys), null, null, null, oppKey, oppSeg.getClientName(), sessionKey);
            resultsSets.add(_commonDll._ReturnError_SP(connection, oppSeg.getClientName(), "T_InsertItems", "Database record insertion failed for new test items", null, oppKey, null));
            return (new MultiDataResultSet(resultsSets));
        }

        // Replaces SQL_QUERY8 and SQL_UPDATE1 for relativePosition
        TesteeResponseHelper.updateItemPosition(itemInsertList);
        dumpInsertList(itemInsertList);

        // Replaces SQL_QUERY9 and SQL_QUERY10
        if ( TesteeResponseHelper.ambiguousItemPosition(itemInsertList) ) {
            _commonDll._LogDBError_SP(connection, "T_InsertItems", String.format("Ambiguous item positions in item group %s", groupId), null, null, null, oppKey, oppSeg.getClientName(), sessionKey);
            resultsSets.add(_commonDll._ReturnError_SP(connection, oppSeg.getClientName(), "T_InsertItems", "Database record insertion failed for new test items"));
            return (new MultiDataResultSet(resultsSets));
        }

        if ( lastPosition == null ) {
            lastPosition = 0;
        }

        // Update the item positions. Replaces SQL_QUERY11, SQL_QUERY12, SQL_UPDATE2
        itemInsertList = TesteeResponseHelper.incrementItemPositionByLast(itemInsertList, lastPosition);
        dumpInsertList(itemInsertList);

        // Get the count. Replaces SQL_QUERY13, SQL_QUERY14
        count = itemInsertList.size();

        // Check for duplicate items. Replaces SQL_QUERY15
        if ( opportunitySegmentDao.existsTesteeResponsesByBankKeyAndOpportunity(oppKey, itemKeyList) ) {
            msg = String.format("Attempt to duplicate existing item: %s", itemKeys);
            _commonDll._LogDBError_SP(connection, "T_InsertItems", msg, null, null, null, oppKey, oppSeg.getClientName(), sessionKey);
            resultsSets.add(_commonDll._ReturnError_SP(connection, oppSeg.getClientName(), "T_InsertItems", "Database record insertion failed for new test items", null, oppKey, null));
            return (new MultiDataResultSet(resultsSets));
        }

        String errmsg;
        Integer itemcnt = null;
        try {
            boolean preexistingAutoCommitMode = connection.getAutoCommit();
            connection.setAutoCommit(false);

            String newTempTable = opportunitySegmentDao.loadInsertTableForTesteeResponses(connection, itemInsertList);
            Map<String, String> unquotedParamsTempInsert = new HashMap<>();
            unquotedParamsTempInsert.put("insertsTableName", newTempTable);

            final String SQL_INSERT3 = "insert into testeeresponse (_fk_TestOpportunity, Position) select ${oppkey}, R.position from ${insertsTableName} R " +
                    " where not exists (select * from testeeresponse where _fk_TestOpportunity = ${oppkey} and position = R.position);";
            SqlParametersMaps parms9 = parms1;
            int insertCount = executeStatement(connection, fixDataBaseNames(SQL_INSERT3, unquotedParamsTempInsert), parms9, false).getUpdateCount();
            logger.debug("*** SQL_INSERT3 to testeeresponse count: {}", insertCount);

            final String SQL_EXISTS1 = "select page from testeeresponse T,  ${insertsTableName} R where T._fk_TestOpportunity = ${oppkey} and "
                    + " (T.page = ${page} or (T._efk_ITSBank = R.bankkey and T._efk_ITSItem = R._efk_ITSItem))";
            SqlParametersMaps prm = (new SqlParametersMaps()).put("oppkey", oppKey).put("page", page);

            if ( !exists( executeStatement(connection, fixDataBaseNames(SQL_EXISTS1, unquotedParamsTempInsert), prm, false) ) ) {
                final String SQL_UPDATE3 = "Update testeeresponse T, ${insertsTableName} R  set T.isRequired = R.IsRequired, T._efk_ITSItem = R._efk_ITSItem, T._efk_ITSBank = R.bankkey, "
                        + " T.response = null, T.OpportunityRestart = ${opprestart}, T.Page = ${page}, T.Answer = R.Answer, T.ScorePoint = R.ScorePoint, T.DateGenerated = ${today},"
                        + " T._fk_Session = ${session}, T.Format = R.format, T.isFieldTest = R.isFieldTest, T.Hostname = ${hostname}, T.GroupID = ${groupID}, T.groupItemsRequired = ${groupItemsRequired},"
                        + " T._efk_Itemkey = R.bankitemkey, T.segment = ${segment}, T.segmentID = ${segmentID}, T.groupB = ${groupB}, T.itemB = b   "
                        + " where  _fk_TestOpportunity = ${oppkey} and T.position = R.position and T._efk_ITSItem is null ";

                SqlParametersMaps parms10 = new SqlParametersMaps();
                parms10.put("oppkey", oppKey);
                parms10.put("opprestart", oppSeg.getRestart());
                parms10.put("page", page);
                parms10.put("today", starttime);
                parms10.put("session", sessionKey);
                parms10.put("hostname", localhostname);
                parms10.put("groupID", groupId);
                parms10.put("groupItemsRequired", groupItemsRequired);
                parms10.put("segment", segment);
                parms10.put("segmentID", segmentId);
                parms10.put("groupB", groupB);

                int existsUpdateCnt = executeStatement(connection, fixDataBaseNames(SQL_UPDATE3, unquotedParamsTempInsert), parms10, false).getUpdateCount();
                opportunitySegmentDao.dropTempTable(connection, newTempTable);
                logger.debug("*** Not SQL_EXISTS1 execute SQL_UPDATE3 updated: " + existsUpdateCnt);
            }

            // check for successful insertion of ALL and ONLY the items in the group given here
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
                _commonDll._LogDBError_SP(connection, "T_InsertItems", errmsg, null, null, null, oppKey, oppSeg.getClientName(), sessionKey);
                resultsSets.add(_commonDll._ReturnError_SP(connection, oppSeg.getClientName(), "T_InsertItems", "Database record insertion failed for new test items", null, oppKey, null));
                return (new MultiDataResultSet(resultsSets));
            }

            // New Replaces  SQL_QUERY17, SQL_QUERY18
            if ( TesteeResponseHelper.isFieldTestList(itemInsertList).size() > 0 ) {
                Integer minFTpos = TesteeResponseHelper.minimumPosition(itemInsertList);
                final String SQL_UPDATE4 = "update ft_opportunityitem set dateAdministered = ${now}, positionAdministered = ${minFTpos} where _fk_TestOpportunity = ${oppkey} " +
                        "and segment = ${segment} and groupID = ${groupID};";
                SqlParametersMaps paramsFieldTest = new SqlParametersMaps().put("oppkey", oppKey).put("groupID", groupId).put("now", starttime).put("minFTpos", minFTpos).put("segment", segment);
                executeStatement(connection, SQL_UPDATE4, paramsFieldTest, false).getUpdateCount();
            }


            if (_studentDll._AA_IsSegmentSatisfied_FN(connection, oppKey, segment)) {
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
            _commonDll._LogDBError_SP(connection, "T_InsertItems", errmsg, null, null, null, oppKey, oppSeg.getClientName(), sessionKey);
            resultsSets.add(_commonDll._ReturnError_SP(connection, oppSeg.getClientName(), "T_InsertItems", "Database record insertion failed for new test items", null, oppKey, null));
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
            _commonDll._LogDBError_SP(connection, "T_InsertItems", errmsg, null, null, null, oppKey, oppSeg.getClientName(), sessionKey);
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

        // New result set from data structure. Replaces SQL_QUERY21
        resultsSets.add(createResultSetFromItemList(itemInsertList, page));

        _commonDll._LogDBLatency_SP(connection, "T_InsertItems", starttime, null, true, page, oppKey, sessionKey, oppSeg.getClientName(), null);
        return (new MultiDataResultSet(resultsSets));
    }


    private SingleDataResultSet createResultSetFromItemList(List<InsertTesteeResponse> items, Integer page)
            throws ReturnStatusException {

        SingleDataResultSet newResultSet = new SingleDataResultSet();
        newResultSet.addColumn("bankitemkey", SQL_TYPE_To_JAVA_TYPE.VARCHAR);
        newResultSet.addColumn("bankkey", SQL_TYPE_To_JAVA_TYPE.BIGINT);
        newResultSet.addColumn("itemkey", SQL_TYPE_To_JAVA_TYPE.BIGINT);
        newResultSet.addColumn("page", SQL_TYPE_To_JAVA_TYPE.INT);
        newResultSet.addColumn("position", SQL_TYPE_To_JAVA_TYPE.INT);
        newResultSet.addColumn("format", SQL_TYPE_To_JAVA_TYPE.VARCHAR);

        for (InsertTesteeResponse newItem : items) {
            List<CaseInsensitiveMap<Object>> newResultList = new ArrayList<>();

            CaseInsensitiveMap<Object> map = new CaseInsensitiveMap<>();
            map.put("bankitemkey", newItem.getBankItemKey());
            map.put("bankkey", newItem.getBankKey());
            map.put("itemkey", newItem.getItemKey());
            map.put("page", page);
            map.put("position", newItem.getPosition());
            map.put("format", newItem.getItemType());

            newResultList.add(map);
            newResultSet.addRecords(newResultList);
        }

        return newResultSet;
    }

    // Helper to debug dump item list
    private void dumpInsertList(Collection<InsertTesteeResponse> items) {
        for (InsertTesteeResponse i : items) {
            logger.debug("  Item: {} ", i.toString());
        }
    }

}
