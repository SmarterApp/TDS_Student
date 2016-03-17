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
package tds.student.performance.caching;


import AIR.Common.collections.MultiValueDictionary;
import org.junit.Assert;
import org.junit.Test;
import tds.itemrenderer.data.AccLookup;
import tds.student.performance.domain.AccLookupWrapper;

import java.util.UUID;

public class HashCodeTest {
    @Test
    public void should_Get_Same_HashCode_For_Basic_Types() {
        Integer i1 = 1;
        Integer i2 = 1;
        String s1 = "string1";
        String s2 = "string1";

        UUID uuid1 = UUID.fromString("9f881758-0b4a-4eaa-b59f-b6dea0934223");
        UUID uuid2 = UUID.fromString("9f881758-0b4a-4eaa-b59f-b6dea0934223");

        Assert.assertEquals(i1.hashCode(), i2.hashCode());
        Assert.assertEquals(s1.hashCode(), s2.hashCode());
        Assert.assertEquals(uuid1.hashCode(), uuid2.hashCode());
    }

    @Test
    public void should_Get_Different_HashCode_For_Basic_Types() {
        Integer i1 = 1;
        Integer i2 = 2;
        String s1 = "string1";
        String s2 = "string2";

        UUID uuid1 = UUID.fromString("9f881758-0b4a-4eaa-b59f-b6dea0934223");
        UUID uuid2 = UUID.fromString("9f881758-0b4a-4eaa-b59f-b6dea0934224");

        Assert.assertNotEquals(i1.hashCode(), i2.hashCode());
        Assert.assertNotEquals(s1.hashCode(), s2.hashCode());
        Assert.assertNotEquals(uuid1.hashCode(), uuid2.hashCode());
    }

    /**
     * MultiValueDictionary is used in AccLookup and inherits from HashMap so will be using it's hashCode as part of AccLookups
     */
    @Test
    public void should_Get_Same_HashCode_For_MultiValueDictionary() {
        MultiValueDictionary<String, String> d1 = new MultiValueDictionary();
        d1.add("i1", "v1-1");
        d1.add("i1", "v1-2");
        d1.add("i2", "v2-1");

        MultiValueDictionary<String, String> d2 = new MultiValueDictionary();
        d2.add("i1", "v1-1");
        d2.add("i1", "v1-2");
        d2.add("i2", "v2-1");

        Assert.assertEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    public void should_Get_Different_HashCode_For_MultiValueDictionary() {
        MultiValueDictionary<String, String> d1 = new MultiValueDictionary();
        d1.add("i1", "v1-1");
        d1.add("i1", "v1-2");
        d1.add("i2", "v2-1");

        MultiValueDictionary<String, String> d2 = new MultiValueDictionary();
        d2.add("i1", "v1-1");
        d2.add("i2", "v2-1");

        Assert.assertNotEquals(d1.hashCode(), d2.hashCode());
    }

    @Test
    public void should_Get_Same_HashCode_For_AccLookupWrapper() {
        AccLookup l1 = new AccLookup(1);
        l1.add("i1", "v1-1", "v1-2");
        l1.add("i2", "v2-1");
        AccLookupWrapper w1 = new AccLookupWrapper(l1);

        AccLookup l2 = new AccLookup(1);
        l2.add("i1", "v1-1", "v1-2");
        l2.add("i2", "v2-1");
        AccLookupWrapper w2 = new AccLookupWrapper(l2);

        Assert.assertEquals(w1.hashCode(), w2.hashCode());
    }

    @Test
    public void should_Get_Different_HashCode_For_AccLookupWrapper() {
        AccLookup l1 = new AccLookup(1);
        l1.add("i1", "v1-1", "v1-2", "v1-3");
        l1.add("i2", "v2-1");
        AccLookupWrapper w1 = new AccLookupWrapper(l1);

        AccLookup l2 = new AccLookup(1);
        l2.add("i1", "v1-1", "v1-2");
        l2.add("i2", "v2-1");
        AccLookupWrapper w2 = new AccLookupWrapper(l2);

        Assert.assertNotEquals(w1.hashCode(), w2.hashCode());
    }
}
