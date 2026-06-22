package com.example.mentorisebackend.dto.admin.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GrowthTimelinePointDto {
    private String month;
    private long users;
    private long sessions;
}