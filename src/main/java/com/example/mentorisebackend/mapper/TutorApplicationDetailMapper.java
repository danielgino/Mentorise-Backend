package com.example.mentorisebackend.mapper;

import com.example.mentorisebackend.api.entity.TutorApplication;
import com.example.mentorisebackend.api.entity.TutorApplicationScope;
import com.example.mentorisebackend.api.entity.Course;
import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.dto.admin.TutorApplicationDetailDto;
import com.example.mentorisebackend.dto.admin.TutorApplicationScopeDto;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TutorApplicationDetailMapper {

    public TutorApplicationDetailDto toDetailDto(TutorApplication app) {
        if (app == null) {
            return null;
        }

        User user = app.getUser();
        User reviewer = app.getReviewedBy();

        List<TutorApplicationScope> scopeEntities = app.getScopes();
        List<TutorApplicationScopeDto> scopeDtos =
                scopeEntities == null
                        ? Collections.emptyList()
                        : scopeEntities.stream()
                        .map(this::toScopeDto)
                        .collect(Collectors.toList());

        String majorName =
                (user != null && user.getMajor() != null) ? user.getMajor().getName() : null;
        Long majorId = (user != null && user.getMajor() != null)
                ? user.getMajor().getId()
                : null;

        String fullName =
                user != null ? safeJoin(user.getFirstName(), user.getLastName()) : null;

        String nationalId =
                user != null ? user.getNationalId() : null;

        String reviewedByName =
                reviewer != null ? safeJoin(reviewer.getFirstName(), reviewer.getLastName()) : null;

        return TutorApplicationDetailDto.builder()
                .id(app.getId())
                .userId(user != null ? user.getId() : null)
                .fullName(fullName)
                .nationalId(nationalId)
                .majorId(majorId)
                .majorName(majorName)
                .role(user != null ? user.getRole() : null)
                .status(app.getStatus())
                .applicationType(app.getApplicationType())
                .requestText(app.getRequestText())
                .transcriptUrl(app.getTranscriptUrl())
                .createdAt(app.getCreatedAt())
                .adminComment(app.getAdminComment())
                .reviewedByName(reviewedByName)
                .reviewedAt(app.getReviewedAt())
                .scopes(scopeDtos)
                .build();
    }

    public TutorApplicationScopeDto toScopeDto(TutorApplicationScope scope) {
        if (scope == null) {
            return null;
        }

        Course course = scope.getCourse();

        TutorApplicationScopeDto dto = new TutorApplicationScopeDto();
        dto.setId(scope.getId());
        dto.setScopeType(scope.getScopeType() != null ? scope.getScopeType().name() : null);
        dto.setYear(scope.getYearNumber());
        dto.setCourseId(course != null ? course.getId() : null);
        dto.setCourseCode(course != null ? course.getCourseCode() : null);
        dto.setCourseName(course != null ? course.getName() : null);
        return dto;
    }

    private String safeJoin(String a, String b) {
        String aFixed = a == null ? "" : a.trim();
        String bFixed = b == null ? "" : b.trim();
        String joined = (aFixed + " " + bFixed).trim();
        return joined.isEmpty() ? null : joined;
    }
}
