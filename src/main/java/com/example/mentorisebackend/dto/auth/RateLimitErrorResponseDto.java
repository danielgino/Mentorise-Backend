package com.example.mentorisebackend.dto.auth;

import lombok.Getter;

@Getter
public class RateLimitErrorResponseDto {

    private final String error;
    private final String message;
    private final int retryAfterSeconds;

    public RateLimitErrorResponseDto(String error, String message, int retryAfterSeconds) {
        this.error = error;
        this.message = message;
        this.retryAfterSeconds = retryAfterSeconds;
    }
}
