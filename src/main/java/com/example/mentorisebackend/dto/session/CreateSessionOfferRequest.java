package com.example.mentorisebackend.dto.session;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CreateSessionOfferRequest {

    @NotNull(message = "יש לבחור סטודנט")
    private Long studentUserId;

    @NotNull(message = "יש לבחור זמן התחלה")
    @Future(message = "זמן התחלה חייב להיות בעתיד")
    private LocalDateTime startTime;

    @NotNull(message = "יש לבחור זמן סיום")
    @Future(message = "זמן סיום חייב להיות בעתיד")
    private LocalDateTime endTime;

    @Size(max = 300, message = "הערה יכולה להכיל עד 300 תווים")
    private String note;
}