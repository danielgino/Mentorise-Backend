package com.example.mentorisebackend.dto.admin.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUsersStatsResponse {
    private long totalRegisteredUsers;
    private long totalTutors;
    private long completedLessons;
    private BigDecimal totalCollectedAmount;

    private List<NamedCountDto> topRequestedCourses;

    private String topTutorMajorName;
    private long topTutorMajorCount;
    private String topUsersMajorName;
    private long topUsersMajorCount;

    private String topStudentMajorName;
    private long topStudentMajorCount;

    private long monthlyLessons;
    private long previousMonthLessons;
    private double monthlyLessonsGrowthPct;
    private double usersGrowthPct;

    private BigDecimal currentMonthRevenue;
    private BigDecimal previousMonthRevenue;
    private double revenueGrowthPct;
    private List<MajorStatsDto> topTutorMajors;
    private List<GrowthTimelinePointDto> growthTimeline;

}