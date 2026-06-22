package com.example.mentorisebackend.api.controller.admin;

import com.example.mentorisebackend.api.entity.Major;
import com.example.mentorisebackend.dto.major.MajorDto;
import com.example.mentorisebackend.service.admin.AdminMajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/majors")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminMajorController {

private final AdminMajorService adminMajorService;

    @PatchMapping("/{id}")
    public ResponseEntity<MajorDto> updateMajor(
            @PathVariable Long id,
            @RequestBody MajorDto patch) {
        return ResponseEntity.ok(adminMajorService.updateMajor(id, patch));
    }

    @PostMapping("/add")
    public ResponseEntity<MajorDto> createMajor(@RequestBody MajorDto request) {
        MajorDto created = adminMajorService.createMajor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @GetMapping("/getAll")
    public List<Major> getAllMajors() {
        return adminMajorService.getAllMajors();
    }

    @DeleteMapping("/delete/{id}")
    public void deleteMajor(@PathVariable Long id) {
        adminMajorService.deleteMajor(id);
    }

}
