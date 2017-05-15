package tds.student.configuration;

import org.springframework.beans.factory.FactoryBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

/**
 * This configuration enables caching with an EhCache in-memory implementation
 * if redis-caching is not available/configured.
 */
@Profile("!redisCaching")
@Configuration
public class EhCacheConfiguration {

    @Bean
    public EhCacheManagerFactoryBean cacheManagerFactoryBean() {
        final EhCacheManagerFactoryBean factoryBean = new EhCacheManagerFactoryBean();
        factoryBean.setShared(true);
        factoryBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
        return factoryBean;
    }

    @Bean
    public CacheManager cacheManager(final EhCacheManagerFactoryBean cacheManagerFactoryBean) throws Exception {
        return new EhCacheCacheManager(cacheManagerFactoryBean.getObject());
    }
}
