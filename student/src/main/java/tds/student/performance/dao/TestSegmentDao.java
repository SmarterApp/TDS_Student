package tds.student.performance.dao;

import java.util.UUID;

/**
 * Created by jjohnson on 1/2/16.
 */
public interface TestSegmentDao {
    Integer getTestLengthForOpportunitySegment(UUID oppKey);
}
