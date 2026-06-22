package com.example.mentorisebackend.api.controller.user;

import com.example.mentorisebackend.security.CurrentUser;
import com.example.mentorisebackend.dto.user.LearningPreferencesDto;
import com.example.mentorisebackend.dto.user.LearningPreferencesViewDto;
import com.example.mentorisebackend.dto.user.MeUserResponse;
import com.example.mentorisebackend.dto.notification.SavePushTokenRequest;
import com.example.mentorisebackend.service.user.UserMeService;
import com.example.mentorisebackend.service.notification.UserPushTokenService;
import com.example.mentorisebackend.service.user.UserScopeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class UserMeController {

    private final CurrentUser currentUser;
    private final UserMeService userMeService;
    private final UserScopeService userScopeService;
    private final UserPushTokenService userPushTokenService;

    @GetMapping
    public MeUserResponse me() {
        Long userId = currentUser.getUserId();
        return userMeService.getMe(userId);
    }

    @GetMapping("/learning-preferences")
    public LearningPreferencesViewDto getPrefs() {
        Long userId = currentUser.getUserId();
        return userScopeService.getUserScopesView(userId);
    }

    @PutMapping("/learning-preferences")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsertPrefs(@RequestBody LearningPreferencesDto dto) {
        Long userId = currentUser.getUserId();
        userScopeService.upsert(userId, dto);
    }

    @PostMapping("/push-token")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void savePushToken(@RequestBody @Valid SavePushTokenRequest request) {
        userPushTokenService.saveTokenForCurrentUser(request);
    }
}
