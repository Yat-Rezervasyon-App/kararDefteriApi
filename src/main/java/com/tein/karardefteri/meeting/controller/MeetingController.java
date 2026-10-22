
package com.tein.karardefteri.meeting.controller;

import com.tein.karardefteri.meeting.dto.MeetingCreateRequest;
import com.tein.karardefteri.meeting.dto.MeetingUpdateRequest;
import com.tein.karardefteri.meeting.entity.Meeting;
import com.tein.karardefteri.meeting.service.MeetingService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService meetingService;

    public MeetingController(MeetingService meetingService) {
        this.meetingService = meetingService;
    }

    @GetMapping
    public ResponseEntity<List<Meeting>> getAllMeetings(HttpSession session) {
        UUID currentUserId = getUserId(session);

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(meetingService.getAllMeetings());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Meeting> getMeeting(
            @PathVariable UUID id,
            HttpSession session) {

        UUID currentUserId = getUserId(session);

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(meetingService.getMeetingById(id));
    }

    @PostMapping
    public ResponseEntity<Meeting> createMeeting(
            @Valid @RequestBody MeetingCreateRequest request,
            HttpSession session) {

        UUID currentUserId = getUserId(session);

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Meeting meeting = meetingService.createMeeting(request, currentUserId);

        return ResponseEntity.status(HttpStatus.CREATED).body(meeting);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Meeting> updateMeeting(
            @PathVariable UUID id,
            @Valid @RequestBody MeetingUpdateRequest request,
            HttpSession session) {

        UUID currentUserId = getUserId(session);

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isAdmin = "ADMIN".equals(session.getAttribute("role"));

        Meeting meeting = meetingService.updateMeeting(
                id,
                request.getTitle(),
                request.getDescription(),
                currentUserId,
                isAdmin
        );

        return ResponseEntity.ok(meeting);
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Meeting> archiveMeeting(
            @PathVariable UUID id,
            HttpSession session) {

        UUID currentUserId = getUserId(session);

        if (currentUserId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isAdmin = "ADMIN".equals(session.getAttribute("role"));

        Meeting meeting = meetingService.archiveMeeting(
                id,
                currentUserId,
                isAdmin
        );

        return ResponseEntity.ok(meeting);
    }

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
