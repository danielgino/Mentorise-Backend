package com.example.mentorisebackend.repository;

import com.example.mentorisebackend.api.entity.Payment;
import com.example.mentorisebackend.api.entity.SessionOffer;
import com.example.mentorisebackend.enums.PaymentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByCheckoutSessionId(String checkoutSessionId);

    Optional<Payment> findTopBySessionOfferIdOrderByCreatedAtDesc(Long sessionOfferId);

    List<Payment> findBySessionOfferIdOrderByCreatedAtDesc(Long sessionOfferId);

    boolean existsBySessionOfferIdAndStatus(Long sessionOfferId, PaymentStatus status);
}