package com.example.mentorisebackend.repository;

import com.example.mentorisebackend.api.entity.SessionOffer;
import com.example.mentorisebackend.enums.SessionOfferStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SessionOfferRepository extends JpaRepository<SessionOffer, Long> {

    List<SessionOffer> findByTutorUserIdOrderByCreatedAtDesc(Long tutorUserId);

    List<SessionOffer> findByStudentUserIdOrderByCreatedAtDesc(Long studentUserId);

    List<SessionOffer> findByTutorUserIdAndStatusOrderByCreatedAtDesc(Long tutorUserId, SessionOfferStatus status);

    List<SessionOffer> findByStudentUserIdAndStatusOrderByCreatedAtDesc(Long studentUserId, SessionOfferStatus status);

    Optional<SessionOffer> findByIdAndTutorUserId(Long id, Long tutorUserId);

    Optional<SessionOffer> findByIdAndStudentUserId(Long id, Long studentUserId);

    long countByStatusAndEndTimeBefore(SessionOfferStatus status, LocalDateTime now);


    long countByStatusAndStartTimeGreaterThanEqualAndStartTimeLessThan(
            SessionOfferStatus status,
            LocalDateTime from,
            LocalDateTime to
    );

    @Query("""
           SELECT COALESCE(SUM(s.price), 0)
           FROM SessionOffer s
           WHERE s.status = :status
           """)
    BigDecimal sumPriceByStatus(@Param("status") SessionOfferStatus status);

    @Query("""
           SELECT COALESCE(SUM(s.price), 0)
           FROM SessionOffer s
           WHERE s.status = :status
             AND s.startTime >= :start
             AND s.startTime < :end
           """)
    BigDecimal sumPriceByStatusAndStartTimeGreaterThanEqualAndStartTimeLessThan(
            @Param("status") SessionOfferStatus status,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );




    boolean existsByTutorUserIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
            Long tutorUserId,
            Collection<SessionOfferStatus> statuses,
            LocalDateTime endTime,
            LocalDateTime startTime
    );

    // טאב "הצעות שיעור" לסטודנט
    List<SessionOffer> findByStudentUserIdAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
            Long studentUserId,
            SessionOfferStatus status,
            LocalDateTime now
    );

    // טאב "השיעורים שלי" - סטודנט
    List<SessionOffer> findByStudentUserIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(
            Long studentUserId,
            SessionOfferStatus status,
            LocalDateTime now
    );

    // טאב "השיעורים שלי" - מתרגל
    List<SessionOffer> findByTutorUserIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(
            Long tutorUserId,
            SessionOfferStatus status,
            LocalDateTime now
    );

    // טאב "שיעורים שהתקיימו" - סטודנט
    List<SessionOffer> findByStudentUserIdAndStatusAndEndTimeBeforeOrderByStartTimeDesc(
            Long studentUserId,
            SessionOfferStatus status,
            LocalDateTime now
    );

    // טאב "שיעורים שהתקיימו" - מתרגל
    List<SessionOffer> findByTutorUserIdAndStatusAndEndTimeBeforeOrderByStartTimeDesc(
            Long tutorUserId,
            SessionOfferStatus status,
            LocalDateTime now
    );

    // לצורך expire אוטומטי
    List<SessionOffer> findByStatusAndExpiresAtBefore(
            SessionOfferStatus status,
            LocalDateTime now
    );

    List<SessionOffer> findByTutorUserIdAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
            Long tutorUserId,
            SessionOfferStatus status,
            LocalDateTime now
    );

    @Query("""
            SELECT s FROM SessionOffer s
            WHERE s.status = com.example.mentorisebackend.enums.SessionOfferStatus.ACCEPTED
              AND s.startTime > :now
              AND s.startTime < :cutoff
              AND (s.reminderDayBeforeSent  = false
                OR s.reminderSameDaySent    = false
                OR s.reminderHourBeforeSent = false
                OR s.reminderFifteenMinSent = false)
            """)
    List<SessionOffer> findSessionsNeedingReminders(
            @Param("now")    LocalDateTime now,
            @Param("cutoff") LocalDateTime cutoff
    );
}