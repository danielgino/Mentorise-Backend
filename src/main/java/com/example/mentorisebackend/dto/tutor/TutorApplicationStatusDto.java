package com.example.mentorisebackend.dto.tutor;

import com.example.mentorisebackend.enums.ApplicationType;
import com.example.mentorisebackend.enums.Status;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TutorApplicationStatusDto {
    private boolean hasPending;
    private Long applicationId;
    private ApplicationType applicationType;
    private Status status;
    private LocalDateTime createdAt;
    private List<TutorScopeDto> scopes;
    private boolean lastRejected;
}
