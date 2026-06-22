package com.example.mentorisebackend.api.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "conversations",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_conversations_user_pair", columnNames = {"user1_id", "user2_id"})
        },
        indexes = {
                @Index(name = "idx_conversations_user1_last", columnList = "user1_id,last_message_at"),
                @Index(name = "idx_conversations_user2_last", columnList = "user2_id,last_message_at")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Conversation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // תמיד לשמור: user1_id = min(a,b), user2_id = max(a,b)
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user1_id", nullable = false)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user2_id", nullable = false)
    private User user2;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "last_message_at")
    private Instant lastMessageAt;

    // לא FK בכוונה (פשוט מצביע להודעה אחרונה לצורך preview)
    @Column(name = "last_message_id")
    private Long lastMessageId;
}