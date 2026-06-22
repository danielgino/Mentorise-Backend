package com.example.mentorisebackend.repository.chat;


import com.example.mentorisebackend.api.entity.ConversationRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.Optional;

public interface ConversationReadRepository extends JpaRepository<ConversationRead, Long> {

    Optional<ConversationRead> findByConversation_IdAndUser_Id(Long conversationId, Long userId);

    @Modifying
    @Query("""
        UPDATE ConversationRead cr
        SET cr.unreadCount = cr.unreadCount + 1, cr.updatedAt = CURRENT_TIMESTAMP
        WHERE cr.conversation.id = :conversationId AND cr.user.id = :userId
        """)
    int incrementUnread(@Param("conversationId") Long conversationId, @Param("userId") Long userId);

    @Modifying
    @Query("""
        UPDATE ConversationRead cr
        SET cr.lastReadAt = :now, cr.unreadCount = 0, cr.updatedAt = CURRENT_TIMESTAMP
        WHERE cr.conversation.id = :conversationId AND cr.user.id = :userId
        """)
    int markRead(@Param("conversationId") Long conversationId,
                 @Param("userId") Long userId,
                 @Param("now") Instant now);
}