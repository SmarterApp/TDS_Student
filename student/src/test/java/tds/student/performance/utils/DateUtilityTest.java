package tds.student.performance.utils;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
