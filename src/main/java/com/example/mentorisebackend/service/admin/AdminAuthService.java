package com.example.mentorisebackend.service.admin;

import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.dto.auth.LoginRequestDto;
import com.example.mentorisebackend.dto.admin.AdminLoginResponseDto;
import com.example.mentorisebackend.repository.UserRepository;
import com.example.mentorisebackend.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminAuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;


    public AdminLoginResponseDto login(LoginRequestDto request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        String token = jwtUtil.generateToken(request.getEmail());
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow();

        return new AdminLoginResponseDto(
                token,
                user.getId(),
                user.getEmail(),
                user.getRole(),
                user.getFullName()
        );
    }

}
