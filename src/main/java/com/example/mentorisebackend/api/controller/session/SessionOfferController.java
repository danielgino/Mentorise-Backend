package com.example.mentorisebackend.api.controller.session;

import com.example.mentorisebackend.security.CurrentUser;
import com.example.mentorisebackend.api.entity.SessionOffer;
import com.example.mentorisebackend.dto.session.SessionOfferCardDto;
import com.example.mentorisebackend.dto.session.CreateSessionOfferRequest;
import com.example.mentorisebackend.service.session.SessionOfferService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users/session-offers")
@RequiredArgsConstructor
public class SessionOfferController {

    private final SessionOfferService sessionOfferService;
    private final CurrentUser currentUser;
    @PreAuthorize("hasRole('TUTOR')")
    @PostMapping
    public ResponseEntity<SessionOffer> createOffer(@Valid @RequestBody CreateSessionOfferRequest request) {
        SessionOffer createdOffer = sessionOfferService.createOffer(currentUser.getUserId(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdOffer);
    }

    @GetMapping("/pending")
    public ResponseEntity<List<SessionOfferCardDto>> getMyPendingOffers() {
        List<SessionOfferCardDto> offers =
                sessionOfferService.getMyPendingOffers(currentUser.getUserId());
        return ResponseEntity.ok(offers);
    }

    @GetMapping("/upcoming")
    public ResponseEntity<List<SessionOfferCardDto>> getMyUpcomingLessons() {
        List<SessionOfferCardDto> offers =
                sessionOfferService.getMyUpcomingLessons(currentUser.getUserId());
        return ResponseEntity.ok(offers);
    }

    @GetMapping("/completed")
    public ResponseEntity<List<SessionOfferCardDto>> getMyCompletedLessons() {
        List<SessionOfferCardDto> offers =
                sessionOfferService.getMyCompletedLessons(currentUser.getUserId());
        return ResponseEntity.ok(offers);
    }

    @PostMapping("/{offerId}/decline")
    public ResponseEntity<Void> declineOffer(@PathVariable Long offerId) {
        sessionOfferService.declineOfferForCurrentUser(currentUser.getUserId(), offerId);
        return ResponseEntity.noContent().build();
    }
}