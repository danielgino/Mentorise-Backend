package com.example.mentorisebackend.service.scheduler;

import com.example.mentorisebackend.api.entity.SessionOffer;
import com.example.mentorisebackend.repository.SessionOfferRepository;
import com.example.mentorisebackend.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class LessonReminderScheduler {

    private static final ZoneId    ISRAEL_ZONE       = ZoneId.of("Asia/Jerusalem");
    private static final LocalTime DAY_BEFORE_CUTOFF = LocalTime.of(20, 0);
    private static final LocalTime SAME_DAY_CUTOFF   = LocalTime.of(8, 0);

    private final SessionOfferRepository sessionOfferRepository;
    private final NotificationService    notificationService;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void sendLessonReminders() {
        LocalDateTime now    = LocalDateTime.now(ISRAEL_ZONE);
        LocalDate     today  = now.toLocalDate();
        LocalTime     time   = now.toLocalTime();
        // Covers all of today's remaining lessons + all of tomorrow's lessons
        LocalDateTime cutoff = today.plusDays(2).atStartOfDay();

        List<SessionOffer> sessions =
                sessionOfferRepository.findSessionsNeedingReminders(now, cutoff);

        for (SessionOffer offer : sessions) {
            processReminders(offer, now, today, time);
        }
    }

    private void processReminders(SessionOffer offer,
                                  LocalDateTime now,
                                  LocalDate today,
                                  LocalTime time) {
        boolean dirty     = false;
        LocalDateTime start     = offer.getStartTime();
        LocalDate     lessonDay = start.toLocalDate();

        // ── Day-before: at 20:00 on the calendar day before the lesson ──────
        if (!offer.isReminderDayBeforeSent()
                && today.equals(lessonDay.minusDays(1))
                && !time.isBefore(DAY_BEFORE_CUTOFF)) {
            String t = formatTime(start);
            sendToBoth(offer,
                    "תזכורת לשיעור מחר",
                    "תזכורת: יש לך שיעור מחר בשעה " + t);
            offer.setReminderDayBeforeSent(true);
            dirty = true;
        }

        // ── Same-day: at 08:00, only if lesson is still >2 h away ───────────
        if (!offer.isReminderSameDaySent()
                && today.equals(lessonDay)
                && !time.isBefore(SAME_DAY_CUTOFF)
                && now.plusHours(2).isBefore(start)) {
            String t = formatTime(start);
            sendToBoth(offer,
                    "תזכורת לשיעור היום",
                    "תזכורת: יש לך שיעור היום בשעה " + t);
            offer.setReminderSameDaySent(true);
            dirty = true;
        }

        // ── 1-hour: window [startTime-65min, startTime-55min) ───────────────
        if (!offer.isReminderHourBeforeSent()
                && !now.isBefore(start.minusMinutes(65))
                &&  now.isBefore(start.minusMinutes(55))) {
            sendToBoth(offer,
                    "השיעור מתחיל בעוד שעה",
                    "תזכורת: השיעור שלך מתחיל בעוד שעה");
            offer.setReminderHourBeforeSent(true);
            dirty = true;
        }

        // ── 15-min: window [startTime-20min, startTime-10min) ───────────────
        if (!offer.isReminderFifteenMinSent()
                && !now.isBefore(start.minusMinutes(20))
                &&  now.isBefore(start.minusMinutes(10))) {
            sendToBoth(offer,
                    "השיעור מתחיל בעוד רבע שעה",
                    "תזכורת: השיעור שלך מתחיל בעוד רבע שעה");
            offer.setReminderFifteenMinSent(true);
            dirty = true;
        }

        if (dirty) {
            sessionOfferRepository.save(offer);
        }
    }

    private void sendToBoth(SessionOffer offer, String title, String message) {
        try {
            notificationService.sendReminderNotification(offer.getTutorUserId(), title, message);
        } catch (Exception e) {
            log.warn("Reminder failed for tutor {}: {}", offer.getTutorUserId(), e.getMessage());
        }
        try {
            notificationService.sendReminderNotification(offer.getStudentUserId(), title, message);
        } catch (Exception e) {
            log.warn("Reminder failed for student {}: {}", offer.getStudentUserId(), e.getMessage());
        }
    }

    private String formatTime(LocalDateTime dt) {
        return String.format("%02d:%02d", dt.getHour(), dt.getMinute());
    }
}
