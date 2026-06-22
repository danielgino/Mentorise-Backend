package com.example.mentorisebackend.service.user;



import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.dto.user.UpdatePhoneRequest;
import com.example.mentorisebackend.exception.DuplicateFieldException;
import com.example.mentorisebackend.exception.UserNotFoundException;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.util.AppConstants;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public User updatePhoneNumber(Long userId, UpdatePhoneRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND));

        String normalizedPhone = normalizePhone(request.getPhoneNumber());

        if (normalizedPhone != null
                && !normalizedPhone.equals(user.getPhoneNumber())
                && userRepository.existsByPhoneNumber(normalizedPhone)) {
            throw new DuplicateFieldException("phoneNumber", normalizedPhone, AppConstants.PHONE_NUMBER_ALREADY_IN_USE);
        }

        user.setPhoneNumber(normalizedPhone);

        return userRepository.save(user);
    }

    private String normalizePhone(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        String normalized = phoneNumber.trim();

        if (normalized.isEmpty()) {
            return null;
        }

        return normalized;
    }
}