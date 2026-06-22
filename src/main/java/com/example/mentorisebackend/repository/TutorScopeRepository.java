package com.example.mentorisebackend.repository;


import com.example.mentorisebackend.api.entity.TutorScope;
import com.example.mentorisebackend.enums.ScopeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface TutorScopeRepository extends JpaRepository<TutorScope, Long> {

    // ----- Exists (למנוע כפילויות בזמן APPROVE) -----

    boolean existsByTutor_IdAndScopeTypeAndYearNumber(Long tutorId, ScopeType scopeType, Integer yearNumber);

    boolean existsByTutor_IdAndScopeTypeAndCourse_Id(Long tutorId, ScopeType scopeType, Long courseId);

    // ניקוי scopes של מתרגל (אם בעתיד תחליט "להחליף" scopes באישור חדש)
    void deleteAllByTutor_Id(Long tutorId);

    @EntityGraph(attributePaths = {"course"})
    List<TutorScope> findAllByTutor_Id(Long tutorId);
}
