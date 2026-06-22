package com.example.mentorisebackend.mapper;
import com.example.mentorisebackend.api.entity.TutorApplication;
import com.example.mentorisebackend.dto.admin.TutorApplicationRowDto;
import org.springframework.stereotype.Component;



@Component
public class TutorApplicationRowMapper {

    public TutorApplicationRowDto toDto(TutorApplication e) {
        if (e == null) return null;
        TutorApplicationRowDto dto = new TutorApplicationRowDto();
        dto.setId(e.getId());
        dto.setStatus(e.getStatus());
        dto.setApplicationType(e.getApplicationType());
        dto.setCreatedAt(e.getCreatedAt());
        dto.setTranscriptUrl(e.getTranscriptUrl());

        if (e.getUser() != null) {
            dto.setUserId(e.getUser().getId());
            dto.setFullName(e.getUser().getFullName());
            dto.setNationalId(e.getUser().getNationalId());
            dto.setRole(e.getUser().getRole());
            if (e.getUser().getMajor() != null) {
                dto.setMajorName(e.getUser().getMajor().getName());
            }
        }
        return dto;

    }
}
