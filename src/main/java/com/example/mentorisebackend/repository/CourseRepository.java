package com.example.mentorisebackend.repository;

import com.example.mentorisebackend.api.entity.Course;
import com.example.mentorisebackend.repository.projection.tutor.CourseNameRow;
import jakarta.annotation.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
public interface CourseRepository extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    @Override
    @EntityGraph(attributePaths = {"major"})
    Page<Course> findAll(@Nullable Specification<Course> spec, Pageable pageable);


    @Query("""
        select c.id as courseId, c.name as courseName
        from Course c
        where c.id in :ids
    """)
    List<CourseNameRow> findCourseNamesByIds(@Param("ids") List<Long> ids);

    boolean existsByCourseCode(String courseCode);

    List<Course> findByMajor_Id(Long majorId);

    @Query(value = """
    SELECT `year`   AS y,
           COUNT(*) AS c
    FROM   `Courses`
    WHERE  `major_id` = :majorId
    GROUP  BY `year`
    ORDER  BY `year`
""", nativeQuery = true)
    List<Object[]> findYearsByMajorIdNative(@Param("majorId") Long majorId);

}
