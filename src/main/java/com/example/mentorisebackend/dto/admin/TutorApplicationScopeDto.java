package com.example.mentorisebackend.dto.admin;

import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TutorApplicationScopeDto {
    private Long id;
    private String scopeType;
    private Long courseId;
    private String courseCode;
    private String courseName;
    private Integer year;

}