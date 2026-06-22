package com.example.mentorisebackend.service.payment;

import com.example.mentorisebackend.security.CurrentUser;
import com.example.mentorisebackend.api.entity.Payment;
import com.example.mentorisebackend.api.entity.SessionOffer;
import com.example.mentorisebackend.api.entity.User;
import com.example.mentorisebackend.dto.payment.ConfirmMockPaymentRequest;
import com.example.mentorisebackend.dto.payment.PaymentResponse;
import com.example.mentorisebackend.enums.PaymentProvider;
import com.example.mentorisebackend.enums.PaymentStatus;
import com.example.mentorisebackend.enums.SessionOfferStatus;
import com.example.mentorisebackend.repository.PaymentRepository;
import com.example.mentorisebackend.repository.SessionOfferRepository;
import com.example.mentorisebackend.util.AppConstants;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final SessionOfferRepository sessionOfferRepository;
    private final CurrentUser currentUser;
    private final EntityManager entityManager;

    public PaymentResponse createCheckoutSession(Long sessionOfferId) {
        SessionOffer sessionOffer = sessionOfferRepository.findById(sessionOfferId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppConstants.OFFER_NOT_FOUND));

        Long currentUserId = currentUser.getUserId();
        validateStudentOwnership(sessionOffer, currentUserId);
        validateOfferCanStartPayment(sessionOffer);

        if (paymentRepository.existsBySessionOfferIdAndStatus(sessionOfferId, PaymentStatus.SUCCESS)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppConstants.OFFER_ALREADY_PAID);
        }

        User student = entityManager.getReference(User.class, currentUserId);

        Payment payment = Payment.builder()
                .sessionOffer(sessionOffer)
                .payer(student)
                .amount(sessionOffer.getPrice())
                .provider(PaymentProvider.MOCK)
                .status(PaymentStatus.CREATED)
                .checkoutSessionId(generateCheckoutSessionId())
                .build();

        paymentRepository.save(payment);

        return buildResponse(payment, AppConstants.MOCK_PAYMENT_CREATED_SUCCESSFULLY);
    }

    public PaymentResponse confirmMockPayment(ConfirmMockPaymentRequest request) {
        Payment payment = paymentRepository.findByCheckoutSessionId(request.getCheckoutSessionId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, AppConstants.PAYMENT_SESSION_NOT_FOUND));

        SessionOffer sessionOffer = payment.getSessionOffer();
        Long currentUserId = currentUser.getUserId();

        validatePaymentOwnership(payment, currentUserId);
        validatePaymentCanBeConfirmed(payment, sessionOffer);

        String sanitizedCardNumber = sanitizeCardNumber(request.getCardNumber());
        validateMockPaymentForm(request, sanitizedCardNumber);

        payment.setStatus(PaymentStatus.PENDING);

        if (AppConstants.MOCK_DECLINED_CARD.equals(sanitizedCardNumber)) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(AppConstants.CARD_DECLINED);
            paymentRepository.save(payment);

            return buildResponse(payment, AppConstants.PAYMENT_FAILED);
        }

        if (!passesLuhn(sanitizedCardNumber)) {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(AppConstants.INVALID_CARD_NUMBER_LUHN);
            paymentRepository.save(payment);

            return buildResponse(payment, AppConstants.PAYMENT_FAILED);
        }

        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setFailureReason(null);
        payment.setTransactionRef(generateTransactionRef());
        payment.setCardBrand(detectCardBrand(sanitizedCardNumber));
        payment.setCardLast4(extractLast4(sanitizedCardNumber));
        payment.setPaidAt(LocalDateTime.now());

        sessionOffer.setStatus(SessionOfferStatus.ACCEPTED);

        paymentRepository.save(payment);
        sessionOfferRepository.save(sessionOffer);

        return buildResponse(payment, AppConstants.PAYMENT_CONFIRMED_SUCCESSFULLY);
    }

    private void validateStudentOwnership(SessionOffer sessionOffer, Long currentUserId) {
        if (sessionOffer.getStudentUserId() == null || !sessionOffer.getStudentUserId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, AppConstants.NO_PERMISSION_TO_PAY_OFFER);
        }
    }

    private void validatePaymentOwnership(Payment payment, Long currentUserId) {
        if (payment.getPayer() == null
                || payment.getPayer().getId() == null
                || !payment.getPayer().getId().equals(currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, AppConstants.NO_PERMISSION_TO_CONFIRM_PAYMENT);
        }
    }

    private void validateOfferCanStartPayment(SessionOffer sessionOffer) {
        SessionOfferStatus status = sessionOffer.getStatus();

        if (status == SessionOfferStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppConstants.OFFER_ALREADY_APPROVED);
        }

        if (status == SessionOfferStatus.DECLINED || status == SessionOfferStatus.EXPIRED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppConstants.OFFER_CANNOT_BE_PAID_IN_CURRENT_STATUS);
        }

        if (status != SessionOfferStatus.PENDING) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppConstants.OFFER_NOT_AVAILABLE_FOR_PAYMENT);
        }
    }

    private void validatePaymentCanBeConfirmed(Payment payment, SessionOffer sessionOffer) {
        if (payment.getStatus() == PaymentStatus.SUCCESS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppConstants.PAYMENT_ALREADY_APPROVED);
        }

        if (payment.getStatus() == PaymentStatus.CANCELLED || payment.getStatus() == PaymentStatus.EXPIRED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppConstants.PAYMENT_CANNOT_BE_CONFIRMED);
        }

        if (sessionOffer.getStatus() == SessionOfferStatus.ACCEPTED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppConstants.OFFER_ALREADY_APPROVED);
        }
    }

    private void validateMockPaymentForm(ConfirmMockPaymentRequest request, String sanitizedCardNumber) {
        if (request.getCardHolderName() == null || request.getCardHolderName().trim().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppConstants.CARD_HOLDER_NAME_REQUIRED);
        }

        if (!sanitizedCardNumber.matches(AppConstants.CARD_NUMBER_VALIDATION_PATTERN)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppConstants.INVALID_CARD_NUMBER);
        }

        if (request.getCvv() == null || !request.getCvv().trim().matches(AppConstants.CVV_VALIDATION_PATTERN)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppConstants.INVALID_CVV);
        }

        if (!isValidExpiry(request.getExpiry())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, AppConstants.INVALID_CARD_EXPIRY);
        }

        if (!AppConstants.MOCK_SUCCESS_CARD.equals(sanitizedCardNumber) && !AppConstants.MOCK_DECLINED_CARD.equals(sanitizedCardNumber)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    AppConstants.MOCK_CARD_INSTRUCTIONS
            );
        }
    }

    private PaymentResponse buildResponse(Payment payment, String message) {
        return PaymentResponse.builder()
                .paymentId(payment.getId())
                .checkoutSessionId(payment.getCheckoutSessionId())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .transactionRef(payment.getTransactionRef())
                .cardBrand(payment.getCardBrand())
                .cardLast4(payment.getCardLast4())
                .message(message)
                .build();
    }

    private String sanitizeCardNumber(String cardNumber) {
        if (cardNumber == null) {
            return "";
        }
        return cardNumber.replaceAll("[^0-9]", "");
    }

    private String detectCardBrand(String cardNumber) {
        if (cardNumber.startsWith("4")) return AppConstants.CARD_BRAND_VISA;
        if (cardNumber.startsWith("5")) return AppConstants.CARD_BRAND_MASTERCARD;
        if (cardNumber.startsWith("34") || cardNumber.startsWith("37")) return AppConstants.CARD_BRAND_AMEX;
        return AppConstants.CARD_BRAND_UNKNOWN;
    }

    private String extractLast4(String cardNumber) {
        if (cardNumber.length() < 4) {
            return cardNumber;
        }
        return cardNumber.substring(cardNumber.length() - 4);
    }

    private boolean isValidExpiry(String expiry) {
        if (expiry == null || !expiry.matches(AppConstants.EXPIRY_VALIDATION_PATTERN)) {
            return false;
        }

        int month = Integer.parseInt(expiry.substring(0, 2));
        int year = 2000 + Integer.parseInt(expiry.substring(3, 5));

        LocalDate now = LocalDate.now();
        LocalDate expiryDate = LocalDate.of(year, month, 1).withDayOfMonth(1);

        return !expiryDate.plusMonths(1).minusDays(1).isBefore(now);
    }

    private boolean passesLuhn(String cardNumber) {
        int sum = 0;
        boolean shouldDouble = false;

        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = cardNumber.charAt(i) - '0';

            if (shouldDouble) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            shouldDouble = !shouldDouble;
        }

        return sum % 10 == 0;
    }

    private String generateCheckoutSessionId() {
        return AppConstants.CHECKOUT_SESSION_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }

    private String generateTransactionRef() {
        return AppConstants.TRANSACTION_REF_PREFIX + UUID.randomUUID().toString().replace("-", "");
    }
}