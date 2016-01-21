package tds.student.performance.utils;


import java.util.Date;
import java.util.UUID;

/**
 * <p>
 * This replaces DbComparator and is a straight port with the exact same functionality
 *
 * WARNING: NULL != NULL in this implementation, so we must be extra careful not to introduce a bug by replacing calls to DbComparator to var.equals(var2)
 *
 * IMPORTANT NOTE: We were going to replace calls to DbComparator with calls to var1.equals(var2) (with var1 != null as needed)
 *      but noticed that we might introduce subtle bugs with the way that DbComparator handles NULLs
 *      For example, DbComparator.isEqual(NULL, NULL) will return FALSE
 *      and DbComparator.notEqual(NULL, UUID(.....)) will return FALSE
 * </p>
 */
public class LegacyComparer {
    public static boolean greaterThan(Integer one, Integer two) {
        return two != null && one != null?one.intValue() > two.intValue():false;
    }

    public static boolean greaterThan(Float one, Float two) {
        return two != null && one != null?one.floatValue() > two.floatValue():false;
    }

    public static boolean greaterThan(Long one, Long two) {
        return two != null && one != null?one.longValue() > two.longValue():false;
    }

    public static <T extends Number, S extends Number> boolean greaterThan(T one, S two) {
        return two != null && one != null?one.doubleValue() > two.doubleValue():false;
    }

    public static boolean greaterThan(UUID one, UUID two) {
        return two != null && one != null?one.compareTo(two) > 0:false;
    }

    public static boolean greaterThan(Date one, Date two) {
        return two != null && one != null?one.compareTo(two) > 0:false;
    }

    public static boolean lessThan(Integer one, Integer two) {
        return two != null && one != null?one.intValue() < two.intValue():false;
    }

    public static boolean lessThan(Float one, Float two) {
        return two != null && one != null?one.floatValue() < two.floatValue():false;
    }

    public static boolean lessThan(Long one, Long two) {
        return two != null && one != null?one.longValue() < two.longValue():false;
    }

    public static <T extends Number, S extends Number> boolean lessThan(T one, S two) {
        return two != null && one != null?one.doubleValue() < two.doubleValue():false;
    }

    public static boolean lessThan(UUID one, UUID two) {
        return two != null && one != null?one.compareTo(two) < 0:false;
    }

    public static boolean lessThan(Date one, Date two) {
        return two != null && one != null?one.compareTo(two) < 0:false;
    }

    public static boolean greaterOrEqual(Integer one, Integer two) {
        return two != null && one != null?one.intValue() >= two.intValue():false;
    }

    public static boolean greaterOrEqual(Float one, Float two) {
        return two != null && one != null?one.floatValue() >= two.floatValue():false;
    }

    public static boolean greaterOrEqual(Long one, Long two) {
        return two != null && one != null?one.longValue() >= two.longValue():false;
    }

    public static <T extends Number, S extends Number> boolean greaterOrEqual(T one, S two) {
        return two != null && one != null?one.doubleValue() >= two.doubleValue():false;
    }

    public static boolean greaterOrEqual(UUID one, UUID two) {
        return two != null && one != null?one.compareTo(two) >= 0:false;
    }

    public static boolean greaterOrEqual(Date one, Date two) {
        return two != null && one != null?one.compareTo(two) >= 0:false;
    }

    public static boolean lessOrEqual(Integer one, Integer two) {
        return two != null && one != null?one.intValue() <= two.intValue():false;
    }

    public static boolean lessOrEqual(Float one, Float two) {
        return two != null && one != null?one.floatValue() <= two.floatValue():false;
    }

    public static boolean lessOrEqual(Long one, Long two) {
        return two != null && one != null?one.longValue() <= two.longValue():false;
    }

    public static <T extends Number, S extends Number> boolean lessOrEqual(T one, S two) {
        return two != null && one != null?one.doubleValue() <= two.doubleValue():false;
    }

    public static boolean lessOrEqual(UUID one, UUID two) {
        return two != null && one != null?one.compareTo(two) <= 0:false;
    }

    public static boolean lessOrEqual(Date one, Date two) {
        return two != null && one != null?one.compareTo(two) <= 0:false;
    }

    public static boolean isEqual(String one, String two) {
        return two != null && one != null?one.equalsIgnoreCase(two):false;
    }

    public static boolean isEqual(Object one, Object two) {
        return two != null && one != null?one.equals(two):false;
    }

    public static <T extends Number, S extends Number> boolean isEqual(T one, S two) {
        return two != null && one != null?one.doubleValue() == two.doubleValue():false;
    }

    public static boolean notEqual(String one, String two) {
        return two != null && one != null?!one.equalsIgnoreCase(two):false;
    }

    public static boolean notEqual(Object one, Object two) {
        return two != null && one != null?!one.equals(two):false;
    }

    public static <T extends Number, S extends Number> boolean notEqual(T one, S two) {
        return two != null && one != null?one.doubleValue() != two.doubleValue():false;
    }

    public static boolean containsIgnoreCase(String one, String two) {
        if(two != null && one != null) {
            String oneIC = one.toLowerCase();
            String twoIC = two.toLowerCase();
            return oneIC.contains(twoIC);
        } else {
            return false;
        }
    }
}
