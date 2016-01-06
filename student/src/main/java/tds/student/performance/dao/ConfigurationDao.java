package tds.student.performance.dao;

import tds.student.performance.domain.*;

import java.util.List;

/**
 * Created by jjohnson on 12/25/15.
 */
public interface ConfigurationDao {
    List<ClientSystemFlag> getSystemFlags(String clientName);
    ClientTestProperty getClientTestProperty(String clientName, String testId);
    List<StudentLoginField> getStudentLoginFields(String clientName);
    ConfigTestToolType getTestToolType(String clientName, String toolName, String contextType, String context);
    Boolean isSetForScoreByTDS(String clientName, String testId);
    ClientTestMode getClientTestMode(TestOpportunity testOpportunity);
    List<TestFormWindow> getTestFormWindows(TestOpportunity testOpportunity, TestSession testSession);
}
