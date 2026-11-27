package com.tein.karardefteri.summary.service;

import com.tein.karardefteri.decision.service.DecisionService;
import com.tein.karardefteri.meeting.service.MeetingService;
import com.tein.karardefteri.project.service.ProjectService;
import com.tein.karardefteri.summary.dto.ProjectSummaryResponseDto;
import com.tein.karardefteri.summary.dto.UserSummaryResponseDto;
import com.tein.karardefteri.task.service.TaskService;
import java.time.ZoneId;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Service
public class SummaryService {

    private static final ZoneId TURKEY_ZONE =
            ZoneId.of("Europe/Istanbul");

    private final ProjectService projectService;
    private final MeetingService meetingService;
    private final DecisionService decisionService;
    private final TaskService taskService;
    private final Clock clock;

    public SummaryService(
            ProjectService projectService,
            MeetingService meetingService,
            DecisionService decisionService,
            TaskService taskService,
            Clock clock
    ) {
        this.projectService = projectService;
        this.meetingService = meetingService;
        this.decisionService = decisionService;
        this.taskService = taskService;
        this.clock = clock;
    }

    public ProjectSummaryResponseDto getProjectSummary(
            UUID projectId,
            UUID currentUserId,
            boolean isAdmin
    ) {
        projectService.getProjectById(projectId);

        if (!isAdmin && !projectService.isOwner(projectId, currentUserId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }

        OffsetDateTime todayStart = LocalDate.now(clock)
                .atStartOfDay(TURKEY_ZONE)
                .toOffsetDateTime();

        return new ProjectSummaryResponseDto(
                meetingService.countMeetingsByProjectId(projectId),
                decisionService.countActiveDecisionsByProjectId(projectId),
                taskService.countOpenTasksByProjectId(projectId),
                taskService.countOverdueTasksByProjectId(projectId, todayStart)
        );
    }

    public UserSummaryResponseDto getUserSummary(UUID userId) {

        OffsetDateTime todayStart = LocalDate.now(clock)
                .atStartOfDay(TURKEY_ZONE)
                .toOffsetDateTime();

        return new UserSummaryResponseDto(
                taskService.countOpenTasksByAssigneeId(userId),
                taskService.countOverdueTasksByAssigneeId(userId, todayStart)
        );
    }
}