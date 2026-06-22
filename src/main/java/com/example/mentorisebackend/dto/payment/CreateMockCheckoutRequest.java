package com.example.mentorisebackend.dto.payment;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateMockCheckoutRequest {
    @NotNull
    private Long sessionOfferId;
}