package tds.student.performance.caching;

import org.springframework.cache.interceptor.DefaultKeyGenerator;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

/**
 * Created by jtreuting on 12/28/15.
 */
public class CacheKeyGenerator extends DefaultKeyGenerator {
    @Override
    public Object generate(Object target, Method method, Object... params) {
        return generate(target.getClass().getName(), method.getName(), params);
    }

    public String generate(String className, String methodName, Object... params) {
        // This will generate a unique key of the class name, the method name, and all method parameters appended.
        StringBuilder sb = new StringBuilder();
        sb.append(className);
        sb.append(".");
        sb.append(methodName);
        for (Object obj : params) {
            sb.append(obj.hashCode());
            sb.append(":");
        }
        return sb.toString();
    }
}
