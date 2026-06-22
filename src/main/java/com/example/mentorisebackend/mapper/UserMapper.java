package com.example.mentorisebackend.mapper;
import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.dto.user.UserDto;

public class UserMapper {

    public  UserDto toDto(User user) {
        if (user == null) return null;
        return UserDto.builder()
                .id(user.getId())
                .nationalId(user.getNationalId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phoneNumber(user.getPhoneNumber())
                .role(user.getRole())
                .isAlumni(user.isAlumni())
                .major(user.getMajor() != null ? user.getMajor().getName() : null)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
