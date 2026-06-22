package com.example.mentorisebackend.security;

import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUser {

    private final UserRepository userRepository;

    public User getEntity() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found for email: " + email));
    }

    public Long getUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof UserPrincipal up) {
            return up.getUserId();
        }
        // Fallback: WebSocket STOMP sessions use a String principal, not UserPrincipal
        return getEntity().getId();
    }

    public String getEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}