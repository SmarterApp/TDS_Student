/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

package tds.student.performance.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSessionTimeLimitConfigurationSerializationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeClass
    public static void setupObjectMapper() {
        OBJECT_MAPPER.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    }

    @Test
    public void itShouldSerializeAndDeSerializeAnInstance() throws Exception {
        final TestSessionTimeLimitConfiguration config = new TestSessionTimeLimitConfiguration();
        config.setClientName("SBAC_PT");
        config.setOpportunityExpiration(1);
        config.setOpportunityRestartMinutes(10);
        config.setOpportunityDelayDays(-1);
        config.setInterfaceTimeoutMinutes(40);
        config.setRequestInterfaceTimeoutMinutes(200);
        config.setEnvironment("Development");
        config.setIsPracticeTest(true);
        config.setRefreshValue(30);
        config.setTaInterfaceTimeout(20);
        config.setTaCheckinTimeMinutes(20);
        config.setDateChanged(new Timestamp(new Date().getTime()));
        config.setDatePublished(new Timestamp(new Date().getTime()));
        config.setSessionExpiration(8);
        config.setRefreshValueMultiplier(2);

        final String value = OBJECT_MAPPER.writeValueAsString(config);
        final TestSessionTimeLimitConfiguration deserialized = OBJECT_MAPPER.readValue(value, TestSessionTimeLimitConfiguration.class);
        assertThat(deserialized.getOpportunityRestartMinutes()).isEqualTo(config.getOpportunityRestartMinutes());
        assertThat(deserialized.getIsPracticeTest()).isEqualTo(config.getIsPracticeTest());
    }
}