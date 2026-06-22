package com.example.mentorisebackend.service.admin;

import com.example.mentorisebackend.api.entity.*;

import com.example.mentorisebackend.dto.admin.TutorApplicationDetailDto;
import com.example.mentorisebackend.dto.admin.TutorApplicationRowDto;
import com.example.mentorisebackend.dto.admin.TutorApplicationScopeDto;
import com.example.mentorisebackend.enums.ApplicationType;
import com.example.mentorisebackend.enums.NotificationType;
import com.example.mentorisebackend.enums.Role;
import com.example.mentorisebackend.enums.Status;
import com.example.mentorisebackend.exception.BadRequestException;
import com.example.mentorisebackend.exception.ResourceNotFoundException;
import com.example.mentorisebackend.mapper.TutorApplicationDetailMapper;
import com.example.mentorisebackend.mapper.TutorApplicationRowMapper;
import com.example.mentorisebackend.repository.TutorApplicationRepository;
import com.example.mentorisebackend.repository.TutorRepository;
import com.example.mentorisebackend.repository.TutorScopeRepository;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.service.notification.NotificationService;
import com.example.mentorisebackend.service.tutor.TutorService;
import com.example.mentorisebackend.util.AppConstants;
import com.example.mentorisebackend.util.PageableUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class AdminTutorApplicationService {

    private final TutorApplicationRepository tutorApplicationRepository;
    private final UserRepository userRepository;
    private final TutorRepository tutorRepository;
    private final TutorScopeRepository tutorScopeRepository;
    private final TutorApplicationRowMapper mapper;
    private final TutorApplicationDetailMapper tutorApplicationDetailMapper;
    private final AdminUserService adminUserService;
    private final TutorService tutorService;
    private final NotificationService notificationService;


    @Transactional(readOnly = true)
    public Page<TutorApplicationRowDto> searchByNationalId(Status status,
                                                           String nationalId,
                                                           int page,
                                                           int size,
                                                           String sort) {
        Pageable pageable = PageableUtil.resolvePageable(page, size, sort, "createdAt");

        String normalized = (nationalId == null) ? null : nationalId.replaceAll("\\D", "");
        if (normalized != null && normalized.isBlank()) {
            normalized = null;
        }

        Page<TutorApplication> pageResult;

        if (normalized == null) {
            pageResult = (status != null)
                    ? tutorApplicationRepository.findByStatus(status, pageable)
                    : tutorApplicationRepository.findAll(pageable);
        } else {
            pageResult = (status != null)
                    ? tutorApplicationRepository.findByStatusAndUser_NationalIdStartsWith(status, normalized, pageable)
                    : tutorApplicationRepository.findByUser_NationalIdStartsWith(normalized, pageable);
        }

        return pageResult.map(mapper::toDto);
    }


    @Transactional(readOnly = true)
    public TutorApplicationDetailDto getTutorApplicationDetails(Long id) {
        TutorApplication application = tutorApplicationRepository.findWithScopesById(id)
                .orElseThrow(() -> new ResourceNotFoundException("id", AppConstants.APPLICATION_NOT_FOUND_HEBREW + id));

        TutorApplicationDetailDto dto = tutorApplicationDetailMapper.toDetailDto(application);

        // Enrich with scope diff for UPDATE applications
        ApplicationType appType = application.getApplicationType();
        if (appType == ApplicationType.UPDATE && application.getUser() != null) {
            Optional<Tutor> tutorOpt = tutorRepository.findByUser_Id(application.getUser().getId());
            if (tutorOpt.isPresent()) {
                List<TutorScope> currentScopeEntities =
                        tutorScopeRepository.findAllByTutor_Id(tutorOpt.get().getId());

                List<TutorApplicationScopeDto> currentDtos =
                        currentScopeEntities.stream().map(this::fromTutorScope).toList();

                List<TutorApplicationScopeDto> requestedDtos =
                        dto.getScopes() != null ? dto.getScopes() : List.of();

                Map<String, TutorApplicationScopeDto> currentByKey = currentDtos.stream()
                        .collect(Collectors.toMap(this::scopeKey, s -> s, (a, b) -> a));
                Map<String, TutorApplicationScopeDto> requestedByKey = requestedDtos.stream()
                        .collect(Collectors.toMap(this::scopeKey, s -> s, (a, b) -> a));

                dto.setCurrentScopes(currentDtos);
                dto.setAddedScopes(requestedDtos.stream()
                        .filter(s -> !currentByKey.containsKey(scopeKey(s)))
                        .toList());
                dto.setRemovedScopes(currentDtos.stream()
                        .filter(s -> !requestedByKey.containsKey(scopeKey(s)))
                        .toList());
                dto.setUnchangedScopes(requestedDtos.stream()
                        .filter(s -> currentByKey.containsKey(scopeKey(s)))
                        .toList());
            }
        }

        return dto;
    }


    @Transactional
    public void approveApplication(Long appId, String adminComment) {

        TutorApplication app = tutorApplicationRepository.findWithScopesById(appId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.APPLICATION_NOT_FOUND + appId));

        if (app.getStatus() != Status.PENDING) {
            throw new BadRequestException(AppConstants.ONLY_PENDING_APPLICATIONS_CAN_BE_PROCESSED);
        }

        List<TutorApplicationScope> requestedScopes = app.getScopes();
        if (requestedScopes == null || requestedScopes.isEmpty()) {
            throw new BadRequestException(AppConstants.AT_LEAST_ONE_SCOPE_REQUIRED);
        }

        User reviewer = adminUserService.getCurrentUserEntity();

        app.setStatus(Status.APPROVED);
        app.setAdminComment(adminComment);
        app.setReviewedBy(reviewer);
        app.setReviewedAt(LocalDateTime.now());

        User applicant = app.getUser();

        // Null-safe: rows inserted before the migration default to INITIAL
        ApplicationType appType = app.getApplicationType() != null
                ? app.getApplicationType()
                : ApplicationType.INITIAL;

        if (appType == ApplicationType.INITIAL) {
            // Promote user role, create Tutor profile, add scopes (additive)
            promoteUserToTutor(applicant);
            Tutor tutor = tutorService.ensureTutorProfile(applicant);
            tutorService.addScopesFromApprovedApplication(tutor, requestedScopes);

        } else {
            // UPDATE: tutor profile already exists — replace scopes entirely
            // Role and isActive remain unchanged
            Tutor tutor = tutorService.ensureTutorProfile(applicant);
            tutorScopeRepository.deleteAllByTutor_Id(tutor.getId());
            tutorService.addScopesFromApprovedApplication(tutor, requestedScopes);
        }

        tutorApplicationRepository.save(app);
        sendApprovedApplicationNotification(applicant, adminComment);
    }

    @Transactional
    public void denyApplication(Long appId, String adminComment) {
        if (adminComment == null || adminComment.isBlank()) {
            throw new IllegalArgumentException(AppConstants.REASON_REQUIRED_FOR_DENIAL);
        }

        TutorApplication app = tutorApplicationRepository.findById(appId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.APPLICATION_NOT_FOUND + appId));

        if (app.getStatus() != Status.PENDING) {
            throw new BadRequestException(AppConstants.ONLY_PENDING_APPLICATIONS_CAN_BE_PROCESSED);
        }
        User reviewer = adminUserService.getCurrentUserEntity();
        app.setStatus(Status.REJECTED);
        app.setAdminComment(adminComment);
        app.setReviewedBy(reviewer);
        app.setReviewedAt(LocalDateTime.now());

        // No scope changes on denial — existing tutor scopes are preserved
        tutorApplicationRepository.save(app);
        User applicant = app.getUser();
        sendRejectedApplicationNotification(applicant, adminComment);
    }


    // ─── Private helpers ───────────────────────────────────────────────────────

    private void promoteUserToTutor(User user) {
        if (user == null) return;
        if (user.getRole() != Role.TUTOR) {
            user.setRole(Role.TUTOR);
            userRepository.save(user);
            sendCongratsTutorNotification(user);
        }
    }

    /**
     * Stable identity key for scope diff comparison.
     * Uses TutorApplicationScopeDto (which has "year" not "yearNumber").
     */
    private String scopeKey(TutorApplicationScopeDto s) {
        if (s == null || s.getScopeType() == null) return "UNKNOWN";
        return switch (s.getScopeType()) {
            case "COURSE" -> "COURSE:" + s.getCourseId();
            case "YEAR"   -> "YEAR:" + s.getYear();
            case "MAJOR"  -> "MAJOR";
            default       -> "UNKNOWN:" + s.getScopeType();
        };
    }

    /** Maps an approved TutorScope entity to the shared TutorApplicationScopeDto shape. */
    private TutorApplicationScopeDto fromTutorScope(TutorScope ts) {
        Course course = ts.getCourse();
        TutorApplicationScopeDto dto = new TutorApplicationScopeDto();
        dto.setScopeType(ts.getScopeType() != null ? ts.getScopeType().name() : null);
        dto.setYear(ts.getYearNumber());
        dto.setCourseId(course != null ? course.getId() : null);
        dto.setCourseName(course != null ? course.getName() : null);
        dto.setCourseCode(course != null ? course.getCourseCode() : null);
        return dto;
    }

    private void sendApprovedApplicationNotification(User applicant, String adminComment) {
        String message = AppConstants.TUTOR_APPLICATION_APPROVED_MESSAGE;
        if (adminComment != null && !adminComment.isBlank()) {
            message += AppConstants.TUTOR_APPLICATION_APPROVED_WITH_COMMENT + adminComment;
        }

        notificationService.createNotification(
                applicant.getId(),
                NotificationType.TUTOR_APPLICATION_APPROVED,
                AppConstants.TUTOR_APPLICATION_APPROVED,
                message
        );
    }

    private void sendRejectedApplicationNotification(User applicant, String adminComment) {
        notificationService.createNotification(
                applicant.getId(),
                NotificationType.TUTOR_APPLICATION_REJECTED,
                AppConstants.TUTOR_APPLICATION_REJECTED,
                AppConstants.TUTOR_APPLICATION_REJECTED_MESSAGE + adminComment
        );
    }

    private void sendCongratsTutorNotification(User newTutor) {
        notificationService.createNotification(
                newTutor.getId(),
                NotificationType.USER_PROMOTED_TO_TUTOR,
                AppConstants.USER_PROMOTED_TO_TUTOR,
                AppConstants.USER_PROMOTED_TO_TUTOR_MESSAGE
        );
    }
}
