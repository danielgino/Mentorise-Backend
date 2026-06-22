package com.example.mentorisebackend.mapper;

import com.example.mentorisebackend.api.entity.Major;
import com.example.mentorisebackend.dto.major.MajorDto;
import org.springframework.stereotype.Component;

@Component
public class MajorMapper {
    public MajorDto toDto(Major major) {
        return MajorDto.builder()
                .id(major.getId())
                .name(major.getName())
                .build();
    }
}