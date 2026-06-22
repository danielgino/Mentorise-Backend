package com.example.mentorisebackend.service.user;

import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.dto.user.MeUserResponse;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.repository.UserScopeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserMeService {

    private final UserRepository userRepository;
    private final UserScopeRepository userScopeRepository;

    public MeUserResponse getMe(Long userId) {
        User u = userRepository.findByIdWithMajor(userId).orElseThrow();

        String majorName = (u.getMajor() != null) ? u.getMajor().getName() : null;
        boolean hasPrefs = userScopeRepository.existsByUserId(userId);

        return new MeUserResponse(
                u.getId(),
                u.getFirstName(),
                u.getLastName(),
                u.getFullName(),
                u.getEmail(),
                u.getPhoneNumber(),
                (u.getMajor() != null ? u.getMajor().getId() : null),
                majorName,
                u.getRole(),
                u.isAlumni(),
                hasPrefs,
                u.getProfileImageUrl(),
                u.getProfileImagePublicId()
                );
    }
}
