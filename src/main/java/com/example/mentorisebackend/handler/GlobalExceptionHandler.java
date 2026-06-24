package com.example.mentorisebackend.handler;

import com.example.mentorisebackend.dto.admin.ErrorResponseDto;
import com.example.mentorisebackend.dto.auth.RateLimitErrorResponseDto;
import com.example.mentorisebackend.exception.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<RateLimitErrorResponseDto> handleRateLimit(RateLimitExceededException ex) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));
        return ResponseEntity.status(429)
                .headers(headers)
                .body(new RateLimitErrorResponseDto(
                        "RATE_LIMIT_EXCEEDED",
                        "בוצעו יותר מדי ניסיונות התחברות. נסה שוב בעוד 15 דקות.",
                        ex.getRetryAfterSeconds()
                ));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(404).body(
                new ErrorResponseDto(
                        "UserNotFound",
                        ex.getMessage(),
                        404
                )
        );
    }


    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleResourceNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(404).body(
                new ErrorResponseDto(
                        "ResourceNotFound",
                        ex.getMessage(),
                        404
                )
        );
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponseDto> handleAuth(AuthenticationException ex) {
        return ResponseEntity.status(401).body(
                new ErrorResponseDto(
                        "InvalidCredentials",
                        ex.getMessage(),
                        401
                )
        );
    }

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneric(Exception ex) {
        log.error("Unhandled exception", ex);

        return ResponseEntity.status(500).body(
                new ErrorResponseDto(
                        "InternalServerError",
                        "אירעה שגיאה פנימית בשרת",
                        500
                )
        );
    }

    @ExceptionHandler(DuplicateFieldException.class)
    public ResponseEntity<ErrorResponseDto> handleDuplicateField(DuplicateFieldException ex) {
        return ResponseEntity.status(409).body(
                new ErrorResponseDto(
                        "DuplicateField",
                        ex.getMessage(),
                        409
                )
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ErrorResponseDto> handleResponseStatus(ResponseStatusException ex) {
        return ResponseEntity.status(ex.getStatusCode()).body(
                new ErrorResponseDto(
                        ex.getStatusCode().toString(),
                        ex.getReason() != null ? ex.getReason() : "Request failed",
                        ex.getStatusCode().value()
                )
        );
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponseDto> handleValidation(MethodArgumentNotValidException ex) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.status(400).body(
                new ErrorResponseDto("ValidationError", message, 400)
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ErrorResponseDto> handleBadRequest(BadRequestException ex) {
        return ResponseEntity.status(400).body(
                new ErrorResponseDto(
                        "BadRequest",
                        ex.getMessage(),
                        400
                )
        );
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponseDto> handleConflict(ConflictException ex) {
        return ResponseEntity.status(409).body(
                new ErrorResponseDto(
                        "Conflict",
                        ex.getMessage(),
                        409
                )
        );
    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponseDto> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(403).body(
                new ErrorResponseDto(
                        "AccessDenied",
                        ex.getMessage(),
                        403
                )
        );
    }
}
