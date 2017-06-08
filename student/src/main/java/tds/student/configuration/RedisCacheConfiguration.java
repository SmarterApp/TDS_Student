package tds.student.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;
import tds.dll.common.performance.caching.impl.JedisSentinelConnectionFactory;
import tds.dll.common.performance.caching.impl.NameAwareRedisCacheManager;
import tds.dll.common.performance.caching.impl.RedisJsonSerializer;

import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

/**
 * This configuration initializes a Redis Sentinel-backed cache manager if the "redisCaching" profile is active.
 */
@Profile("redisCaching")
@Configuration
public class RedisCacheConfiguration {

    @Bean
    public RedisConnectionFactory redisConnectionFactory(@Value("${spring.redis.sentinel.master:}") final String masterName,
                                                         @Value("${spring.redis.sentinel.nodes:}") final String sentinelCsv,
                                                         @Value("${spring.redis.host:}") final String redisHost,
                                                         @Value("${spring.redis.port:6379}") final int redisPort) {

        final Set<String> sentinels = StringUtils.commaDelimitedListToSet(sentinelCsv);
        if (isNotBlank(masterName) && !sentinels.isEmpty()) {
            return new JedisSentinelConnectionFactory(masterName, sentinels);
        }

        final JedisConnectionFactory connectionFactory = new JedisConnectionFactory();
        connectionFactory.setHostName(redisHost);
        connectionFactory.setPort(redisPort);
        return connectionFactory;
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(final RedisConnectionFactory redisConnectionFactory,
                                                       @Qualifier("integrationObjectMapper") final ObjectMapper objectMapper) {
        final RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new RedisJsonSerializer(objectMapper));
        return redisTemplate;
    }

    @Bean
    public CacheManager cacheManager(@Value("${tds.cache.shortTerm:20}") final int shortTerm,
                                          @Value("${tds.cache.mediumTerm:600}") final int mediumTerm,
                                          @Value("${tds.cache.longTerm:7200}") final int longTerm,
                                          final RedisTemplate<String, Object> redisTemplate) {
        return new NameAwareRedisCacheManager(redisTemplate, shortTerm, mediumTerm, longTerm);
    }
}
