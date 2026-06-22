package com.example.mentorisebackend.exception;


import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT) // 409
public class DuplicateFieldException extends RuntimeException {
    private final String field;
    private final String value;

    public DuplicateFieldException(String field, String value, String message) {
        super(message);
        this.field = field;
        this.value = value;
    }

    public String getField() { return field; }
    public String getValue() { return value; }
}
