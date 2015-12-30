package tds.student.performance.utils;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import tds.student.performance.IntegrationTest;

import java.sql.Timestamp;
import java.util.*;

public class DateUtilityTest extends IntegrationTest {
    @Autowired
    DateUtility dateUtility;

    @Test
    public void should_Return_Db_Timezone() {
        String timezone = dateUtility.getDbTimeZone();

        Assert.assertEquals("UTC", timezone);
    }

    @Test
    public void testing() {
        TimeZone utcTimezone = TimeZone.getTimeZone("UTC");
        TimeZone pstTimezone = TimeZone.getTimeZone("PST");
        TimeZone estTimezone = TimeZone.getTimeZone("EST");

        Calendar utcCal = Calendar.getInstance(utcTimezone);
        Calendar pstCal = Calendar.getInstance(pstTimezone);
        Calendar estCal = Calendar.getInstance(estTimezone);

        printDate(utcCal);
        printDate(pstCal);
        printDate(estCal);
    }

    @Test
    public void testingDb() {
        String SQL = "INSERT INTO archive._dblatency (starttime, difftime, duration, procname, userkey, dbname) " +
                "SELECT :now, :now, 1234, 'testingDb', 1, 'session' " +
                "UNION " +
                "SELECT :utcnow, :utcnow, 1234, 'testingDb', 2, 'session' " +
                "UNION " +
                "SELECT :pstnow, :pstnow, 1234, 'testingDb', 3, 'session' " +
                "UNION " +
                "SELECT now(3), now(3), 1234, 'testingDb', 4, 'session'";

        DateTime now = new DateTime();
        DateTime utcNow = now.withZone(DateTimeZone.forID("UTC"));
        DateTime pstNow = now.withZone(DateTimeZone.forID("America/Los_Angeles"));

        System.out.println("DEFAULT TIMEZONE");
        printDateTime(now);
        System.out.println("\nUTC");
        printDateTime(utcNow);
        System.out.println("\nPST");
        printDateTime(pstNow);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("now", now.toDate());
//        parameters.put("utcnow", utcNow.toDate());
//        parameters.put("pstnow", pstNow.toDate());
        parameters.put("utcnow", jodaTimeToMysqlDateString(utcNow));
        parameters.put("pstnow", jodaTimeToMysqlDateString(pstNow));

        namedParameterJdbcTemplate.update(SQL, parameters);

        Map<String, Object> parameters2 = new HashMap<>();
        parameters2.put("procname", "testingDb");
        List<Date> dates = namedParameterJdbcTemplate.queryForList("SELECT starttime FROM archive._dblatency WHERE procname=:procname ORDER BY userkey", parameters2, Date.class);

        System.out.println("\n\nRETRIEVED VALUES FROM DB");
        for (Date date : dates) {
            System.out.println(date);
        }



    }

    private String jodaTimeToMysqlDateString(DateTime dt) {
        return String.format("%d-%02d-%02d %02d:%02d:%02d.%03d", dt.getYear(), dt.getMonthOfYear(), dt.getDayOfMonth(), dt.getHourOfDay(), dt.getMinuteOfHour(), dt.getSecondOfMinute(), dt.getMillisOfSecond());
    }

    private void printDate(Calendar c) {
        System.out.println(c.get(java.util.Calendar.HOUR_OF_DAY)+":"+c.get(java.util.Calendar.MINUTE)+":"+c.get(java.util.Calendar.SECOND));
        System.out.println(c.getTimeInMillis());
        System.out.println(c.getTime());
    }

    private void printDateTime(DateTime dt) {
        System.out.println("DateTime: " + dt);
        System.out.println("DateTime.toDate(): " + dt.toDate());
        System.out.println("Milliseconds: " + dt.getMillis());
        System.out.println("Joda time to string: " + jodaTimeToMysqlDateString(dt));
    }
}
