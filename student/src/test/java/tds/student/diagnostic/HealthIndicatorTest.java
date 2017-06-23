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

package tds.student.diagnostic;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;
import tds.dll.common.diagnostic.domain.Rating;
import tds.dll.common.diagnostic.domain.Status;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

public class HealthIndicatorTest {
    static RestTemplate restTemplate;
    static HealthIndicatorClient client;
    static MockRestServiceServer mockServer;

    static String UP = statusBody("UP");
    static String DOWN = statusBody("DOWN");

    static String statusBody(String status) {
        return String.format("{\"status\" : \"%s\",  \"details\" : \"value\"}", status);
    }

    @BeforeClass
    public static void before() {
        restTemplate = new RestTemplate();
        client = new HealthIndicatorClient(restTemplate);
    }

    @Before
    public void setUp() {
        mockServer = MockRestServiceServer.createServer(restTemplate);
    }

    @Test
    public void runningDependencyShouldReturnIdealStatus() {
        mockServer.expect(anything()).andRespond(
                withSuccess(UP, MediaType.APPLICATION_JSON));
        Status status = client.getStatus("Proctor", "localhost");
        assertThat(status.getRating(), is(Rating.IDEAL));
    }

    @Test
    public void downDependencyShouldReturnFailedStatus() {
        mockServer.expect(anything()).andRespond(
                withSuccess(DOWN, MediaType.APPLICATION_JSON));
        Status status = client.getStatus("unit", "host");
        assertThat(status.getRating(), is(Rating.FAILED));
    }

}
