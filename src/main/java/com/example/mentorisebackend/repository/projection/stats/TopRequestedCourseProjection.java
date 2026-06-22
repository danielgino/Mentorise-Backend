package com.example.mentorisebackend.repository.projection.stats;


public interface TopRequestedCourseProjection {
    String getCourseName();
    long getRequestCount();
}