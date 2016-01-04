package tds.student.performance.dao;

import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.domain.TestSegmentItem;

import java.util.List;

/**
 * Created by jjohnson on 1/2/16.
 */
public interface TestSegmentDao {
    List<TestSegmentItem> getForSimulation(TestOpportunity testOpportunity);
    List<TestSegmentItem> getSegmented(TestOpportunity testOpportunity);
    List<TestSegmentItem> get(TestOpportunity testOpportunity);
    void createTestOpportunitySegments(TestOpportunity testOpportunity, List<TestSegmentItem> items);
}
