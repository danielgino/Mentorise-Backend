package com.example.mentorisebackend.service.admin;


import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.dto.admin.AdminSendNotificationRequest;
import com.example.mentorisebackend.enums.Role;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminNotificationService {

    private final UserRepository userRepository;
    private final NotificationService notificationService;

    @Transactional
    public void sendNotification(AdminSendNotificationRequest request) {
        List<User> recipients = switch (request.getTarget()) {
            case ALL -> userRepository.findAll();
            case STUDENTS -> userRepository.findAllByRole(Role.STUDENT);
            case TUTORS -> userRepository.findAllByRole(Role.TUTOR);
        };

        for (User user : recipients) {
            notificationService.sendAdminBroadcastToUser(
                    user,
                    request.getTitle().trim(),
                    request.getMessage().trim()
            );
        }
    }
}