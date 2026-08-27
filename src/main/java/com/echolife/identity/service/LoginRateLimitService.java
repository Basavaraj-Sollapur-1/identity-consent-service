package com.echolife.identity.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class LoginRateLimitService {
    private static final int MAX_ATTEMPTS = 5;
    private static final Duration WINDOW = Duration.ofMinutes(15);
    private final StringRedisTemplate redis;

    public LoginRateLimitService(StringRedisTemplate redis) { this.redis = redis; }

    public void check(String normalizedEmail) {
        checkKey("identity:login-rate-limit:" + normalizedEmail);
    }

    public void recordFailure(String normalizedEmail) {
        recordFailureKey("identity:login-rate-limit:" + normalizedEmail);
    }

    public void clear(String normalizedEmail) { redis.delete("identity:login-rate-limit:" + normalizedEmail); }

    public void checkMfa(String userId, String ipAddress) {
        checkKey("identity:mfa-rate-limit:" + userId + ":" + normalizeIp(ipAddress));
    }

    public void recordMfaFailure(String userId, String ipAddress) {
        recordFailureKey("identity:mfa-rate-limit:" + userId + ":" + normalizeIp(ipAddress));
    }

    public void clearMfa(String userId, String ipAddress) {
        redis.delete("identity:mfa-rate-limit:" + userId + ":" + normalizeIp(ipAddress));
    }

    public boolean consumeMfaChallenge(String jti) {
        if (jti == null || jti.isBlank()) return false;
        Boolean inserted = redis.opsForValue().setIfAbsent(
            "identity:mfa-challenge-used:" + jti, "1", Duration.ofMinutes(10));
        return Boolean.TRUE.equals(inserted);
    }

    private void checkKey(String key) {
        String value = redis.opsForValue().get(key);
        if (value != null && Integer.parseInt(value) >= MAX_ATTEMPTS) {
            throw new IllegalArgumentException("RATE_LIMITED");
        }
    }

    private void recordFailureKey(String key) {
        Long count = redis.opsForValue().increment(key);
        if (count != null && count == 1L) redis.expire(key, WINDOW);
    }

    private String normalizeIp(String ipAddress) {
        return ipAddress == null || ipAddress.isBlank() ? "unknown" : ipAddress;
    }
}
