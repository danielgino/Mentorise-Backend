package com.example.mentorisebackend.dto.tutor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyTutorProfileResponse {
    private String bio;
    private String tutorImageUrl;
    private String tutorImagePublicId;
}