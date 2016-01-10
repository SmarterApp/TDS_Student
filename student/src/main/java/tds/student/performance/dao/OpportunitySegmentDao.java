package tds.student.performance.dao;

import tds.student.performance.domain.OpportunitySegment;

import java.util.UUID;

public interface OpportunitySegmentDao {

    OpportunitySegment getOpportunitySegmentAccommodation(UUID oppKey, Integer segment);


}
