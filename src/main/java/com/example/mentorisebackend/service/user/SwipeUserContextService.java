package com.example.mentorisebackend.service.user;

import com.example.mentorisebackend.api.entity.UserScope;
import com.example.mentorisebackend.enums.ScopeType;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.repository.UserScopeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SwipeUserContextService {

    private final UserRepository userRepository;
    private final UserScopeRepository userScopeRepository;

    @Cacheable(value = "userMajor", key = "#userId")
    public Long getMajorId(Long userId) {
        return userRepository.findMajorIdByUserId(userId);
    }

    @Cacheable(value = "userScopes", key = "#userId")
    public UserScopeSnapshot getScopeSnapshot(Long userId) {
        List<UserScope> scopes = userScopeRepository.findByUserId(userId);
        return buildSnapshot(scopes);
    }

    private UserScopeSnapshot buildSnapshot(List<UserScope> scopes) {
        if (scopes == null || scopes.isEmpty()) {
            return new UserScopeSnapshot(ScopeType.MAJOR, List.of(), List.of());
        }

        LinkedHashSet<Integer> years     = new LinkedHashSet<>();
        LinkedHashSet<Long>    courseIds = new LinkedHashSet<>();

        for (UserScope s : scopes) {
            if (s.getScopeType() == ScopeType.YEAR && s.getYearNumber() != null) {
                years.add(s.getYearNumber());
            }
            if (s.getScopeType() == ScopeType.COURSE && s.getCourse() != null) {
                courseIds.add(s.getCourse().getId());
            }
        }

        ScopeType mode;
        if (!courseIds.isEmpty())  mode = ScopeType.COURSE;
        else if (!years.isEmpty()) mode = ScopeType.YEAR;
        else                       mode = ScopeType.MAJOR;

        return new UserScopeSnapshot(mode, new ArrayList<>(years), new ArrayList<>(courseIds));
    }
}
