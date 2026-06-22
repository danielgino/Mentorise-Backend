package com.example.mentorisebackend.repository.projection.chat;


import java.time.LocalDateTime;

public interface ConversationInboxItemProjection {
    Long getConversationId();
    Long getOtherUserId();
    String getOtherFirstName();
    String getOtherLastName();
    String getOtherProfileImageUrl();
    Long getLastMessageId();
    String getLastMessageType();
    String getLastMessageContent();
    LocalDateTime getLastMessageAt();
    Integer getUnreadCount();
}