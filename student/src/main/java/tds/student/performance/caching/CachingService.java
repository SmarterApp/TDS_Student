package tds.student.performance.caching;

import java.util.concurrent.Callable;

/**
 * Created by jtreuting on 12/28/15.
 */
public interface CachingService {
    void setEnabled(Boolean enabled);
    Boolean isEnabled();

    void setValue(String cacheName, String key, Object value);
    void setValue(String key, Object value);

    Object getValue(String cacheName, String key);
    Object getValue(String key);

    void removeAll(String cacheName);
    void removeAll();

    void remove(String cacheName, String key);
    void remove(String key);

    <T> T cacheableCall(Callable<T> func, String cacheName, String className, String methodName, Object... params) throws Exception;
    <T> T cacheableCall(Callable<T> func, String key) throws Exception;
    <T> T cacheableCall(Callable<T> func, String cacheName, String key) throws Exception;

    String generateKey(String className, String methodName, Object... params);
}
