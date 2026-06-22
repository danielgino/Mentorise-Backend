package com.example.mentorisebackend.repository.projection.tutor;

public interface TutorScopeRow {
    Long getTutorId();
    String getScopeType();   // 'YEAR' / 'COURSE' / 'MAJOR'
    Integer getYearNumber();
    Long getCourseId();
}
