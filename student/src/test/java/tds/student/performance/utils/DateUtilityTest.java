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

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tds.dll.common.performance.utils.DateUtility;
import tds.student.performance.IntegrationTest;
import java.util.*;

public class DateUtilityTest extends IntegrationTest {
    @Autowired
    DateUtility dateUtility;

    @Test
    public void should_Return_Db_Timezone() {
        Date now = dateUtility.getDbDate();

        System.out.println(now);
    }
}
