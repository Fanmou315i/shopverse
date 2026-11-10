package com.shopverse.app.api;

import io.github.bucket4j.Bucket;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.time.Duration;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** One shared bucket per application instance, refreshed every second. */
@Component
public class ApiRateLimitFilter extends OncePerRequestFilter {
    private final Bucket bucket = Bucket.builder()
        .addLimit(limit -> limit.capacity(100).refillIntervally(100, Duration.ofSeconds(1))).build();
    @Override protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        return !(path.equals("/api") || path.startsWith("/api/"));
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                               FilterChain chain) throws ServletException, IOException {
        var probe = bucket.tryConsumeAndReturnRemaining(1);
        response.setHeader("X-RateLimit-Limit", "100");
        response.setHeader("X-RateLimit-Remaining", Long.toString(probe.getRemainingTokens()));
        if (probe.isConsumed()) { chain.doFilter(request, response); return; }
        response.setStatus(429);
        response.setHeader("Retry-After", "1");
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":\"RATE_LIMITED\",\"message\":\"请求过于频繁，请稍后重试\"}");
    }
}
