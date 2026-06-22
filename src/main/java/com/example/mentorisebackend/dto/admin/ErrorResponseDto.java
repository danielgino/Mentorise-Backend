package com.example.mentorisebackend.dto.admin;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class ErrorResponseDto {
    private String error;
    private String message;
    private int status;

    public ErrorResponseDto(String error, String message, int status) {
        this.error = error;
        this.message = message;
        this.status = status;
    }

}
