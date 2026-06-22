package com.example.mentorisebackend.dto.tutor;

import java.util.List;

public record TutorSwipeResponse(
        List<TutorCardDto> items,
        String nextCursor,
        boolean hasMore,
        boolean cycleResetSuggested
) {}