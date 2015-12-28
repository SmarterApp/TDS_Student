package tds.student.performance.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;

/**
 * Tests for {@code TestOpportunityAuditDao} implementations.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/performance-integration-context.xml")
@TransactionConfiguration
public class TestOpportunityAuditDaoTest {
    @Autowired
    TestOpportunityAuditDao testOpportunityAuditDao;

    @Test
    public void should_Create_TestOpportunityAudit_Records_For_Specified_TestOpportunity() {
        // TODO: write unit test
    }
}
