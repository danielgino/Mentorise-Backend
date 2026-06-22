package com.example.mentorisebackend.api.controller.user;

import com.example.mentorisebackend.security.CurrentUser;
import com.example.mentorisebackend.dto.user.LearningPreferencesDto;
import com.example.mentorisebackend.service.user.UserScopeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserScopeController {

    private final UserScopeService userScopeService;
    private final CurrentUser currentUser;
    @PutMapping("/learning-preferences")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void upsert(@RequestBody LearningPreferencesDto dto) {
        Long userId = currentUser.getUserId();   // 👈 מזהה מה-SecurityContext
        userScopeService.upsert(userId, dto);
    }

}
