
package com.tein.karardefteri.summary;

import com.tein.karardefteri.decision.service.DecisionService;
import com.tein.karardefteri.meeting.service.MeetingService;
import com.tein.karardefteri.project.entity.Project;
import com.tein.karardefteri.summary.dto.UserSummaryResponseDto;
import com.tein.karardefteri.project.service.ProjectService;
import com.tein.karardefteri.summary.dto.ProjectSummaryResponseDto;
import com.tein.karardefteri.summary.service.SummaryService;
import com.tein.karardefteri.task.service.TaskService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.junit.jupiter.api.BeforeEach;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SummaryServiceTest {

    @Mock
    private ProjectService projectService;

    @Mock
    private MeetingService meetingService;

    @Mock
    private DecisionService decisionService;

    @Mock
    private TaskService taskService;
    private SummaryService summaryService;

    @BeforeEach
    void setUp() {
        summaryService = new SummaryService(
                projectService,
                meetingService,
                decisionService,
                taskService,
                Clock.fixed(
                        Instant.parse("2026-11-18T09:00:00Z"),
                        ZoneId.of("Europe/Istanbul")
                )
        );
    }
    @Test
    void getProjectSummary_owner_shouldReturnCounts() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectService.getProjectById(projectId))
                .thenReturn(new Project());

        when(projectService.isOwner(projectId, userId))
                .thenReturn(true);

        when(meetingService.countMeetingsByProjectId(projectId))
                .thenReturn(5L);

        when(decisionService.countActiveDecisionsByProjectId(projectId))
                .thenReturn(3L);

        when(taskService.countOpenTasksByProjectId(projectId))
                .thenReturn(8L);

        when(taskService.countOverdueTasksByProjectId(
                org.mockito.ArgumentMatchers.eq(projectId),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(2L);

        ProjectSummaryResponseDto result =
                summaryService.getProjectSummary(projectId, userId, false);

        assertEquals(5L, result.meetingCount());
        assertEquals(3L, result.activeDecisionCount());
        assertEquals(8L, result.openTaskCount());
        assertEquals(2L, result.overdueTaskCount());
    }

    @Test
    void getProjectSummary_admin_shouldReturnCountsWithoutOwnerCheck() {
        UUID projectId = UUID.randomUUID();
        UUID adminId = UUID.randomUUID();

        when(projectService.getProjectById(projectId))
                .thenReturn(new Project());

        when(meetingService.countMeetingsByProjectId(projectId))
                .thenReturn(2L);

        when(decisionService.countActiveDecisionsByProjectId(projectId))
                .thenReturn(1L);

        when(taskService.countOpenTasksByProjectId(projectId))
                .thenReturn(4L);

        when(taskService.countOverdueTasksByProjectId(
                org.mockito.ArgumentMatchers.eq(projectId),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(1L);

        ProjectSummaryResponseDto result =
                summaryService.getProjectSummary(projectId, adminId, true);

        assertEquals(2L, result.meetingCount());
        assertEquals(1L, result.activeDecisionCount());
        assertEquals(4L, result.openTaskCount());
        assertEquals(1L, result.overdueTaskCount());

        verify(projectService, never()).isOwner(projectId, adminId);
    }

    @Test
    void getProjectSummary_nonOwner_shouldThrowForbidden() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectService.getProjectById(projectId))
                .thenReturn(new Project());

        when(projectService.isOwner(projectId, userId))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> summaryService.getProjectSummary(projectId, userId, false)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(meetingService, never())
                .countMeetingsByProjectId(projectId);

        verify(decisionService, never())
                .countActiveDecisionsByProjectId(projectId);

        verify(taskService, never())
                .countOpenTasksByProjectId(projectId);
    }
    @Test
    void getProjectSummary_projectNotFound_shouldThrowNotFound(){
        UUID projectId=UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectService.getProjectById(projectId))
                .thenThrow(new ResponseStatusException(HttpStatus.NOT_FOUND));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> summaryService.getProjectSummary(projectId,userId,false)
        );

        assertEquals(HttpStatus.NOT_FOUND,exception.getStatusCode());

        verify(projectService,never()).isOwner(projectId,userId);
        verify(meetingService,never()).countMeetingsByProjectId(projectId);
        verify(decisionService,never()).countActiveDecisionsByProjectId(projectId);
        verify(taskService,never()).countOpenTasksByProjectId(projectId);

    }
    @Test
    void getUserSummary_shouldReturnCounts() {
        UUID userId = UUID.randomUUID();

        when(taskService.countOpenTasksByAssigneeId(userId))
                .thenReturn(6L);

        when(taskService.countOverdueTasksByAssigneeId(
                org.mockito.ArgumentMatchers.eq(userId),
                org.mockito.ArgumentMatchers.any()
        )).thenReturn(2L);

        UserSummaryResponseDto result =
                summaryService.getUserSummary(userId);

        assertEquals(6L, result.openTaskCount());
        assertEquals(2L, result.overdueTaskCount());
    }
    @Test
    void getProjectSummary_emptyProject_shouldReturnZeroCounts() {
        UUID projectId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();

        when(projectService.getProjectById(projectId))
                .thenReturn(new Project());

        when(projectService.isOwner(projectId, userId))
                .thenReturn(true);

        ProjectSummaryResponseDto result =
                summaryService.getProjectSummary(projectId, userId, false);

        assertEquals(0L, result.meetingCount());
        assertEquals(0L, result.activeDecisionCount());
        assertEquals(0L, result.openTaskCount());
        assertEquals(0L, result.overdueTaskCount());
    }




}
