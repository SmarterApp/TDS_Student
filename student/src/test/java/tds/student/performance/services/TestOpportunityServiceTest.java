package tds.student.performance.services;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.dao.ConfigurationDao;
import tds.student.performance.dao.TestOpportunityDao;
import tds.student.performance.domain.ClientTestProperty;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.IntegrationTest;

import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class TestOpportunityServiceTest extends IntegrationTest {

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
