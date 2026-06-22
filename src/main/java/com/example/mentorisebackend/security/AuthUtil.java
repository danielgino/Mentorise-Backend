package com.example.mentorisebackend.security;

import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.exception.UserNotFoundException;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.util.AppConstants;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class for authentication-related operations.
 * Provides common methods for working with current user context.
 */
public class AuthUtil {

    private AuthUtil() {
        // Prevent instantiation
    }

    /**
     * Gets the currently authenticated user from SecurityContext.
     *
     * @param userRepository the user repository to fetch the user entity
     * @return the current User entity
     * @throws UserNotFoundException if user is not found or not authenticated
     */
    public static User getCurrentUserEntity(UserRepository userRepository) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new UserNotFoundException(AppConstants.UNAUTHENTICATED);
        }

        String email = auth.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND));
    }
}

