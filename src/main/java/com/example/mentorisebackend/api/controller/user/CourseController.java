package com.example.mentorisebackend.api.controller.user;

import com.example.mentorisebackend.dto.course.CourseDto;
import com.example.mentorisebackend.service.user.UserCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor

public class CourseController {
    private final UserCourseService userCourseService;


    @GetMapping("/my-courses")
    public List<CourseDto> getCurrentUserCourses() {
        return userCourseService.getUserCourses();
    }

}
