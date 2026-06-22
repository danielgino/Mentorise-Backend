package com.example.mentorisebackend.service.admin;


import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.exception.UserNotFoundException;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.security.UserPrincipal;
import com.example.mentorisebackend.util.AppConstants;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminCustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public AdminCustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException(AppConstants.USER_NOT_FOUND + ": " + email));

        return new UserPrincipal(
                user.getId(),
                user.getEmail(),
                user.getPasswordHash(),
                switch (user.getRole()) {
                    case ADMIN   -> java.util.List.of(() -> "ROLE_ADMIN");
                    case TUTOR   -> java.util.List.of(() -> "ROLE_TUTOR");
                    case STUDENT -> java.util.List.of(() -> "ROLE_STUDENT");
                }
        );
    }
}
