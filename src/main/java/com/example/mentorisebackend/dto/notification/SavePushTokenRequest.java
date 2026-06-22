package com.example.mentorisebackend.dto.notification;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SavePushTokenRequest {

    @NotBlank
    private String token;

    private String platform;
}
