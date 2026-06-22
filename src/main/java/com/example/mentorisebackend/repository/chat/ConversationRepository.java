package com.example.mentorisebackend.repository.chat;



import com.example.mentorisebackend.api.entity.Conversation;
import com.example.mentorisebackend.repository.projection.chat.ConversationInboxItemProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversationRepository extends JpaRepository<Conversation, Long> {

    Optional<Conversation> findByUser1_IdAndUser2_Id(Long user1Id, Long user2Id);

    @Query(value = """
        SELECT
            c.id AS conversationId,
            CASE WHEN c.user1_id = :userId THEN c.user2_id ELSE c.user1_id END AS otherUserId,
            u.first_name AS otherFirstName,
            u.last_name AS otherLastName,
            u.profile_image_url AS otherProfileImageUrl,
            m.id AS lastMessageId,
            m.type AS lastMessageType,
            m.content AS lastMessageContent,
            c.last_message_at AS lastMessageAt,
            r.unread_count AS unreadCount
        FROM conversations c
        JOIN conversation_reads r
            ON r.conversation_id = c.id AND r.user_id = :userId
        LEFT JOIN messages m
            ON m.id = c.last_message_id
        JOIN users u
            ON u.id = CASE WHEN c.user1_id = :userId THEN c.user2_id ELSE c.user1_id END
        WHERE c.user1_id = :userId OR c.user2_id = :userId
        ORDER BY c.last_message_at DESC
        """, nativeQuery = true)
    List<ConversationInboxItemProjection> findInbox(@Param("userId") Long userId, Pageable pageable);
}