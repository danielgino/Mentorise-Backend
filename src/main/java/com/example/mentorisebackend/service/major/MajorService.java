package com.example.mentorisebackend.service.major;

import com.example.mentorisebackend.api.entity.Major;
import com.example.mentorisebackend.repository.CourseRepository;
import com.example.mentorisebackend.repository.MajorRepository;
import com.example.mentorisebackend.util.HebrewUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
@RequiredArgsConstructor
public class MajorService {
private final MajorRepository majorRepository;
private final CourseRepository courseRepository;
    public List<Major> getAllMajors() {
        return majorRepository.findAll();

    }


    public record YearDto(Integer id, String name, Long courseCount) {}

    public List<YearDto> getYearsForMajor(Long majorId) {
        var rows = courseRepository.findYearsByMajorIdNative(majorId);
        return rows.stream()
                .map(r -> {
                    Integer y = ((Number) r[0]).intValue();
                    Long cnt = ((Number) r[1]).longValue();
                    return new YearDto(y, HebrewUtils.getHebrewYearLabel(y), cnt);
                })
                .toList();
    }

    // Removed: hebrewYearName() method - now using HebrewUtils.getHebrewYearLabel()
}
