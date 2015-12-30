package tds.student.performance.utils;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.IntegrationTest;

public class DateUtilityTest extends IntegrationTest {
    @Autowired
    DateUtility dateUtility;

    @Test
    public void should_Return_Db_Timezone() {
        String timezone = dateUtility.getDbTimeZone();

        Assert.assertEquals("UTC", timezone);
    }
}
