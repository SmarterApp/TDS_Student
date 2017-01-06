package tds.student.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfiguration {
    @Bean(name = "integrationRestTemplate")
    public RestTemplate getRestTemplate() {
        return new RestTemplate();
    }
}
