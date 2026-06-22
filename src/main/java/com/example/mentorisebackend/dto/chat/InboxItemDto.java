package com.example.mentorisebackend.dto.chat;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class InboxItemDto {
    Long conversationId;

    Long otherUserId;
    String otherFirstName;
    String otherLastName;
    String otherProfileImageUrl;

    Long lastMessageId;
    String lastMessageType;
    String lastMessageContent;
    Instant lastMessageAt;

    Integer unreadCount;
}