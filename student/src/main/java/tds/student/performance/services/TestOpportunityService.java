package tds.student.performance.services;

import tds.student.sql.data.OpportunityInstance;

import java.util.List;

/**
 * Created by jjohnson on 12/26/15.
 */
public interface TestOpportunityService {
    void startTestOpportunity(OpportunityInstance opportunityInstance, String testKey, List<String> formKeys);

}
