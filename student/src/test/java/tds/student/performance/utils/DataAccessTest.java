package tds.student.performance.utils;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import tds.student.performance.IntegrationTest;
import tds.student.performance.domain.TestSession;

import java.util.HashMap;
import java.util.Map;

/**
 * A class for exploratory testing of various data access methods.
 */
public class DataAccessTest extends IntegrationTest {

    /**
     * The intent of this test is to determine if the {@code BeanRowPropertyMapper} sets primitive types
     * to 0 or null.  That is, will the {@code BeanRowPropertyMapper} set an {@code Integer} to 0 when the
     * value is null in the database?
     */
    @Test
    public void should_Have_a_Null_Proctor_Id_for_Guest_TestSession() {
        String sessionId = "GUEST Session";
        Map<String, String> parameters = new HashMap<>();
        parameters.put("sessionId", sessionId);
        final String SQL =
                "SELECT\n" +
                "   _efk_proctor AS proctorId,\n" +
                "   sessionid AS sessionId,\n" +
                "   proctorname AS proctorName\n" +
                "FROM\n" +
                "   session.session\n" +
                "WHERE\n" +
                "   sessionid = :sessionId";

        TestSession result = namedParameterJdbcTemplate.queryForObject(
                SQL,
                parameters,
                new BeanPropertyRowMapper<>(TestSession.class));

        Assert.assertNotNull(result);
        Assert.assertNull(result.getProctorId());
        Assert.assertNull(result.getProctorName());
        Assert.assertNotNull(result.getSessionId());
    }
}
