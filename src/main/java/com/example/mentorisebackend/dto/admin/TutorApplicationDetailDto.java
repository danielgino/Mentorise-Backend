package com.example.mentorisebackend.dto.admin;

import com.example.mentorisebackend.enums.ApplicationType;
import com.example.mentorisebackend.enums.Role;
import com.example.mentorisebackend.enums.Status;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TutorApplicationDetailDto {

    private Long id;
    private Long userId;
    private String fullName;
    private String nationalId;
    private Long majorId;
    private String majorName;
    private Role role;
    private Status status;
    private ApplicationType applicationType;
    private String requestText;
    private String transcriptUrl;
    private LocalDateTime createdAt;
    private String adminComment;
    private String reviewedByName;
    private LocalDateTime reviewedAt;
    private List<TutorApplicationScopeDto> scopes;

    // Populated for UPDATE applications only — null for INITIAL
    private List<TutorApplicationScopeDto> currentScopes;
    private List<TutorApplicationScopeDto> addedScopes;
    private List<TutorApplicationScopeDto> removedScopes;
    private List<TutorApplicationScopeDto> unchangedScopes;
}
