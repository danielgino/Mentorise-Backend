package com.example.mentorisebackend.api.controller.user;



import com.example.mentorisebackend.dto.tutor.TutorSwipeResponse;
import com.example.mentorisebackend.service.tutor.TutorSwipeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/tutors")
public class TutorSwipeController {

    private final TutorSwipeService swipeService;



    @GetMapping("/swipe")
    public TutorSwipeResponse swipe(
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) String cursor,
            @RequestParam(required = false) String excludeIds
    ) {
        List<Long> exclude = parseExcludeIds(excludeIds);
        return swipeService.getSwipeTutors(limit, cursor, exclude);
    }

    private List<Long> parseExcludeIds(String raw) {
        if (raw == null || raw.isBlank()) return Collections.emptyList();

        String[] parts = raw.split(",");
        LinkedHashSet<Long> set = new LinkedHashSet<>();

        for (String p : parts) {
            String s = p.trim();
            if (s.isEmpty()) continue;
            try {
                set.add(Long.parseLong(s));
            } catch (NumberFormatException ignored) {
            }
            if (set.size() >= 50) break;
        }
        return new ArrayList<>(set);
    }
}
