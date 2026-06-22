package com.example.mentorisebackend.api.entity;

import com.example.mentorisebackend.enums.MessageType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@Entity
@Table(
        name = "messages",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_messages_conv_client", columnNames = {"conversation_id", "client_message_id"})
        },
        indexes = {
                @Index(name = "idx_messages_conversation_id_id", columnList = "conversation_id,id"),
                @Index(name = "idx_messages_sender", columnList = "sender_id")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "conversation_id", nullable = false)
    private Conversation conversation;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private MessageType type;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    // זמן שרת
    @CreationTimestamp
    @Column(name = "sent_at", nullable = false, updatable = false)
    private Instant sentAt;

    // UUID מהלקוח (string) כדי למנוע כפילויות
    @Column(name = "client_message_id", nullable = false, length = 36)
    private String clientMessageId;
}