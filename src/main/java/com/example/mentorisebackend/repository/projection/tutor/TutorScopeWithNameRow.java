package com.example.mentorisebackend.repository.projection.tutor;

public interface TutorScopeWithNameRow {
    Long    getTutorId();
    String  getScopeType();
    Integer getYearNumber();
    Long    getCourseId();
    String  getCourseName();
}
