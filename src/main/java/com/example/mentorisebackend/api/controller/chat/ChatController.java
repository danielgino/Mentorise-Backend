package com.example.mentorisebackend.api.controller.chat;
import com.example.mentorisebackend.dto.chat.ConversationIdDto;
import com.example.mentorisebackend.dto.chat.InboxItemDto;
import com.example.mentorisebackend.dto.chat.MessageDto;
import com.example.mentorisebackend.dto.chat.SendMessageRequest;
import com.example.mentorisebackend.service.chat.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/conversations/with/{otherUserId}")
    public ResponseEntity<ConversationIdDto> getOrCreateConversation(@PathVariable Long otherUserId) {
        return ResponseEntity.ok(chatService.getOrCreateConversationWith(otherUserId));
    }

    @GetMapping("/conversations")
    public ResponseEntity<List<InboxItemDto>> getInbox(@RequestParam(defaultValue = "30") int limit) {
        return ResponseEntity.ok(chatService.getInboxForCurrentUser(limit));
    }

    @GetMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<List<MessageDto>> getMessages(@PathVariable Long conversationId,
                                                        @RequestParam(required = false) Long cursorId,
                                                        @RequestParam(defaultValue = "30") int limit) {
        return ResponseEntity.ok(chatService.getMessagesForCurrentUser(conversationId, cursorId, limit));
    }

    @PostMapping("/conversations/{conversationId}/messages")
    public ResponseEntity<MessageDto> sendMessage(@PathVariable Long conversationId,
                                                  @Valid @RequestBody SendMessageRequest request) {
        return ResponseEntity.ok(chatService.sendMessageForCurrentUser(conversationId, request));
    }

    @PostMapping("/conversations/{conversationId}/read")
    public ResponseEntity<Void> markAsRead(@PathVariable Long conversationId) {
        chatService.markAsReadForCurrentUser(conversationId);
        return ResponseEntity.noContent().build();
    }
}