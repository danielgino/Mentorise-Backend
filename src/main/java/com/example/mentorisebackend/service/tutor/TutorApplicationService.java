package com.example.mentorisebackend.service.tutor;

import com.example.mentorisebackend.api.entity.Course;
import com.example.mentorisebackend.api.entity.TutorApplication;
import com.example.mentorisebackend.api.entity.TutorApplicationScope;
import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.dto.admin.TutorApplicationDetailDto;
import com.example.mentorisebackend.dto.tutor.TutorApplicationCreateRequest;
import com.example.mentorisebackend.dto.tutor.TutorApplicationScopeCreate;
import com.example.mentorisebackend.dto.tutor.TutorApplicationStatusDto;
import com.example.mentorisebackend.dto.tutor.TutorScopeDto;
import com.example.mentorisebackend.enums.ApplicationType;
import com.example.mentorisebackend.enums.ScopeType;
import com.example.mentorisebackend.enums.Status;
import com.example.mentorisebackend.exception.BadRequestException;
import com.example.mentorisebackend.exception.ConflictException;
import com.example.mentorisebackend.exception.UserNotFoundException;
import com.example.mentorisebackend.mapper.TutorApplicationDetailMapper;
import com.example.mentorisebackend.repository.CourseRepository;
import com.example.mentorisebackend.repository.TutorApplicationRepository;
import com.example.mentorisebackend.repository.TutorRepository;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class TutorApplicationService {

    private final TutorApplicationRepository tutorApplicationRepository;
    private final UserRepository userRepository;
    private final TutorApplicationDetailMapper tutorApplicationDetailMapper;
    private final CourseRepository courseRepository;
    private final TutorRepository tutorRepository;

    @Transactional
    public TutorApplicationDetailDto create(Long userId, TutorApplicationCreateRequest req) {
        if (req == null) {
            throw new BadRequestException(AppConstants.REQUEST_EMPTY);
        }

        if (req.getTranscriptUrl() == null || req.getTranscriptUrl().isBlank()) {
            throw new BadRequestException(AppConstants.TRANSCRIPT_URL_REQUIRED);
        }

        List<TutorApplicationScopeCreate> scopeRequests = req.getScopes();
        if (scopeRequests == null || scopeRequests.isEmpty()) {
            throw new BadRequestException(AppConstants.AT_LEAST_ONE_SCOPE_REQUIRED);
        }

        boolean hasAllMajor = scopeRequests.stream()
                .anyMatch(s -> s.getScopeType() == ScopeType.MAJOR);
        if (hasAllMajor && scopeRequests.size() > 1) {
            throw new BadRequestException(AppConstants.CANNOT_MIX_MAJOR_WITH_OTHERS);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND));

        // Prevent duplicate PENDING applications
        if (tutorApplicationRepository.existsByUserAndStatus(user, Status.PENDING)) {
            throw new ConflictException(AppConstants.TUTOR_APPLICATION_ALREADY_PENDING);
        }

        // Backend determines type — never trust frontend
        ApplicationType appType = tutorRepository.existsByUser_IdAndIsActiveTrue(userId)
                ? ApplicationType.UPDATE
                : ApplicationType.INITIAL;

        TutorApplication app = new TutorApplication();
        app.setUser(user);
        app.setStatus(Status.PENDING);
        app.setRequestText(req.getRequestText());
        app.setTranscriptUrl(req.getTranscriptUrl());
        app.setApplicationType(appType);

        // Build scope list in memory (no per-scope saves)
        List<TutorApplicationScope> scopes = new ArrayList<>();

        if (hasAllMajor) {
            TutorApplicationScope scope = new TutorApplicationScope();
            scope.setApplication(app);
            scope.setScopeType(ScopeType.MAJOR);
            scopes.add(scope);
        } else {
            Set<Long> courseIds = new HashSet<>();
            Set<Integer> years = new HashSet<>();

            for (TutorApplicationScopeCreate s : scopeRequests) {
                ScopeType type = s.getScopeType();
                if (type == ScopeType.COURSE) {
                    Long courseId = s.getCourseId();
                    if (courseId == null || !courseIds.add(courseId)) continue;
                } else if (type == ScopeType.YEAR) {
                    Integer y = s.getYearNumber();
                    if (y == null || y < 1 || y > 6 || !years.add(y)) continue;
                }
            }

            // Batch-fetch all needed courses (avoids N+1)
            if (!courseIds.isEmpty()) {
                List<Course> courses = courseRepository.findAllById(courseIds);
                for (Course course : courses) {
                    TutorApplicationScope scope = new TutorApplicationScope();
                    scope.setApplication(app);
                    scope.setScopeType(ScopeType.COURSE);
                    scope.setCourse(course);
                    scopes.add(scope);
                }
            }

            for (Integer y : years) {
                TutorApplicationScope scope = new TutorApplicationScope();
                scope.setApplication(app);
                scope.setScopeType(ScopeType.YEAR);
                scope.setYearNumber(y);
                scopes.add(scope);
            }
        }

        app.setScopes(scopes);

        tutorApplicationRepository.save(app);

        return tutorApplicationDetailMapper.toDetailDto(app);
    }

    @Transactional(readOnly = true)
    public TutorApplicationStatusDto getMyPendingApplication(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND));

        Optional<TutorApplication> pendingOpt =
                tutorApplicationRepository.findFirstByUserAndStatus(user, Status.PENDING);

        if (pendingOpt.isPresent()) {
            // Use the scopes-eager fetch to avoid lazy-load issues on course names
            TutorApplication app = tutorApplicationRepository
                    .findWithScopesById(pendingOpt.get().getId())
                    .orElse(pendingOpt.get());

            List<TutorScopeDto> scopeDtos = app.getScopes() == null ? List.of() :
                    app.getScopes().stream().map(s -> {
                        Course course = s.getCourse();
                        return TutorScopeDto.builder()
                                .scopeType(s.getScopeType() != null ? s.getScopeType().name() : null)
                                .courseId(course != null ? course.getId() : null)
                                .courseName(course != null ? course.getName() : null)
                                .yearNumber(s.getYearNumber())
                                .build();
                    }).toList();

            return TutorApplicationStatusDto.builder()
                    .hasPending(true)
                    .applicationId(app.getId())
                    .applicationType(app.getApplicationType())
                    .status(app.getStatus())
                    .createdAt(app.getCreatedAt())
                    .scopes(scopeDtos)
                    .build();
        }

        // No pending — check if the most recent application was rejected
        boolean lastRejected = tutorApplicationRepository
                .findFirstByUserOrderByCreatedAtDesc(user)
                .map(a -> a.getStatus() == Status.REJECTED)
                .orElse(false);

        return TutorApplicationStatusDto.builder()
                .hasPending(false)
                .lastRejected(lastRejected)
                .build();
    }
}
