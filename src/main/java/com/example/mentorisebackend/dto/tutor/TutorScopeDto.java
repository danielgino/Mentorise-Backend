package com.example.mentorisebackend.dto.tutor;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TutorScopeDto {
    private String scopeType;
    private Long courseId;
    private String courseName;
    private Integer yearNumber;
}
