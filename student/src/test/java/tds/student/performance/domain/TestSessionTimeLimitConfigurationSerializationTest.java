package tds.student.performance.domain;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.BeforeClass;
import org.junit.Test;

import java.sql.Timestamp;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

public class TestSessionTimeLimitConfigurationSerializationTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @BeforeClass
    public static void setupObjectMapper() {
        OBJECT_MAPPER.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL, JsonTypeInfo.As.PROPERTY);
    }

    @Test
    public void itShouldSerializeAndDeSerializeAnInstance() throws Exception {
        final TestSessionTimeLimitConfiguration config = new TestSessionTimeLimitConfiguration();
        config.setClientName("SBAC_PT");
        config.setOpportunityExpiration(1);
        config.setOpportunityRestartMinutes(10);
        config.setOpportunityDelayDays(-1);
        config.setInterfaceTimeoutMinutes(40);
        config.setRequestInterfaceTimeoutMinutes(200);
        config.setEnvironment("Development");
        config.setIsPracticeTest(true);
        config.setRefreshValue(30);
        config.setTaInterfaceTimeout(20);
        config.setTaCheckinTimeMinutes(20);
        config.setDateChanged(new Timestamp(new Date().getTime()));
        config.setDatePublished(new Timestamp(new Date().getTime()));
        config.setSessionExpiration(8);
        config.setRefreshValueMultiplier(2);

        final String value = OBJECT_MAPPER.writeValueAsString(config);
        final TestSessionTimeLimitConfiguration deserialized = OBJECT_MAPPER.readValue(value, TestSessionTimeLimitConfiguration.class);
        assertThat(deserialized.getOpportunityRestartMinutes()).isEqualTo(config.getOpportunityRestartMinutes());
        assertThat(deserialized.getIsPracticeTest()).isEqualTo(config.getIsPracticeTest());
    }
}