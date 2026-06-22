package com.example.mentorisebackend.repository;


import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.repository.projection.tutor.CourseNameRow;
import com.example.mentorisebackend.repository.projection.tutor.TutorCoreRow;
import com.example.mentorisebackend.repository.projection.tutor.TutorMajorRow;
import com.example.mentorisebackend.repository.projection.tutor.TutorScopeRow;
import com.example.mentorisebackend.repository.projection.tutor.TutorScopeWithNameRow;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TutorSwipeDetailsRepository extends Repository<User, Long> {

    @Query(value = """
        SELECT u.id                                   AS tutorId,
               CONCAT(u.first_name, ' ', u.last_name) AS fullName,
               u.is_alumni                             AS alumni,
               COALESCE(m.name, '')                    AS majorName,
               t.bio                                   AS bio,
               t.tutor_image_url                       AS tutorImageUrl
        FROM users u
        JOIN   tutors t    ON t.user_id = u.id AND t.is_active = 1
        LEFT JOIN majors m ON m.id = u.major_id
        WHERE u.id IN (:tutorIds)
        """, nativeQuery = true)
    List<TutorCoreRow> findCoreByTutorIds(@Param("tutorIds") List<Long> tutorIds);

    @Query(value = """
        SELECT t.user_id      AS tutorId,
               ts.scope_type  AS scopeType,
               ts.year_number AS yearNumber,
               c.id           AS courseId,
               c.name         AS courseName
        FROM tutor_scopes ts
        JOIN  tutors t     ON t.id = ts.tutor_id AND t.is_active = 1
        LEFT JOIN courses c ON c.id = ts.course_id
        WHERE t.user_id IN (:tutorIds)
        """, nativeQuery = true)
    List<TutorScopeWithNameRow> findScopesWithNamesByTutorIds(@Param("tutorIds") List<Long> tutorIds);

    // kept — still used by TutorService
    @Query(value = """
        SELECT u.id AS tutorId,
               m.name AS majorName
        FROM users u
        JOIN majors m ON m.id = u.major_id
        WHERE u.id IN (:tutorIds)
        """, nativeQuery = true)
    List<TutorMajorRow> findMajorsByTutorIds(@Param("tutorIds") List<Long> tutorIds);

    // kept — still used by TutorService
    @Query(value = """
    SELECT t.user_id AS tutorId,
           ts.scope_type AS scopeType,
           ts.year_number AS yearNumber,
           ts.course_id AS courseId
    FROM tutor_scopes ts
    JOIN tutors t ON t.id = ts.tutor_id
    WHERE t.is_active = 1
      AND t.user_id IN (:tutorIds)
    """, nativeQuery = true)
    List<TutorScopeRow> findScopesByTutorIds(@Param("tutorIds") List<Long> tutorIds);

    // kept — still used by TutorService
    @Query(value = """
        SELECT c.id AS courseId,
               c.name AS courseName
        FROM courses c
        WHERE c.id IN (:courseIds)
        """, nativeQuery = true)
    List<CourseNameRow> findCourseNamesByCourseIds(@Param("courseIds") List<Long> courseIds);
}
