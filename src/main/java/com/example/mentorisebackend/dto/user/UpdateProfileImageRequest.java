package com.example.mentorisebackend.dto.user;


import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProfileImageRequest {

    @Size(max = 500, message = "קישור התמונה ארוך מדי")
    private String profileImageUrl;

    @Size(max = 255, message = "מזהה התמונה ארוך מדי")
    private String profileImagePublicId;
}