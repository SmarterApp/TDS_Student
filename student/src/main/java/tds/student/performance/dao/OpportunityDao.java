package tds.student.performance.dao;

import TDS.Shared.Exceptions.ReturnStatusException;
import tds.student.performance.domain.TestOpportunity;

import java.util.UUID;

/**
 * Created by jjohnson on 12/24/15.
 */
public interface OpportunityDao {
    TestOpportunity get(UUID key);
}
