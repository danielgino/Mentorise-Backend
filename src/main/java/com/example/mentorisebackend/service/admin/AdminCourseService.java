package com.example.mentorisebackend.service.admin;
import com.example.mentorisebackend.api.entity.Course;
import com.example.mentorisebackend.api.entity.Major;
import com.example.mentorisebackend.dto.admin.AdminCourseDto;
import com.example.mentorisebackend.exception.BadRequestException;
import com.example.mentorisebackend.exception.DuplicateFieldException;
import com.example.mentorisebackend.exception.ResourceNotFoundException;
import com.example.mentorisebackend.mapper.CourseMapper;
import com.example.mentorisebackend.repository.CourseRepository;
import com.example.mentorisebackend.repository.MajorRepository;
import com.example.mentorisebackend.util.AppConstants;
import jakarta.persistence.criteria.JoinType;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;



@Service
public class AdminCourseService {

    private final CourseRepository courseRepository;
    private final MajorRepository majorRepository;
    private final CourseMapper courseMapper;

    public AdminCourseService(CourseRepository courseRepository, MajorRepository majorRepository, CourseMapper courseMapper){
        this.courseRepository = courseRepository;
        this.majorRepository = majorRepository;
        this.courseMapper = courseMapper;
    }


    @Transactional
    public AdminCourseDto createCourse(AdminCourseDto request) {
        if (courseRepository.existsByCourseCode(request.getCourseCode())) {
            throw new DuplicateFieldException("courseCode", request.getCourseCode(), AppConstants.COURSE_CODE_ALREADY_EXISTS);
        }

        Major major = majorRepository.findById(request.getMajorId())
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.MAJOR_NOT_FOUND_WITH_ID + request.getMajorId()));

        Course course = new Course();
        course.setCourseCode(request.getCourseCode());
        course.setName(request.getName());
        course.setYear(request.getYear());
        course.setSemester(request.getSemester());
        course.setMajor(major);
        Course saved = courseRepository.save(course);

        return AdminCourseDto.builder()
                .id(saved.getId())
                .courseCode(saved.getCourseCode())
                .name(saved.getName())
                .year(saved.getYear())
                .semester(saved.getSemester())
                .majorId(major.getId())
                .majorName(major.getName())
                .build();
    }
    public void deleteCourse(Long courseId) {
        throw new BadRequestException(AppConstants.CANNOT_DELETE_COURSE);
    }


    public Page<AdminCourseDto> getCoursesAndSearch(
            int page, int size, String search,
            Long majorId, Integer year, String semester,
            String sortBy, Sort.Direction dir
    ) {
        String sortProp = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
        Sort.Direction direction = (dir == null) ? Sort.Direction.DESC : dir;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortProp, "id"));

        Specification<Course> spec = (root, query, cb) -> cb.conjunction();
        if (search != null && !search.isBlank()) {
            String like = "%" + search.toLowerCase().trim() + "%";
            spec = spec.and((root, query, cb) -> cb.or(
                    cb.like(cb.lower(root.get("courseCode")), like),
                    cb.like(cb.lower(root.get("name")), like)
            ));
        }
        if (majorId != null) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(root.join("major", JoinType.INNER).get("id"), majorId)
            );
        }
        if (year != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get("year"), year));
        }
        if (semester != null && !semester.isBlank()) {
            spec = spec.and((root, query, cb) ->
                    cb.equal(cb.lower(root.get("semester")), semester.toLowerCase().trim())
            );
        }
        Page<Course> result = courseRepository.findAll(spec, pageable);
        return result.map(courseMapper::toDto);
    }

    @Transactional
    public AdminCourseDto updateCoursePartial(Long courseId, AdminCourseDto patch) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new ResourceNotFoundException(AppConstants.COURSE_NOT_FOUND_WITH_ID + courseId));

        if (patch.getCourseCode() != null && !patch.getCourseCode().equals(course.getCourseCode())) {
            if (courseRepository.existsByCourseCode(patch.getCourseCode())) {
                throw new DuplicateFieldException("courseCode", patch.getCourseCode(), AppConstants.COURSE_CODE_ALREADY_IN_USE);
            }
            course.setCourseCode(patch.getCourseCode());
        }
        if (patch.getName() != null) {
            course.setName(patch.getName());
        }
        if (patch.getMajorId() != null && patch.getMajorId() != 0) {
            Major major = majorRepository.findById(patch.getMajorId())
                    .orElseThrow(() -> new ResourceNotFoundException(AppConstants.MAJOR_NOT_FOUND_WITH_ID_COLON + patch.getMajorId()));
            course.setMajor(major);
        }
        if (patch.getYear() != 0) {
            course.setYear(patch.getYear());
        }
        if (patch.getSemester() != null ) {
            course.setSemester(patch.getSemester());
        }


        Course saved = courseRepository.save(course);
        return courseMapper.toDto(saved);
    }





}
