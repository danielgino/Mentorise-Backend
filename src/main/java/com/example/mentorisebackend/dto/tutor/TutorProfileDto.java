package com.example.mentorisebackend.dto.tutor;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TutorProfileDto {

    private Long id;
    private String fullName;
    private String majorName;
    private String profileImageUrl;
    private List<Integer> years;
    private List<String> courses;
    private boolean isAlumni;
    private String bio;
    private String tutorImageUrl;
    private Double ratingAvg;
    private Integer totalReviews;
    private Double hourlyRate;
    private LocalDateTime createdAt;

}
