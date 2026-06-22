package com.example.mentorisebackend.api.controller.user;

import com.example.mentorisebackend.dto.admin.TutorApplicationDetailDto;
import com.example.mentorisebackend.dto.tutor.MyTutorProfileResponse;
import com.example.mentorisebackend.dto.tutor.TutorApplicationCreateRequest;
import com.example.mentorisebackend.dto.tutor.TutorApplicationStatusDto;
import com.example.mentorisebackend.dto.tutor.TutorProfileDto;
import com.example.mentorisebackend.dto.tutor.TutorScopeDto;
import com.example.mentorisebackend.dto.tutor.UpdateTutorProfileRequest;
import com.example.mentorisebackend.security.UserPrincipal;
import com.example.mentorisebackend.service.tutor.TutorApplicationService;
import com.example.mentorisebackend.service.tutor.TutorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/tutors")
@RequiredArgsConstructor
public class TutorController {

    private final TutorApplicationService tutorApplicationService;
    private final TutorService tutorService;

    @PostMapping
    public ResponseEntity<TutorApplicationDetailDto> createTutorRequest(
            @AuthenticationPrincipal UserDetails principal,
            @Validated @RequestBody TutorApplicationCreateRequest request
    ) {
        Long userId = ((UserPrincipal) principal).getUserId();
        TutorApplicationDetailDto dto = tutorApplicationService.create(userId, request);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/{userId}/profile")
    public TutorProfileDto getTutorProfile(@PathVariable Long userId) {
        return tutorService.getTutorProfile(userId);
    }

    @GetMapping("/me/profile")
    public ResponseEntity<MyTutorProfileResponse> getMyTutorProfile(
            @AuthenticationPrincipal UserDetails principal
    ) {
        return ResponseEntity.ok(tutorService.getMyTutorProfile(((UserPrincipal) principal).getUserId()));
    }

    @PreAuthorize("hasRole('TUTOR')")
    @PutMapping("/me/profile")
    public TutorProfileDto updateMyTutorProfile(
            @Valid @RequestBody UpdateTutorProfileRequest request,
            @AuthenticationPrincipal UserDetails principal
    ) {
        return tutorService.updateProfileByEmail(((UserPrincipal) principal).getUserId(), request);
    }

    @GetMapping("/me/application/pending")
    public ResponseEntity<TutorApplicationStatusDto> getMyPendingApplication(
            @AuthenticationPrincipal UserDetails principal
    ) {
        Long userId = ((UserPrincipal) principal).getUserId();
        return ResponseEntity.ok(tutorApplicationService.getMyPendingApplication(userId));
    }

    @GetMapping("/me/scopes")
    @PreAuthorize("hasRole('TUTOR')")
    public ResponseEntity<List<TutorScopeDto>> getMyApprovedScopes(
            @AuthenticationPrincipal UserDetails principal
    ) {
        Long userId = ((UserPrincipal) principal).getUserId();
        return ResponseEntity.ok(tutorService.getMyApprovedScopes(userId));
    }
}
