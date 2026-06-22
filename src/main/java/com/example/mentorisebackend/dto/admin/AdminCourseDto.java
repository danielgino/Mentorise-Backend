package com.example.mentorisebackend.dto.admin;

import com.example.mentorisebackend.enums.Semester;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminCourseDto {

    private Long id;
    private String courseCode;
    private String name;
    private int year;
    private Semester semester;
    private Long majorId;
    private String majorName;
}