package com.backend.notes.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Bucket4j;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {
    
    @Value("${app.rate-limit.auth-rpm:5}")
    private int authRpm;
    
    @Value("${app.rate-limit.api-rpm:100}")
    private int apiRpm;
    
    private final LoadingCache<String, Bucket> authBuckets;
    private final LoadingCache<String, Bucket> apiBuckets;
    
    public RateLimitFilter() {
        this.authBuckets = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(key -> createAuthBucket());
        
        this.apiBuckets = Caffeine.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build(key -> createApiBucket());
    }
    
    private Bucket createAuthBucket() {
        return Bucket4j.builder()
            .addLimit(Bandwidth.classic(authRpm, Refill.intervally(authRpm, Duration.ofMinutes(1))))
            .build();
    }
    
    private Bucket createApiBucket() {
        return Bucket4j.builder()
            .addLimit(Bandwidth.classic(apiRpm, Refill.intervally(apiRpm, Duration.ofMinutes(1))))
            .build();
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {
        
        String path = request.getRequestURI();
        boolean isAuthEndpoint = path.matches(".*(register|login|auth/refresh).*");
        
        String key = getClientKey(request);
        
        Bucket bucket = isAuthEndpoint ? authBuckets.get(key) : apiBuckets.get(key);
        
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(429); // Too Many Requests
            response.setContentType("application/json");
            response.setHeader("Retry-After", "60");
            response.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
        }
    }
    
    private String getClientKey(HttpServletRequest request) {
        // Try to get IP address, fallback to user ID if authenticated
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty()) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }
}
