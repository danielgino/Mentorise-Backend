package com.example.mentorisebackend.api.controller.admin;

import com.example.mentorisebackend.dto.admin.ErrorResponseDto;
import com.example.mentorisebackend.dto.admin.ForgotPasswordRequestDto;
import com.example.mentorisebackend.dto.admin.ResetPasswordRequestDto;
import com.example.mentorisebackend.dto.auth.LoginRequestDto;
import com.example.mentorisebackend.service.admin.AdminAuthService;
import com.example.mentorisebackend.service.admin.AdminPasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/admin/auth")
@RequiredArgsConstructor
public class AdminAuthController {

    private final AdminAuthService adminAuthService;
    private final AdminPasswordResetService adminPasswordResetService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request) {
        try {
            return ResponseEntity.ok(adminAuthService.login(request));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(401).body(
                    new ErrorResponseDto("InvalidCredentials", "Invalid email or password", 401)
            );
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(@Valid @RequestBody ForgotPasswordRequestDto request) {
        adminPasswordResetService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "If this email exists, a reset link was sent."));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(@Valid @RequestBody ResetPasswordRequestDto request) {
        adminPasswordResetService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok(Map.of("message", "Password reset successfully."));
    }
}
