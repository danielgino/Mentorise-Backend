package com.example.mentorisebackend.service.tutor;

import com.example.mentorisebackend.api.entity.*;
import com.example.mentorisebackend.dto.tutor.MyTutorProfileResponse;
import com.example.mentorisebackend.dto.tutor.TutorProfileDto;
import com.example.mentorisebackend.dto.tutor.TutorScopeDto;
import com.example.mentorisebackend.dto.tutor.UpdateTutorProfileRequest;
import com.example.mentorisebackend.enums.ScopeType;
import com.example.mentorisebackend.exception.ResourceNotFoundException;
import com.example.mentorisebackend.exception.UserNotFoundException;
import com.example.mentorisebackend.repository.TutorRepository;
import com.example.mentorisebackend.repository.TutorScopeRepository;
import com.example.mentorisebackend.repository.TutorSwipeDetailsRepository;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.repository.projection.tutor.CourseNameRow;
import com.example.mentorisebackend.repository.projection.tutor.TutorMajorRow;
import com.example.mentorisebackend.repository.projection.tutor.TutorScopeRow;
import com.example.mentorisebackend.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class TutorService {

    private final TutorRepository tutorRepository;
    private final TutorScopeRepository tutorScopeRepository;
    private final UserRepository userRepository;
    private final TutorSwipeDetailsRepository detailsRepository;

    /**
     * יוצר Tutor profile אם לא קיים (אחד-לאחד עם User).
     * לא נוגע ב-role — את זה אתה עושה ב-Admin approve.
     */
    @Transactional
    public Tutor ensureTutorProfile(User user) {
        return tutorRepository.findByUser_Id(user.getId())
                .orElseGet(() -> tutorRepository.save(
                        Tutor.builder()
                                .user(user)
                                .isActive(true)
                                .totalReviews(0)
                                .build()
                ));
    }


    public TutorProfileDto getTutorProfile(Long userId) {
        User u = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND));

        Tutor t = tutorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND));

        if (!t.isActive()) {
            throw new ResourceNotFoundException(AppConstants.TUTOR_NOT_FOUND_OR_INACTIVE);
        }

        String majorName = "";
        List<TutorMajorRow> majorRows = detailsRepository.findMajorsByTutorIds(List.of(userId));
        if (!majorRows.isEmpty() && majorRows.get(0).getMajorName() != null) {
            majorName = majorRows.get(0).getMajorName();
        }

        List<TutorScopeRow> scopeRows = detailsRepository.findScopesByTutorIds(List.of(userId));

        List<Integer> years = scopeRows.stream()
                .filter(r -> "YEAR".equals(r.getScopeType()) && r.getYearNumber() != null)
                .map(TutorScopeRow::getYearNumber)
                .distinct()
                .sorted()
                .toList();

        Set<Long> courseIds = scopeRows.stream()
                .filter(r -> "COURSE".equals(r.getScopeType()) && r.getCourseId() != null)
                .map(TutorScopeRow::getCourseId)
                .collect(java.util.stream.Collectors.toSet());

        List<String> courses = List.of();
        if (!courseIds.isEmpty()) {
            List<CourseNameRow> courseRows = detailsRepository.findCourseNamesByCourseIds(new ArrayList<>(courseIds));
            courses = courseRows.stream()
                    .map(CourseNameRow::getCourseName)
                    .filter(Objects::nonNull)
                    .distinct()
                    .sorted()
                    .toList();
        }

        return new TutorProfileDto(
                u.getId(),
                u.getFullName(),
                majorName,
                u.getProfileImageUrl(),
                years,
                courses,
                u.isAlumni(),
                t.getBio(),
                t.getTutorImageUrl(),
                t.getRatingAvg(),
                t.getTotalReviews(),
                t.getHourlyRate(),
                t.getCreatedAt()
        );
    }

    @Transactional
    public void deactivateTutorForUser(Long userId) {
        tutorRepository.findByUser_Id(userId).ifPresent(tutor -> {
            tutorScopeRepository.deleteAllByTutor_Id(tutor.getId());
            tutor.setActive(false);
            tutorRepository.save(tutor);
        });
    }

    /**
     * מוסיף scopes למתרגל מתוך בקשה מאושרת.
     * לא מוחק scopes קיימים, רק מוסיף מה שחסר (נמנע מכפילויות).
     */
    @Transactional
    public void addScopesFromApprovedApplication(Tutor tutor, List<TutorApplicationScope> appScopes) {
        if (appScopes == null || appScopes.isEmpty()) return;

        for (TutorApplicationScope s : appScopes) {
            if (s == null || s.getScopeType() == null) continue;

            if (s.getScopeType() == ScopeType.YEAR) {
                Integer year = s.getYearNumber();
                if (year == null) continue;

                boolean exists = tutorScopeRepository
                        .existsByTutor_IdAndScopeTypeAndYearNumber(tutor.getId(), ScopeType.YEAR, year);

                if (!exists) {
                    TutorScope ts = TutorScope.builder()
                            .tutor(tutor)
                            .scopeType(ScopeType.YEAR)
                            .yearNumber(year)
                            .course(null)
                            .build();
                    tutorScopeRepository.save(ts);
                }

            } else if (s.getScopeType() == ScopeType.COURSE) {
                // אם אצלך זה שדה Course ב-ApplicationScope:
                Course course = s.getCourse();
                if (course == null || course.getId() == null) continue;

                boolean exists = tutorScopeRepository
                        .existsByTutor_IdAndScopeTypeAndCourse_Id(tutor.getId(), ScopeType.COURSE, course.getId());

                if (!exists) {
                    TutorScope ts = TutorScope.builder()
                            .tutor(tutor)
                            .scopeType(ScopeType.COURSE)
                            .yearNumber(null)
                            .course(course)
                            .build();
                    tutorScopeRepository.save(ts);
                }
            }
        }
    }


    /**
     * עדכון פרופיל מתרגל (MVP).
     * אפשר להרחיב אח"כ.
     */
    @Transactional
    public TutorProfileDto updateProfileByEmail(Long userId, UpdateTutorProfileRequest request) {
        Tutor tutor = tutorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND));

        if (request.getBio() != null) {
            tutor.setBio(request.getBio().trim());
        }

        if (request.getTutorImageUrl() != null) {
            tutor.setTutorImageUrl(request.getTutorImageUrl().trim());
        }

        if (request.getTutorImagePublicId() != null) {
            tutor.setTutorImagePublicId(request.getTutorImagePublicId().trim());
        }

        if (request.getHourlyRate() != null) {
            tutor.setHourlyRate(request.getHourlyRate());
        }
        tutorRepository.save(tutor);
        return getTutorProfile(userId);
    }

    public MyTutorProfileResponse getMyTutorProfile(Long userId) {
        Tutor tutor = tutorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found"));

        return MyTutorProfileResponse.builder()
                .bio(tutor.getBio())
                .tutorImageUrl(tutor.getTutorImageUrl())
                .tutorImagePublicId(tutor.getTutorImagePublicId())
                .build();
    }

    @Transactional(readOnly = true)
    public List<TutorScopeDto> getMyApprovedScopes(Long userId) {
        Tutor tutor = tutorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Tutor profile not found"));

        List<TutorScope> scopes = tutorScopeRepository.findAllByTutor_Id(tutor.getId());

        return scopes.stream().map(s -> {
            Course course = s.getCourse();
            return TutorScopeDto.builder()
                    .scopeType(s.getScopeType() != null ? s.getScopeType().name() : null)
                    .courseId(course != null ? course.getId() : null)
                    .courseName(course != null ? course.getName() : null)
                    .yearNumber(s.getYearNumber())
                    .build();
        }).toList();
    }
}
