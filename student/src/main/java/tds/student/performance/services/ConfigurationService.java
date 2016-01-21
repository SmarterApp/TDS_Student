package tds.student.performance.services;


import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestSession;

public interface ConfigurationService {
    Boolean isFlagOn(String clientName, String auditObject);
    String selectTestFormDriver(TestOpportunity testOpportunity, TestSession testSession, String formList);
}
