package com.example.mentorisebackend.api.controller.user;


import com.example.mentorisebackend.dto.auth.InvalidCredentialsResponseDto;
import com.example.mentorisebackend.dto.auth.LoginRequestDto;
import com.example.mentorisebackend.dto.auth.ForgotPasswordRequest;
import com.example.mentorisebackend.dto.auth.RegisterDto;
import com.example.mentorisebackend.dto.auth.ResetPasswordRequest;
import com.example.mentorisebackend.service.auth.LoginRateLimitService;
import com.example.mentorisebackend.service.auth.UserAuthService;
import com.example.mentorisebackend.service.auth.UserPasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users/auth")
@RequiredArgsConstructor
public class UserAuthController {

    private final UserAuthService userAuthService;
    private final UserPasswordResetService userPasswordResetService;
    private final LoginRateLimitService loginRateLimitService;

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterDto dto) {
        Long id = userAuthService.register(dto);
        return ResponseEntity.ok(new RegisterResponse(id, "נרשמת בהצלחה"));
    }

    public record RegisterResponse(Long id, String message) {

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request, HttpServletRequest httpRequest) {
        String ip    = httpRequest.getRemoteAddr();
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";

        loginRateLimitService.checkRateLimit(email, ip);
        try {
            var result = userAuthService.login(request);
            loginRateLimitService.onSuccess(email, ip);
            return ResponseEntity.ok(result);
        } catch (AuthenticationException e) {
            InvalidCredentialsResponseDto body = loginRateLimitService.onFailure(email, ip);
            return ResponseEntity.status(401).body(body);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        userPasswordResetService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message",
                "אם קיים חשבון עבור כתובת המייל הזו, נשלח אליה קישור לאיפוס הסיסמה."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        userPasswordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "הסיסמה אופסה בהצלחה."));
    }
}