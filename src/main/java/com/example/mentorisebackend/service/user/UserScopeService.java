package com.example.mentorisebackend.service.user;

import com.example.mentorisebackend.api.entity.Course;
import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.api.entity.UserScope;
import com.example.mentorisebackend.dto.user.LearningPreferencesDto;
import com.example.mentorisebackend.dto.user.LearningPreferencesViewDto;
import com.example.mentorisebackend.enums.ScopeType;
import com.example.mentorisebackend.exception.BadRequestException;
import com.example.mentorisebackend.exception.ResourceNotFoundException;
import com.example.mentorisebackend.exception.UserNotFoundException;
import com.example.mentorisebackend.repository.CourseRepository;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.repository.UserScopeRepository;
import com.example.mentorisebackend.repository.projection.tutor.CourseNameRow;
import com.example.mentorisebackend.util.AppConstants;
import com.example.mentorisebackend.util.HebrewUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.*;


@Service
@RequiredArgsConstructor
public class UserScopeService {

    private final UserScopeRepository userScopeRepository;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    @Transactional
    @CacheEvict(value = "userScopes", key = "#userId")
    public void upsert(Long userId, LearningPreferencesDto dto) {
        if (dto.getScopeType() == null) {
                throw new BadRequestException(AppConstants.SCOPE_TYPE_REQUIRED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND));

        if (user.getMajor() == null) {
            throw new BadRequestException(AppConstants.USER_MUST_HAVE_MAJOR);
        }

        ScopeType mode = dto.getScopeType();

        // תמיד מאפסים ואז מכניסים מחדש (מונע מצבים מעורבים)
        userScopeRepository.deleteByUserId(userId);
        userScopeRepository.flush();

        if (mode == ScopeType.MAJOR) {
            // MAJOR = אין scopes בכלל
            return;
        }

        if (mode == ScopeType.YEAR) {
            saveYears(user, dto);
            return;
        }

        if (mode == ScopeType.COURSE) {
            saveCourses(user, dto);
            return;
        }

        throw new BadRequestException(AppConstants.UNSUPPORTED_SCOPE_TYPE + mode);
    }


    @Transactional
    public LearningPreferencesViewDto getUserScopesView(Long userId) {
        List<UserScope> scopes = userScopeRepository.findByUserId(userId);

        if (scopes.isEmpty()) {
            return LearningPreferencesViewDto.builder()
                    .scopeType(ScopeType.MAJOR)
                    .yearLabels(List.of())
                    .courseNames(List.of())
                    .build();
        }

        ScopeType mode = scopes.get(0).getScopeType();
        for (UserScope s : scopes) {
            if (s.getScopeType() != mode) {
                throw new BadRequestException(AppConstants.USER_HAS_MIXED_SCOPE_TYPES);
            }
        }

        if (mode == ScopeType.YEAR) {
            LinkedHashSet<Integer> yearsSet = new LinkedHashSet<>();
            for (UserScope s : scopes) {
                Integer y = s.getYearNumber();
                if (y != null) {
                    yearsSet.add(y);
                }
            }

            List<String> labels = new ArrayList<>();
            for (Integer y : yearsSet) {
                labels.add(HebrewUtils.getHebrewYearLabel(y));
            }

            return LearningPreferencesViewDto.builder()
                    .scopeType(ScopeType.YEAR)
                    .yearLabels(labels)
                    .courseNames(List.of())
                    .build();
        }

        if (mode == ScopeType.COURSE) {
            LinkedHashSet<Long> courseIdsSet = new LinkedHashSet<>();
            for (UserScope s : scopes) {
                Course c = s.getCourse();
                if (c != null && c.getId() != null) {
                    courseIdsSet.add(c.getId());
                }
            }

            List<Long> courseIds = new ArrayList<>(courseIdsSet);

            List<CourseNameRow> rows = courseIds.isEmpty()
                    ? List.of()
                    : courseRepository.findCourseNamesByIds(courseIds);

            Map<Long, String> nameById = new HashMap<>();
            for (CourseNameRow row : rows) {
                nameById.put(row.getCourseId(), row.getCourseName());
            }

            List<String> orderedNames = new ArrayList<>();
            for (Long id : courseIds) {
                String name = nameById.get(id);
                if (name != null) {
                    orderedNames.add(name);
                }
            }

            return LearningPreferencesViewDto.builder()
                    .scopeType(ScopeType.COURSE)
                    .yearLabels(List.of())
                    .courseNames(orderedNames)
                    .build();
        }

        // fallback
        return LearningPreferencesViewDto.builder()
                .scopeType(ScopeType.MAJOR)
                .yearLabels(List.of())
                .courseNames(List.of())
                .build();
    }

    // Removed: hebrewYearLabel() method - now using HebrewUtils.getHebrewYearLabel()
    /* ===== helpers ===== */

    private void saveYears(User user, LearningPreferencesDto dto) {
        List<Integer> years = dto.getYears();
        if (years == null || years.isEmpty()) {
            throw new BadRequestException(AppConstants.YEARS_REQUIRED_FOR_YEAR_SCOPE);
        }

        LinkedHashSet<Integer> unique = new LinkedHashSet<>();
        for (Integer y : years) {
            if (y == null || y < 1 || y > 6) {
                throw new BadRequestException(AppConstants.YEAR_OUT_OF_RANGE + y);
            }
            unique.add(y);
        }

        List<UserScope> batch = new ArrayList<>(unique.size());
        for (Integer y : unique) {
            UserScope s = new UserScope();
            s.setUser(user);
            s.setScopeType(ScopeType.YEAR);
            s.setYearNumber(y);
            s.setCourse(null);
            batch.add(s);
        }
        userScopeRepository.saveAll(batch);
    }

    private void saveCourses(User user, LearningPreferencesDto dto) {
        List<Long> ids = dto.getCourseIds();
        if (ids == null || ids.isEmpty()) {
            throw new BadRequestException(AppConstants.COURSE_IDS_REQUIRED_FOR_COURSE_SCOPE);
        }

        LinkedHashSet<Long> uniqueIds = new LinkedHashSet<>(ids);

        // עדיף למשוך בבת אחת (פחות שאילתות)
        List<Course> courses = courseRepository.findAllById(uniqueIds);
        if (courses.size() != uniqueIds.size()) {
            // למצוא מי חסר
            Set<Long> found = new HashSet<>();
            for (Course c : courses) found.add(c.getId());
            for (Long cid : uniqueIds) {
                if (!found.contains(cid)) {
                    throw new ResourceNotFoundException(AppConstants.COURSE_ID_NOT_FOUND + cid);
                }
            }
        }

        // לשמור סדר כמו שהגיע
        Map<Long, Course> map = new HashMap<>();
        for (Course c : courses) map.put(c.getId(), c);

        List<UserScope> batch = new ArrayList<>(uniqueIds.size());
        for (Long cid : uniqueIds) {
            UserScope s = new UserScope();
            s.setUser(user);
            s.setScopeType(ScopeType.COURSE);
            s.setYearNumber(null);
            s.setCourse(map.get(cid));
            batch.add(s);
        }
        userScopeRepository.saveAll(batch);
    }
}
