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
package tds.student.performance.services.impl;

import AIR.Common.DB.*;
import AIR.Common.DB.results.DbResultRecord;
import AIR.Common.DB.results.MultiDataResultSet;
import AIR.Common.DB.results.SingleDataResultSet;
import AIR.Common.Helpers._Ref;
import AIR.Common.Sql.AbstractDateUtilDll;
import TDS.Shared.Exceptions.ReturnStatusException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.dll.api.ICommonDLL;
import tds.dll.api.IStudentDLL;
import tds.student.performance.dao.OpportunitySegmentDao;
import tds.student.performance.domain.OpportunitySegmentInsert;
import tds.student.performance.domain.OpportunitySegmentProperties;
import tds.student.performance.services.InitializeTestSegmentsService;
import tds.student.performance.utils.InitializeSegmentsHelper;

import java.util.*;


@Service
public class InitializeTestSegmentsImpl extends AbstractDLL implements InitializeTestSegmentsService {
    private static final Logger logger = LoggerFactory.getLogger(InitializeTestSegmentsImpl.class);


    @Autowired
    private AbstractDateUtilDll _dateUtil = null;

    @Autowired
    private ICommonDLL _commonDll = null;

    @Autowired
    private IStudentDLL _studentDll = null;

    @Autowired
    private OpportunitySegmentDao opportunitySegmentDao;


    public MultiDataResultSet _InitializeTestSegments_SP(SQLConnection connection, UUID oppKey, _Ref<String> error, String formKeyList, Boolean debug) throws ReturnStatusException {
        List<SingleDataResultSet> resultsets = new ArrayList<SingleDataResultSet>();

        // Step 1: Get db date used for segments and latency
        Date now = _dateUtil.getDateWRetStatus(connection);

        // Step 2: Return empty results if segments exist for this opp
        final String SQL_QUERY1 = "select  _efk_Segment from testopportunitysegment where _fk_TestOpportunity = ${oppkey} and ${debug} = 0 limit 1";
        SqlParametersMaps parms1 = new SqlParametersMaps().put("oppkey", oppKey).put("debug", debug);
        if (exists(executeStatement(connection, SQL_QUERY1, parms1, false))) {
            /* NEW comment out debug
            if (debug == true) {
                // System.err.println ("Segments already exist"); // for testing purpose
            }*/
            return (new MultiDataResultSet(resultsets));
        }
        String testKey = null;
        String testId = null;
        String segmentId = null;
        String parentKey = null;
        String clientName = null;
        String query = null;
        Boolean isSegmented = null;
        String algorithm = null;
        Integer pos = null;
        Integer opitems = null;
        Integer ftitems = null;
        String language = null;
        String formCohort = null; // for enforcing form consistency across segments
        Boolean isSatisfied = null;
        UUID session = null;
        UUID sessionPoolKey = null;
        Integer segcnt = null;
        Integer segpos = null;
        _Ref<String> formKeyRef = new _Ref<>();
        _Ref<String> formIdRef = new _Ref<>();
        _Ref<Integer> formlengthRef = new _Ref<>();
        _Ref<Integer> newlenRef = new _Ref<>();
        _Ref<Integer> poolcountRef = new _Ref<>();
        _Ref<Integer> ftcntRef = new _Ref<>();
        _Ref<String> itemStringRef = new _Ref<>();
        Boolean isSimulation = _studentDll.IsSimulation_FN(connection, oppKey);
        // create a temporary table to build segments in. WHen done, insert them en
        // masse into testopportunitysegment table with guard against duplication

        // Step 3: Create a temp table to help build segments to insert
        DataBaseTable segmentsTable = getDataBaseTable("Segments").addColumn("_fk_TestOpportunity", SQL_TYPE_To_JAVA_TYPE.UNIQUEIDENTIFIER)
                .addColumn("_efk_Segment", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 250)
                .addColumn("SegmentPosition", SQL_TYPE_To_JAVA_TYPE.INT).addColumn("formKey", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 50).addColumn("FormID", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 200)
                .addColumn("algorithm", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 50).addColumn("opItemCnt", SQL_TYPE_To_JAVA_TYPE.INT).addColumn("ftItemCnt", SQL_TYPE_To_JAVA_TYPE.INT)
                .addColumn("ftItems", SQL_TYPE_To_JAVA_TYPE.TEXT).addColumn("IsPermeable", SQL_TYPE_To_JAVA_TYPE.INT).addColumn("restorePermOn", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 50)
                .addColumn("segmentID", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 100).addColumn("entryApproved", SQL_TYPE_To_JAVA_TYPE.DATETIME).addColumn("exitApproved", SQL_TYPE_To_JAVA_TYPE.DATETIME)
                .addColumn("formCohort", SQL_TYPE_To_JAVA_TYPE.VARCHAR, 20).addColumn("IsSatisfied", SQL_TYPE_To_JAVA_TYPE.BIT).addColumn("initialAbility", SQL_TYPE_To_JAVA_TYPE.FLOAT)
                .addColumn("currentAbility", SQL_TYPE_To_JAVA_TYPE.FLOAT).addColumn("_date", SQL_TYPE_To_JAVA_TYPE.DATETIME).addColumn("dateExited", SQL_TYPE_To_JAVA_TYPE.DATETIME)
                .addColumn("itempool", SQL_TYPE_To_JAVA_TYPE.TEXT).addColumn("poolcount", SQL_TYPE_To_JAVA_TYPE.INT);

        connection.createTemporaryDiskTable(segmentsTable);
        Map<String, String> unquotedParms = new HashMap<>();
        unquotedParms.put("segmentsTableName", segmentsTable.getTableName());
        error.set(null);

        // Step 4: get the lang ( could this be passed in ? )
        language = _studentDll.GetOpportunityLanguage_FN(connection, oppKey);

        // Step 5: get details of the opp ( could this be passed in ? )
        final String SQL_QUERY2 = "select _fk_Session as session, clientname, _efk_TestID as testID, _efk_AdminSubject as testkey, isSegmented, algorithm from testopportunity where _Key = ${oppkey};";
        SqlParametersMaps parms2 = new SqlParametersMaps().put("oppkey", oppKey);
        SingleDataResultSet result = executeStatement(connection, SQL_QUERY2, parms2, false).getResultSets().next();
        DbResultRecord record = (result.getCount() > 0 ? result.getRecords().next() : null);
        if (record != null) {
            session = record.<UUID>get("session");
            clientName = record.<String>get("clientname");
            testId = record.<String>get("testID");
            testKey = record.<String>get("testkey");
            isSegmented = record.<Boolean>get("isSegmented");
            algorithm = record.<String>get("algorithm");
        }
        parentKey = testKey;

        /* Comment out debug
        if (debug == true) {
            final String SQL_QUERY3 = "select cast(${testkey} as CHAR) as testkey, cast(${language} as CHAR) as lang, cast( ${algorithm} as CHAR) as algorithm;";
            SqlParametersMaps parms3 = new SqlParametersMaps().put("testkey", testKey).put("language", language).put("isSegmented", isSegmented).put("algorithm", algorithm);
            SingleDataResultSet rs1 = executeStatement(connection, SQL_QUERY3, parms3, false).getResultSets().next();
            rs1.addColumn("segmented", SQL_TYPE_To_JAVA_TYPE.BIT);
            DbResultRecord rec = rs1.getRecords().next();
            rec.addColumnValue("segmented", isSegmented);
            resultsets.add(rs1);
        } */

        // Step 6 : populate the temp table properties from tblsetofadminsubjects
        try {
            if (DbComparator.isEqual(isSimulation, true)) {
                final String SQL_INSERT1 = "insert into ${segmentsTableName} (_fk_TestOpportunity, _efk_Segment, segmentID, SegmentPosition, algorithm, opItemCnt, IsPermeable, IsSatisfied, _date)" +
                        " select ${oppkey}, _efk_Segment, segmentID, segmentPosition, selectionalgorithm, MaxItems, ${IsPermeable}, ${IsSatisfied}, ${_date} from sim_segment SS " +
                        " where _fk_Session = ${session} and _efk_AdminSubject = ${testkey}; ";
                SqlParametersMaps parms4 = new SqlParametersMaps().put("oppkey", oppKey).put("session", session).put("testkey", testKey).put("IsPermeable", -1).put("IsSatisfied", false)
                        .put("_date", now);
                executeStatement(connection, fixDataBaseNames(SQL_INSERT1, unquotedParms), parms4, false).getUpdateCount();

                sessionPoolKey = session;
            } else if (DbComparator.isEqual(isSegmented, true)) {
                final String SQL_INSERT2 = "insert into ${segmentsTableName} (_fk_TestOpportunity, _efk_Segment, segmentID, SegmentPosition, algorithm, opItemCnt, IsPermeable, IsSatisfied, _date)"
                        + " select ${oppkey}, _Key, testID, testPosition, selectionAlgorithm, maxItems, ${IsPermeable}, ${IsSatisfied}, ${_date} from ${ItemBankDB}.tblsetofadminsubjects SS where VirtualTest = ${testkey};";
                String finalQuery = fixDataBaseNames(SQL_INSERT2);
                SqlParametersMaps parms5 = new SqlParametersMaps().put("oppkey", oppKey).put("testkey", testKey).put("IsPermeable", -1).put("IsSatisfied", false).put("_date", now);
                executeStatement(connection, fixDataBaseNames(finalQuery, unquotedParms), parms5, false).getUpdateCount();
            } else { // not segmented, so make the test its own segment
                final String SQL_INSERT3 = "insert into ${segmentsTableName} (_fk_TestOpportunity, _efk_Segment, segmentID, SegmentPosition, algorithm, opItemCnt, IsPermeable, IsSatisfied, _date) " +
                        " select ${oppkey}, ${testkey}, TestID, 1, selectionAlgorithm, maxItems, ${IsPermeable}, ${IsSatisfied}, ${_date}  from ${ItemBankDB}.tblsetofadminsubjects SS where _Key = ${testkey}; ";
                String finalQuery = fixDataBaseNames(SQL_INSERT3);
                SqlParametersMaps parms6 = new SqlParametersMaps().put("oppkey", oppKey).put("testkey", testKey).put("IsPermeable", -1).put("IsSatisfied", false).put("_date", now);
                executeStatement(connection, fixDataBaseNames(finalQuery, unquotedParms), parms6, false).getUpdateCount();
                // System.err.println (insertedCnt);
            }
            /* NEW comment out debug
            if (debug == true) {
                final String SQL_QUERY4 = "select * from ${segmentsTableName};";
                SingleDataResultSet rs2 = executeStatement(connection, fixDataBaseNames(SQL_QUERY4, unquotedParms), null, false).getResultSets().next();
                resultsets.add(rs2);
            } */

            // New get data to prep for insert.
            List<OpportunitySegmentProperties> segmentPropertiesList = opportunitySegmentDao.getOpportunitySegmentProperties(oppKey, testKey, 1);
            dumpPropertiesList(segmentPropertiesList);

            // New copy database properties to insert list
            List<OpportunitySegmentInsert> segmentInsertList = InitializeSegmentsHelper.createOpportunitySegmentInsertList(segmentPropertiesList);

            // Step 7 : Get max and min segmentPosition from temp table
            /*
            final String SQL_QUERY5 = "select max(segmentPosition) as segcnt, min(segmentPosition) as segpos from ${segmentsTableName};";
            result = executeStatement(connection, fixDataBaseNames(SQL_QUERY5, unquotedParms), null, false).getResultSets().next();
            record = (result.getCount() > 0 ? result.getRecords().next() : null);
            if (record != null) {
                segcnt = record.<Integer>get("segcnt");
                segpos = record.<Integer>get("segpos");
            }
            */
            // New Step 7 : get min and max position
            segcnt = InitializeSegmentsHelper.maximumSegmentPosition(segmentInsertList);
            segpos = InitializeSegmentsHelper.minimumSegmentPosition(segmentInsertList);

            // Step 8 : while loop on min vs max
            // initialize form selection and field test item selection on each segment
            int counter = 0;
            while (DbComparator.lessOrEqual(segpos, segcnt)) {
                counter++;
                ftcntRef.set(0);
                formKeyRef.set(null);
                formIdRef.set(null);
                formlengthRef.set(null);
                itemStringRef.set("");
                isSatisfied = false;

                /*
                // Step 8.1: If segpos exists in temp table at segmentPosition then process else continue.
                final String SQL_QUERY6 = "select  _fk_TestOpportunity from ${segmentsTableName} where segmentPosition = ${segpos} limit 1";
                SqlParametersMaps parms7 = new SqlParametersMaps().put("segpos", segpos);
                if (exists(executeStatement(connection, fixDataBaseNames(SQL_QUERY6, unquotedParms), parms7, false))) {

                    // Step 8.1.1 get fields from temp table based on oppKey and segpos
                    final String SQL_QUERY7 = "select _efk_Segment as testkey, SegmentPosition as pos, algorithm, segmentID, opItemCnt as opitems from ${segmentsTableName} where _fk_TestOpportunity = ${oppkey} and segmentPosition = ${segpos} limit 1;";
                    SqlParametersMaps parms8 = new SqlParametersMaps().put("oppkey", oppKey).put("segpos", segpos);
                    result = executeStatement(connection, fixDataBaseNames(SQL_QUERY7, unquotedParms), parms8, false).getResultSets().next();
                    record = (result.getCount() > 0 ? result.getRecords().next() : null);
                    if (record != null) {
                        testKey = record.<String>get("testkey");
                        pos = record.<Integer>get("pos");
                        algorithm = record.<String>get("algorithm");
                        segmentId = record.<String>get("segmentID");
                        opitems = record.<Integer>get("opitems");
                    }
                } else {
                    segpos += 1;
                    continue;
                } */

                // New Step 8.1
                // Step 8.1: If segpos exists in temp table at segmentPosition then process else continue.
                // final String SQL_QUERY6 = "select  _fk_TestOpportunity from ${segmentsTableName} where segmentPosition = ${segpos} limit 1";
                // Get from inserts list based on segmentPosition
                if ( InitializeSegmentsHelper.segmentPositionFiltered(segmentInsertList, segpos).size() > 0 ) {
                    // Step 8.1.1 get fields from temp table based on oppKey and segpos
                    // final String SQL_QUERY7 = "select _efk_Segment as testkey, SegmentPosition as pos, algorithm, segmentID, opItemCnt as opitems from ${segmentsTableName}
                    // where _fk_TestOpportunity = ${oppkey} and segmentPosition = ${segpos} limit 1;";
                    List<OpportunitySegmentInsert> segmentsByPostionAndKey = InitializeSegmentsHelper.segmentPositionAndOppKeyFiltered(segmentInsertList, segpos, oppKey);
                    if (segmentsByPostionAndKey != null && segmentsByPostionAndKey.size() > 0) {
                        testKey = segmentsByPostionAndKey.get(0).get_efk_Segment();  // testKey = record.<String>get("testkey");
                        pos = segmentsByPostionAndKey.get(0).getSegmentPosition();   // pos = record.<Integer>get("pos");
                        algorithm = segmentsByPostionAndKey.get(0).getAlgorithm();   // algorithm = record.<String>get("algorithm");
                        segmentId = segmentsByPostionAndKey.get(0).getSegmentId();   // segmentId = record.<String>get("segmentID");
                        opitems = segmentsByPostionAndKey.get(0).getOpItemCnt();     // opitems = record.<Integer>get("opitems");
                    }
                } else {
                    segpos += 1;
                    continue;
                }

                // Step 9 : check if fixed form
                if (DbComparator.isEqual("fixedform", algorithm)) {
                    _studentDll._SelectTestForm_Driver_SP(connection, oppKey, testKey, language, formKeyRef, formIdRef, formlengthRef, formKeyList, formCohort);
                    if (formKeyRef.get() == null) {
                        // Step 9.1 : check form key not null
                        /* Old
                        error.set("Unable to complete test form selection");
                        // don't leave garbage around if we failed to do everything
                        final String SQL_DELETE1 = "delete from ${segmentsTableName} where _fk_TestOpportunity = ${oppkey}; ";
                        SqlParametersMaps parms9 = new SqlParametersMaps().put("oppkey", oppKey);
                        executeStatement(connection, fixDataBaseNames(SQL_DELETE1, unquotedParms), parms9, false).getUpdateCount();
                        return (new MultiDataResultSet(resultsets));  */

                        // New
                        // Step 9.1 : check form key not null no need to delete from temp table
                        error.set("Unable to complete test form selection");
                        return (new MultiDataResultSet(resultsets));
                    }
                    poolcountRef.set(formlengthRef.get());
                    // Step 9.1 : get formCohort, Cache!
                    if (formCohort == null) {
                        final String SQL_QUERY8 = "select cohort as formCohort from ${ItemBankDB}.testform where _fk_AdminSubject = ${testkey} and _Key = ${formkey};";
                        String finalQuery = fixDataBaseNames(SQL_QUERY8);
                        SqlParametersMaps parms10 = new SqlParametersMaps().put("formkey", formKeyRef.get()).put("testkey", testKey);
                        result = executeStatement(connection, finalQuery, parms10, false).getResultSets().next();
                        record = (result.getCount() > 0 ? result.getRecords().next() : null);
                        if (record != null) {
                            formCohort = record.<String>get("formCohort");
                        }
                    }
                } else {
                    // Step 9.2 : Not fixed form
                    _studentDll._ComputeSegmentPool_SP(connection, oppKey, testKey, newlenRef, poolcountRef, itemStringRef, sessionPoolKey);
                    int isEligible = _studentDll.FT_IsEligible_FN(connection, oppKey, testKey, parentKey, language);
                    if (DbComparator.isEqual(isEligible, 1) && DbComparator.isEqual(newlenRef.get(), opitems)) {
                        _studentDll._FT_SelectItemgroups_SP(connection, oppKey, testKey, pos, segmentId, language, ftcntRef);
                    } else {
                        ftcntRef.set(0);
                    }
                    if (ftcntRef.get() != null && newlenRef.get() != null && DbComparator.isEqual(ftcntRef.get() + newlenRef.get(), 0)) {
                        isSatisfied = true;
                    }
                }
                // Step 10 : Update the current postion
                /*
                final String SQL_UPDATE1 = "update ${segmentsTableName} set itempool = (${itemString}), poolcount = ${poolcount}, opItemCnt = case when ${algorithm} = ${fixedform} " +
                        " then ${formLength} else ${newlen} end, formCohort = ${formCohort}, formKey = ${formkey}, formID = ${formID}, ftItemCnt = ${ftcnt}, isSatisfied = ${isSatisfied}" +
                        " where _fk_TestOpportunity = ${oppkey} and _efk_Segment = ${testkey} and SegmentPosition = ${pos}; ";
                SqlParametersMaps parms11 = new SqlParametersMaps();
                parms11.put("itemString", itemStringRef.get());
                parms11.put("poolcount", poolcountRef.get());
                parms11.put("algorithm", algorithm);
                parms11.put("fixedform", "fixedform");
                parms11.put("formLength", formlengthRef.get());
                parms11.put("newlen", newlenRef.get());
                parms11.put("formCohort", formCohort);
                parms11.put("formkey", formKeyRef.get());
                parms11.put("formID", formIdRef.get());
                parms11.put("ftcnt", ftcntRef.get());
                parms11.put("isSatisfied", isSatisfied);
                parms11.put("oppkey", oppKey);
                parms11.put("testkey", testKey);
                parms11.put("pos", pos);
                executeStatement(connection, fixDataBaseNames(SQL_UPDATE1, unquotedParms), parms11, false).getUpdateCount();
                // System.err.println (updatedCnt);
                */

                // New Step 10 : Update the current postion
                // Get the segment as a list to mirror current logic.
                List<OpportunitySegmentInsert> listToUpdate = InitializeSegmentsHelper.insertListFiltered(segmentInsertList, pos, oppKey, testKey);
                for ( OpportunitySegmentInsert updateSegment : listToUpdate ) {
                    updateSegment.setItemPool(itemStringRef.get());
                    updateSegment.setPoolCount(poolcountRef.get());
                    if ( algorithm.equals("fixedform") ) {
                        updateSegment.setOpItemCnt(formlengthRef.get());
                    } else {
                        updateSegment.setOpItemCnt(newlenRef.get());
                    }
                    updateSegment.setFormCohort(formCohort);
                    updateSegment.setFormKey(formKeyRef.get());
                    updateSegment.setFormId(formIdRef.get());
                    updateSegment.setFtItemCnt(ftcntRef.get());
                    updateSegment.setSatisfied(isSatisfied);
                }
                // End New Step 10
                segpos += 1;
            }

            /*if (debug == true) {
                final String SQL_QUERY9 = "SELECT _fk_TestOpportunity, _efk_Segment, SegmentPosition, formKey, FormID, algorithm, opItemCnt, ftItemCnt, " +
                        " ftItems, IsPermeable, restorePermOn, segmentID, entryApproved, exitApproved, formCohort, IsSatisfied, initialAbility, currentAbility, " +
                        " _date, dateExited, itempool, poolcount  FROM ${segmentsTableName};";
                SqlParametersMaps parms12 = new SqlParametersMaps();
                SingleDataResultSet rs3 = executeStatement(connection, fixDataBaseNames(SQL_QUERY9, unquotedParms), parms12, false).getResultSets().next();
                resultsets.add(rs3);
            }*/

            // Step 11 : Check items in pool
            /*
            final String SQL_QUERY10 = "select  _fk_TestOpportunity from ${segmentsTableName} where _fk_TestOpportunity = ${oppkey} and opItemCnt + ftItemCnt > 0 limit 1";
            SqlParametersMaps parms11 = parms2;
            if (!exists(executeStatement(connection, fixDataBaseNames(SQL_QUERY10, unquotedParms), parms11, false))) {
                // RAISERROR ('No items in pool', 15, 1);
                // TODO Udaya, talk to oksana about the severity of the error
                logger.error("No items in pool"); // for testing
                throw new ReturnStatusException("No items in pool for _InitializeTestSegments");
            } */
            // New Step 11
            if (InitializeSegmentsHelper.insertListFilteredItemsInPool(segmentInsertList, oppKey).size() <= 0) {
                logger.error("No items in pool");
                throw new ReturnStatusException("No items in pool for _InitializeTestSegments");
            }

            // Step 12 : insert from temp table
            /*
            if (debug == false) {

                final String SQL_INSERT4 = "INSERT INTO testopportunitysegment (_fk_TestOpportunity, _efk_Segment, SegmentPosition, formKey, FormID, algorithm, opItemCnt, "
                        + " ftItemCnt, ftItems, IsPermeable, restorePermOn, segmentID, entryApproved, exitApproved, formCohort, IsSatisfied, initialAbility, currentAbility, _date,"
                        + " dateExited, itempool, poolcount) "
                        + " SELECT _fk_TestOpportunity, _efk_Segment, SegmentPosition, formKey, FormID, algorithm, opItemCnt, ftItemCnt, ftItems,"
                        + " IsPermeable, restorePermOn, segmentID, entryApproved, exitApproved, formCohort, IsSatisfied, initialAbility, currentAbility, "
                        + " _date, dateExited, itempool, poolcount "
                        + " FROM ${segmentsTableName} where not exists (select * from testopportunitysegment where _fk_TestOpportunity = ${Oppkey});";
                SqlParametersMaps parms13 = new SqlParametersMaps().put("oppkey", oppKey);
                executeStatement(connection, fixDataBaseNames(SQL_INSERT4, unquotedParms), parms13, false).getUpdateCount();
                // System.err.println (insertedCnt);
            } */

            // New Step 12
            int[] bytes = opportunitySegmentDao.insertOpportunitySegments(segmentInsertList);
            logger.debug(" Insert {} ", bytes);

            connection.dropTemporaryTable(segmentsTable);
        } catch (ReturnStatusException re) {
            String msg = null;
            // set @msg = ERROR_PROCEDURE() + ': ' + ERROR_MESSAGE();
            // ERROR_PROCEDURE() returns name of the stored procedure TODO udaya, talk
            // to oksana/elena
            msg = String.format("_InitializeTestSegments %s", re.getMessage());
            _commonDll._LogDBError_SP(connection, "_InitializeTestSegments", msg, null, null, null, oppKey, clientName, null);
            if (debug == true) {
                error.set(msg);
            } else {
                error.set("Segment initialization failed");
            }
        }
        _commonDll._LogDBLatency_SP(connection, "_InitializeTestSegments_SP", now, null, true, null, oppKey);
        return (new MultiDataResultSet(resultsets));
    }

    // NEW Helper to see contents of temp table
    private void dumpTempTable(SQLConnection connection, String tableName) {
        List<Map<String, Object>> results = opportunitySegmentDao.dumpTable(connection, tableName);
        for ( Map<String, Object> map : results) {
            logger.debug(" {}: {} ", tableName, map.toString() );
        }
    }

    // NEW Helper to log properties collection
    private void dumpPropertiesList(Collection<OpportunitySegmentProperties> properties) {
        for (OpportunitySegmentProperties i : properties) {
            logger.debug("  Property: {} ", i.toString());
        }
    }

}