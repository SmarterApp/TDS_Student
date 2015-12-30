package tds.student.performance.dao;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.transaction.annotation.Transactional;
import tds.student.performance.domain.TestOpportunity;

import java.sql.Timestamp;
import java.util.UUID;

/**
 * Created by jjohnson on 12/24/15.
 */
public interface TestOpportunityDao {
    TestOpportunity get(UUID key);

    void update(TestOpportunity opportunity);

    Timestamp getLastActivity(UUID key);
}