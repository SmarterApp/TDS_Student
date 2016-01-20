package tds.student.performance.utils;

import AIR.Common.DB.DbComparator;
import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class LegacyComparerTest {
    @Test
    public void legacy_Comparer_Should_Match_DbComparator_Functionality() {
        UUID uuidNull1 = null;
        UUID uuidNull2 = null;
        UUID uuid1 = UUID.fromString("2B20031D-4BD8-42A8-9963-F6FFA44A9271");
        UUID uuid2 = UUID.fromString("2B20031D-4BD8-42A8-9963-F6FFA44A9271");
        UUID uuid3 = UUID.fromString("2B20031D-4BD8-42A8-9963-F6FFA44A9273");

        Assert.assertEquals(true, DbComparator.isEqual(uuid1, uuid2));
        Assert.assertEquals(true, LegacyComparer.isEqual(uuid1, uuid2));

        Assert.assertEquals(false, DbComparator.isEqual(uuidNull1, uuid2));
        Assert.assertEquals(false, LegacyComparer.isEqual(uuidNull1, uuid2));

        // NOT INTUITIVE RESULTS with current codebase that we can't change right now
        Assert.assertEquals(false, DbComparator.isEqual(uuidNull1, uuidNull2));
        Assert.assertEquals(false, LegacyComparer.isEqual(uuidNull1, uuidNull2));

        Assert.assertEquals(false, DbComparator.notEqual(uuidNull1, uuidNull2));
        Assert.assertEquals(false, LegacyComparer.notEqual(uuidNull1, uuidNull2));
    }
}
