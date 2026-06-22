package com.example.mentorisebackend.api.controller.user;

import com.example.mentorisebackend.api.entity.Major;
import com.example.mentorisebackend.service.major.MajorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
@RestController
@RequestMapping("/api/users/major")
@RequiredArgsConstructor
public class MajorController {
private final MajorService majorService;


    @GetMapping("/get-all")
    public List<Major> getAllMajors() {
        return majorService.getAllMajors();
    }


    @GetMapping("/{majorId}/years")
    public ResponseEntity<List<MajorService.YearDto>> getYears(@PathVariable Long majorId) {
        return ResponseEntity.ok(majorService.getYearsForMajor(majorId));
    }
}
