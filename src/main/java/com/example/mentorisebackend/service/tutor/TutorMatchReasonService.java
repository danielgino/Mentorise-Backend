package com.example.mentorisebackend.service.tutor;

import com.example.mentorisebackend.dto.tutor.TutorCardDto;
import com.example.mentorisebackend.enums.ScopeType;
import com.example.mentorisebackend.service.ai.GeminiService;
import com.example.mentorisebackend.service.user.UserScopeSnapshot;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TutorMatchReasonService {

    private final GeminiService geminiService;

    @JsonIgnoreProperties(ignoreUnknown = true)
    static class MatchReasonItem {
        public Long tutorId;
        public String reason;
    }

    public List<TutorCardDto> enrich(
            List<TutorCardDto> cards,
            UserScopeSnapshot snapshot,
            String majorName) {

        if (cards.isEmpty()) {
            return cards;
        }

        try {
            String prompt = buildPrompt(cards, snapshot, majorName);
            log.debug("Sending Gemini prompt:\n{}", prompt);
            MatchReasonItem[] items = geminiService.generateStructured(prompt, MatchReasonItem[].class);

            if (items == null) {
                return applyFallbackReasons(cards, snapshot);
            }

            Map<Long, String> reasonMap = new HashMap<>();
            for (MatchReasonItem item : items) {
                if (item.tutorId != null && item.reason != null) {
                    reasonMap.put(item.tutorId, item.reason);
                }
            }

            return cards.stream()
                    .map(card -> new TutorCardDto(
                            card.id(),
                            card.fullName(),
                            card.majorName(),
                            card.bio(),
                            card.tutorImageUrl(),
                            card.years(),
                            card.courses(),
                            card.isAlumni(),
                            reasonMap.getOrDefault(card.id(), fallbackReason(card, snapshot))
                    ))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.warn("Tutor enrichment failed, returning unranked results: {}", e.getMessage());
            log.debug("Tutor enrichment error details:", e);
            return applyFallbackReasons(cards, snapshot);
        }
    }

    private List<TutorCardDto> applyFallbackReasons(List<TutorCardDto> cards, UserScopeSnapshot snapshot) {
        return cards.stream()
                .map(card -> new TutorCardDto(
                        card.id(),
                        card.fullName(),
                        card.majorName(),
                        card.bio(),
                        card.tutorImageUrl(),
                        card.years(),
                        card.courses(),
                        card.isAlumni(),
                        fallbackReason(card, snapshot)
                ))
                .collect(Collectors.toList());
    }

    private String fallbackReason(TutorCardDto card, UserScopeSnapshot snapshot) {
        if (snapshot.mode() == ScopeType.COURSE && !card.courses().isEmpty()) {
            return "מתרגל מתאים לקורסים שבחרת";
        }
        if (card.bio() != null && !card.bio().isBlank()) {
            return "מתרגל עם ניסיון רלוונטי שיכול לעזור לך להתקדם";
        }
        return "מתרגל מהמסלול שלך, זמין לעזור בקורסים שונים";
    }

    private String buildPrompt(List<TutorCardDto> cards, UserScopeSnapshot snapshot, String majorName) {
        StringBuilder sb = new StringBuilder();
        sb.append("You must respond in Hebrew (עברית). All reasons must be written in Hebrew only.\n");
        sb.append("You are a tutor-matching assistant for a university platform.\n");
        sb.append("Use university terminology: use \"מתרגל\" (not \"מורה\"), \"מסלול\" (not \"חוג\"), \"סטודנט\" (not \"תלמיד\").\n");
        sb.append("For each tutor listed, write a single short sentence (max 12 words) explaining specifically why this tutor is a good match for this student.\n");
        sb.append("If a tutor has no bio, years, or courses listed, write something natural and friendly like: \"מתרגל מהמסלול שלך, זמין לעזור בקורסים שונים\" — do NOT write robotic or generic sentences.\n");
        sb.append("Respond with ONLY a JSON array. No markdown. No extra text.\n");
        sb.append("Format: [{\"tutorId\": 1, \"reason\": \"...\"}, ...]\n\n");

        sb.append("Student context:\n");
        sb.append("  Major: ").append(majorName).append("\n");

        switch (snapshot.mode()) {
            case COURSE -> {
                sb.append("  Looking for: course-specific tutors\n");
                List<String> allCourses = cards.stream()
                        .flatMap(c -> c.courses().stream())
                        .distinct()
                        .toList();
                if (!allCourses.isEmpty()) {
                    sb.append("  Relevant courses: ").append(String.join(", ", allCourses)).append("\n");
                }
            }
            case YEAR -> {
                sb.append("  Looking for: tutors for specific study years\n");
                if (snapshot.years() != null && !snapshot.years().isEmpty()) {
                    sb.append("  Student year(s): ").append(snapshot.years()).append("\n");
                }
            }
            case MAJOR -> sb.append("  Looking for: any tutor in the major\n");
        }

        sb.append("\nTutors:\n");
        for (TutorCardDto card : cards) {
            sb.append("  ID ").append(card.id());
            if (card.bio() != null && !card.bio().isBlank()) {
                sb.append(" | Bio: \"").append(card.bio()).append("\"");
            }
            if (!card.years().isEmpty()) {
                sb.append(" | Teaches years: ").append(card.years());
            }
            if (!card.courses().isEmpty()) {
                sb.append(" | Teaches courses: ").append(String.join(", ", card.courses()));
            }
            sb.append(" | Alumni: ").append(card.isAlumni()).append("\n");
        }

        return sb.toString();
    }
}
