package tds.student.performance.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.util.Date;

@Component
public class DateUtility {
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public Date getDbDate() {
        return jdbcTemplate.queryForObject("SELECT now(3)", Date.class);
    }

    public Date getLocalDate() {
        return new Date();
    }

    public Timestamp getTimestamp() {
        return new Timestamp(getLocalDate().getTime());
    }

    public static Long minutesDiff (Date from, Date to) {
        if (from == null || to == null)
            return null;
        return (to.getTime () - from.getTime ()) / 1000 / 60;
    }
}
