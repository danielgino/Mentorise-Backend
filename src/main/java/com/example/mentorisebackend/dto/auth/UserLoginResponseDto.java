package com.example.mentorisebackend.dto.auth;

import com.example.mentorisebackend.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor

public class UserLoginResponseDto {
    private String token;
    private Long userId;
    private String email;
    private Role role;
    private String fullName;
    private Long majorId;
}
