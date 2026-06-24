package com.example.mentorisebackend.api.controller.admin;

import com.example.mentorisebackend.dto.admin.ForgotPasswordRequestDto;
import com.example.mentorisebackend.dto.admin.ResetPasswordRequestDto;
import com.example.mentorisebackend.dto.auth.InvalidCredentialsResponseDto;
import com.example.mentorisebackend.dto.auth.LoginRequestDto;
import com.example.mentorisebackend.service.admin.AdminAuthService;
import com.example.mentorisebackend.service.admin.AdminPasswordResetService;
import com.example.mentorisebackend.service.auth.LoginRateLimitService;
import jakarta.servlet.http.HttpServletRequest;
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
    private final LoginRateLimitService loginRateLimitService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto request, HttpServletRequest httpRequest) {
        String ip    = httpRequest.getRemoteAddr();
        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase() : "";

        loginRateLimitService.checkRateLimit(email, ip);
        try {
            var result = adminAuthService.login(request);
            loginRateLimitService.onSuccess(email, ip);
            return ResponseEntity.ok(result);
        } catch (AuthenticationException e) {
            InvalidCredentialsResponseDto body = loginRateLimitService.onFailure(email, ip);
            return ResponseEntity.status(401).body(body);
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
