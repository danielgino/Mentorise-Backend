package com.example.mentorisebackend.service.chat;

import com.example.mentorisebackend.security.CurrentUser;
import com.example.mentorisebackend.api.entity.Conversation;
import com.example.mentorisebackend.api.entity.ConversationRead;
import com.example.mentorisebackend.api.entity.Message;
import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.dto.chat.ConversationIdDto;
import com.example.mentorisebackend.dto.chat.InboxItemDto;
import com.example.mentorisebackend.dto.chat.MessageDto;
import com.example.mentorisebackend.dto.chat.SendMessageRequest;
import com.example.mentorisebackend.enums.MessageType;
import com.example.mentorisebackend.exception.BadRequestException;
import com.example.mentorisebackend.exception.ResourceNotFoundException;
import com.example.mentorisebackend.exception.UserNotFoundException;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.repository.chat.ConversationReadRepository;
import com.example.mentorisebackend.repository.chat.ConversationRepository;
import com.example.mentorisebackend.repository.chat.MessageRepository;
import com.example.mentorisebackend.repository.projection.chat.ConversationInboxItemProjection;
import com.example.mentorisebackend.service.notification.ExpoPushNotificationService;
import com.example.mentorisebackend.util.AppConstants;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final ConversationReadRepository conversationReadRepository;
    private final EntityManager entityManager;
    private final CurrentUser currentUser;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ExpoPushNotificationService expoPushNotificationService;

    // ===================== Public API (for controllers) =====================

    @Transactional
    public ConversationIdDto getOrCreateConversationWith(Long otherUserId) {
        Long meId = currentUser.getUserId();
        Conversation conversation = getOrCreateConversation(meId, otherUserId);
        return new ConversationIdDto(conversation.getId());
    }

    @Transactional(readOnly = true)
    public List<InboxItemDto> getInboxForCurrentUser(int limit) {
        Long meId = currentUser.getUserId();
        return getInbox(meId, limit);
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getMessagesForCurrentUser(Long conversationId, Long cursorId, int limit) {
        Long meId = currentUser.getUserId();
        return getMessages(conversationId, meId, cursorId, limit);
    }

    @Transactional
    public MessageDto sendMessageForCurrentUser(Long conversationId, SendMessageRequest request) {
        Long meId = currentUser.getUserId();

        Message saved = sendMessage(
                conversationId,
                meId,
                request.getContent(),
                request.getClientMessageId()
        );

        return toMessageDto(saved, conversationId);
    }

    @Transactional
    public void markAsReadForCurrentUser(Long conversationId) {
        Long meId = currentUser.getUserId();
        markAsRead(conversationId, meId);
    }

    // ===================== Core Logic =====================

    @Transactional
    public Conversation getOrCreateConversation(Long userAId, Long userBId) {
        requireIds(userAId, userBId);

        if (userAId.equals(userBId)) {
            throw new BadRequestException(AppConstants.CANNOT_CREATE_CONVERSATION_WITH_SELF);
        }

        long u1 = Math.min(userAId, userBId);
        long u2 = Math.max(userAId, userBId);

        Optional<Conversation> existing = conversationRepository.findByUser1_IdAndUser2_Id(u1, u2);
        if (existing.isPresent()) {
            return existing.get();
        }

        return createConversationSafely(u1, u2);
    }

    @Transactional
    public Message sendMessage(Long conversationId,
                               Long senderId,
                               String content,
                               String clientMessageId) {
        return sendMessageWithType(
                conversationId,
                senderId,
                content,
                clientMessageId,
                MessageType.TEXT
        );
    }

    @Transactional
    public Message sendMessageWithType(Long conversationId,
                                       Long senderId,
                                       String content,
                                       String clientMessageId,
                                       MessageType messageType) {

        Objects.requireNonNull(conversationId, AppConstants.CONVERSATION_ID_REQUIRED);
        Objects.requireNonNull(senderId, AppConstants.SENDER_ID_REQUIRED);
        Objects.requireNonNull(clientMessageId, AppConstants.CLIENT_MESSAGE_ID_REQUIRED);
        Objects.requireNonNull(messageType, AppConstants.MESSAGE_TYPE_REQUIRED);

        if (content == null || content.isBlank()) {
            throw new BadRequestException(AppConstants.MESSAGE_CONTENT_EMPTY);
        }

        Optional<Message> existingMsg =
                messageRepository.findByConversation_IdAndClientMessageId(conversationId, clientMessageId);

        if (existingMsg.isPresent()) {
            return existingMsg.get();
        }

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.CONVERSATION_NOT_FOUND + conversationId));

        Long user1Id = conversation.getUser1().getId();
        Long user2Id = conversation.getUser2().getId();

        if (!senderId.equals(user1Id) && !senderId.equals(user2Id)) {
            throw new BadRequestException(AppConstants.SENDER_NOT_PARTICIPANT_OF_CONVERSATION);
        }

        Long recipientId = senderId.equals(user1Id) ? user2Id : user1Id;
        Instant now = Instant.now();

        User senderRef = entityManager.getReference(User.class, senderId);

        Message msg = Message.builder()
                .conversation(conversation)
                .sender(senderRef)
                .type(messageType)
                .content(content)
                .clientMessageId(clientMessageId)
                .build();

        try {
            msg = messageRepository.save(msg);
        } catch (DataIntegrityViolationException e) {
            entityManager.clear();

            Optional<Message> msgAfterConstraint =
                    messageRepository.findByConversation_IdAndClientMessageId(conversationId, clientMessageId);

            if (msgAfterConstraint.isPresent()) {
                return msgAfterConstraint.get();
            }

            throw e;
        }

        conversation.setLastMessageAt(now);
        conversation.setLastMessageId(msg.getId());
        conversationRepository.save(conversation);

        ensureReadRowExists(conversation, user1Id);
        ensureReadRowExists(conversation, user2Id);

        ConversationRead recipientRead = conversationReadRepository
                .findByConversation_IdAndUser_Id(conversationId, recipientId)
                .orElseThrow(() -> new BadRequestException(AppConstants.CONVERSATION_READS_ROW_MISSING_RECIPIENT));

        recipientRead.setUnreadCount(recipientRead.getUnreadCount() + 1);
        conversationReadRepository.save(recipientRead);

        ConversationRead senderRead = conversationReadRepository
                .findByConversation_IdAndUser_Id(conversationId, senderId)
                .orElseThrow(() -> new BadRequestException(AppConstants.CONVERSATION_READS_ROW_MISSING_SENDER));

        senderRead.setLastReadAt(now);
        senderRead.setUnreadCount(0);
        conversationReadRepository.save(senderRead);

        return msg;
    }
    @Transactional
    public void markAsRead(Long conversationId, Long userId) {
        Objects.requireNonNull(conversationId, AppConstants.CONVERSATION_ID_REQUIRED);
        Objects.requireNonNull(userId, AppConstants.USER_ID_REQUIRED);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.CONVERSATION_NOT_FOUND + conversationId));

        Long user1Id = conversation.getUser1().getId();
        Long user2Id = conversation.getUser2().getId();

        if (!userId.equals(user1Id) && !userId.equals(user2Id)) {
            throw new BadRequestException(AppConstants.USER_NOT_PARTICIPANT_OF_CONVERSATION);
        }

        ensureReadRowExists(conversation, userId);

        Instant now = Instant.now();
        ConversationRead cr = conversationReadRepository
                .findByConversation_IdAndUser_Id(conversationId, userId)
                .orElseThrow(() -> new BadRequestException(AppConstants.CONVERSATION_READS_ROW_MISSING));

        cr.setLastReadAt(now);
        cr.setUnreadCount(0);
        conversationReadRepository.save(cr);
    }

    // ===================== Reads =====================

    @Transactional(readOnly = true)
    public List<InboxItemDto> getInbox(Long userId, int limit) {
        Objects.requireNonNull(userId, AppConstants.USER_ID_REQUIRED);

        int safeLimit = clamp(limit, 1, 50);
        List<ConversationInboxItemProjection> rows =
                conversationRepository.findInbox(userId, PageRequest.of(0, safeLimit));

        List<InboxItemDto> result = new ArrayList<>(rows.size());

        for (ConversationInboxItemProjection row : rows) {
            Instant lastAt = toInstant(row.getLastMessageAt());

            InboxItemDto dto = InboxItemDto.builder()
                    .conversationId(row.getConversationId())
                    .otherUserId(row.getOtherUserId())
                    .otherFirstName(row.getOtherFirstName())
                    .otherLastName(row.getOtherLastName())
                    .otherProfileImageUrl(row.getOtherProfileImageUrl())
                    .lastMessageId(row.getLastMessageId())
                    .lastMessageType(row.getLastMessageType())
                    .lastMessageContent(row.getLastMessageContent())
                    .lastMessageAt(lastAt)
                    .unreadCount(row.getUnreadCount() == null ? 0 : row.getUnreadCount())
                    .build();

            result.add(dto);
        }

        return result;
    }

    @Transactional(readOnly = true)
    public List<MessageDto> getMessages(Long conversationId, Long userId, Long cursorId, int limit) {
        Objects.requireNonNull(conversationId, AppConstants.CONVERSATION_ID_REQUIRED);
        Objects.requireNonNull(userId, AppConstants.USER_ID_REQUIRED);

        Conversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.CONVERSATION_NOT_FOUND + conversationId));

        Long u1 = conversation.getUser1().getId();
        Long u2 = conversation.getUser2().getId();
        if (!userId.equals(u1) && !userId.equals(u2)) {
            throw new BadRequestException(AppConstants.USER_NOT_PARTICIPANT_OF_CONVERSATION);
        }

        int safeLimit = clamp(limit, 1, 50);

        List<Message> rows;
        if (cursorId == null) {
            rows = messageRepository.findByConversation_IdOrderByIdDesc(conversationId, PageRequest.of(0, safeLimit));
        } else {
            rows = messageRepository.findByConversation_IdAndIdLessThanOrderByIdDesc(
                    conversationId, cursorId, PageRequest.of(0, safeLimit)
            );
        }

        Collections.reverse(rows);

        List<MessageDto> result = new ArrayList<>(rows.size());
        for (Message m : rows) {
            result.add(toMessageDto(m, conversationId));
        }

        return result;
    }
    @Transactional
    public MessageDto sendMessageAndBroadcast(Long conversationId,
                                              Long senderId,
                                              String content,
                                              String clientMessageId) {

        Message saved = sendMessageWithType(
                conversationId,
                senderId,
                content,
                clientMessageId,
                MessageType.TEXT
        );

        MessageDto dto = toMessageDto(saved, conversationId);

        Conversation conv = saved.getConversation();
        Long user1Id      = conv.getUser1().getId();
        Long user2Id      = conv.getUser2().getId();
        Long recipientId  = senderId.equals(user1Id) ? user2Id : user1Id;

        String senderEmail = normalizeEmail(
                userRepository.findEmailById(senderId)
                        .orElseThrow(() -> new UserNotFoundException(AppConstants.SENDER_NOT_FOUND + senderId))
        );

        String recipientEmail = normalizeEmail(
                userRepository.findEmailById(recipientId)
                        .orElseThrow(() -> new UserNotFoundException(AppConstants.SENDER_NOT_FOUND + recipientId))
        );

        messagingTemplate.convertAndSendToUser(senderEmail, AppConstants.WEBSOCKET_QUEUE_CHAT, dto);
        messagingTemplate.convertAndSendToUser(recipientEmail, AppConstants.WEBSOCKET_QUEUE_CHAT, dto);

        sendChatPushToRecipient(recipientId, senderId, content, conversationId);

        return dto;
    }


    @Transactional
    public MessageDto sendMessageAndBroadcastWithType(Long conversationId,
                                                      Long senderId,
                                                      String content,
                                                      String clientMessageId,
                                                      MessageType messageType) {

        Message saved = sendMessageWithType(
                conversationId,
                senderId,
                content,
                clientMessageId,
                messageType
        );

        MessageDto dto = toMessageDto(saved, conversationId);

        Conversation conv = saved.getConversation();
        Long user1Id      = conv.getUser1().getId();
        Long user2Id      = conv.getUser2().getId();
        Long recipientId  = senderId.equals(user1Id) ? user2Id : user1Id;

        String senderEmail = normalizeEmail(
                userRepository.findEmailById(senderId)
                        .orElseThrow(() -> new UserNotFoundException(AppConstants.SENDER_NOT_FOUND + senderId))
        );

        String recipientEmail = normalizeEmail(
                userRepository.findEmailById(recipientId)
                        .orElseThrow(() -> new UserNotFoundException(AppConstants.SENDER_NOT_FOUND + recipientId))
        );

        messagingTemplate.convertAndSendToUser(senderEmail, AppConstants.WEBSOCKET_QUEUE_CHAT, dto);
        messagingTemplate.convertAndSendToUser(recipientEmail, AppConstants.WEBSOCKET_QUEUE_CHAT, dto);

        sendChatPushToRecipient(recipientId, senderId, content, conversationId);

        return dto;
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase(Locale.ROOT);
    }

    private void sendChatPushToRecipient(Long recipientId, Long senderId, String content, Long conversationId) {
        User sender = userRepository.findById(senderId).orElse(null);
        String senderName = sender != null
                ? sender.getFirstName() + " " + sender.getLastName()
                : "Mentorise";
        String senderAvatar = (sender != null && sender.getProfileImageUrl() != null)
                ? sender.getProfileImageUrl()
                : "";

        Map<String, Object> pushData = Map.of(
                "type", "NEW_MESSAGE",
                "screen", "chat",
                "conversationId", String.valueOf(conversationId),
                "otherUserId", String.valueOf(senderId),
                "otherUserName", senderName,
                "avatar", senderAvatar
        );

        expoPushNotificationService.sendToUser(recipientId, senderName, content, pushData);
    }
    // ===================== Helpers =====================

    private Conversation createConversationSafely(long u1, long u2) {
        try {
            User user1Ref = entityManager.getReference(User.class, u1);
            User user2Ref = entityManager.getReference(User.class, u2);

            Conversation c = Conversation.builder()
                    .user1(user1Ref)
                    .user2(user2Ref)
                    .build();

            Conversation saved = conversationRepository.save(c);

            createReadRow(saved, u1);
            createReadRow(saved, u2);

            return saved;
        } catch (DataIntegrityViolationException e) {
            Optional<Conversation> existing = conversationRepository.findByUser1_IdAndUser2_Id(u1, u2);
            if (existing.isPresent()) {
                return existing.get();
            }
            throw e;
        }
    }

    private void ensureReadRowExists(Conversation conversation, Long userId) {
        Optional<ConversationRead> existing =
                conversationReadRepository.findByConversation_IdAndUser_Id(conversation.getId(), userId);

        if (existing.isEmpty()) {
            createReadRow(conversation, userId);
        }
    }

    private void createReadRow(Conversation conversation, Long userId) {
        User userRef = entityManager.getReference(User.class, userId);

        ConversationRead cr = ConversationRead.builder()
                .conversation(conversation)
                .user(userRef)
                .lastReadAt(null)
                .unreadCount(0)
                .build();

        try {
            conversationReadRepository.save(cr);
        } catch (DataIntegrityViolationException ignored) {
            // אם נוצר במקביל, לא נורא
        }
    }

    private MessageDto toMessageDto(Message message, Long conversationId) {
        return MessageDto.builder()
                .id(message.getId())
                .conversationId(conversationId)
                .senderId(message.getSender().getId())
                .type(message.getType().name())
                .content(message.getContent())
                .sentAt(message.getSentAt())
                .clientMessageId(message.getClientMessageId())
                .build();
    }

    private static void requireIds(Long a, Long b) {
        Objects.requireNonNull(a, AppConstants.CONVERSATION_ID_REQUIRED);
        Objects.requireNonNull(b, AppConstants.USER_ID_REQUIRED);
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) return min;
        if (value > max) return max;
        return value;
    }

    private static Instant toInstant(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.toInstant(ZoneOffset.UTC);
    }

    @Transactional(readOnly = true)
    public String getOtherParticipantEmailForCurrentUser(Long conversationId) {
        Long meId = currentUser.getUserId();

        Conversation c = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.CONVERSATION_NOT_FOUND + conversationId));

        Long u1 = c.getUser1().getId();
        Long u2 = c.getUser2().getId();

        if (!meId.equals(u1) && !meId.equals(u2)) {
            throw new BadRequestException(AppConstants.USER_NOT_PARTICIPANT_OF_CONVERSATION);
        }

        return meId.equals(u1) ? c.getUser2().getEmail() : c.getUser1().getEmail();
    }


    @Transactional
    public MessageDto sendMessageForSender(Long conversationId, Long senderId, SendMessageRequest request) {
        Objects.requireNonNull(senderId, AppConstants.SENDER_ID_REQUIRED);

        Message saved = sendMessage(
                conversationId,
                senderId,
                request.getContent(),
                request.getClientMessageId()
        );

        return toMessageDto(saved, conversationId);
    }

    @Transactional(readOnly = true)
    public String getOtherParticipantEmail(Long conversationId, Long senderId) {
        Objects.requireNonNull(conversationId, AppConstants.CONVERSATION_ID_REQUIRED);
        Objects.requireNonNull(senderId, AppConstants.SENDER_ID_REQUIRED);

        Conversation c = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.CONVERSATION_NOT_FOUND + conversationId));

        Long u1 = c.getUser1().getId();
        Long u2 = c.getUser2().getId();

        if (!senderId.equals(u1) && !senderId.equals(u2)) {
            throw new BadRequestException(AppConstants.USER_NOT_PARTICIPANT_OF_CONVERSATION);
        }

        return senderId.equals(u1) ? c.getUser2().getEmail() : c.getUser1().getEmail();
    }
}