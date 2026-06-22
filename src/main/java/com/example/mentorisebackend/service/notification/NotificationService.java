package com.example.mentorisebackend.service.notification;

import com.example.mentorisebackend.security.CurrentUser;
import com.example.mentorisebackend.api.entity.Notification;
import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.dto.notification.NotificationResponseDto;
import com.example.mentorisebackend.enums.NotificationType;
import com.example.mentorisebackend.exception.ResourceNotFoundException;
import com.example.mentorisebackend.exception.UserNotFoundException;
import com.example.mentorisebackend.repository.NotificationRepository;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;
    private final SimpMessagingTemplate messagingTemplate;
    private final ExpoPushNotificationService expoPushNotificationService;

    public Notification createNotification(Long recipientId,
                                           NotificationType type,
                                           String title,
                                           String message) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND_WITH_ID + recipientId));
        return createNotification(recipient, type, title, message);
    }

    public Notification createNotification(User recipient,
                                           NotificationType type,
                                           String title,
                                           String message) {

        Notification notification = Notification.builder()
                .recipient(recipient)
                .type(type)
                .title(title)
                .message(message)
                .isRead(false)
                .build();

        Notification saved = notificationRepository.save(notification);

        Map<String, Object> pushData = Map.of(
                "notificationId", String.valueOf(saved.getId()),
                "type", type.name(),
                "screen", "notifications"
        );
        expoPushNotificationService.sendToUser(recipient.getId(), title, message, pushData);

        NotificationResponseDto dto = NotificationResponseDto.builder()
                .id(saved.getId())
                .type(saved.getType())
                .title(saved.getTitle())
                .message(saved.getMessage())
                .isRead(saved.isRead())
                .createdAt(saved.getCreatedAt())
                .build();

        messagingTemplate.convertAndSendToUser(
                recipient.getEmail(),
                AppConstants.WEBSOCKET_QUEUE_NOTIFICATIONS,
                dto
        );

        return saved;
    }

    public void sendReminderNotification(Long recipientId, String title, String message) {
        User recipient = userRepository.findById(recipientId)
                .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND_WITH_ID + recipientId));
        createNotification(recipient, NotificationType.LESSON_REMINDER, title, message);
    }

    public void sendAdminBroadcastToUser(User recipient,
                                         String title,
                                         String message) {
        createNotification(recipient, NotificationType.ADMIN_MESSAGE, title, message);
    }

    @Transactional(readOnly = true)
    public Page<Notification> getMyNotifications(Pageable pageable) {
        Long currentUserId = currentUser.getUserId();
        return notificationRepository.findByRecipientIdOrderByCreatedAtDesc(currentUserId, pageable);
    }

    @Transactional(readOnly = true)
    public long countMyUnreadNotifications() {
        Long currentUserId = currentUser.getUserId();
        return notificationRepository.countByRecipientIdAndIsReadFalse(currentUserId);
    }

    public void markAsRead(Long notificationId) {
        Long currentUserId = currentUser.getUserId();

        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.NOTIFICATION_NOT_FOUND + notificationId));

        if (!notification.getRecipient().getId().equals(currentUserId)) {
            throw new AccessDeniedException(AppConstants.NOT_ALLOWED_TO_UPDATE_NOTIFICATION);
        }

        if (!notification.isRead()) {
            notification.setRead(true);
            notificationRepository.save(notification);
        }
    }

    public void markAllAsRead() {
        Long currentUserId = currentUser.getUserId();
        List<Notification> unreadNotifications = notificationRepository.findByRecipientIdAndIsReadFalse(currentUserId);
        for (Notification notification : unreadNotifications) {
            notification.setRead(true);
        }
        notificationRepository.saveAll(unreadNotifications);
    }
}