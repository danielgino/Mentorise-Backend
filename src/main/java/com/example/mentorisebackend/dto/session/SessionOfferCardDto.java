package com.example.mentorisebackend.dto.session;

import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record SessionOfferCardDto(
        Long id,
        Long tutorUserId,
        Long studentUserId,
        String tutorFullName,
        String tutorProfileImageUrl,
        String studentFullName,
        String studentProfileImageUrl,
        LocalDateTime startTime,
        LocalDateTime endTime,
        BigDecimal price,
        String note,
        String status,
        LocalDateTime expiresAt
) {
}