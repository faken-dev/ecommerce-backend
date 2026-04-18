package com.ecommerce.auth.infrastructure.config;

import com.ecommerce.shared.exception.ErrorCode;
import com.ecommerce.shared.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.redis.lettuce.cas.LettuceBasedProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final LettuceBasedProxyManager<String> proxyManager;
    private final ObjectMapper objectMapper;
    private final List<String> trustedProxies;

    // Limits per endpoint pattern
    private static final Map<String, Bandwidth> ENDPOINT_LIMITS = Map.of(
            "/api/v1/auth/login",            bandwidth(5, Duration.ofMinutes(1)),
            "/api/v1/auth/register",         bandwidth(3, Duration.ofMinutes(1)),
            "/api/v1/auth/otp/send",         bandwidth(3, Duration.ofMinutes(5)),
            "/api/v1/auth/otp/verify",       bandwidth(3, Duration.ofMinutes(5)),
            "/api/v1/auth/password/forgot",   bandwidth(3, Duration.ofMinutes(10))
    );

    private static final Bandwidth DEFAULT_LIMIT = bandwidth(60, Duration.ofMinutes(1));

    private static final String FORWARDED_FOR_HEADER = "X-Forwarded-For";
    private static final String X_REAL_IP_HEADER    = "X-Real-IP";

    public RateLimitInterceptor(
            LettuceBasedProxyManager<String> proxyManager,
            ObjectMapper objectMapper,
            @Value("${app.rate-limit.trusted-proxies:}") List<String> trustedProxies) {
        this.proxyManager = proxyManager;
        this.objectMapper = objectMapper;
        this.trustedProxies = trustedProxies != null ? trustedProxies : List.of();
    }

    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws IOException {

        String uri = request.getRequestURI();
        String ip = resolveClientIp(request);

        Bandwidth limit = ENDPOINT_LIMITS.entrySet().stream()
                .filter(e -> uri.endsWith(e.getKey()))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(DEFAULT_LIMIT);

        // Key: rl:{endpoint}:{ip}
        String bucketKey = "rl:" + uri + ":" + ip;
        BucketConfiguration config = BucketConfiguration.builder()
                .addLimit(limit)
                .build();

        Bucket bucket = proxyManager.builder()
                .build(bucketKey, () -> config);

        if (bucket.tryConsume(1)) {
            return true;
        }

        response.setStatus(429);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<?> errorResponse = ApiResponse.error(ErrorCode.RATE_LIMIT_EXCEEDED);
        response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        return false;
    }

    /**
     * Resolves the real client IP address.
     *
     * Priority:
     * 1. X-Forwarded-For — only trusted when request comes from a known proxy.
     * 2. X-Real-IP        — same conditions as above.
     * 3. getRemoteAddr() — fallback for direct connections.
     */
    private String resolveClientIp(HttpServletRequest request) {
        String remoteAddr = request.getRemoteAddr();

        // Only honour forwarded headers when the request originates from a trusted proxy.
        if (isTrustedProxy(remoteAddr)) {
            String forwardedFor = request.getHeader(FORWARDED_FOR_HEADER);
            if (forwardedFor != null && !forwardedFor.isBlank()) {
                // X-Forwarded-For can contain multiple IPs: client, proxy1, proxy2...
                // The leftmost non-trusted IP is the real client.
                return extractFirstClientIp(forwardedFor);
            }

            String realIp = request.getHeader(X_REAL_IP_HEADER);
            if (realIp != null && !realIp.isBlank()) {
                return realIp.trim();
            }
        }

        return remoteAddr;
    }

    private boolean isTrustedProxy(String ip) {
        return trustedProxies.contains(ip);
    }

    /**
     * Extracts the leftmost non-trusted IP from X-Forwarded-For.
     * If all IPs are trusted proxies, returns the last one (rightmost).
     */
    private String extractFirstClientIp(String forwardedFor) {
        String[] ips = forwardedFor.split(",");
        for (String ip : ips) {
            String trimmed = ip.trim();
            if (!isTrustedProxy(trimmed)) {
                return trimmed;
            }
        }
        // Fallback: rightmost IP when no non-trusted IP found
        return ips[ips.length - 1].trim();
    }

    private static Bandwidth bandwidth(long capacity, Duration period) {
        return Bandwidth.builder()
                .capacity(capacity)
                .refillGreedy(capacity, period)
                .build();
    }
}