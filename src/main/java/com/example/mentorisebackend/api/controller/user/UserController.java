package com.example.mentorisebackend.api.controller.user;

import com.example.mentorisebackend.security.CurrentUser;
import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.dto.user.UpdatePhoneRequest;
import com.example.mentorisebackend.dto.user.UpdateProfileImageRequest;
import com.example.mentorisebackend.service.media.ProfileImageService;
import com.example.mentorisebackend.service.user.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final ProfileImageService profileImageService;
    private final CurrentUser currentUser;

    @PatchMapping("/me/phone")
    public ResponseEntity<String> updateMyPhone(@Valid @RequestBody UpdatePhoneRequest request) {
        User updatedUser = userService.updatePhoneNumber(currentUser.getUserId(), request);
        return ResponseEntity.ok(updatedUser.getPhoneNumber());
    }

    @PutMapping("/me/profile-image")
    public ResponseEntity<Map<String, String>> updateMyProfileImage(
            @Valid @RequestBody UpdateProfileImageRequest request) {
        Map<String, String> response =
                profileImageService.updateProfileImageByEmail(currentUser.getUserId(), request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/me/profile-image")
    public ResponseEntity<Void> deleteMyProfileImage() {
        profileImageService.deleteProfileImageByEmail(currentUser.getUserId());
        return ResponseEntity.noContent().build();
    }
}