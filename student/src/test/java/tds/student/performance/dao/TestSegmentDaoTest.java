package tds.student.performance.dao;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestSegmentItem;
import tds.student.performance.utils.UuidAdapter;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Created by jjohnson on 1/2/16.
 */
public class TestSegmentDaoTest extends IntegrationTest {
    @Autowired
    private TestSegmentDao testSegmentDao;

    @Test
    public void should_Return_TestSegmentItems_For_a_Simulation_TestOpportunity() {
        TestOpportunity mockTestOpportunity = new TestOpportunity();
        String mockVirtualTest = "UNIT TEST SIMULATION TEST";
        mockTestOpportunity.setKey(UUID.randomUUID());
        mockTestOpportunity.setSessionKey(UUID.randomUUID());
        mockTestOpportunity.setBrowserKey(UUID.randomUUID());
        mockTestOpportunity.setClientName("unit test");
        mockTestOpportunity.setTestKey(mockVirtualTest);
        mockTestOpportunity.setEnvironment("dev");
        Map<String, Object> mockRecordParameters = new HashMap<>();

        final String SESSION_SQL =
                "INSERT INTO session.session(_key, clientname, datecreated, _fk_browser, environment)" +
                "VALUES(:sessionKey, :clientName, :dateCreated, :browserKey, :environment)";
        mockRecordParameters.put("sessionKey", UuidAdapter.getBytesFromUUID(mockTestOpportunity.getSessionKey()));
        mockRecordParameters.put("clientName", mockTestOpportunity.getClientName());
        mockRecordParameters.put("dateCreated", new Timestamp(new Date().getTime()));
        mockRecordParameters.put("browserKey", UuidAdapter.getBytesFromUUID(mockTestOpportunity.getBrowserKey()));
        mockRecordParameters.put("environment", mockTestOpportunity.getEnvironment());
        namedParameterJdbcTemplate.update(SESSION_SQL, mockRecordParameters);

        final String SESSIONTEST_SQL =
                "INSERT INTO session.sessiontests(_fk_session, _efk_adminsubject, _efk_testid)\n" +
                "VALUES(:sessionKey, :testKey, :testId)";
        mockRecordParameters.put("testKey", mockTestOpportunity.getTestKey());
        mockRecordParameters.put("testId", "UNIT-TEST-SIMULATION-ID");
        namedParameterJdbcTemplate.update(SESSIONTEST_SQL, mockRecordParameters);

        final String SQL =
                "INSERT INTO session.sim_segment(_fk_session, _efk_segment, _efk_adminsubject, segmentId, segmentposition, selectionalgorithm, maxitems, blueprintweight, cset1size, cset2random, cset2initialrandom)\n" +
                "VALUES(:sessionKey, :segment, :testKey, :segmentId, :segmentPosition, :selectionAlgorithm, :maxItems, :blueprintweight, :cset1size, :cset2random, :cset2initialrandom)";
        mockRecordParameters.put("segment", "SBAC_PT-UNIT-TEST-SUBJECT");
        mockRecordParameters.put("segmentId", mockTestOpportunity.getTestKey());
        mockRecordParameters.put("segmentPosition", 1);
        mockRecordParameters.put("selectionAlgorithm", "unit test");
        mockRecordParameters.put("maxItems", 8);
        mockRecordParameters.put("blueprintweight", 5);
        mockRecordParameters.put("cset1size", 20);
        mockRecordParameters.put("cset2random", 1);
        mockRecordParameters.put("cset2initialrandom", 5);
        namedParameterJdbcTemplate.update(SQL, mockRecordParameters);

        List<TestSegmentItem> result = testSegmentDao.getForSimulation(mockTestOpportunity);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.size() == 1);
    }

    @Test
    public void should_Return_a_TestSegmentItem_For_a_Segmented_TestOpportunity() {
        TestOpportunity mockTestOpportunity = new TestOpportunity();
        String mockVirtualTest = "UNIT TEST VIRTUAL TEST";
        mockTestOpportunity.setKey(UUID.randomUUID());
        mockTestOpportunity.setTestKey(mockVirtualTest);
        Map<String, Object> mockRecordParameters = new HashMap<>();

        final String SQL =
                "INSERT INTO itembank.tblsetofadminsubjects(_key, _fk_testadmin, _fk_subject, testid, testposition, selectionalgorithm, maxitems, virtualTest)\n" +
                "VALUES(:key, :testAdmin, :subject, :testId, :testPosition, :selectionAlgorithm, :maxItems, :virtualTest)";
        mockRecordParameters.put("key", "Segmented Unit Test - FIRST SEGMENT");
        mockRecordParameters.put("testAdmin", "SBAC_PT");
        mockRecordParameters.put("subject", "SBAC_PT-UNIT-TEST-SUBJECT");
        mockRecordParameters.put("testId", "SBAC Unit Test FIRST Subject");
        mockRecordParameters.put("testPosition", 1);
        mockRecordParameters.put("selectionAlgorithm", "unit test");
        mockRecordParameters.put("maxItems", 8);
        mockRecordParameters.put("virtualTest", mockVirtualTest);
        namedParameterJdbcTemplate.update(SQL, mockRecordParameters);

        mockRecordParameters.put("key", "Segmented Unit Test - SECOND SEGMENT");
        mockRecordParameters.put("testAdmin", "SBAC_PT");
        mockRecordParameters.put("subject", "SBAC_PT-UNIT-TEST-SUBJECT");
        mockRecordParameters.put("testId", "SBAC Unit Test SECOND Subject");
        mockRecordParameters.put("testPosition", 10);
        mockRecordParameters.put("selectionAlgorithm", "unit test");
        mockRecordParameters.put("maxItems", 16);
        mockRecordParameters.put("virtualTest", mockVirtualTest);
        namedParameterJdbcTemplate.update(SQL, mockRecordParameters);

        List<TestSegmentItem> result = testSegmentDao.getSegmented(mockTestOpportunity);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.size() == 2);;
    }

    @Test
    public void should_Return_an_Empty_List_For_a_Segmented_TestOpportunity_With_a_Non_Existent_TestKey() {
        TestOpportunity mockTestOpportunity = new TestOpportunity();
        String mockVirtualTest = "UNIT TEST VIRTUAL TEST";
        mockTestOpportunity.setKey(UUID.randomUUID());
        mockTestOpportunity.setTestKey(mockVirtualTest);

        List<TestSegmentItem> result = testSegmentDao.getSegmented(mockTestOpportunity);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.size() == 0);
    }

    @Test
    public void should_Get_a_TestSegmentItem_For_a_Non_Segmented_TestOpportunity() {
        TestOpportunity mockTestOpportunity = new TestOpportunity();
        mockTestOpportunity.setKey(UUID.randomUUID());
        mockTestOpportunity.setTestKey("(SBAC_PT)SBAC Math 3-MATH-3-Spring-2014-2015");

        List<TestSegmentItem> result = testSegmentDao.get(mockTestOpportunity);

        Assert.assertNotNull(result);
        Assert.assertTrue(result.size() == 1);
        TestSegmentItem segmentItem = result.get(0);
        Assert.assertEquals(mockTestOpportunity.getKey(), segmentItem.getOpportunityKey());
        Assert.assertEquals(mockTestOpportunity.getTestKey(), segmentItem.getSegmentKey());
        Assert.assertEquals("SBAC Math 3-MATH-3", segmentItem.getSegmentId());
        Assert.assertEquals((Integer)1, segmentItem.getSegmentPosition());
        Assert.assertEquals("fixedform", segmentItem.getAlgorithm());
        Assert.assertEquals((Integer)9, segmentItem.getOpItemCount());
        Assert.assertEquals(Integer.valueOf(-1), segmentItem.getIsPermeable());
        Assert.assertEquals(false, segmentItem.getIsSatisfied());
    }

    @Test
    public void should_Return_No_Records_For_a_Non_Segmented_TestOpportunity_With_Fake_TestKey() {
        TestOpportunity mockTestOpportunity = new TestOpportunity();
        mockTestOpportunity.setKey(UUID.randomUUID());
        mockTestOpportunity.setTestKey("foo");

        List<TestSegmentItem> result = testSegmentDao.get(mockTestOpportunity);

        Assert.assertTrue(result.size() == 0);
    }
}
