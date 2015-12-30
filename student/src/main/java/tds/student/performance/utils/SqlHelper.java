package tds.student.performance.utils;


public class SqlHelper {
    public static String createNullCheckedClause(String namedParameter, String column, Object value) {
        String operator = (value == null ? "is" : "=");
        return String.format("(%s %s :%s)", column, operator, namedParameter);
    }
}
