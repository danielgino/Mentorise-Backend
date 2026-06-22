package com.example.mentorisebackend.api.controller.notification;


import com.example.mentorisebackend.api.entity.Notification;
import com.example.mentorisebackend.dto.notification.NotificationResponseDto;
import com.example.mentorisebackend.service.notification.NotificationService;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/my")
    public Page<NotificationResponseDto> getMyNotifications(
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) @Max(100) int size
    ) {
        Pageable pageable = PageRequest.of(page, size);

        return notificationService.getMyNotifications(pageable)
                .map(this::toDto);
    }

    @GetMapping("/my/unread-count")
    public long getMyUnreadCount() {
        return notificationService.countMyUnreadNotifications();
    }

    @PatchMapping("/{notificationId}/read")
    public void markAsRead(@PathVariable Long notificationId) {
        notificationService.markAsRead(notificationId);
    }

    @PatchMapping("/my/read-all")
    public void markAllAsRead() {
        notificationService.markAllAsRead();
    }

    private NotificationResponseDto toDto(Notification notification) {
        return NotificationResponseDto.builder()
                .id(notification.getId())
                .type(notification.getType())
                .title(notification.getTitle())
                .message(notification.getMessage())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}