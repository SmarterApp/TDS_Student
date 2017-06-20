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

package tds.student.web.controls.dummy;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static tds.student.web.controls.dummy.GlobalJavascript.CONTEXT_DIAGNOSTIC_SHELL;
import static tds.student.web.controls.dummy.GlobalJavascript.CONTEXT_LOGIN_SHELL;
import static tds.student.web.controls.dummy.GlobalJavascript.CONTEXT_REVIEW_SHELL;
import static tds.student.web.controls.dummy.GlobalJavascript.CONTEXT_TEST_SHELL;

public class GlobalJavascriptTest {

    @Test
    public void itShouldIncludeAdditionalContextsBasedUponRootContext() {
        assertThat(GlobalJavascript.getContexts(CONTEXT_LOGIN_SHELL)).hasSize(13);
        assertThat(GlobalJavascript.getContexts(CONTEXT_REVIEW_SHELL)).hasSize(5);
        assertThat(GlobalJavascript.getContexts(CONTEXT_TEST_SHELL)).hasSize(20);
        assertThat(GlobalJavascript.getContexts(CONTEXT_DIAGNOSTIC_SHELL)).hasSize(6);
    }

    @Test
    public void itShouldAlwaysIncludeGlobalAndSelfContexts() {
        assertThat(GlobalJavascript.getContexts("UnknownContext"))
            .containsOnly("Global", "UnknownContext");
    }

}