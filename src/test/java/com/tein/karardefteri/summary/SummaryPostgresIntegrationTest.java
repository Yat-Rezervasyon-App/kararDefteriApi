
package com.tein.karardefteri.summary;

import com.tein.karardefteri.decision.dto.DecisionCreateRequest;
import com.tein.karardefteri.decision.dto.DecisionStatusRequest;
import com.tein.karardefteri.decision.entity.Decision;
import com.tein.karardefteri.decision.entity.DecisionStatus;
import com.tein.karardefteri.decision.service.DecisionService;
import com.tein.karardefteri.meeting.entity.Meeting;
import com.tein.karardefteri.meeting.repository.MeetingRepository;
import com.tein.karardefteri.meeting.service.MeetingService;
import com.tein.karardefteri.project.entity.Project;
import com.tein.karardefteri.project.repository.ProjectRepository;
import com.tein.karardefteri.summary.dto.ProjectSummaryResponseDto;
import com.tein.karardefteri.summary.dto.UserSummaryResponseDto;
import com.tein.karardefteri.summary.service.SummaryService;
import com.tein.karardefteri.task.entity.Task;
import com.tein.karardefteri.task.entity.TaskPriority;
import com.tein.karardefteri.task.entity.TaskStatus;
import com.tein.karardefteri.task.repository.TaskRepository;
import com.tein.karardefteri.task.service.TaskService;
import com.tein.karardefteri.user.entity.User;
import com.tein.karardefteri.user.entity.UserRole;
import com.tein.karardefteri.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
@Import(SummaryPostgresIntegrationTest.FixedClockTestConfig.class)
class SummaryPostgresIntegrationTest {

    private static final ZoneId TURKEY_ZONE =
            ZoneId.of("Europe/Istanbul");

    private static final Clock FIXED_CLOCK =
            Clock.fixed(
                    Instant.parse("2026-11-18T09:00:00Z"),
                    TURKEY_ZONE
            );

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MeetingService meetingService;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private DecisionService decisionService;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskService taskService;

    @Autowired
    private SummaryService summaryService;

    @TestConfiguration
    static class FixedClockTestConfig {

        @Bean
        @Primary
        public Clock fixedClock() {
            return FIXED_CLOCK;
        }
    }

    private User createUser() {
        User user = new User();
        user.setFullName("Summary Integration User");
        user.setEmail(
                "summary-" + java.util.UUID.randomUUID() + "@test.com"
        );
        user.setPasswordHash("test-password-hash");
        user.setRole(UserRole.MEMBER);
        user.setActive(true);

        return userRepository.save(user);
    }

    private Project createProject(User user) {
        Project project = new Project();
        project.setName("Summary Integration Project");
        project.setDescription("Summary PostgreSQL test");
        project.setCreatedBy(user.getId());

        return projectRepository.save(project);
    }

    private Meeting createMeeting(Project project, User user) {
        Meeting meeting = new Meeting();
        meeting.setProjectId(project.getId());
        meeting.setTitle("Summary Integration Meeting");
        meeting.setMeetingAt(OffsetDateTime.now(FIXED_CLOCK));
        meeting.setCreatedBy(user.getId());

        return meetingRepository.save(meeting);
    }

    private Task createTask(
            Meeting meeting,
            User assignee,
            String title,
            TaskStatus status
    ) {
        Task task = new Task();
        task.setMeetingId(meeting.getId());
        task.setAssigneeId(assignee.getId());
        task.setTitle(title);
        task.setStatus(status);
        task.setPriority(TaskPriority.MEDIUM);

        return taskRepository.save(task);
    }

    @Test
    void activeDecisionCountChangesAfterCreateAndCancel() {
        User owner = createUser();
        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        ProjectSummaryResponseDto initialSummary =
                summaryService.getProjectSummary(
                        project.getId(),
                        owner.getId(),
                        false
                );

        assertThat(initialSummary.activeDecisionCount())
                .isZero();

        DecisionCreateRequest createRequest =
                new DecisionCreateRequest();
        createRequest.setContent("Integration test decision");

        Decision decision = decisionService.createDecision(
                meeting.getId(),
                createRequest,
                owner.getId(),
                false
        );

        ProjectSummaryResponseDto createdSummary =
                summaryService.getProjectSummary(
                        project.getId(),
                        owner.getId(),
                        false
                );

        assertThat(createdSummary.activeDecisionCount())
                .isEqualTo(1);

        DecisionStatusRequest statusRequest =
                new DecisionStatusRequest();
        statusRequest.setStatus(DecisionStatus.CANCELLED);

        decisionService.updateDecisionStatus(
                decision.getId(),
                statusRequest,
                owner.getId(),
                false
        );

        ProjectSummaryResponseDto cancelledSummary =
                summaryService.getProjectSummary(
                        project.getId(),
                        owner.getId(),
                        false
                );

        assertThat(cancelledSummary.activeDecisionCount())
                .isZero();
    }

    @Test
    void projectSummaryCountsOnlyTasksBelongingToThatProject() {
        User owner = createUser();

        Project firstProject = createProject(owner);
        Project secondProject = createProject(owner);

        Meeting firstMeeting = createMeeting(firstProject, owner);
        Meeting secondMeeting = createMeeting(secondProject, owner);

        createTask(
                firstMeeting,
                owner,
                "First project task",
                TaskStatus.TODO
        );

        createTask(
                secondMeeting,
                owner,
                "Second project task 1",
                TaskStatus.TODO
        );

        createTask(
                secondMeeting,
                owner,
                "Second project task 2",
                TaskStatus.IN_PROGRESS
        );

        ProjectSummaryResponseDto firstSummary =
                summaryService.getProjectSummary(
                        firstProject.getId(),
                        owner.getId(),
                        false
                );

        ProjectSummaryResponseDto secondSummary =
                summaryService.getProjectSummary(
                        secondProject.getId(),
                        owner.getId(),
                        false
                );

        assertThat(firstSummary.openTaskCount()).isEqualTo(1);
        assertThat(secondSummary.openTaskCount()).isEqualTo(2);
    }

    @Test
    void overdueTaskCountExcludesTodayAndTomorrowTasks() {
        User owner = createUser();
        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        OffsetDateTime now = OffsetDateTime.now(FIXED_CLOCK);

        Task yesterdayTask = createTask(
                meeting,
                owner,
                "Yesterday task",
                TaskStatus.TODO
        );
        yesterdayTask.setDueDate(now.minusDays(1));
        taskRepository.save(yesterdayTask);

        Task todayTask = createTask(
                meeting,
                owner,
                "Today task",
                TaskStatus.TODO
        );
        todayTask.setDueDate(
                now.withHour(23)
                        .withMinute(59)
                        .withSecond(59)
                        .withNano(0)
        );
        taskRepository.save(todayTask);

        Task tomorrowTask = createTask(
                meeting,
                owner,
                "Tomorrow task",
                TaskStatus.TODO
        );
        tomorrowTask.setDueDate(now.plusDays(1));
        taskRepository.save(tomorrowTask);

        ProjectSummaryResponseDto summary =
                summaryService.getProjectSummary(
                        project.getId(),
                        owner.getId(),
                        false
                );

        assertThat(summary.overdueTaskCount()).isEqualTo(1);
    }

    @Test
    void userSummaryCountsOpenAndOverdueTasksCorrectly() {
        User owner = createUser();
        User anotherUser = createUser();

        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        Task overdueTask = createTask(
                meeting,
                owner,
                "Overdue task",
                TaskStatus.TODO
        );
        overdueTask.setDueDate(
                OffsetDateTime.now(FIXED_CLOCK).minusDays(1)
        );
        taskRepository.save(overdueTask);

        createTask(
                meeting,
                owner,
                "Open task",
                TaskStatus.IN_PROGRESS
        );

        Task completedTask = createTask(
                meeting,
                owner,
                "Completed task",
                TaskStatus.DONE
        );
        completedTask.setDueDate(
                OffsetDateTime.now(FIXED_CLOCK).minusDays(1)
        );
        taskRepository.save(completedTask);

        createTask(
                meeting,
                owner,
                "Task without due date",
                TaskStatus.TODO
        );

        createTask(
                meeting,
                anotherUser,
                "Another user's task",
                TaskStatus.TODO
        );

        UserSummaryResponseDto summary =
                summaryService.getUserSummary(owner.getId());

        assertThat(summary.openTaskCount()).isEqualTo(3);
        assertThat(summary.overdueTaskCount()).isEqualTo(1);
    }

    @Test
    void activeDecisionCountChangesAfterDelete() {
        User owner = createUser();
        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        DecisionCreateRequest createRequest =
                new DecisionCreateRequest();
        createRequest.setContent("Decision to delete");

        Decision decision = decisionService.createDecision(
                meeting.getId(),
                createRequest,
                owner.getId(),
                false
        );

        ProjectSummaryResponseDto createdSummary =
                summaryService.getProjectSummary(
                        project.getId(),
                        owner.getId(),
                        false
                );

        assertThat(createdSummary.activeDecisionCount())
                .isEqualTo(1);

        decisionService.deleteDecision(
                decision.getId(),
                owner.getId(),
                false
        );

        ProjectSummaryResponseDto deletedSummary =
                summaryService.getProjectSummary(
                        project.getId(),
                        owner.getId(),
                        false
                );

        assertThat(deletedSummary.activeDecisionCount())
                .isZero();
    }

    @Test
    void meetingCountRemainAfterArchive() {
        User owner = createUser();
        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        ProjectSummaryResponseDto beforeArchive =
                summaryService.getProjectSummary(
                        project.getId(),
                        owner.getId(),
                        false
                );

        assertThat(beforeArchive.meetingCount())
                .isEqualTo(1);

        meetingService.archiveMeeting(
                meeting.getId(),
                owner.getId(),
                false
        );

        ProjectSummaryResponseDto afterArchive =
                summaryService.getProjectSummary(
                        project.getId(),
                        owner.getId(),
                        false
                );

        assertThat(afterArchive.meetingCount())
                .isEqualTo(1);
    }

    @Test
    void openTaskCountDecreasesAfterTaskCompleted() {
        User owner = createUser();
        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        Task task = createTask(
                meeting,
                owner,
                "Complete task",
                TaskStatus.TODO
        );

        ProjectSummaryResponseDto beforeComplete =
                summaryService.getProjectSummary(
                        project.getId(),
                        owner.getId(),
                        false
                );

        assertThat(beforeComplete.openTaskCount())
                .isEqualTo(1);

        taskService.updateTaskStatus(
                task.getId(),
                "DONE",
                owner.getId()
        );

        ProjectSummaryResponseDto afterComplete =
                summaryService.getProjectSummary(
                        project.getId(),
                        owner.getId(),
                        false
                );

        assertThat(afterComplete.openTaskCount())
                .isZero();
    }

    @Test
    void openTaskCountIncludesTodoAndInProgressTasks() {
        User owner = createUser();
        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        createTask(
                meeting,
                owner,
                "Todo task",
                TaskStatus.TODO
        );

        createTask(
                meeting,
                owner,
                "In progress task",
                TaskStatus.IN_PROGRESS
        );

        createTask(
                meeting,
                owner,
                "Completed task",
                TaskStatus.DONE
        );

        ProjectSummaryResponseDto summary =
                summaryService.getProjectSummary(
                        project.getId(),
                        owner.getId(),
                        false
                );

        assertThat(summary.openTaskCount()).isEqualTo(2);
    }

    @Test
    void projectMeetingDecisionTaskWorkflowUpdatesSummary() {
        User owner = createUser();
        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        DecisionCreateRequest decisionRequest =
                new DecisionCreateRequest();
        decisionRequest.setContent("Complete project task");

        decisionService.createDecision(
                meeting.getId(),
                decisionRequest,
                owner.getId(),
                false
        );

        Task task = createTask(
                meeting,
                owner,
                "Complete project work",
                TaskStatus.TODO
        );

        ProjectSummaryResponseDto beforeCompletion =
                summaryService.getProjectSummary(
                        project.getId(),
                        owner.getId(),
                        false
                );

        assertThat(beforeCompletion.meetingCount()).isEqualTo(1);
        assertThat(beforeCompletion.activeDecisionCount()).isEqualTo(1);
        assertThat(beforeCompletion.openTaskCount()).isEqualTo(1);
        assertThat(beforeCompletion.overdueTaskCount()).isZero();

        taskService.updateTaskStatus(
                task.getId(),
                "DONE",
                owner.getId()
        );

        ProjectSummaryResponseDto afterCompletion =
                summaryService.getProjectSummary(
                        project.getId(),
                        owner.getId(),
                        false
                );

        assertThat(afterCompletion.meetingCount()).isEqualTo(1);
        assertThat(afterCompletion.activeDecisionCount()).isEqualTo(1);
        assertThat(afterCompletion.openTaskCount()).isZero();
        assertThat(afterCompletion.overdueTaskCount()).isZero();
    }

    @Test
    void overdueTaskCountExcludesCompletedAndTasksWithoutDueDate() {
        User owner = createUser();
        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        Task overdueTask = createTask(
                meeting,
                owner,
                "Overdue task",
                TaskStatus.TODO
        );
        overdueTask.setDueDate(
                OffsetDateTime.now(FIXED_CLOCK).minusDays(1)
        );
        taskRepository.save(overdueTask);

        Task completedTask = createTask(
                meeting,
                owner,
                "Completed task",
                TaskStatus.DONE
        );
        completedTask.setDueDate(
                OffsetDateTime.now(FIXED_CLOCK).minusDays(1)
        );
        taskRepository.save(completedTask);

        createTask(
                meeting,
                owner,
                "Task without due date",
                TaskStatus.TODO
        );

        ProjectSummaryResponseDto summary =
                summaryService.getProjectSummary(
                        project.getId(),
                        owner.getId(),
                        false
                );

        assertThat(summary.overdueTaskCount()).isEqualTo(1);
    }
}
