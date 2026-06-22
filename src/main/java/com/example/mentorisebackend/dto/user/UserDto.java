package com.example.mentorisebackend.dto.user;


import com.example.mentorisebackend.enums.Role;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDto {
    private Long id;
    private String nationalId;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private Role role;
    @JsonProperty("isAlumni")
    private boolean isAlumni;
    private String major;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
