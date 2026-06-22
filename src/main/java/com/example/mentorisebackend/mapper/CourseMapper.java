package com.example.mentorisebackend.mapper;


import com.example.mentorisebackend.api.entity.Course;
import com.example.mentorisebackend.api.entity.Major;
import com.example.mentorisebackend.dto.admin.AdminCourseDto;
import com.example.mentorisebackend.exception.ResourceNotFoundException;
import com.example.mentorisebackend.repository.MajorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CourseMapper {

    private final MajorRepository majorRepository;

    //  Entity → DTO
    public AdminCourseDto toDto(Course course) {
        if (course == null) return null;

        return AdminCourseDto.builder()
                .id(course.getId())
                .courseCode(course.getCourseCode())
                .name(course.getName())
                .year(course.getYear())
                .semester(course.getSemester())
                .majorId(course.getMajor() != null ? course.getMajor().getId() : null)
                .majorName(course.getMajor() != null ? course.getMajor().getName() : null)
                .build();
    }

    // 🔹 DTO → Entity
    public Course toEntity(AdminCourseDto dto) {
        if (dto == null) return null;

        Major major = null;
        if (dto.getMajorId() != null) {
            major = majorRepository.findById(dto.getMajorId())
                    .orElseThrow(() ->  new ResourceNotFoundException("Major not found with id: " + dto.getMajorId()));
        }

        return Course.builder()
                .id(dto.getId())
                .courseCode(dto.getCourseCode())
                .name(dto.getName())
                .year(dto.getYear())
                .semester(dto.getSemester())
                .major(major)
                .build();
    }

    public void updateEntityFromDto(AdminCourseDto dto, Course course) {
        if (dto == null || course == null) return;

        if (dto.getCourseCode() != null) course.setCourseCode(dto.getCourseCode());
        if (dto.getName() != null) course.setName(dto.getName());
        if (dto.getYear() != 0) course.setYear(dto.getYear());
        if (dto.getSemester() != null) course.setSemester(dto.getSemester());

        if (dto.getMajorId() != null) {
            Major major = majorRepository.findById(dto.getMajorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Major not found with id: " + dto.getMajorId()));
            course.setMajor(major);
        }
    }
}
