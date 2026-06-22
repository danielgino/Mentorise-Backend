package com.example.mentorisebackend.dto.admin;

import com.example.mentorisebackend.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class UserUpdateDto {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private Role role;
    @JsonProperty("isAlumni")
    private Boolean alumni;
}
