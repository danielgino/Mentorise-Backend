package com.example.mentorisebackend.dto.chat;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SendMessageRequest {
    @NotBlank(message = "Message content is required")
    private String content;
    private String clientMessageId; // UUID
}