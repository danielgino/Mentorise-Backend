package com.example.mentorisebackend.dto.tutor;

import com.example.mentorisebackend.enums.ScopeType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutorApplicationScopeCreate {

    private ScopeType scopeType;  // COURSE / YEAR / ALL_MAJOR
    private Long courseId;        // חובה כש scopeType = COURSE
    private Integer yearNumber;   // חובה כש scopeType = YEAR (1–6)
}
