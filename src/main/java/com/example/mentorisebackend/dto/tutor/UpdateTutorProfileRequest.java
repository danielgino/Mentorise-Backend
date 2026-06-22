package com.example.mentorisebackend.dto.tutor;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateTutorProfileRequest {

    @Size(max = 300, message = "הביוגרפיה יכולה להכיל עד 300 תווים")
    private String bio;

    @Size(max = 500, message = "קישור התמונה ארוך מדי")
    private String tutorImageUrl;

    @Size(max = 255, message = "מזהה התמונה ארוך מדי")
    private String tutorImagePublicId;

    private Double hourlyRate;

}