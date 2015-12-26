package tds.student.performance.dao;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import sun.tools.tree.AssignShiftLeftExpression;
import tds.student.performance.domain.TestSession;

import java.util.Date;
import java.util.UUID;


/**
 * Tests for {@code TestSessionDao} implementations.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/performance-integration-context.xml")
@TransactionConfiguration
public class TestSessionDaoTest {
    @Autowired
    TestSessionDao testSessionDao;

    /**
     * Record used for testing:
     # key, status, dateBegin, dateEnd, dateVisited, clientName, proctor, sessionBrowser
     '2B20031D4BD842A89963F6FFA44A9271', 'open', '2015-12-23 23:00:03.253', '2015-12-24 07:00:03.253', '2015-12-23 23:45:51.459', 'SBAC_PT', '93', '9DE1471D0A834A4FA3810B1D7EC24E4A'
     */
    @Test
    public void should_Get_a_TestSession_For_Specified_Key() {
        UUID key = UUID.fromString("2B20031D-4BD8-42A8-9963-F6FFA44A9271");
        UUID expectedSessionBrowserKey = UUID.fromString("9DE1471D-0A83-4A4F-A381-0B1D7EC24E4A");

        TestSession result = testSessionDao.get(key);

        Assert.assertNotNull(result);
        Assert.assertEquals(key, result.getKey());
        Assert.assertEquals((Integer)0, result.getSessionType());
        Assert.assertEquals("open", result.getStatus());
        Assert.assertEquals("SBAC_PT", result.getClientName());
        Assert.assertEquals(93d, result.getProctorId());
        Assert.assertEquals(expectedSessionBrowserKey, result.getSessionBrowser());
    }

    @Test
    public void should_Return_Null_When_a_TestSession_is_Not_Found() {
        UUID key = UUID.randomUUID();

        TestSession result = testSessionDao.get(key);

        Assert.assertNull(result);
    }

    @Test
    public void should_Be_an_Open_TestSession() {

    }

    @Test
    public void should_Not_Be_an_Open_TestSession() {
        TestSession testSession = testSessionDao.get(UUID.fromString("2B20031D-4BD8-42A8-9963-F6FFA44A9271"));

        Assert.assertTrue(!testSession.isOpen(new Date()));
    }

    @Test
    public void should_Get_a_CheckIn_Value() {
        String clientName = "SBAC_PT";

        Integer result = testSessionDao.getCheckIn(clientName);

        Assert.assertNotNull(result);
        Assert.assertEquals((Integer)20, result);
    }

    @Test
    public void should_Return_Null_When_Getting_a_CheckIn_Value_For_a_Client_Name_That_Does_Not_Exist() {
        String clientName = "FOO";

        Integer result = testSessionDao.getCheckIn(clientName);

        Assert.assertNull(result);
    }
}
