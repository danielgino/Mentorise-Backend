package com.example.mentorisebackend.dto.payment;


import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentResponse {
    private Long paymentId;
    private String checkoutSessionId;
    private String status;
    private BigDecimal amount;
    private String transactionRef;
    private String cardBrand;
    private String cardLast4;
    private String message;
}