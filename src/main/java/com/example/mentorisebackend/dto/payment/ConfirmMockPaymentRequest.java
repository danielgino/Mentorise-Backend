package com.example.mentorisebackend.dto.payment;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ConfirmMockPaymentRequest {

    @NotBlank
    private String checkoutSessionId;

    @NotBlank
    private String cardNumber;

    @NotBlank
    private String cardHolderName;

    @NotBlank
    private String expiry;

    @NotBlank
    private String cvv;
}