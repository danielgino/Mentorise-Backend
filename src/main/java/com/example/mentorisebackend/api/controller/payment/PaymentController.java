package com.example.mentorisebackend.api.controller.payment;

import com.example.mentorisebackend.dto.payment.ConfirmMockPaymentRequest;
import com.example.mentorisebackend.dto.payment.CreateMockCheckoutRequest;
import com.example.mentorisebackend.dto.payment.PaymentResponse;
import com.example.mentorisebackend.service.payment.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/mock/checkout")
    public PaymentResponse createCheckout(@Valid @RequestBody CreateMockCheckoutRequest request) {
        return paymentService.createCheckoutSession(request.getSessionOfferId());
    }

    @PostMapping("/mock/confirm")
    public PaymentResponse confirmPayment(@Valid @RequestBody ConfirmMockPaymentRequest request) {
        return paymentService.confirmMockPayment(request);
    }
}