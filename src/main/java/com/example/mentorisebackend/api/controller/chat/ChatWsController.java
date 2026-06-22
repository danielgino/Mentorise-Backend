package com.example.mentorisebackend.api.controller.chat;

import com.example.mentorisebackend.dto.chat.MessageDto;
import com.example.mentorisebackend.security.UserPrincipal;
import com.example.mentorisebackend.service.chat.ChatService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatWsController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

@MessageMapping("/chat.send")
public void send(@Payload ChatSendWsRequest req, Principal principal) {
    if (principal == null) {
        throw new IllegalStateException("Unauthenticated STOMP session");
    }
    if (req == null || req.getContent() == null || req.getContent().isBlank()) {
        return;
    }

    Long senderId = ((UserPrincipal) ((Authentication) principal).getPrincipal()).getUserId();

    MessageDto saved = chatService.sendMessageAndBroadcast(
            req.getConversationId(),
            senderId,
            req.getContent(),
            req.getClientMessageId()
    );
}

    @Data
    public static class ChatSendWsRequest {
        private Long conversationId;
        private String content;
        private String clientMessageId;
    }
}