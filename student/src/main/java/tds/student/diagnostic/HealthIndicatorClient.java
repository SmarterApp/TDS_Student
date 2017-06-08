package tds.student.diagnostic;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Date;

import tds.dll.common.diagnostic.domain.Level;
import tds.dll.common.diagnostic.domain.Rating;
import tds.dll.common.diagnostic.domain.Status;

import static java.lang.String.format;

/**
 * Rest client to Spring Boot’s Actuator Project health check endpoint.
 *
 * @see <a href="http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready">Spring Boot’s Actuator Project</a>
 */
public class HealthIndicatorClient {
    private final RestTemplate restTemplate;

    @Autowired
    public HealthIndicatorClient(final RestTemplate restTemplate) {
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            protected boolean hasError(HttpStatus statusCode) {
                // spring boot health endpoint returns a 503 http status code when health checks fail
                return super.hasError(statusCode) && !statusCode.equals(HttpStatus.SERVICE_UNAVAILABLE);
            }
        });
        this.restTemplate = restTemplate;
    }

    /**
     * Health status of a dependency that exposes the "health" rest API from Spring Boot’s Actuator Project.
     *
     * @see <a href="http://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#production-ready">Spring Boot’s Actuator Project</a>
     *
     * @param url Base URL of service
     * @return Service's health
     */
    protected Health health(final String url)  {
        // parse url for existing url components
        final UriComponents components = UriComponentsBuilder.fromUriString(url).build();
        final UriComponentsBuilder builder;
        if (components.getHost() != null) {
            builder = UriComponentsBuilder.fromUriString(url);
        } else {
            // add default http protocol if missing
            builder = UriComponentsBuilder.fromHttpUrl(String.format("http://%s", url));
        }
        builder.replacePath("health");

        final String restUrl = builder.build().toUriString();
        return restTemplate.getForObject(restUrl, Health.class);
    }

    /**
     * Returns the status of a dependency that exposes the "status" Rest API
     *
     * @return dependency status
     */
    public Status getStatus(final String unit, final String host) {
        final Status status = new Status(unit, Level.LEVEL_0, new Date());
        try {
            final Health health = health(host);
            switch (health.getStatus()) {
                case "UP":
                    // ok response from dependency health check
                    break;
                default:
                    setStatusError(status, getDependencyErrorMessage(unit, host, health.getStatus()));
                    break;
            }
        } catch (Exception e) {
            setStatusError(status, getDependencyErrorMessage(unit, host, e.getLocalizedMessage()));
        }

        return status;
    }

    // sets error status parameters
    public static void setStatusError(final Status status, final String message) {
        status.setRating(Rating.FAILED);
        status.setError(message);
        status.setWarning(null);
    }

    // returns a message for a dependency error
    public static String getDependencyErrorMessage(final String unit, final String host, final String reason) {
        return format("Dependency error with %s. Check server %s - Reason: %s", unit, host, reason);
    }
}