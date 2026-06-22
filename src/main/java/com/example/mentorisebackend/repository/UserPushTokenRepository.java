package com.example.mentorisebackend.repository;

import com.example.mentorisebackend.api.entity.UserPushToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserPushTokenRepository extends JpaRepository<UserPushToken, Long> {
    List<UserPushToken> findByUser_IdAndActiveTrue(Long userId);
    Optional<UserPushToken> findByToken(String token);
}
