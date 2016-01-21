package tds.student.performance.dao;

import org.junit.Assert;;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.*;

import java.util.List;

/**
 * Created by jjohnson on 12/25/15.
 */
public class ConfigurationDaoTest extends IntegrationTest {
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
        Assert.assertEquals(false, result.getIsPrintable());
        Assert.assertEquals(true, result.getIsSelectable());
        Assert.assertEquals("Grades 3 - 5 MATH", result.getLabel());
        Assert.assertEquals(true, result.getScoreByTds());
        Assert.assertEquals(false, result.getBatchModeReport());
        Assert.assertEquals("MATH", result.getSubjectName());
        Assert.assertEquals(true, result.getMaskItemsBySubject());
        Assert.assertEquals("MATH", result.getAccommodationFamily());
        Assert.assertEquals("tds-testform", result.getRtsFormField());
        Assert.assertEquals("tds-testwindow", result.getRtsWindowField());
        Assert.assertEquals(false, result.getWindowTideSelectable());
        Assert.assertEquals(false, result.getRequireRtsWindow());
        Assert.assertEquals(true, result.getForceComplete());
        Assert.assertEquals("tds-testmode", result.getRtsModeField());
        Assert.assertEquals(false, result.getModeTideSelectable());
        Assert.assertEquals(false, result.getRequireRtsMode());
        Assert.assertEquals(false, result.getRequireRtsModeWindow());
        Assert.assertEquals(false, result.getDeleteUnansweredItems());
        Assert.assertEquals((Double)1d, result.getAbilitySlope());
        Assert.assertEquals((Double)0d, result.getAbilityIntercept());
        Assert.assertEquals(false, result.getValidateCompleteness());
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

    @Test
    public void should_Get_All_StudentLoginFields_For_SBAC_PT_Client_Name() {
        String clientName = "SBAC_PT";

        List<StudentLoginField> studentLoginFields = configurationDao.getStudentLoginFields(clientName);

        Assert.assertNotNull(studentLoginFields);
    }

    @Test
    public void should_Return_Null_When_No_Record_Exists() {
        Assert.assertNull(configurationDao.getTestToolType("none", "none", "none", "none"));
    }

    @Test
    public void should_Return_Record_For_Valid_Query() {
        ConfigTestToolType toolType = configurationDao.getTestToolType("SBAC_PT", "Other", "*", "TEST");

        Assert.assertNotNull(toolType);
        Assert.assertEquals("SBAC_PT", toolType.getClientName());
        Assert.assertEquals("Other", toolType.getToolName());
        Assert.assertEquals("*", toolType.getContext());
        Assert.assertEquals("TEST", toolType.getContextType());
        Assert.assertEquals("TDSAcc-Other", toolType.getRtsFieldName());
        Assert.assertEquals("OSS", toolType.getSource());
        Assert.assertEquals("ALL", toolType.getTestMode());
    }

    @Test
    public void should_Return_False_For_IsScoreByTDS_For_SBAC_PT_and_SBAC_Math_3_MATH_3() {
        String clientName = "SBAC_PT";
        String testId = "SBAC Math 3-MATH-3";

        Boolean result = configurationDao.isSetForScoreByTDS(clientName, testId);

        Assert.assertFalse(result);
    }

    @Test
    public void should_Return_Null_Get_Externs_Fake_Client_Name() {
        Assert.assertNull(configurationDao.getExterns("FAKE_CLIENT"));
    }

    @Test
    public void should_Return_Externs_Data_For_SBAC_PT() {
        Externs externs = configurationDao.getExterns("SBAC_PT");

        Assert.assertEquals("session", externs.getSessionDb());
        Assert.assertEquals("itembank", externs.getTestDb());
        Assert.assertEquals("RTS", externs.getTesteeType());
        Assert.assertEquals("dev", externs.getEnvironment());
    }
}
