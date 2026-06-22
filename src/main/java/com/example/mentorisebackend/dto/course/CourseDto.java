package com.example.mentorisebackend.dto.course;

import com.example.mentorisebackend.enums.Semester;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseDto {
    private Long id;
    private String courseCode;
    private String name;
    private int year;
    private Semester semester;
}
