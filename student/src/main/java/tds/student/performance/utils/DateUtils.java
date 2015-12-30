package tds.student.performance.utils;

import java.util.Date;

/**
 * Helper class for calculating date differences
 *
 * Created by emunoz on 12/30/15.
 */
public class DateUtils {
    public static Long minutesDiff (Date from, Date to) {
        if (from == null || to == null)
            return null;
        return (to.getTime () - from.getTime ()) / 1000 / 60;
    }
}
