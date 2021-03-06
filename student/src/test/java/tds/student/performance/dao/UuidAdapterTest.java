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
package tds.student.performance.dao;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.TransactionConfiguration;
import tds.dll.common.performance.utils.UuidAdapter;

import java.util.UUID;


/**
 * Tests for {@code UuidAdapter} methods.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:performance-integration-context.xml")
@TransactionConfiguration
public class UuidAdapterTest {

    @Test
    public void should_Get_16_Bytes_From_a_UUID() {
        UUID uuid = UUID.randomUUID(); //UUID.fromString("9f881758-0b4a-4eaa-b59f-b6dea0934223");

        byte[] result = UuidAdapter.getBytesFromUUID(uuid);

        Assert.assertEquals("Expected result to be a byte array w/16 elements.", 16, result.length);
    }

    @Test
    public void should_Reconstruct_Same_UUID_From_Byte_Array() {
        UUID uuid = UUID.randomUUID();

        byte[] bytes = UuidAdapter.getBytesFromUUID(uuid);
        UUID reconstructedUuid = UuidAdapter.getUUIDFromBytes(bytes);

        Assert.assertEquals(uuid, reconstructedUuid);
    }

    @Test
    public void should_Not_Generate_the_Same_UUID_From_Bytes() {
        UUID uuid = UUID.fromString("9f881758-0b4a-4eaa-b59f-b6dea0934223");

        byte[] result = UuidAdapter.getBytesFromUUID(uuid);
        UUID newUuid = UUID.nameUUIDFromBytes(result);

        Assert.assertFalse(uuid.equals(newUuid));
    }
}
