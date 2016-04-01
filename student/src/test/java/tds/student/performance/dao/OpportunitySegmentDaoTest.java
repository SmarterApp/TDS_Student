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
package tds.student.performance.dao;

import com.google.common.base.Splitter;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tds.dll.common.performance.utils.LegacyDbNameUtility;
import tds.dll.common.performance.utils.UuidAdapter;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.*;
import tds.student.performance.utils.InitializeSegmentsHelper;
import tds.student.performance.utils.TesteeResponseHelper;

import javax.sql.DataSource;
import javax.validation.constraints.AssertTrue;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

import static org.junit.Assert.*;

public class OpportunitySegmentDaoTest extends IntegrationTest {

    private static final Logger logger = LoggerFactory.getLogger(OpportunitySegmentDaoTest.class);

    private static final String adminSubject = "(SBAC_PT)SBAC-IRP-Perf-MATH-3-Summer-2015-2016";
    private static final String testForm = "187-764";
    private static final String groupId = "G-187-3700-0";
    private static final String languagePropertyValue = "ENU";
    private static final String itemKeys = "187-2788|187-1576|187-2789|187-1578";
    private static final Character delimiter = '|';
    private static final UUID opportunityKey = UUID.fromString("25D23379-DD98-4A64-A663-6A11FFE24EAE");

    @Autowired
    OpportunitySegmentDao opportunitySegmentDao;

    @Autowired
    DataSource dataSourceConnection;

    @Autowired
    LegacyDbNameUtility dbNameUtility;

    @Test
    public void should_Get_a_OpportunitySegment() {
        OpportunitySegment opportunitySegment = opportunitySegmentDao.getOpportunitySegmentAccommodation(opportunityKey, 1);
        Assert.assertNotNull(opportunitySegment);
    }

    @Test
    public void should_Get_list_of_OpportunitySegment() {
        List<ItemForTesteeResponse> itemInsertList = opportunitySegmentDao.getItemForTesteeResponse(adminSubject, testForm, groupId, languagePropertyValue);

        Assert.assertNotNull(itemInsertList);
        assertTrue(itemInsertList.size() == 4);

        String itemKeys2 = "187-2788|187-1578";

        List<InsertTesteeResponse> t1 = TesteeResponseHelper.createInsertsList(itemInsertList, itemKeys, delimiter);
        List<InsertTesteeResponse> t2 = TesteeResponseHelper.createInsertsList(itemInsertList, itemKeys2, delimiter);

        assertTrue(t1.size() == 4);
        assertTrue(t2.size() == 2);
    }

    @Test
    public void should_Get_Items_no_null_pos() {
        List<ItemForTesteeResponse> itemList = opportunitySegmentDao.getItemForTesteeResponse(adminSubject, testForm, groupId, languagePropertyValue);
        assertTrue(itemList.size() == 4);

        List<InsertTesteeResponse> t1 = TesteeResponseHelper.createInsertsList(itemList, itemKeys, delimiter);
        assertTrue(t1.size() == 4);

        // Any null form position?
        assertTrue(TesteeResponseHelper.nullFormPositionList(t1).size() == 0);

        // Change it
        t1.get(1).setFormPosition(null);
        t1.get(3).setFormPosition(null);

        // Check it
        assertTrue(TesteeResponseHelper.nullFormPositionList(t1).size() == 2);
    }

    @Test
    public void check_contains_field_test() {
        List<ItemForTesteeResponse> itemList = opportunitySegmentDao.getItemForTesteeResponse(adminSubject, testForm, groupId, languagePropertyValue);
        assertTrue(itemList.size() == 4);

        List<InsertTesteeResponse> t1 = TesteeResponseHelper.createInsertsList(itemList, itemKeys, delimiter);
        assertTrue(t1.size() == 4);

        // Any null form position?
        assertTrue(TesteeResponseHelper.isFieldTestList(t1).size() == 0);

        // Change it
        t1.get(0).setIsFieldTest(true);
        t1.get(1).setIsFieldTest(true);
        t1.get(3).setIsFieldTest(null);

        // Check it
        assertTrue(TesteeResponseHelper.isFieldTestList(t1).size() == 2);
    }

    @Test
    public void check_minimum_position() {
        List<ItemForTesteeResponse> itemList = opportunitySegmentDao.getItemForTesteeResponse(adminSubject, testForm, groupId, languagePropertyValue);
        assertTrue(itemList.size() == 4);

        List<InsertTesteeResponse> t1 = TesteeResponseHelper.createInsertsList(itemList, itemKeys, delimiter);
        assertTrue(t1.size() == 4);

        List<InsertTesteeResponse> s1 = TesteeResponseHelper.incrementItemPositionByLast(t1, 0);
        assertEquals(TesteeResponseHelper.minimumPosition(s1), new Integer(1));

        // Change it
        s1.get(3).setPosition(0);
        s1.get(1).setPosition(null);
        assertEquals(TesteeResponseHelper.minimumPosition(s1), new Integer(0));

        // All Null
        s1.get(0).setPosition(null);
        s1.get(1).setPosition(null);
        s1.get(2).setPosition(null);
        s1.get(3).setPosition(null);
        assertNull(TesteeResponseHelper.minimumPosition(s1));

        // Empty list
        assertNull(TesteeResponseHelper.minimumPosition(new ArrayList<InsertTesteeResponse>()));
    }

    @Test
    public void should_exists_TesteeResponses_ByBankKey_And_Opportunity() {
        final List<String> itemKeyList1 = Splitter.on(delimiter).omitEmptyStrings().trimResults()
                .splitToList(itemKeys);

        assertTrue(opportunitySegmentDao.existsTesteeResponsesByBankKeyAndOpportunity(opportunityKey, itemKeyList1));

        final List<String> itemKeyList2 = Splitter.on(delimiter).omitEmptyStrings().trimResults()
                .splitToList("187-1578");

        assertTrue(opportunitySegmentDao.existsTesteeResponsesByBankKeyAndOpportunity(opportunityKey, itemKeyList2));
    }

    @Test
    public void should_not_exists_TesteeResponses_ByBankKey_And_Opportunity() {
        final List<String> itemKeyList1 = Splitter.on(delimiter).omitEmptyStrings().trimResults()
                .splitToList("9187-2788|9187-1576|9187-2789|9187-1578");

        assertFalse(opportunitySegmentDao.existsTesteeResponsesByBankKeyAndOpportunity(opportunityKey, itemKeyList1));
    }

    @Test
    public void get_min_form_position_and_update_position() {
        List<ItemForTesteeResponse> itemList = opportunitySegmentDao.getItemForTesteeResponse(adminSubject, testForm, groupId, languagePropertyValue);
        assertTrue(itemList.size() == 4);

        List<InsertTesteeResponse> t1 = TesteeResponseHelper.createInsertsList(itemList, itemKeys, delimiter);
        assertTrue(t1.size() == 4);

        TesteeResponseHelper.updateItemPosition(t1);

        assertEquals(t1.get(0).getItemPosition().intValue(), 0);
        assertEquals(t1.get(1).getItemPosition().intValue(), 1);
        assertEquals(t1.get(2).getItemPosition().intValue(), 2);
        assertEquals(t1.get(3).getItemPosition().intValue(), 3);

        List<InsertTesteeResponse> t2 = TesteeResponseHelper.createInsertsList(itemList, itemKeys, delimiter);
        assertTrue(t2.size() == 4);

        // Modify it to 6,2,3,1
        t2.get(0).setFormPosition(6);
        t2.get(0).setItemPosition(10);
        t2.get(3).setFormPosition(1);

        TesteeResponseHelper.updateItemPosition(t2);

        assertEquals(t2.get(0).getItemPosition().intValue(), 9);
        assertEquals(t2.get(1).getItemPosition().intValue(), 1);
        assertEquals(t2.get(2).getItemPosition().intValue(), 2);
        assertEquals(t2.get(3).getItemPosition().intValue(), 3);
    }

    @Test
    public void check_ambiguousItemPosition() {
        List<ItemForTesteeResponse> itemList = opportunitySegmentDao.getItemForTesteeResponse(adminSubject, testForm, groupId, languagePropertyValue);
        assertTrue(itemList.size() == 4);

        List<InsertTesteeResponse> t1 = TesteeResponseHelper.createInsertsList(itemList, itemKeys, delimiter);
        assertTrue(t1.size() == 4);

        TesteeResponseHelper.updateItemPosition(t1);
        assertFalse(TesteeResponseHelper.ambiguousItemPosition(t1));

        t1.get(0).setItemPosition(3);
        assertTrue(TesteeResponseHelper.ambiguousItemPosition(t1));

        t1.get(0).setItemPosition(0);
        assertFalse(TesteeResponseHelper.ambiguousItemPosition(t1));

        t1.get(2).setItemPosition(null);
        assertTrue(TesteeResponseHelper.ambiguousItemPosition(t1));
    }

    @Test
    public void check_increment_item_position() {
        List<ItemForTesteeResponse> itemList = opportunitySegmentDao.getItemForTesteeResponse(adminSubject, testForm, groupId, languagePropertyValue);
        assertTrue(itemList.size() == 4);

        List<InsertTesteeResponse> t1 = TesteeResponseHelper.createInsertsList(itemList, itemKeys, delimiter);
        assertTrue(t1.size() == 4);
        TesteeResponseHelper.updateItemPosition(t1);
        List<InsertTesteeResponse> s1 = TesteeResponseHelper.incrementItemPositionByLast(t1, 0);
        dumpInsertList(s1);
        assertEquals(s1.get(0).getPosition().intValue(), 1);
        assertEquals(s1.get(3).getPosition().intValue(), 4);

        List<InsertTesteeResponse> t2 = TesteeResponseHelper.createInsertsList(itemList, itemKeys, delimiter);
        assertTrue(t1.size() == 4);
        TesteeResponseHelper.updateItemPosition(t2);
        List<InsertTesteeResponse> s2 = TesteeResponseHelper.incrementItemPositionByLast(t1, 7);
        dumpInsertList(s2);
        assertEquals(s2.get(0).getPosition().intValue(), 8);
        assertEquals(s2.get(3).getPosition().intValue(), 11);
    }

    @Test
    public void check_create_inserts_temp_table() throws SQLException {
        List<ItemForTesteeResponse> itemList = opportunitySegmentDao.getItemForTesteeResponse(adminSubject, testForm, groupId, languagePropertyValue);
        assertTrue(itemList.size() == 4);

        List<InsertTesteeResponse> t1 = TesteeResponseHelper.createInsertsList(itemList, itemKeys, delimiter);
        assertTrue(t1.size() == 4);
        TesteeResponseHelper.incrementItemPositionByLast(t1, 0);

        Connection connection = dataSourceConnection.getConnection();

        String insertTable = opportunitySegmentDao.loadInsertTableForTesteeResponses(connection, t1);

        assertNotNull(insertTable);
        logger.debug("Temp Table Name {} ", insertTable);

        opportunitySegmentDao.dropTempTable(connection, insertTable);
    }

    @Test
    public void check_get_opp_seg_not_segmented() throws SQLException {

        UUID oppKey = UUID.fromString("13BC24BF-08CE-49E9-AE8D-2F495BECCEED");
        String segment = "";
        List<OpportunitySegmentProperties> propertiesList = opportunitySegmentDao.getOpportunitySegmentProperties(oppKey, segment, 1);
        Assert.assertNotNull(propertiesList);
    }

    @Test
    public void check_get_opp_seg_insert_list() throws SQLException {

        UUID oppKey = UUID.fromString("13BC24BF-08CE-49E9-AE8D-2F495BECCEED");
        String segment = "";
        List<OpportunitySegmentProperties> propertiesList = opportunitySegmentDao.getOpportunitySegmentProperties(oppKey, segment, 1);
        Assert.assertNotNull(propertiesList);
        List<OpportunitySegmentInsert> insertList = InitializeSegmentsHelper.createOpportunitySegmentInsertList(propertiesList);
        Assert.assertNotNull(insertList);
    }

    @Test
    public void check_opp_seg_min_max_segment_position() throws SQLException {

        OpportunitySegmentInsert seg1 = new OpportunitySegmentInsert();
        seg1.setSegmentPosition(5);
        OpportunitySegmentInsert seg2 = new OpportunitySegmentInsert();
        seg2.setSegmentPosition(3);
        OpportunitySegmentInsert seg3 = new OpportunitySegmentInsert();
        seg3.setSegmentPosition(10);


        List<OpportunitySegmentInsert> insertList = new ArrayList<>();
        insertList.add(seg1);
        insertList.add(seg2);
        insertList.add(seg3);

        Integer min = InitializeSegmentsHelper.minimumSegmentPosition(insertList);
        Integer max = InitializeSegmentsHelper.maximumSegmentPosition(insertList);

        assertTrue(min == 3);
        assertTrue(max == 10);
    }

    @Test
    public void check_get_opp_seg_by_segmentPosition() throws SQLException {

        OpportunitySegmentInsert seg1 = new OpportunitySegmentInsert();
        seg1.setSegmentPosition(5);
        OpportunitySegmentInsert seg2 = new OpportunitySegmentInsert();
        seg2.setSegmentPosition(3);
        OpportunitySegmentInsert seg3 = new OpportunitySegmentInsert();
        seg3.setSegmentPosition(10);
        OpportunitySegmentInsert seg4 = new OpportunitySegmentInsert();

        List<OpportunitySegmentInsert> insertList = new ArrayList<>();
        insertList.add(seg1);
        insertList.add(seg2);
        insertList.add(seg3);
        insertList.add(seg4);

        List<OpportunitySegmentInsert> resultList = InitializeSegmentsHelper.segmentPositionFiltered(insertList, 3);
        assertTrue(resultList.size() == 1);
        assertTrue(resultList.get(0).getSegmentPosition().equals(3));
    }

    @Test
    public void check_get_opp_seg_by_segmentPosition_and_oppKey() throws SQLException {

        UUID oppKey = UUID.fromString("13BC24BF-08CE-49E9-AE8D-2F495BECCEED");

        OpportunitySegmentInsert seg1 = new OpportunitySegmentInsert();
        seg1.setSegmentPosition(5);
        seg1.set_fk_TestOpportunity(oppKey);
        OpportunitySegmentInsert seg2 = new OpportunitySegmentInsert();
        seg2.set_fk_TestOpportunity(oppKey);
        seg2.setSegmentPosition(3);
        OpportunitySegmentInsert seg3 = new OpportunitySegmentInsert();
        seg2.set_fk_TestOpportunity(oppKey);
        seg3.setSegmentPosition(10);

        OpportunitySegmentInsert seg4 = new OpportunitySegmentInsert();

        List<OpportunitySegmentInsert> insertList = new ArrayList<>();
        insertList.add(seg1);
        insertList.add(seg2);
        insertList.add(seg3);
        insertList.add(seg4);

        List<OpportunitySegmentInsert> resultList = InitializeSegmentsHelper.segmentPositionAndOppKeyFiltered(
                insertList, 5, oppKey);
        assertTrue(resultList.size() == 1);
        assertTrue(resultList.get(0).getSegmentPosition().equals(5));
    }

    @Test
    public void check_filter_opp_seg_by_segmentPosition_oppKey_efk_segment() throws SQLException {

        UUID oppKey = UUID.fromString("13BC24BF-08CE-49E9-AE8D-2F495BECCEED");
        String _efk_Segment = "(SBAC_PT)SBAC-IRP-Perf-MATH-3-Summer-2015-2016";

        OpportunitySegmentInsert seg1 = new OpportunitySegmentInsert();
        seg1.setSegmentPosition(5);
        seg1.set_fk_TestOpportunity(oppKey);
        seg1.set_efk_Segment(_efk_Segment);
        OpportunitySegmentInsert seg2 = new OpportunitySegmentInsert();
        seg2.set_fk_TestOpportunity(oppKey);
        seg2.setSegmentPosition(3);
        seg2.set_efk_Segment(_efk_Segment);
        OpportunitySegmentInsert seg3 = new OpportunitySegmentInsert();
        seg3.set_fk_TestOpportunity(oppKey);
        seg3.setSegmentPosition(10);
        seg3.set_efk_Segment(_efk_Segment);

        OpportunitySegmentInsert seg4 = new OpportunitySegmentInsert();

        List<OpportunitySegmentInsert> insertList = new ArrayList<>();
        insertList.add(seg1);
        insertList.add(seg2);
        insertList.add(seg3);
        insertList.add(seg4);

        List<OpportunitySegmentInsert> resultList = InitializeSegmentsHelper.insertListFiltered(
                insertList, 10, oppKey, _efk_Segment);
        assertTrue(resultList.size() == 1);
        assertTrue(resultList.get(0).getSegmentPosition().equals(10));
        assertTrue(resultList.get(0).get_efk_Segment().equals(_efk_Segment));
    }

    @Test
    public void check_insert_List_Filtered_Items_In_Pool() throws SQLException {

        UUID oppKey = UUID.fromString("13BC24BF-08CE-49E9-AE8D-2F495BECCEED");
        String _efk_Segment = "(SBAC_PT)SBAC-IRP-Perf-MATH-3-Summer-2015-2016";

        OpportunitySegmentInsert seg1 = new OpportunitySegmentInsert();
        seg1.set_fk_TestOpportunity(oppKey);
        seg1.setOpItemCnt(1);

        OpportunitySegmentInsert seg2 = new OpportunitySegmentInsert();
        seg2.set_fk_TestOpportunity(oppKey);
        seg2.setFtItemCnt(2);

        OpportunitySegmentInsert seg3 = new OpportunitySegmentInsert();
        seg3.set_fk_TestOpportunity(oppKey);

        OpportunitySegmentInsert seg4 = new OpportunitySegmentInsert();

        List<OpportunitySegmentInsert> insertList = new ArrayList<>();
        insertList.add(seg1);
        insertList.add(seg2);
        insertList.add(seg3);
        insertList.add(seg4);

        List<OpportunitySegmentInsert> resultList = InitializeSegmentsHelper.insertListFilteredItemsInPool(
                insertList, oppKey);

        assertTrue(resultList.size() == 2);
        assertFalse( InitializeSegmentsHelper.insertListFilteredItemsInPool( insertList, oppKey).size() <= 0  );
    }

    @Test
    public void check_insert_opp_seg() throws SQLException {

        UUID oppKey = UUID.fromString("6D386CCB-18DC-42D1-A0A9-C4F5E1451C19");

        String segment = "(SBAC_PT)SBAC-IRP-Perf-MATH-3-Summer-2015-2016";

       List<OpportunitySegmentProperties> propertiesList = opportunitySegmentDao.getOpportunitySegmentProperties(oppKey, segment, 1);
       List<OpportunitySegmentInsert> insertList = InitializeSegmentsHelper.createOpportunitySegmentInsertList(propertiesList);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("oppKey", UuidAdapter.getBytesFromUUID(oppKey));
        final String SQL = "SELECT * FROM ${sessiondb}.testopportunitysegment WHERE _fk_testopportunity = :oppKey";
        final String delete = "delete FROM ${sessiondb}.testopportunitysegment WHERE _fk_testopportunity = :oppKey";

        namedParameterJdbcTemplate.update(dbNameUtility.setDatabaseNames(delete), parameters);

        int[] bytes = opportunitySegmentDao.insertOpportunitySegments(insertList);

        List<Map<String, Object>> mapList = namedParameterJdbcTemplate.queryForList(dbNameUtility.setDatabaseNames(SQL), parameters);

        assertTrue(mapList.size() > 0);

    }


    // Debug Helper
    private void dumpInsertList(Collection<InsertTesteeResponse> items) {
        for (InsertTesteeResponse i : items) {
            System.out.println(i.toString());
        }
    }


}
