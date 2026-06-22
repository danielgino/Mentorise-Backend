package com.example.mentorisebackend.dto.tutor;


import java.util.List;

public record TutorCardDto(
        Long id,
        String fullName,
        String majorName,
        String bio,
        String tutorImageUrl,
        List<Integer> years,
        List<String> courses,
        boolean isAlumni,
        String matchReason
) {}

