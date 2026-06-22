package com.example.mentorisebackend.dto.admin;


import com.example.mentorisebackend.enums.ApplicationType;
import com.example.mentorisebackend.enums.Role;
import com.example.mentorisebackend.enums.Status;
import lombok.*;
import java.time.LocalDateTime;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TutorApplicationRowDto {
    private Long id;
    private Long userId;
    private String fullName;
    private String nationalId;
    private String majorName;
    private Status status;
    private Role role;
    private ApplicationType applicationType;
    private LocalDateTime createdAt;
    private String transcriptUrl;

}
