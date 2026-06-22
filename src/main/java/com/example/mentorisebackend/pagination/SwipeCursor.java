package com.example.mentorisebackend.pagination;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

public record SwipeCursor(int rank, long lastId) {

    public static SwipeCursor start() {
        return new SwipeCursor(0, 0L);
    }

    public static SwipeCursor decode(String token) {
        if (token == null || token.isBlank()) {
            return start();
        }
        byte[] decoded = Base64.getUrlDecoder().decode(token);
        String raw = new String(decoded, StandardCharsets.UTF_8);
        String[] parts = raw.split(":");
        int rank = Integer.parseInt(parts[0]);
        long lastId = Long.parseLong(parts[1]);
        return new SwipeCursor(rank, lastId);
    }

    public String encode() {
        String raw = this.rank + ":" + this.lastId;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}