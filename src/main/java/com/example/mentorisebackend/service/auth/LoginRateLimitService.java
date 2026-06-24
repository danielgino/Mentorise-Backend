package com.example.mentorisebackend.service.auth;

import com.example.mentorisebackend.dto.auth.InvalidCredentialsResponseDto;
import com.example.mentorisebackend.exception.RateLimitExceededException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class LoginRateLimitService {

    private static final int MAX_ATTEMPTS    = 5;
    private static final int WINDOW_MINUTES  = 15;
    private static final int WINDOW_SECONDS  = WINDOW_MINUTES * 60;

    private final Cache<String, AtomicInteger> cache = Caffeine.newBuilder()
            .expireAfterWrite(WINDOW_MINUTES, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();

    /**
     * Called before the authentication attempt.
     * Throws 429 if the key is already at or above the limit.
     */
    public void checkRateLimit(String email, String ip) {
        AtomicInteger counter = cache.getIfPresent(buildKey(email, ip));
        if (counter != null && counter.get() >= MAX_ATTEMPTS) {
            throw new RateLimitExceededException(WINDOW_SECONDS);
        }
    }

    /**
     * Called after a failed authentication attempt.
     * Increments the counter and returns the response body for a 401.
     * On the 5th failure, throws RateLimitExceededException (429) instead.
     */
    public InvalidCredentialsResponseDto onFailure(String email, String ip) {
        AtomicInteger counter = cache.get(buildKey(email, ip), k -> new AtomicInteger(0));
        int newCount  = counter.incrementAndGet();
        int remaining = MAX_ATTEMPTS - newCount;

        if (remaining <= 0) {
            throw new RateLimitExceededException(WINDOW_SECONDS);
        }

        String message = "פרטי ההתחברות שגויים. נותרו לך " + remaining + " ניסיונות לפני חסימה זמנית.";
        return new InvalidCredentialsResponseDto("InvalidCredentials", message, remaining);
    }

    /**
     * Called after a successful login. Clears the failure counter for this key.
     */
    public void onSuccess(String email, String ip) {
        cache.invalidate(buildKey(email, ip));
    }

    private String buildKey(String email, String ip) {
        return email + ":" + ip;
    }
}
