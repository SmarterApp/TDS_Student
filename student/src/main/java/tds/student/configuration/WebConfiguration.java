package tds.student.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Configuration
public class WebConfiguration {
  @Bean(name = "integrationRestTemplate")
  public RestTemplate getRestTemplate() {
    MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
    converter.setObjectMapper(getIntegrationObjectMapper());
    RestTemplate template = new RestTemplate();
    List<HttpMessageConverter<?>> converters = new ArrayList<>();
    converters.add(converter);
    template.setMessageConverters(converters);
    return template;
  }

  @Bean(name = "integrationObjectMapper")
  public ObjectMapper getIntegrationObjectMapper() {
    return new ObjectMapper()
      .registerModule(new GuavaModule())
      .registerModule(new JodaModule());
  }
}
