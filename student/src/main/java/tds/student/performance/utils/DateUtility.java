package tds.student.performance.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

@Component
public class DateUtility {
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);

        try {
            String timezoneId = jdbcTemplate.queryForObject("SELECT @@system_time_zone", String.class);
            databaseTimezone = TimeZone.getTimeZone(timezoneId);
        } catch (Exception e) {
            databaseTimezone = TimeZone.getTimeZone(defaultTimezoneId);
        }
    }

    // default to UTC
    private static final String defaultTimezoneId = "UTC";
    private static TimeZone databaseTimezone = null;


    public Date getCurrentDbDate() {
        return Calendar.getInstance(databaseTimezone).getTime();
    }

    public Timestamp getCurrentDbTimestamp() {
        return new Timestamp(getCurrentDbDate().getTime());
    }

    public String getDbTimeZone() {
        return databaseTimezone.getID();
    }
}
