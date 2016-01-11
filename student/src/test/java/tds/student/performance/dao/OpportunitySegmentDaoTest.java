package tds.student.performance.dao;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Splitter;
import com.google.common.collect.*;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.ItemForTesteeResponse;
import tds.student.performance.domain.OpportunitySegment;
import tds.student.performance.domain.TestOpportunity;
import tds.student.performance.utils.UuidAdapter;

import java.util.*;

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

        String itemKeys = "187-2788|187-1576|187-2789|187-1578";
        String itemKeys2 = "187-2788|187-1578";

        Character delimiter = '|';

        List<ItemForTesteeResponse> l1 = createInsertsList(itemInsertList, itemKeys, delimiter);
        List<ItemForTesteeResponse> l2 = createInsertsList(itemInsertList, itemKeys2, delimiter);

        assertTrue(l1.size() == 4);
        assertTrue(l2.size() == 2);

        List<ItemForTesteeResponse> t1 = createInsertsListTransform(itemInsertList, itemKeys, delimiter);
        List<ItemForTesteeResponse> t2 = createInsertsListTransform(itemInsertList, itemKeys2, delimiter);

        assertTrue(t1.size() == 4);
        assertTrue(t2.size() == 2);

    }

    // Create a copy as list comes from query that could be cached.
    // List is used as a replacement for a temporary table.
    private List<ItemForTesteeResponse> createInsertsList(List<ItemForTesteeResponse> itemInsertList, String itemKeys, Character delimiter) {

        final List<String> itemKeysIterable = Splitter.on(delimiter).omitEmptyStrings().trimResults().splitToList(itemKeys);

        List<ItemForTesteeResponse> filtered = new ArrayList<>();

        for (ItemForTesteeResponse item : itemInsertList) {
            if (itemKeysIterable.contains(item.getBankItemKey())) {
                filtered.add(new ItemForTesteeResponse(item));
            }
        }
        return filtered;
    }

    // Transform
    private List<ItemForTesteeResponse> createInsertsListTransform(List<ItemForTesteeResponse> itemInsertList, String itemKeys, Character delimiter) {

        final List<String> itemKeysIterable = Splitter.on(delimiter).omitEmptyStrings().trimResults().splitToList(itemKeys);

        Predicate<ItemForTesteeResponse> predicate = new Predicate<ItemForTesteeResponse>() {
            @Override
            public boolean apply(ItemForTesteeResponse input) {
                return input != null && itemKeysIterable.contains(input.getBankItemKey());
            }
        };

        Function<ItemForTesteeResponse, ItemForTesteeResponse> transform = new Function<ItemForTesteeResponse,ItemForTesteeResponse>(){
            @Override
            public ItemForTesteeResponse apply(ItemForTesteeResponse input) {
                return new ItemForTesteeResponse(input);
            }
        };

        return FluentIterable
                .from(itemInsertList)
                .filter(predicate)
                .transform(transform)
                .toList();

    }

}
