/***************************************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2017 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 **************************************************************************************************/

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

import tds.dll.common.performance.caching.impl.NameAwareRedisCacheManager;
import tds.dll.common.performance.caching.impl.RedisJsonSerializer;

/**
 * This configuration initializes a Redis Sentinel-backed cache manager if the "redisCaching" profile is active.
 */
@Profile("redisCaching")
@Configuration
public class RedisCacheConfiguration {

    @Bean
    public RedisConnectionFactory redisConnectionFactory(@Value("${spring.redis.host:}") final String redisHost,
                                                         @Value("${spring.redis.port:6379}") final int redisPort) {
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
