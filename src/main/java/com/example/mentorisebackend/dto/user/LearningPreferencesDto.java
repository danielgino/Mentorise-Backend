package com.example.mentorisebackend.dto.user;

import com.example.mentorisebackend.enums.ScopeType;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LearningPreferencesDto {

    @NotNull(message = "scopeType is required") // MAJOR | YEAR | COURSE
    private ScopeType scopeType;


    // כש-YEAR
    @Builder.Default
    private List<Integer> years = new ArrayList<>();

    // כש-COURSE
    @Builder.Default
    private List<Long> courseIds = new ArrayList<>(); // 👈 שונה ל-courseIds


}
