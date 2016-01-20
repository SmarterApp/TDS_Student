package tds.student.performance.dao;

import com.google.common.base.Splitter;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.InsertTesteeResponse;
import tds.student.performance.domain.ItemForTesteeResponse;
import tds.student.performance.domain.OpportunitySegment;
import tds.student.performance.utils.TesteeResponseHelper;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

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

    // Debug Helper
    private void dumpInsertList(Collection<InsertTesteeResponse> items) {
        for (InsertTesteeResponse i : items) {
            System.out.println(i.toString());
        }
    }


}
