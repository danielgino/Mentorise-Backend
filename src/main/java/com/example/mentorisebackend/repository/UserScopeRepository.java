package com.example.mentorisebackend.repository;

import com.example.mentorisebackend.api.entity.UserScope;
import com.example.mentorisebackend.enums.ScopeType;
import com.example.mentorisebackend.repository.projection.stats.TopRequestedCourseProjection;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserScopeRepository extends JpaRepository<UserScope, Long> {

    @EntityGraph(attributePaths = {"course"})
    List<UserScope> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    long deleteByUserId(Long userId);

    @Query("""
           SELECT us.course.id AS courseId,
                  us.course.name AS courseName,
                  COUNT(DISTINCT us.user.id) AS requestCount
           FROM UserScope us
           WHERE us.scopeType = :scopeType
             AND us.course IS NOT NULL
           GROUP BY us.course.id, us.course.name
           ORDER BY COUNT(DISTINCT us.user.id) DESC
           """)
    List<TopRequestedCourseProjection> findTopRequestedCourses(
            @Param("scopeType") ScopeType scopeType
    );

    @Query("""
           SELECT c.name AS courseName, COUNT(us.id) AS requestCount
           FROM UserScope us
           JOIN us.course c
           WHERE us.scopeType = :scopeType
           GROUP BY c.id, c.name
           ORDER BY COUNT(us.id) DESC
           """)
    List<TopRequestedCourseProjection> findTopRequestedCourses(
            @Param("scopeType") ScopeType scopeType,
            Pageable pageable
    );
}
