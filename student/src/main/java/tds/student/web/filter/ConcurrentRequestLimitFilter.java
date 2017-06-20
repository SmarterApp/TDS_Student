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

package tds.student.web.filter;

import org.apache.http.HttpHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.Integer.parseInt;

/**
 * This concurrent request limiting filter will fast-fail with a 503 if the maximum
 * number of concurrent requests are already being resolved.
 */
@Component
public class ConcurrentRequestLimitFilter extends OncePerRequestFilter {
    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentRequestLimitFilter.class);

    private static AtomicInteger concurrentRequests = new AtomicInteger(0);

    private int maxConcurrentRequests;

    @Autowired
    public ConcurrentRequestLimitFilter(@Value("${performance.request.limit:-1}") final String maxConcurrentRequests) {
        try {
            this.maxConcurrentRequests = parseInt(maxConcurrentRequests, 10);
        } catch (final NumberFormatException e) {
            this.maxConcurrentRequests = -1;
        }
    }

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain) throws ServletException, IOException {
        final int currentRequest = concurrentRequests.incrementAndGet();
        try {
            if (maxConcurrentRequests < 0 || currentRequest <= maxConcurrentRequests) {
                filterChain.doFilter(request, response);
            } else {
                LOG.error("Rejecting request due to too many concurrent requests: {}, {}/{}", request.getRequestURI(), currentRequest, maxConcurrentRequests);
                response.addHeader(HttpHeaders.RETRY_AFTER, "30");
                response.sendError(503, "Too many requests");
            }
        } finally {
            concurrentRequests.decrementAndGet();
        }
    }

}
