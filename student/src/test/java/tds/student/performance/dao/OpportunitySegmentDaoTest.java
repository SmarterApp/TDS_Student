package tds.student.performance.dao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.ItemForTesteeResponse;
import tds.student.performance.domain.OpportunitySegment;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.utils.UuidAdapter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertTrue;


public class OpportunitySegmentDaoTest extends IntegrationTest {

    @Autowired
    OpportunitySegmentDao opportunitySegmentDao;

    @Test
    public void should_Get_a_OpportunitySegment() {
        UUID key = UUID.fromString("B8876987-F3CF-44E0-A526-AE4EBE6CA8E2");

        OpportunitySegment opportunitySegment = opportunitySegmentDao.getOpportunitySegmentAccommodation(key, 1);

        Assert.assertNotNull(opportunitySegment);

    }

    @Test
    public void should_Get_list_of_OpportunitySegment() {

        String adminSubject = "(SBAC_PT)SBAC-IRP-Perf-MATH-3-Summer-2015-2016";
        String testForm = "187-764";
        String groupId = "G-187-3700-0";
        String languagePropertyValue = "ENU";

        List<ItemForTesteeResponse> itemInsertList = opportunitySegmentDao.getItemForTesteeResponse(adminSubject, testForm, groupId, languagePropertyValue);

        Assert.assertNotNull(itemInsertList);
        assertTrue(itemInsertList.size() == 4);

    }


}
