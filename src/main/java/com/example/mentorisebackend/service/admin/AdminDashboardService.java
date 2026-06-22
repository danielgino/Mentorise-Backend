package com.example.mentorisebackend.service.admin;

import com.example.mentorisebackend.dto.admin.stats.AdminUsersStatsResponse;
import com.example.mentorisebackend.dto.admin.stats.GrowthTimelinePointDto;
import com.example.mentorisebackend.dto.admin.stats.MajorStatsDto;
import com.example.mentorisebackend.dto.admin.stats.NamedCountDto;
import com.example.mentorisebackend.enums.Role;
import com.example.mentorisebackend.enums.ScopeType;
import com.example.mentorisebackend.enums.SessionOfferStatus;
import com.example.mentorisebackend.repository.SessionOfferRepository;
import com.example.mentorisebackend.repository.TutorRepository;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.repository.UserScopeRepository;
import com.example.mentorisebackend.repository.projection.stats.TopMajorProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AdminDashboardService {

    private final UserRepository userRepository;
    private final TutorRepository tutorRepository;
    private final SessionOfferRepository sessionOfferRepository;
    private final UserScopeRepository userScopeRepository;

    public AdminUsersStatsResponse getUsersStats() {
        LocalDate today = LocalDate.now();

        LocalDateTime currentMonthStart = today.withDayOfMonth(1).atStartOfDay();
        LocalDateTime nextMonthStart = currentMonthStart.plusMonths(1);
        LocalDateTime previousMonthStart = currentMonthStart.minusMonths(1);

        long totalUsers = userRepository.count();
        long totalTutors = tutorRepository.count();

        long completedLessons = sessionOfferRepository
                .countByStatusAndEndTimeBefore(SessionOfferStatus.ACCEPTED, LocalDateTime.now());

        BigDecimal totalCollectedAmount = sessionOfferRepository.sumPriceByStatus(SessionOfferStatus.ACCEPTED);
        if (totalCollectedAmount == null) {
            totalCollectedAmount = BigDecimal.ZERO;
        }

        List<NamedCountDto> topRequestedCourses = userScopeRepository
                .findTopRequestedCourses(ScopeType.COURSE, PageRequest.of(0, 5))
                .stream()
                .map(course -> NamedCountDto.builder()
                        .name(course.getCourseName())
                        .count(course.getRequestCount())
                        .build())
                .toList();

        List<TopMajorProjection> topTutorMajors =
                tutorRepository.findTopTutorMajors(PageRequest.of(0, 1));

        String topTutorMajorName = null;
        long topTutorMajorCount = 0;

        if (!topTutorMajors.isEmpty()) {
            topTutorMajorName = topTutorMajors.get(0).getMajorName();
            topTutorMajorCount = topTutorMajors.get(0).getCount();
        }

        List<TopMajorProjection> topStudentMajors =
                userRepository.findTopMajorsByRole(Role.STUDENT, PageRequest.of(0, 1));
        List<TopMajorProjection> topUserMajors =
                userRepository.findTopMajorsByRoles(
                        List.of(Role.STUDENT, Role.TUTOR),
                        PageRequest.of(0, 1)
                );

        String topUsersMajorName = null;
        long topUsersMajorCount = 0;

        if (!topUserMajors.isEmpty()) {
            topUsersMajorName = topUserMajors.get(0).getMajorName();
            topUsersMajorCount = topUserMajors.get(0).getCount();
        }
        String topStudentMajorName = null;
        long topStudentMajorCount = 0;

        if (!topStudentMajors.isEmpty()) {
            topStudentMajorName = topStudentMajors.get(0).getMajorName();
            topStudentMajorCount = topStudentMajors.get(0).getCount();
        }

        long monthlyLessons = sessionOfferRepository
                .countByStatusAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                        SessionOfferStatus.ACCEPTED,
                        currentMonthStart,
                        nextMonthStart
                );

        long previousMonthLessons = sessionOfferRepository
                .countByStatusAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                        SessionOfferStatus.ACCEPTED,
                        previousMonthStart,
                        currentMonthStart
                );

        long currentMonthUsers = userRepository
                .countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        currentMonthStart,
                        nextMonthStart
                );

        long previousMonthUsers = userRepository
                .countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                        previousMonthStart,
                        currentMonthStart
                );

        BigDecimal currentMonthRevenue = sessionOfferRepository
                .sumPriceByStatusAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                        SessionOfferStatus.ACCEPTED,
                        currentMonthStart,
                        nextMonthStart
                );

        BigDecimal previousMonthRevenue = sessionOfferRepository
                .sumPriceByStatusAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                        SessionOfferStatus.ACCEPTED,
                        previousMonthStart,
                        currentMonthStart
                );

        if (currentMonthRevenue == null) {
            currentMonthRevenue = BigDecimal.ZERO;
        }
        if (previousMonthRevenue == null) {
            previousMonthRevenue = BigDecimal.ZERO;
        }

        double monthlyLessonsGrowthPct = calculateGrowthPercentage(monthlyLessons, previousMonthLessons);
        double usersGrowthPct = calculateGrowthPercentage(currentMonthUsers, previousMonthUsers);
        double revenueGrowthPct = calculateGrowthPercentage(currentMonthRevenue, previousMonthRevenue);

        List<MajorStatsDto> topTutorMajorsStats = tutorRepository
                .findTopTutorMajors(PageRequest.of(0, 5))
                .stream()
                .map(major -> MajorStatsDto.builder()
                        .name(major.getMajorName())
                        .tutorCount(major.getCount())
                        .percentageOfTotalUsers(calculatePercentage(major.getCount(), totalUsers))
                        .build())
                .toList();

        List<GrowthTimelinePointDto> growthTimeline = new ArrayList<>();

        Locale hebrewLocale = new Locale("he", "IL");
        YearMonth currentYearMonth = YearMonth.now();

        for (int i = 7; i >= 0; i--) {
            YearMonth yearMonth = currentYearMonth.minusMonths(i);

            LocalDateTime monthStart = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime nextMonth = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

            long usersInMonth = userRepository.countByCreatedAtGreaterThanEqualAndCreatedAtLessThan(
                    monthStart,
                    nextMonth
            );

            long sessionsInMonth = sessionOfferRepository.countByStatusAndStartTimeGreaterThanEqualAndStartTimeLessThan(
                    SessionOfferStatus.ACCEPTED,
                    monthStart,
                    nextMonth
            );

            String monthName = yearMonth.getMonth().getDisplayName(TextStyle.FULL, hebrewLocale);

            growthTimeline.add(
                    GrowthTimelinePointDto.builder()
                            .month(monthName)
                            .users(usersInMonth)
                            .sessions(sessionsInMonth)
                            .build()
            );
        }
        return AdminUsersStatsResponse.builder()
                .totalRegisteredUsers(totalUsers)
                .totalTutors(totalTutors)
                .completedLessons(completedLessons)
                .totalCollectedAmount(totalCollectedAmount)
                .topRequestedCourses(topRequestedCourses)
                .topTutorMajorName(topTutorMajorName)
                .topTutorMajorCount(topTutorMajorCount)
                .topUsersMajorName(topUsersMajorName)
                .topUsersMajorCount(topUsersMajorCount)
                .topStudentMajorName(topStudentMajorName)
                .topStudentMajorCount(topStudentMajorCount)
                .monthlyLessons(monthlyLessons)
                .previousMonthLessons(previousMonthLessons)
                .monthlyLessonsGrowthPct(monthlyLessonsGrowthPct)
                .usersGrowthPct(usersGrowthPct)
                .currentMonthRevenue(currentMonthRevenue)
                .previousMonthRevenue(previousMonthRevenue)
                .revenueGrowthPct(revenueGrowthPct)
                .topTutorMajors(topTutorMajorsStats)
                .growthTimeline(growthTimeline)
                .build();
    }

    private double calculateGrowthPercentage(long currentValue, long previousValue) {
        if (previousValue == 0) {
            return currentValue == 0 ? 0.0 : 100.0;
        }

        double growth = ((double) (currentValue - previousValue) / previousValue) * 100;
        return Math.round(growth * 10.0) / 10.0;
    }
    private double calculatePercentage(long value, long total) {
        if (total == 0) {
            return 0.0;
        }

        double percentage = ((double) value / total) * 100;
        return Math.round(percentage * 10.0) / 10.0;
    }

    private double calculateGrowthPercentage(BigDecimal currentValue, BigDecimal previousValue) {
        if (previousValue == null || previousValue.compareTo(BigDecimal.ZERO) == 0) {
            return currentValue == null || currentValue.compareTo(BigDecimal.ZERO) == 0 ? 0.0 : 100.0;
        }

        BigDecimal growth = currentValue
                .subtract(previousValue)
                .divide(previousValue, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        return growth.setScale(1, RoundingMode.HALF_UP).doubleValue();
    }
}