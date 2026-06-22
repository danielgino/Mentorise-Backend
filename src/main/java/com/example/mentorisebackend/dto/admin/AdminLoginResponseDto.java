package com.example.mentorisebackend.dto.admin;


import com.example.mentorisebackend.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AdminLoginResponseDto {
    private String token;
    private Long userId;
    private String email;
    private Role role;
    private String fullName;
}