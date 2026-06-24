package com.example.mentorisebackend.dto.auth;

import lombok.Getter;

@Getter
public class InvalidCredentialsResponseDto {

    private final String error;
    private final String message;
    private final int attemptsRemaining;

    public InvalidCredentialsResponseDto(String error, String message, int attemptsRemaining) {
        this.error = error;
        this.message = message;
        this.attemptsRemaining = attemptsRemaining;
    }
}
