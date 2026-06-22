package com.example.mentorisebackend.dto.user;

import com.example.mentorisebackend.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MeUserResponse {
    private Long userId;
    private String firstName;
    private String lastName;
    private String fullName;
    private String email;
    private String phoneNumber;
    private Long majorId;
    private String majorName;
    private Role role;
    @JsonProperty("isAlumni")
    private Boolean alumni;
    private Boolean hasLearningPreferences;
//    private String avatarUrl;
    private String profileImageUrl;
    private String profileImagePublicId;


}
