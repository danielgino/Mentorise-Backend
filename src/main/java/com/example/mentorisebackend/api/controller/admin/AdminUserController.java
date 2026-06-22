package com.example.mentorisebackend.api.controller.admin;


import com.example.mentorisebackend.dto.admin.CreateAdminDto;
import com.example.mentorisebackend.dto.admin.MeResponseDto;
import com.example.mentorisebackend.dto.admin.PageResponse;
import com.example.mentorisebackend.dto.user.UserDto;
import com.example.mentorisebackend.dto.admin.UserUpdateDto;
import com.example.mentorisebackend.enums.Role;
import com.example.mentorisebackend.service.admin.AdminUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private final AdminUserService adminUserService;

    @GetMapping("/me")
    public MeResponseDto getCurrentUser() {
    return adminUserService.getCurrentUser();
    }


@GetMapping
    public PageResponse<UserDto> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = "") String search,
        @RequestParam(required = false) Role role,
        @RequestParam(required = false) Boolean alumni,
        @RequestParam(defaultValue = "ALL") String joinDateFilter) {
    Page<UserDto> result = adminUserService.getUsersAndSearch(
            page,
            size,
            search,
            role,
            alumni,
            joinDateFilter
    );
    return PageResponse.from(result);
}


    @GetMapping("/{userId}")
    public UserDto getUserById(@PathVariable Long userId) {
        return adminUserService.getUserById(userId);
    }

    @PatchMapping("/{userId}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long userId,
            @RequestBody UserUpdateDto patch) {
        return ResponseEntity.ok(adminUserService.updateUserPartial(userId, patch));
    }
    @PostMapping("/{userId}/revoke-tutor")
    public ResponseEntity<UserDto> revokeTutorPermissions(@PathVariable Long userId) {
        return ResponseEntity.ok(adminUserService.revokeTutorPermissions(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminUserService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin")
    public ResponseEntity<UserDto> createAdmin(@RequestBody CreateAdminDto request) {
        UserDto created = adminUserService.createAdmin(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }
}




