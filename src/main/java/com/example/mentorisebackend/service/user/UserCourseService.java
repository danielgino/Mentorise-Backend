package com.example.mentorisebackend.service.user;

import com.example.mentorisebackend.security.CurrentUser;
import com.example.mentorisebackend.dto.course.CourseDto;
import com.example.mentorisebackend.repository.CourseRepository;
import com.example.mentorisebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserCourseService {

    private final CurrentUser currentUser;
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;

    public List<CourseDto> getUserCourses() {
        Long userId = currentUser.getUserId();
        Long majorId = userRepository.findMajorIdByUserId(userId);
        if (majorId == null) {
            return List.of();
        }
        return courseRepository.findByMajor_Id(majorId)
                .stream()
                .map(c -> new CourseDto(c.getId(), c.getCourseCode(), c.getName(), c.getYear(), c.getSemester()))
                .toList();
    }
}
