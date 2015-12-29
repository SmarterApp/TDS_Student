package tds.student.performance.caching.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import tds.student.performance.caching.CacheKeyGenerator;
import tds.student.performance.caching.CacheType;
import tds.student.performance.caching.CachingService;

import java.util.concurrent.Callable;

/**
 * Created by jtreuting on 12/28/15.
 */
public class CachingServiceImpl implements CachingService {
    private CacheManager cacheManager;
    private Boolean enabled = true;
    private String defaultCache = CacheType.ShortTerm;

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean isEnabled() {
        return enabled && cacheManager != null;
    }

    @Autowired
    public void setCacheManager(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void setValue(String key, Object value) {
        setValue(defaultCache, key, value);
    }

    public void setValue(String cacheName, String key, Object value) {
        if (!isEnabled()) {
            return;
        }

        cacheManager.getCache(cacheName).put(key, value);
    }

    public Object getValue(String key) {
        return getValue(defaultCache, key);
    }

    public Object getValue(String cacheName, String key) {
        if (!isEnabled()) {
            return null;
        }

        Cache.ValueWrapper result = cacheManager.getCache(cacheName).get(key);

        if (result != null) {
            return result.get();
        }

        return null;
    }

    public void removeAll(String cacheName) {
        cacheManager.getCache(cacheName).clear();
    }


    public void removeAll() {
        removeAll(defaultCache);
    }

    public void remove(String cacheName, String key) {
        cacheManager.getCache(cacheName).evict(key);
    }

    public void remove(String key) {
        remove(defaultCache, key);
    }

    public <T> T cacheableCall(Callable<T> func, String key) throws Exception {
        return cacheableCall(func, defaultCache, key);
    }

    public <T> T cacheableCall(Callable<T> func, String cacheName, String className, String methodName, Object... params) throws Exception {
        return cacheableCall(func, cacheName, generateKey(className, methodName, params));
    }

    public <T> T cacheableCall(Callable<T> func, String cacheName, String key) throws Exception {
        T result = (T)getValue(cacheName, key);

        if (result == null) {
            result = func.call();

            setValue(key, result);
        }

        return result;
    }

    public String generateKey(String className, String methodName, Object... params) {
        return new CacheKeyGenerator().generate(className, methodName, params);
    }
}