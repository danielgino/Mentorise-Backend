package com.example.mentorisebackend.api.controller.admin;


import com.example.mentorisebackend.dto.admin.PageResponse;
import com.example.mentorisebackend.dto.admin.TutorApplicationDetailDto;
import com.example.mentorisebackend.dto.admin.TutorApplicationRowDto;
import com.example.mentorisebackend.enums.Status;
import com.example.mentorisebackend.service.admin.AdminTutorApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/tutor-applications")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminTutorApplicationsController {

    private final AdminTutorApplicationService adminTutorApplicationService;

    @GetMapping
    public PageResponse<TutorApplicationRowDto> getTutorApplications(
            @RequestParam(required = false) Status status,
            @RequestParam(required = false) String nationalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort
    ) {
        Page<TutorApplicationRowDto> results =
                adminTutorApplicationService.searchByNationalId(status, nationalId, page, size, sort);

        return PageResponse.from(results);
    }

    @GetMapping("/{id}")
    public TutorApplicationDetailDto getTutorApplicationDetails(@PathVariable("id") Long id) {
        return adminTutorApplicationService.getTutorApplicationDetails(id);
    }
    /** אישור בקשה */
    @PostMapping("/{id}/approve")
    public void approveApplication(@PathVariable Long id,
                        @RequestParam(required = false) String adminComment) {
        adminTutorApplicationService.approveApplication(id, adminComment);
    }

    /** דחיית בקשה */
    @PostMapping("/{id}/deny")
    public void denyApplication(@PathVariable Long id,
                     @RequestParam String reason) {
        adminTutorApplicationService.denyApplication(id, reason);
    }

}
