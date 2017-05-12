package tds.student.web.filter;

import org.apache.http.HttpHeaders;
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
                response.addHeader(HttpHeaders.RETRY_AFTER, "30");
                response.sendError(503, "Too many requests");
            }
        } finally {
            concurrentRequests.decrementAndGet();
        }
    }

}
