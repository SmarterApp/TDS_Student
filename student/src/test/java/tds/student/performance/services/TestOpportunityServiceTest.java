package tds.student.performance.services;

import AIR.Common.Helpers._Ref;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import tds.student.performance.dao.ConfigurationDao;
import tds.student.performance.dao.TestOpportunityDao;
import tds.student.performance.domain.ClientTestProperty;
import tds.student.performance.domain.TestOpportunity;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Created by jjohnson on 12/27/15.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:performance-integration-context.xml")
@TransactionConfiguration
public class TestOpportunityServiceTest {

    @Autowired
    TestOpportunityService service;

    @Autowired
    TestOpportunityDao testOpportunityDao;

    @Autowired
    ConfigurationDao configDao;

    @Test
    public void testGetInitialAbility() throws Exception {
        UUID oppKey = UUID.fromString("9f881758-0b4a-4eaa-b59f-b6dea0934223");
        TestOpportunity opportunity = testOpportunityDao.get(oppKey);
        ClientTestProperty property = configDao.getClientTestProperty(opportunity.getClientName(), opportunity.getTestId());
        Float ability = service.getInitialAbility(opportunity, property);
        assertNotNull(ability);
        assertEquals(ability, new Float(0));

    }
}
