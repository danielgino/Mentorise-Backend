package com.example.mentorisebackend.dto.chat;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class MessageDto {
    Long id;
    String clientMessageId;
    Long conversationId;
    Long senderId;
    String type;
    String content;
    Instant sentAt;
}