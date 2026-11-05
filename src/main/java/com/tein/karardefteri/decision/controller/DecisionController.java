package com.tein.karardefteri.decision.controller;

import com.tein.karardefteri.decision.dto.DecisionCreateRequest;
import com.tein.karardefteri.decision.dto.DecisionStatusRequest;
import com.tein.karardefteri.decision.entity.Decision;
import com.tein.karardefteri.decision.service.DecisionService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
public class DecisionController {

    private final DecisionService decisionService;

    public DecisionController(DecisionService decisionService) {
        this.decisionService = decisionService;
    }

    // Toplantıya ait kararları listeler.
    @GetMapping("/api/meetings/{meetingId}/decisions")
    public ResponseEntity<List<Decision>> getDecisions(
            @PathVariable UUID meetingId,
            HttpSession session) {

        UUID currentUserId = getUserId(session);

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                decisionService.getDecisionsByMeetingId(meetingId));
    }

    // Yeni karar oluşturur.
    @PostMapping("/api/meetings/{meetingId}/decisions")
    public ResponseEntity<Decision> createDecision(
            @PathVariable UUID meetingId,
            @Valid @RequestBody DecisionCreateRequest request,
            HttpSession session) {

        UUID currentUserId = getUserId(session);

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isAdmin = "ADMIN".equals(session.getAttribute("role"));

        Decision decision = decisionService.createDecision(
                meetingId, request, currentUserId, isAdmin);

        return ResponseEntity.status(HttpStatus.CREATED).body(decision);
    }

    // Kararın içeriğini günceller.
    @PutMapping("/api/decisions/{id}")
    public ResponseEntity<Decision> updateDecision(
            @PathVariable UUID id,
            @Valid @RequestBody DecisionCreateRequest request,
            HttpSession session) {

        UUID currentUserId = getUserId(session);

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isAdmin = "ADMIN".equals(session.getAttribute("role"));

        Decision decision = decisionService.updateDecision(
                id, request, currentUserId, isAdmin);

        return ResponseEntity.ok(decision);
    }

    // Kararı siler.
    @DeleteMapping("/api/decisions/{id}")
    public ResponseEntity<Void> deleteDecision(
            @PathVariable UUID id,
            HttpSession session) {

        UUID currentUserId = getUserId(session);

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isAdmin = "ADMIN".equals(session.getAttribute("role"));

        decisionService.deleteDecision(id, currentUserId, isAdmin);

        return ResponseEntity.noContent().build();
    }

    // Kararın durumunu değiştirir.
    @PatchMapping("/api/decisions/{id}/status")
    public ResponseEntity<Decision> updateDecisionStatus(
            @PathVariable UUID id,
            @Valid @RequestBody DecisionStatusRequest request,
            HttpSession session) {

        UUID currentUserId = getUserId(session);

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isAdmin = "ADMIN".equals(session.getAttribute("role"));

        Decision decision = decisionService.updateDecisionStatus(
                id, request, currentUserId, isAdmin);

        return ResponseEntity.ok(decision);
    }

    // Oturumdaki kullanıcı ID'sini alır.
    private UUID getUserId(HttpSession session) {
        Object userId = session.getAttribute("userId");

        if (userId instanceof UUID) {
            return (UUID) userId;
        }

        if (userId instanceof String) {
            try {
                return UUID.fromString((String) userId);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        return null;
    }
}

