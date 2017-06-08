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