package com.example.mentorisebackend.api.entity;


import com.example.mentorisebackend.enums.SessionOfferStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
        name = "session_offers",
        indexes = @Index(name = "idx_session_offers_status_start_time", columnList = "status, start_time")
)
@Getter
@Setter
public class SessionOffer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "tutor_user_id", nullable = false)
    private Long tutorUserId;

    @Column(name = "student_user_id", nullable = false)
    private Long studentUserId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "note", length = 300)
    private String note;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SessionOfferStatus status = SessionOfferStatus.PENDING;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "reminder_day_before_sent", nullable = false, columnDefinition = "boolean default false")
    private boolean reminderDayBeforeSent = false;

    @Column(name = "reminder_same_day_sent", nullable = false, columnDefinition = "boolean default false")
    private boolean reminderSameDaySent = false;

    @Column(name = "reminder_hour_before_sent", nullable = false, columnDefinition = "boolean default false")
    private boolean reminderHourBeforeSent = false;

    @Column(name = "reminder_fifteen_min_sent", nullable = false, columnDefinition = "boolean default false")
    private boolean reminderFifteenMinSent = false;
}