package com.example.mentorisebackend.api.controller.admin;


import com.example.mentorisebackend.dto.admin.AdminSendNotificationRequest;
import com.example.mentorisebackend.service.admin.AdminNotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/notifications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminNotificationController {

    private final AdminNotificationService adminNotificationService;

    @PostMapping
    public ResponseEntity<String> sendNotification(@Valid @RequestBody AdminSendNotificationRequest request) {
        adminNotificationService.sendNotification(request);
        return ResponseEntity.ok("Notification sent successfully");
    }
}