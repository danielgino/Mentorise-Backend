package com.example.mentorisebackend.dto.user;

import com.example.mentorisebackend.enums.ScopeType;
import lombok.*;

import java.util.List;
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LearningPreferencesViewDto {
    private ScopeType scopeType;
    private List<String> yearLabels;
    private List<String> courseNames;
}






