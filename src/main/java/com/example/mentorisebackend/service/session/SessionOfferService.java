package com.example.mentorisebackend.service.session;

import com.example.mentorisebackend.api.entity.Conversation;
import com.example.mentorisebackend.api.entity.SessionOffer;
import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.dto.session.SessionOfferCardDto;
import com.example.mentorisebackend.dto.session.CreateSessionOfferRequest;
import com.example.mentorisebackend.enums.MessageType;
import com.example.mentorisebackend.enums.SessionOfferStatus;
import com.example.mentorisebackend.exception.BadRequestException;
import com.example.mentorisebackend.exception.ResourceNotFoundException;
import com.example.mentorisebackend.exception.UserNotFoundException;
import com.example.mentorisebackend.repository.SessionOfferRepository;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.service.chat.ChatService;
import com.example.mentorisebackend.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class SessionOfferService {

    private final SessionOfferRepository sessionOfferRepository;
    private final UserRepository userRepository;
    private final ChatService chatService;

    @Transactional
    public SessionOffer createOffer(Long tutorId, CreateSessionOfferRequest request) {
        if (request.getStartTime() == null || request.getEndTime() == null) {
            throw new BadRequestException(AppConstants.START_AND_END_TIME_REQUIRED);
        }

        if (request.getStudentUserId() == null) {
            throw new BadRequestException(AppConstants.STUDENT_USER_ID_REQUIRED);
        }

        if (!request.getEndTime().isAfter(request.getStartTime())) {
            throw new BadRequestException(AppConstants.END_TIME_MUST_BE_AFTER_START);
        }

        if (!request.getStartTime().isAfter(LocalDateTime.now())) {
            throw new BadRequestException(AppConstants.CANNOT_CREATE_OFFER_FOR_PAST);
        }

        if (tutorId.equals(request.getStudentUserId())) {
            throw new BadRequestException(AppConstants.CANNOT_SEND_OFFER_TO_YOURSELF);
        }

        if (!userRepository.existsById(request.getStudentUserId())) {
            throw new UserNotFoundException(AppConstants.USER_NOT_FOUND);
        }

        boolean tutorHasConflict = sessionOfferRepository
                .existsByTutorUserIdAndStatusInAndStartTimeLessThanAndEndTimeGreaterThan(
                        tutorId,
                        EnumSet.of(
                                SessionOfferStatus.PENDING,
                                SessionOfferStatus.PENDING_PAYMENT,
                                SessionOfferStatus.ACCEPTED
                        ),
                        request.getEndTime(),
                        request.getStartTime()
                );

        if (tutorHasConflict) {
            throw new BadRequestException(AppConstants.TUTOR_HAS_CONFLICTING_OFFER);
        }

        long durationMinutes = Duration.between(request.getStartTime(), request.getEndTime()).toMinutes();
        if (durationMinutes <= 0 || durationMinutes % 45 != 0) {
            throw new BadRequestException(AppConstants.LESSON_DURATION_MUST_BE_45_MINUTES);
        }

        BigDecimal totalPrice = BigDecimal.valueOf(AppConstants.ACADEMIC_HOUR_PRICE).multiply(BigDecimal.valueOf(durationMinutes / 45));

        SessionOffer offer = new SessionOffer();
        offer.setTutorUserId(tutorId);
        offer.setStudentUserId(request.getStudentUserId());
        offer.setStartTime(request.getStartTime());
        offer.setEndTime(request.getEndTime());
        offer.setPrice(totalPrice);
        offer.setNote(request.getNote());
        offer.setStatus(SessionOfferStatus.PENDING);
        offer.setExpiresAt(LocalDateTime.now().plusHours(AppConstants.OFFER_VALIDITY_HOURS));

        SessionOffer savedOffer = sessionOfferRepository.save(offer);

        sendOfferNoticeToChat(savedOffer);

        return savedOffer;
    }

    @Transactional(readOnly = true)
    public List<SessionOfferCardDto> getMyPendingOffers(Long userId) {
        LocalDateTime now = LocalDateTime.now();

        List<SessionOffer> offers = new ArrayList<>();
        offers.addAll(sessionOfferRepository.findByStudentUserIdAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
                userId,
                SessionOfferStatus.PENDING,
                now
        ));
        offers.addAll(sessionOfferRepository.findByTutorUserIdAndStatusAndExpiresAtAfterOrderByCreatedAtDesc(
                userId,
                SessionOfferStatus.PENDING,
                now
        ));

        offers.sort(Comparator.comparing(SessionOffer::getCreatedAt).reversed());

        return mapToCardDtos(offers);
    }

    @Transactional(readOnly = true)
    public List<SessionOfferCardDto> getMyUpcomingLessons(Long userId) {
        LocalDateTime now = LocalDateTime.now();

        List<SessionOffer> offers = new ArrayList<>();
        offers.addAll(sessionOfferRepository.findByStudentUserIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(
                userId,
                SessionOfferStatus.ACCEPTED,
                now
        ));
        offers.addAll(sessionOfferRepository.findByTutorUserIdAndStatusAndStartTimeAfterOrderByStartTimeAsc(
                userId,
                SessionOfferStatus.ACCEPTED,
                now
        ));

        offers.sort(Comparator.comparing(SessionOffer::getStartTime));

        return mapToCardDtos(offers);
    }

    @Transactional(readOnly = true)
    public List<SessionOfferCardDto> getMyCompletedLessons(Long userId) {
        LocalDateTime now = LocalDateTime.now();

        List<SessionOffer> offers = new ArrayList<>();
        offers.addAll(sessionOfferRepository.findByStudentUserIdAndStatusAndEndTimeBeforeOrderByStartTimeDesc(
                userId,
                SessionOfferStatus.ACCEPTED,
                now
        ));
        offers.addAll(sessionOfferRepository.findByTutorUserIdAndStatusAndEndTimeBeforeOrderByStartTimeDesc(
                userId,
                SessionOfferStatus.ACCEPTED,
                now
        ));

        offers.sort(Comparator.comparing(SessionOffer::getStartTime).reversed());

        return mapToCardDtos(offers);
    }

    @Transactional
    public void declineOfferForCurrentUser(Long userId, Long offerId) {
        SessionOffer offer = sessionOfferRepository.findById(offerId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.OFFER_NOT_FOUND));

        if (!Objects.equals(offer.getStudentUserId(), userId)) {
            throw new  AccessDeniedException(AppConstants.ONLY_STUDENT_CAN_DECLINE);
        }

        if (offer.getStatus() != SessionOfferStatus.PENDING) {
            throw new  BadRequestException(AppConstants.CAN_ONLY_DECLINE_PENDING_OFFER);
        }

        if (isExpired(offer)) {
            offer.setStatus(SessionOfferStatus.EXPIRED);
            sessionOfferRepository.save(offer);
            throw new  BadRequestException(AppConstants.OFFER_EXPIRED_CANNOT_DECLINE);
        }

        offer.setStatus(SessionOfferStatus.DECLINED);
        sessionOfferRepository.save(offer);
    }


    private boolean isExpired(SessionOffer offer) {
        return offer.getExpiresAt() != null && offer.getExpiresAt().isBefore(LocalDateTime.now());
    }

    private List<SessionOfferCardDto> mapToCardDtos(List<SessionOffer> offers) {
        if (offers.isEmpty()) {
            return List.of();
        }

        Set<Long> userIds = new HashSet<>();
        for (SessionOffer offer : offers) {
            userIds.add(offer.getTutorUserId());
            userIds.add(offer.getStudentUserId());
        }

        Map<Long, User> usersById = userRepository.findAllById(userIds)
                .stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        return offers.stream()
                .map(offer -> {
                    User tutor = usersById.get(offer.getTutorUserId());
                    User student = usersById.get(offer.getStudentUserId());
                    if (tutor == null || student == null) {
                        throw new ResourceNotFoundException(AppConstants.MISSING_USER_PARTICIPANT);
                    }
                    return SessionOfferCardDto.builder()
                            .id(offer.getId())
                            .tutorUserId(offer.getTutorUserId())
                            .studentUserId(offer.getStudentUserId())
                            .tutorFullName(tutor.getFullName())
                            .tutorProfileImageUrl(tutor != null ? tutor.getProfileImageUrl() : null)
                            .studentFullName(student.getFullName())
                            .studentProfileImageUrl(student != null ? student.getProfileImageUrl() : null)
                            .startTime(offer.getStartTime())
                            .endTime(offer.getEndTime())
                            .price(offer.getPrice())
                            .note(offer.getNote())
                            .status(offer.getStatus().name())
                            .expiresAt(offer.getExpiresAt())
                            .build();
                })
                .toList();
    }


    private void sendOfferNoticeToChat(SessionOffer offer) {
        Conversation conversation = chatService.getOrCreateConversation(
                offer.getTutorUserId(),
                offer.getStudentUserId()
        );

        DateTimeFormatter dateFormatter =
                DateTimeFormatter.ofPattern(AppConstants.DATE_FORMAT_HEBREW, new Locale(AppConstants.HEBREW_LANGUAGE, AppConstants.HEBREW_COUNTRY));
        DateTimeFormatter timeFormatter =
                DateTimeFormatter.ofPattern(AppConstants.TIME_FORMAT_HEBREW, new Locale(AppConstants.HEBREW_LANGUAGE, AppConstants.HEBREW_COUNTRY));

        String formattedDate = offer.getStartTime().format(dateFormatter);
        String startTime = offer.getStartTime().format(timeFormatter);
        String endTime = offer.getEndTime().format(timeFormatter);

        String content = AppConstants.SESSION_OFFER_SENT + "\n" +
                AppConstants.SESSION_OFFER_DATE + formattedDate + "\n" +
                AppConstants.SESSION_OFFER_TIME + startTime + AppConstants.SESSION_OFFER_SEPARATOR + endTime + "\n" +
                AppConstants.SESSION_OFFER_VIEW_DETAILS;

        String clientMessageId = UUID.randomUUID().toString();

        chatService.sendMessageAndBroadcastWithType(
                conversation.getId(),
                offer.getTutorUserId(),
                content,
                clientMessageId,
                MessageType.SESSION_OFFER
        );
    }
}