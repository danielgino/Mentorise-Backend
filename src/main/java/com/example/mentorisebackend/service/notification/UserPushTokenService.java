package com.example.mentorisebackend.service.notification;

import com.example.mentorisebackend.security.CurrentUser;
import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.api.entity.UserPushToken;
import com.example.mentorisebackend.dto.notification.SavePushTokenRequest;
import com.example.mentorisebackend.repository.UserPushTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserPushTokenService {

    private final UserPushTokenRepository userPushTokenRepository;
    private final CurrentUser currentUser;

    @Transactional
    public void saveTokenForCurrentUser(SavePushTokenRequest request) {
        User user = currentUser.getEntity();
        String token = request.getToken().trim();

        Optional<UserPushToken> existing = userPushTokenRepository.findByToken(token);

        if (existing.isPresent()) {
            UserPushToken pushToken = existing.get();
            pushToken.setUser(user);
            pushToken.setActive(true);
            if (request.getPlatform() != null) {
                pushToken.setPlatform(request.getPlatform());
            }
            userPushTokenRepository.save(pushToken);
        } else {
            UserPushToken pushToken = UserPushToken.builder()
                    .user(user)
                    .token(token)
                    .active(true)
                    .platform(request.getPlatform())
                    .build();
            userPushTokenRepository.save(pushToken);
        }
    }
}
