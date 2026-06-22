package com.example.mentorisebackend.api.controller.admin;

import com.example.mentorisebackend.dto.admin.AdminCourseDto;
import com.example.mentorisebackend.dto.admin.PageResponse;
import com.example.mentorisebackend.service.admin.AdminCourseService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("admin/courses")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCourseController {

    private final AdminCourseService adminCourseService;



    @GetMapping
    public PageResponse<AdminCourseDto> getCourses(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String semester,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false) Sort.Direction dir
    ) {
        Page<AdminCourseDto> result = adminCourseService.getCoursesAndSearch(
                page, size, search, majorId, year, semester, sortBy, dir
        );
        return PageResponse.from(result);
    }


    @PatchMapping("/{courseId}")
    public ResponseEntity<AdminCourseDto> updateCourse(
            @PathVariable Long courseId,
            @RequestBody AdminCourseDto patch) {
        return ResponseEntity.ok(adminCourseService.updateCoursePartial(courseId, patch));
    }

    @PostMapping("/add")
    public AdminCourseDto createCourse(@RequestBody AdminCourseDto adminCourseDto) {
        return adminCourseService.createCourse(adminCourseDto);
    }

    @DeleteMapping("/delete/{id}")
    public void deleteCourse(@PathVariable Long id) {
        adminCourseService.deleteCourse(id);
    }
}
