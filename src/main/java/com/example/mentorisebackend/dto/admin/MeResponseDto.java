package com.example.mentorisebackend.dto.admin;
import com.example.mentorisebackend.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class MeResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private Role role;
}
