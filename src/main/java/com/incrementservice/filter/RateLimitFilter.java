package com.incrementservice.filter;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

@Component
@Order(1)
@Slf4j
public class RateLimitFilter implements Filter {

    @Autowired
    Supplier<BucketConfiguration> bucketConfiguration;

    @Autowired
    ProxyManager<String> proxyManager;

    // List of paths to exclude from rate limiting
    private final List<String> excludedPaths = Arrays.asList(
            "/actuator/health",
            "/actuator/info",
            "/actuator/metrics",
            "/actuator/prometheus"
    );

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;
        String requestURI = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        String clientIp = httpRequest.getRemoteAddr();

        log.info("Incoming request: method = {}, URI = {}, client IP = {}", method, requestURI, clientIp);

        // Check if the request URI contains any of the excluded paths
        if (excludedPaths.stream().anyMatch(requestURI::contains)) {
            // Skip rate limiting for excluded endpoints
            log.info("Rate limiting skipped for URI: {}", requestURI);
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        Bucket bucket = proxyManager.builder().build(clientIp, bucketConfiguration);
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);

        log.debug("Remaining tokens for IP {}: {}", clientIp, probe.getRemainingTokens());

        if (probe.isConsumed()) {
            log.info("Request allowed: method = {}, URI = {}, client IP = {}", method, requestURI, clientIp);
            filterChain.doFilter(servletRequest, servletResponse);
        } else {
            log.warn("Request rate limited: method = {}, URI = {}, client IP = {}", method, requestURI, clientIp);
            HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
            httpResponse.setContentType("text/plain");
            httpResponse.setStatus(429);
            httpResponse.getWriter().append("Too many requests");
        }
    }
}