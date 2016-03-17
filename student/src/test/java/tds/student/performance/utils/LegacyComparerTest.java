/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/
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
