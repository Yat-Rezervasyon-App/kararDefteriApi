package com.tein.karardefteri.decision;

import com.tein.karardefteri.decision.entity.Decision;
import com.tein.karardefteri.decision.entity.DecisionStatus;
import com.tein.karardefteri.decision.repository.DecisionRepository;
import com.tein.karardefteri.decision.service.DecisionService;
import com.tein.karardefteri.meeting.entity.Meeting;
import com.tein.karardefteri.meeting.repository.MeetingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import com.tein.karardefteri.decision.dto.DecisionStatusRequest;
import java.util.Optional;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DecisionServiceTest {

    @Mock
    private DecisionRepository decisionRepository;

    @Mock
    private MeetingRepository meetingRepository;

    @Mock
    private JdbcTemplate jdbcTemplate;

    @InjectMocks
    private DecisionService decisionService;

    private UUID projectId;
    private UUID meetingId;
    private UUID decisionId;
    private UUID ownerId;
    private UUID anotherUserId;

    private Meeting meeting;
    private Decision decision;

    @BeforeEach
    void setUp() {
        projectId = UUID.randomUUID();
        meetingId = UUID.randomUUID();
        decisionId = UUID.randomUUID();
        ownerId = UUID.randomUUID();
        anotherUserId = UUID.randomUUID();

        meeting = new Meeting();
        meeting.setCreatedBy(ownerId);
        meeting.setProjectId(projectId);
        meeting.setArchived(false);

        decision = new Decision();
        decision.setMeetingId(meetingId);
        decision.setStatus(DecisionStatus.ACTIVE);
    }
    @Test
    void ownerCanDeleteDecision() {
        when(decisionRepository.findById(decisionId))
                .thenReturn(Optional.of(decision));

        when(meetingRepository.findById(meetingId))
                .thenReturn(Optional.of(meeting));

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Boolean.class),
                eq(projectId)
        )).thenReturn(false);

        decisionService.deleteDecision(decisionId, ownerId, false);

        verify(decisionRepository).delete(decision);
    }
    @Test
    void anotherUserCannotCancelDecision() {
        when(decisionRepository.findById(decisionId))
                .thenReturn(Optional.of(decision));

        when(meetingRepository.findById(meetingId))
                .thenReturn(Optional.of(meeting));

        DecisionStatusRequest request = new DecisionStatusRequest();
        request.setStatus(DecisionStatus.CANCELLED);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> decisionService.updateDecisionStatus(
                        decisionId, request, anotherUserId, false)
        );

        assertEquals(403, exception.getStatusCode().value());
        verify(decisionRepository, never()).save(any(Decision.class));
    }
    @Test
    void anotherUserCannotDeleteDecision() {
        when(decisionRepository.findById(decisionId))
                .thenReturn(Optional.of(decision));

        when(meetingRepository.findById(meetingId))
                .thenReturn(Optional.of(meeting));

        assertThrows(
                ResponseStatusException.class,
                () -> decisionService.deleteDecision(
                        decisionId, anotherUserId, false)
        );

        verify(decisionRepository, never()).delete(any(Decision.class));
    }
    @Test
    void archivedMeetingCannotCancelDecision() {
        meeting.setArchived(true);

        when(decisionRepository.findById(decisionId))
                .thenReturn(Optional.of(decision));

        when(meetingRepository.findById(meetingId))
                .thenReturn(Optional.of(meeting));

        DecisionStatusRequest request = new DecisionStatusRequest();
        request.setStatus(DecisionStatus.CANCELLED);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> decisionService.updateDecisionStatus(
                        decisionId, request, ownerId, false)
        );

        assertEquals(409, exception.getStatusCode().value());
        verify(decisionRepository, never()).save(any(Decision.class));
    }
    @Test
    void updateDecisionStatus_shouldSaveCancelledStatus() {
        when(decisionRepository.findById(decisionId))
                .thenReturn(Optional.of(decision));

        when(meetingRepository.findById(meetingId))
                .thenReturn(Optional.of(meeting));

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Boolean.class),
                eq(projectId)
        )).thenReturn(false);

        when(decisionRepository.saveAndFlush(any(Decision.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DecisionStatusRequest request = new DecisionStatusRequest();
        request.setStatus(DecisionStatus.CANCELLED);

        Decision result = decisionService.updateDecisionStatus(
                decisionId, request, ownerId, false);

        assertEquals(DecisionStatus.CANCELLED, result.getStatus());
        verify(decisionRepository).saveAndFlush(decision);
    }
    @Test
    void countActiveDecisionsByProjectId_shouldOnlyCountActiveDecisions() {
        when(jdbcTemplate.queryForObject(
                contains("d.status = 'ACTIVE'"),
                eq(Long.class),
                eq(projectId)
        )).thenReturn(2L);

        long result = decisionService.countActiveDecisionsByProjectId(projectId);

        assertEquals(2L, result);
    }
    @Test
    void countActiveDecisionsByProjectId_shouldReturnCount() {
        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Long.class),
                eq(projectId)
        )).thenReturn(5L);

        long result = decisionService.countActiveDecisionsByProjectId(projectId);

        assertEquals(5L, result);
    }
    @Test
    void archivedProjectCannotCancelDecision() {
        when(decisionRepository.findById(decisionId))
                .thenReturn(Optional.of(decision));

        when(meetingRepository.findById(meetingId))
                .thenReturn(Optional.of(meeting));

        when(jdbcTemplate.queryForObject(
                anyString(),
                eq(Boolean.class),
                eq(projectId)
        )).thenReturn(true);

        DecisionStatusRequest request = new DecisionStatusRequest();
        request.setStatus(DecisionStatus.CANCELLED);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> decisionService.updateDecisionStatus(
                        decisionId, request, ownerId, false)
        );

        assertEquals(409, exception.getStatusCode().value());
        verify(decisionRepository, never()).save(any(Decision.class));
    }
}