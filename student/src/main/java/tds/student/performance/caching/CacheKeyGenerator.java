/*******************************************************************************
 * Educational Online Test Delivery System
 * Copyright (c) 2016 Regents of the University of California
 *
 * Distributed under the AIR Open Source License, Version 1.0
 * See accompanying file AIR-License-1_0.txt or at
 * http://www.smarterapp.org/documents/American_Institutes_for_Research_Open_Source_Software_License.pdf
 *
 * SmarterApp Open Source Assessment Software Project: http://smarterapp.org
 * Developed by Fairway Technologies, Inc. (http://fairwaytech.com)
 * for the Smarter Balanced Assessment Consortium (http://smarterbalanced.org)
 ******************************************************************************/
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
            if (obj != null)
                sb.append(obj.hashCode());
            sb.append(":");
        }
        return sb.toString();
    }
}
