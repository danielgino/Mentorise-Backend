package com.example.mentorisebackend.dto.admin;


import com.example.mentorisebackend.enums.NotificationAudience;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AdminSendNotificationRequest {

    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 50, message = "Title must be between 3 and 50 characters")
    private String title;

    @NotBlank(message = "Message is required")
    @Size(max = 500, message = "Message is too long")
    private String message;

    private NotificationAudience target;
}