package com.example.mentorisebackend.repository.projection.tutor;

public interface TutorCoreRow {
    Long   getTutorId();
    String getFullName();
    boolean isAlumni();
    String getMajorName();
    String getBio();
    String getTutorImageUrl();
}
