package com.example.mentorisebackend.api.controller.user;


import com.example.mentorisebackend.dto.auth.LoginRequestDto;
import com.example.mentorisebackend.dto.admin.ErrorResponseDto;
import com.example.mentorisebackend.dto.auth.ForgotPasswordRequest;
import com.example.mentorisebackend.dto.auth.RegisterDto;
import com.example.mentorisebackend.dto.auth.ResetPasswordRequest;
import com.example.mentorisebackend.service.auth.UserAuthService;
import com.example.mentorisebackend.service.auth.UserPasswordResetService;
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

    @PostMapping("/register")
    public ResponseEntity<RegisterResponse> register(@Valid @RequestBody RegisterDto dto) {
        Long id = userAuthService.register(dto);
        return ResponseEntity.ok(new RegisterResponse(id, "נרשמת בהצלחה"));
    }

    public record RegisterResponse(Long id, String message) {

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        try {
            return ResponseEntity.ok(userAuthService.login(request));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(
                    new ErrorResponseDto("InvalidCredentials", "Invalid email or password", 401)
            );
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