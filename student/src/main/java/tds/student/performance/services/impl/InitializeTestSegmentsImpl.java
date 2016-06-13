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

import AIR.Common.DB.AbstractDLL;
import AIR.Common.DB.DbComparator;
import AIR.Common.DB.SQLConnection;
import AIR.Common.DB.SqlParametersMaps;
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
import tds.dll.common.performance.utils.DateUtility;
import tds.student.performance.dao.ItemBankDao;
import tds.student.performance.dao.OpportunitySegmentDao;
import tds.student.performance.domain.OpportunitySegmentInsert;
import tds.student.performance.domain.OpportunitySegmentProperties;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.services.DbLatencyService;
import tds.student.performance.services.InitializeTestSegmentsService;
import tds.student.performance.utils.InitializeSegmentsHelper;

import java.util.*;


@Service
public class InitializeTestSegmentsImpl extends AbstractDLL implements InitializeTestSegmentsService {
    private static final Logger logger = LoggerFactory.getLogger(InitializeTestSegmentsImpl.class);

    @Autowired
    private DateUtility dateUtility;

    @Autowired
    private DbLatencyService dbLatencyService;

    @Autowired
    private ICommonDLL _commonDll = null;

    @Autowired
    private IStudentDLL _studentDll = null;

    @Autowired
    private OpportunitySegmentDao opportunitySegmentDao;

    @Autowired
    private ItemBankDao itemBankDao;


    public void initializeTestSegments(SQLConnection connection, TestOpportunity testOpportunity, _Ref<String> error, String formKeyList) throws ReturnStatusException {
        UUID oppKey = testOpportunity.getKey();
        logger.debug("== _InitializeTestSegments_SP : oppKey: {} ", oppKey);

        // Step 1: Get db date used for segments and latency
        Date dbLatencyTime = dateUtility.getLocalDate();

        // Step 2: Return empty results if segments exist for this opp
        final String SQL_QUERY1 = "select  _efk_Segment from testopportunitysegment where _fk_TestOpportunity = ${oppkey} limit 1";
        SqlParametersMaps parms1 = new SqlParametersMaps().put("oppkey", oppKey);
        if (exists(executeStatement(connection, SQL_QUERY1, parms1, false))) {
            return;
        }

        String testKey = null;
        String segmentId = null;
        String parentKey = null;
        String clientName = null;
        Boolean isSegmented = null;
        String algorithm = null;
        Integer pos = null;
        Integer opitems = null;
        String language = null;
        String formCohort = null; // for enforcing form consistency across segments
        Boolean isSatisfied = null;
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

        // New Skip new implementation of simulation
        if (DbComparator.isEqual(isSimulation, true)) {
           _studentDll._InitializeTestSegments_SP(connection, oppKey, error, formKeyList, false);
            return;
        }

        // Step 3: Create a temp table to help build segments to insert removed
        error.set(null);

        // Step 4: get the lang ( could this be passed in ? )
        language = _studentDll.GetOpportunityLanguage_FN(connection, oppKey);

        // Step 5: get details of the opp from the domain object instad of querying the DB again
        // final String SQL_QUERY2 = "select _fk_Session as session, clientname, _efk_TestID as testID, _efk_AdminSubject as testkey, isSegmented, algorithm from testopportunity where _Key = ${oppkey};";
        clientName = testOpportunity.getClientName();
        testKey = testOpportunity.getTestKey();
        isSegmented = testOpportunity.getIsSegmented();
        algorithm = testOpportunity.getAlgorithm();
        parentKey = testKey;

        // Step 6 : populate collection of properties from tblsetofadminsubjects
        List<OpportunitySegmentProperties> segmentPropertiesList = null;
        try {
            if (DbComparator.isEqual(isSegmented, true)) {
                logger.debug("==== Segmented ====");
                // New 6.2 get data to prep for insert.
                segmentPropertiesList = opportunitySegmentDao.getOpportunitySegmentPropertiesSegmented(oppKey, testKey);

            } else {
                logger.debug("==== NOT Segmented ====");
                // New 6.3 get data to prep for insert.
                segmentPropertiesList = opportunitySegmentDao.getOpportunitySegmentProperties(oppKey, testKey, 1);
            }

            logger.debug( "Dump properties {} ",  dumpPropertiesList(segmentPropertiesList) ) ;
            // New copy database properties to insert list
            List<OpportunitySegmentInsert> segmentInsertList = InitializeSegmentsHelper.createOpportunitySegmentInsertList(segmentPropertiesList);

            // New Step 7 : get min and max position
            segcnt = InitializeSegmentsHelper.maximumSegmentPosition(segmentInsertList);
            segpos = InitializeSegmentsHelper.minimumSegmentPosition(segmentInsertList);

            // New Step 8 : while loop on min vs max initialize form selection and field test item selection on each segment
            while (DbComparator.lessOrEqual(segpos, segcnt)) {
                ftcntRef.set(0);
                formKeyRef.set(null);
                formIdRef.set(null);
                formlengthRef.set(null);
                itemStringRef.set("");
                isSatisfied = false;

                // New Step 8.1
                // Get from inserts list based on segmentPosition
                if ( InitializeSegmentsHelper.segmentPositionFiltered(segmentInsertList, segpos).size() > 0 ) {
                    // Step 8.1.1 update fields based on oppKey and segpos
                    List<OpportunitySegmentInsert> segmentsByPostionAndKey = InitializeSegmentsHelper.segmentPositionAndOppKeyFiltered(segmentInsertList, segpos, oppKey);
                    if (segmentsByPostionAndKey != null && segmentsByPostionAndKey.size() > 0) {
                        testKey = segmentsByPostionAndKey.get(0).get_efk_Segment();
                        pos = segmentsByPostionAndKey.get(0).getSegmentPosition();
                        algorithm = segmentsByPostionAndKey.get(0).getAlgorithm();
                        segmentId = segmentsByPostionAndKey.get(0).getSegmentId();
                        opitems = segmentsByPostionAndKey.get(0).getOpItemCnt();
                    }
                } else {
                    segpos += 1;
                    continue;
                }

                // Step 9 : check if fixed form
                if (DbComparator.isEqual("fixedform", algorithm)) {
                    _studentDll._SelectTestForm_Driver_SP(connection, oppKey, testKey, language, formKeyRef, formIdRef, formlengthRef, formKeyList, formCohort);
                    if (formKeyRef.get() == null) {
                        // New Step 9.1 : check form key not null no need to delete from temp table
                        error.set("Unable to complete test form selection");
                        return;
                    }
                    poolcountRef.set(formlengthRef.get());

                    // Step 9.1 : get formCohort, !Cache!
                    if (formCohort == null) {
//                        final String SQL_QUERY8 = "select cohort as formCohort from ${ItemBankDB}.testform where _fk_AdminSubject = ${testkey} and _Key = ${formkey};";
                        formCohort = itemBankDao.getTestFormCohort(testKey, formKeyRef.get());
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

            // New Step 11
            if (InitializeSegmentsHelper.insertListFilteredItemsInPool(segmentInsertList, oppKey).size() <= 0) {
                logger.error("No items in pool");
                throw new ReturnStatusException("No items in pool for _InitializeTestSegments");
            }

            // New Step 12
            int[] bytes = opportunitySegmentDao.insertOpportunitySegments(segmentInsertList);
            logger.debug(" Insert Count {} ", bytes);

        } catch (ReturnStatusException re) {
            String msg = null;
            msg = String.format("_InitializeTestSegments %s", re.getMessage());
            _commonDll._LogDBError_SP(connection, "_InitializeTestSegments", msg, null, null, null, oppKey, clientName, null);
            error.set("Segment initialization failed");
        }

        dbLatencyService.logLatency("_InitializeTestSegments_SP", dbLatencyTime, null, null, oppKey, null, null, null);
    }

    // NEW Helper to see contents of temp table
    private void dumpTempTable(SQLConnection connection, String tableName) {
        List<Map<String, Object>> results = opportunitySegmentDao.dumpTable(connection, tableName);
        for ( Map<String, Object> map : results) {
            logger.debug(" {}: {} ", tableName, map.toString() );
        }
    }

    // NEW Helper to log properties collection
    private String dumpPropertiesList(Collection<OpportunitySegmentProperties> properties) {
        for (OpportunitySegmentProperties i : properties) {
            logger.debug("  Property: {} ", i.toString());
        }
        return " Dumped properties";
    }

}