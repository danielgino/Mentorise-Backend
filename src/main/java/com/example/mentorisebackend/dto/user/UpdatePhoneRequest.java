package com.example.mentorisebackend.dto.user;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdatePhoneRequest {

    @NotBlank(message = "מספר טלפון הוא שדה חובה")
    @Pattern(
            regexp = "^(?:0(?:2|3|4|8|9)\\d{7}|0(?:5|7)\\d{8})$",
            message = "יש להזין מספר טלפון ישראלי תקין ללא מקפים"
    )
    private String phoneNumber;
}