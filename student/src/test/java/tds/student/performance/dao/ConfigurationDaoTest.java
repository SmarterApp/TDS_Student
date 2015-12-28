package tds.student.performance.dao;

import junit.framework.Assert;;
import org.apache.xpath.operations.Bool;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import org.springframework.test.util.AssertionErrors;
import tds.student.performance.domain.ClientSystemFlag;
import tds.student.performance.domain.ClientTestProperty;

import java.util.List;

/**
 * Created by jjohnson on 12/25/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/performance-integration-context.xml")
@TransactionConfiguration
public class ConfigurationDaoTest {
    @Autowired
    ConfigurationDao configurationDao;

    @Test
    public void should_Get_All_ClientSystemFlags_For_SBAC_PT_Client_Name() {
        String clientName = "SBAC_PT";

        List<ClientSystemFlag> systemFlags = configurationDao.getSystemFlags(clientName);

        Assert.assertNotNull(systemFlags);
        Assert.assertEquals(16, systemFlags.size());
        assertClientSystemFlags(systemFlags, clientName);
    }

    @Test
    public void should_Get_All_ClientSystemFlags_For_SBAC_Client_Name() {
        String clientName = "SBAC";

        List<ClientSystemFlag> systemFlags = configurationDao.getSystemFlags(clientName);

        Assert.assertNotNull(systemFlags);
        Assert.assertEquals(16, systemFlags.size());
        assertClientSystemFlags(systemFlags, clientName);

    }

    @Test
    public void should_Return_Null_For_a_Client_Name_That_Does_Not_Exist() {
        String clientName = "FOO";

        List<ClientSystemFlag> systemFlags = configurationDao.getSystemFlags(clientName);

        Assert.assertEquals(0, systemFlags.size());
    }

    /**
     * Used the following record for testing:
     # clientname, testid, maxopportunities, handscoreproject, prefetch, datechanged, isprintable, isselectable, label, printitemtypes, scorebytds, batchmodereport, subjectname, origin, source, maskitemsbysubject, initialabilitybysubject, startdate, enddate, ftstartdate, ftenddate, accommodationfamily, sortorder, rtsformfield, rtswindowfield, windowtideselectable, requirertswindow, reportinginstrument, tide_id, forcecomplete, rtsmodefield, modetideselectable, requirertsmode, requirertsmodewindow, deleteunanswereditems, abilityslope, abilityintercept, validatecompleteness, gradetext, initialabilitytestid, proctoreligibility, category
     'SBAC_PT', 'SBAC Math 3-MATH-3', '9999', NULL, '2', NULL, '0', '1', 'Grades 3 - 5 MATH', '', '1', '0', 'MATH', NULL, NULL, '1', '1', NULL, NULL, NULL, NULL, 'MATH', NULL, 'tds-testform', 'tds-testwindow', '0', '0', NULL, NULL, '1', 'tds-testmode', '0', '0', '0', '0', '1', '0', '0', 'Grades 3 - 5', NULL, '0', NULL
     */
    @Test
    public void should_Return_a_ClientTestProperty_For_SBAC_PT_and_SBAC_Math_3_MATH_3() {
        String clientName = "SBAC_PT";
        String testId = "SBAC Math 3-MATH-3";

        ClientTestProperty result = configurationDao.getClientTestProperty(clientName, testId);

        Assert.assertNotNull(result);
        Assert.assertEquals("SBAC_PT", result.getClientName());
        Assert.assertEquals("SBAC Math 3-MATH-3", result.getTestId());
        Assert.assertEquals((Integer)9999, result.getMaxOpportunities());
        Assert.assertEquals((Integer)2, result.getPrefetch());
        Assert.assertEquals((Boolean)false, result.getIsPrintable());
        Assert.assertEquals((Boolean)true, result.getIsSelectable());
        Assert.assertEquals("Grades 3 - 5 MATH", result.getLabel());
        Assert.assertEquals((Boolean)true, result.getScoreByTds());
        Assert.assertEquals((Boolean)false, result.getBatchModeReport());
        Assert.assertEquals("MATH", result.getSubjectName());
        Assert.assertEquals((Boolean)true, result.getMaskItemsBySubject());
        Assert.assertEquals("MATH", result.getAccommodationFamily());
        Assert.assertEquals("tds-testform", result.getRtsFormField());
        Assert.assertEquals("tds-testwindow", result.getRtsWindowField());
        Assert.assertEquals((Boolean)false, result.getWindowTideSelectable());
        Assert.assertEquals((Boolean)false, result.getRequireRtsWindow());
        Assert.assertEquals((Boolean)true, result.getForceComplete());
        Assert.assertEquals("tds-testmode", result.getRtsModeField());
        Assert.assertEquals((Boolean)false, result.getModeTideSelectable());
        Assert.assertEquals((Boolean)false, result.getRequireRtsMode());
        Assert.assertEquals((Boolean)false, result.getRequireRtsModeWindow());
        Assert.assertEquals((Boolean)false, result.getDeleteUnansweredItems());
        Assert.assertEquals(1d, result.getAbilitySlope());
        Assert.assertEquals(0d, result.getAbilityIntercept());
        Assert.assertEquals((Boolean)false, result.getValidateCompleteness());
        Assert.assertEquals("Grades 3 - 5", result.getGradeText());
        Assert.assertEquals((Integer)0, result.getProctorEligibility());
    }

    private static void assertClientSystemFlags(List<ClientSystemFlag> clientSystemFlags, String clientName) {
        for (ClientSystemFlag flag : clientSystemFlags) {
            Assert.assertNotNull(flag.getAuditObject());
            Assert.assertNotNull(flag.getClientName());
            Assert.assertEquals(clientName, flag.getClientName());
            Assert.assertNotNull(flag.getDateChanged());
            // Assert.assertNotNull(flag.getDatePublished()); // NOTE:  DatePublished is always null in the database
            Assert.assertNotNull(flag.getDescription());
            Assert.assertNotNull(flag.getIsOn());
            Assert.assertNotNull(flag.getIsPracticeTest());
        }
    }
}