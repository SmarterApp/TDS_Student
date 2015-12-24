package tds.student.performance.dao;

import junit.framework.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import tds.student.performance.domain.TestOpportunity;

import java.util.UUID;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("/performance-integration-context.xml")
@TransactionConfiguration
public class OpportunityDaoTest {
    @Autowired
    OpportunityDao opportunityDao;

    @Test
    public void Should_Get_a_TestOpportunity() {
        UUID key = UUID.fromString("9f881758-0b4a-4eaa-b59f-b6dea0934223");

        TestOpportunity result = opportunityDao.get(key);

        assertNotNull(result);
        Assert.assertEquals(key, result.getKey());
    }

    @Test
    public void Should_Return_Null_When_a_TestOpportunity_is_Not_Found() {
        UUID key = UUID.randomUUID();

        TestOpportunity result = opportunityDao.get(key);

        assertNull(result);
    }
}
