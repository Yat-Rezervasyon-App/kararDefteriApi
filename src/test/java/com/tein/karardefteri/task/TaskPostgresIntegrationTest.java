package com.tein.karardefteri.task;

import com.tein.karardefteri.meeting.entity.Meeting;
import com.tein.karardefteri.meeting.repository.MeetingRepository;
import com.tein.karardefteri.project.entity.Project;
import com.tein.karardefteri.project.repository.ProjectRepository;
import com.tein.karardefteri.task.dto.TaskRequestDto;
import com.tein.karardefteri.task.entity.Task;
import com.tein.karardefteri.task.entity.TaskPriority;
import com.tein.karardefteri.task.entity.TaskStatus;
import com.tein.karardefteri.task.repository.TaskRepository;
import com.tein.karardefteri.task.repository.TaskSpecification;
import com.tein.karardefteri.task.service.TaskService;
import com.tein.karardefteri.user.entity.User;
import com.tein.karardefteri.user.entity.UserRole;
import com.tein.karardefteri.user.repository.UserRepository;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Transactional
@Import(TaskPostgresIntegrationTest.FixedClockTestConfig.class)
public class TaskPostgresIntegrationTest {

    private static final ZoneId TURKEY_ZONE =
            ZoneId.of("Europe/Istanbul");

    private static final Clock FIXED_CLOCK =
            Clock.fixed(
                    Instant.parse("2026-11-18T09:00:00Z"),
                    TURKEY_ZONE
            );

    private static final LocalDate TODAY =
            LocalDate.now(FIXED_CLOCK);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private MeetingRepository meetingRepository;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private TaskService taskService;

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
        user.setFullName("Integration Test User");
        user.setEmail(
                "integration-" + java.util.UUID.randomUUID() + "@test.com"
        );
        user.setPasswordHash("test-password-hash");
        user.setRole(UserRole.MEMBER);
        user.setActive(true);

        return userRepository.save(user);
    }

    private Project createProject(User user) {
        Project project = new Project();
        project.setName("Integration Test Project");
        project.setDescription("PostgreSQL integration test");
        project.setCreatedBy(user.getId());

        return projectRepository.save(project);
    }

    private Meeting createMeeting(Project project, User user) {
        Meeting meeting = new Meeting();
        meeting.setProjectId(project.getId());
        meeting.setTitle("Integration Test Meeting");
        meeting.setMeetingAt(OffsetDateTime.now(FIXED_CLOCK));
        meeting.setCreatedBy(user.getId());

        return meetingRepository.save(meeting);
    }

    private Task createTask(
            Meeting meeting,
            String title,
            LocalDate dueDate,
            TaskStatus status
    ) {
        Task task = new Task();
        task.setMeetingId(meeting.getId());
        task.setTitle(title);
        task.setStatus(status);
        task.setPriority(TaskPriority.MEDIUM);

        if (dueDate != null) {
            task.setDueDate(
                    dueDate.atTime(12, 0)
                            .atZone(TURKEY_ZONE)
                            .toOffsetDateTime()
            );
        }

        return taskRepository.save(task);
    }

    @Test
    void notOverdueFilterWorksWithPostgres() {
        User user = createUser();
        Project project = createProject(user);
        Meeting meeting = createMeeting(project, user);

        Task yesterdayTask = createTask(
                meeting, "Yesterday task", TODAY.minusDays(1), TaskStatus.TODO
        );
        Task todayTask = createTask(
                meeting, "Today task", TODAY, TaskStatus.TODO
        );
        Task tomorrowTask = createTask(
                meeting, "Tomorrow task", TODAY.plusDays(1), TaskStatus.TODO
        );
        Task doneTask = createTask(
                meeting, "Done task", TODAY.minusDays(1), TaskStatus.DONE
        );
        Task noDueDateTask = createTask(
                meeting, "No due date task", null, TaskStatus.TODO
        );

        OffsetDateTime todayStart =
                TODAY.atStartOfDay(TURKEY_ZONE).toOffsetDateTime();

        var notOverdueTasks = taskRepository.findAll(
                TaskSpecification.withFilters(
                        project.getId(), null, null, null,
                        null, null, false,
                        todayStart, null
                )
        );

        assertThat(notOverdueTasks)
                .extracting(Task::getId)
                .contains(
                        todayTask.getId(),
                        tomorrowTask.getId(),
                        doneTask.getId(),
                        noDueDateTask.getId()
                )
                .doesNotContain(yesterdayTask.getId());
    }

    @Test
    void overdueFilterWorksWithPostgres() {
        User user = createUser();
        Project project = createProject(user);
        Meeting meeting = createMeeting(project, user);

        Task yesterdayTask = createTask(
                meeting, "Yesterday task", TODAY.minusDays(1), TaskStatus.TODO
        );

        Task todayTask = createTask(
                meeting, "Today task", TODAY, TaskStatus.TODO
        );

        Task tomorrowTask = createTask(
                meeting, "Tomorrow task", TODAY.plusDays(1), TaskStatus.TODO
        );

        Task doneTask = createTask(
                meeting, "Done task", TODAY.minusDays(1), TaskStatus.DONE
        );

        Task noDueDateTask = createTask(
                meeting, "No due date task", null, TaskStatus.TODO
        );

        OffsetDateTime todayStart =
                TODAY.atStartOfDay(TURKEY_ZONE).toOffsetDateTime();

        var overdueTasks = taskRepository.findAll(
                TaskSpecification.withFilters(
                        project.getId(),
                        null,
                        null,
                        null,
                        null,
                        null,
                        true,
                        todayStart,
                        null
                )
        );

        assertThat(overdueTasks)
                .extracting(Task::getId)
                .contains(yesterdayTask.getId())
                .doesNotContain(
                        todayTask.getId(),
                        tomorrowTask.getId(),
                        doneTask.getId(),
                        noDueDateTask.getId()
                );
    }

    @Test
    void assigneeFilterWorksWithPostgres() {
        User owner = createUser();
        User firstAssignee = createUser();
        User secondAssignee = createUser();

        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        Task firstTask = createTask(
                meeting, "First user's task", TODAY, TaskStatus.TODO
        );
        firstTask.setAssigneeId(firstAssignee.getId());
        taskRepository.save(firstTask);

        Task secondTask = createTask(
                meeting, "Second user's task", TODAY, TaskStatus.TODO
        );
        secondTask.setAssigneeId(secondAssignee.getId());
        taskRepository.save(secondTask);

        var assignedTasks = taskRepository.findAll(
                TaskSpecification.withFilters(
                        project.getId(),
                        firstAssignee.getId(),
                        null, null,
                        null, null, null,
                        null, null
                )
        );

        assertThat(assignedTasks)
                .extracting(Task::getId)
                .contains(firstTask.getId())
                .doesNotContain(secondTask.getId());
    }

    @Test
    void priorityFilterWorksWithPostgres() {
        User user = createUser();
        Project project = createProject(user);
        Meeting meeting = createMeeting(project, user);

        Task lowTask = createTask(
                meeting, "Low priority task", TODAY, TaskStatus.TODO
        );
        lowTask.setPriority(TaskPriority.LOW);
        taskRepository.save(lowTask);

        Task mediumTask = createTask(
                meeting, "Medium priority task", TODAY, TaskStatus.TODO
        );
        mediumTask.setPriority(TaskPriority.MEDIUM);
        taskRepository.save(mediumTask);

        Task highTask = createTask(
                meeting, "High priority task", TODAY, TaskStatus.TODO
        );
        highTask.setPriority(TaskPriority.HIGH);
        taskRepository.save(highTask);

        var highPriorityTasks = taskRepository.findAll(
                TaskSpecification.withFilters(
                        project.getId(), null, null, TaskPriority.HIGH,
                        null, null, null,
                        null, null
                )
        );

        assertThat(highPriorityTasks)
                .extracting(Task::getId)
                .contains(highTask.getId())
                .doesNotContain(lowTask.getId(), mediumTask.getId());
    }

    @Test
    void statusFilterWorksWithPostgres() {
        User user = createUser();
        Project project = createProject(user);
        Meeting meeting = createMeeting(project, user);

        Task todoTask = createTask(
                meeting, "Todo task", TODAY, TaskStatus.TODO
        );

        Task inProgressTask = createTask(
                meeting, "In progress task", TODAY, TaskStatus.IN_PROGRESS
        );

        Task doneTask = createTask(
                meeting, "Done task", TODAY, TaskStatus.DONE
        );

        var todoTasks = taskRepository.findAll(
                TaskSpecification.withFilters(
                        project.getId(), null, TaskStatus.TODO, null,
                        null, null, null, null, null
                )
        );

        assertThat(todoTasks)
                .extracting(Task::getId)
                .contains(todoTask.getId())
                .doesNotContain(inProgressTask.getId(), doneTask.getId());
    }

    @Test
    void combinedFiltersWorkWithPostgres() {
        User owner = createUser();
        User assignee = createUser();
        User otherAssignee = createUser();

        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        Task highAssignedToMe = createTask(
                meeting, "BAna atanmış yüksek öncelikli görev",
                TODAY, TaskStatus.TODO
        );
        highAssignedToMe.setAssigneeId(assignee.getId());
        highAssignedToMe.setPriority(TaskPriority.HIGH);
        taskRepository.save(highAssignedToMe);

        Task highAssignedToOther = createTask(
                meeting, "Başkasına atanmış yüksek öncelikli görev",
                TODAY, TaskStatus.TODO
        );
        highAssignedToOther.setAssigneeId(otherAssignee.getId());
        highAssignedToOther.setPriority(TaskPriority.HIGH);
        taskRepository.save(highAssignedToOther);

        Task mediumAssignedToMe = createTask(
                meeting, "Bana atanmış orta öncelikli görev",
                TODAY, TaskStatus.TODO
        );
        mediumAssignedToMe.setAssigneeId(assignee.getId());
        mediumAssignedToMe.setPriority(TaskPriority.MEDIUM);
        taskRepository.save(mediumAssignedToMe);

        var matchingTasks = taskRepository.findAll(
                TaskSpecification.withFilters(
                        project.getId(),
                        assignee.getId(),
                        null,
                        TaskPriority.HIGH,
                        null, null, null,
                        null, null
                )
        );

        assertThat(matchingTasks)
                .extracting(Task::getId)
                .containsExactly(highAssignedToMe.getId());
    }

    @Test
    void myTasksFilterWorksWithPostgres() {
        User owner = createUser();
        User firstUser = createUser();
        User secondUser = createUser();

        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        Task firstTask = createTask(
                meeting, "İlk kullanıcının görevi", TODAY, TaskStatus.TODO
        );
        firstTask.setAssigneeId(firstUser.getId());
        taskRepository.save(firstTask);

        Task secondTask = createTask(
                meeting, "İlk kullanıcının ikinci görevi",
                TODAY, TaskStatus.IN_PROGRESS
        );
        secondTask.setAssigneeId(firstUser.getId());
        taskRepository.save(secondTask);

        Task otherTask = createTask(
                meeting, "Diğer kullanıcının görevi", TODAY, TaskStatus.TODO
        );
        otherTask.setAssigneeId(secondUser.getId());
        taskRepository.save(otherTask);

        var myTasks = taskRepository.findAll(
                TaskSpecification.withFilters(
                        project.getId(),
                        null,
                        null,
                        null,
                        null, null, null,
                        null,
                        firstUser.getId()
                )
        );

        assertThat(myTasks)
                .extracting(Task::getId)
                .containsExactlyInAnyOrder(
                        firstTask.getId(),
                        secondTask.getId()
                )
                .doesNotContain(otherTask.getId());
    }

    @Test
    void inactiveAssigneeCannotBeAssignedWithPostgres() {
        User owner = createUser();
        User inactiveUser = createUser();

        inactiveUser.setActive(false);
        userRepository.save(inactiveUser);

        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        TaskRequestDto request = new TaskRequestDto(
                "Inactive User Test",
                "Pasif kullanıcıya atama testi",
                inactiveUser.getId(),
                null,
                "TODO",
                "MEDIUM"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.createTask(meeting.getId(), request, owner.getId(), false)
        );

        assertThat(exception.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(taskRepository.findByMeetingId(meeting.getId()))
                .isEmpty();
    }

    @Test
    void archivedMeetingCannotAcceptTaskWithPostgres() {
        User owner = createUser();

        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        meeting.setArchived(true);
        meetingRepository.save(meeting);

        TaskRequestDto request = new TaskRequestDto(
                "Archived Meeting Test",
                "Arşivlenmiş toplantı testi",
                null,
                null,
                "TODO",
                "MEDIUM"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.createTask(meeting.getId(), request, owner.getId(), false)
        );

        assertThat(exception.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(taskRepository.findByMeetingId(meeting.getId()))
                .isEmpty();
    }

    @Test
    void archivedProjectCannotAcceptTaskWithPostgres() {
        User owner = createUser();
        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        project.setArchived(true);
        projectRepository.save(project);

        TaskRequestDto request = new TaskRequestDto(
                "Archived Project Test",
                "Arşivlenmiş projeye görev ekleme testi",
                null,
                null,
                "TODO",
                "MEDIUM"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.createTask(meeting.getId(), request, owner.getId(), false)
        );

        assertThat(exception.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(taskRepository.findByMeetingId(meeting.getId()))
                .isEmpty();
    }

    @Test
    void anotherMemberCannotUpdateTaskWithPostgres() {
        User owner = createUser();
        User anotherMember = createUser();

        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        Task task = createTask(
                meeting,
                "Owner task",
                TODAY,
                TaskStatus.TODO
        );

        TaskRequestDto request = new TaskRequestDto(
                "Unauthorized update",
                "Başka üyenin güncelleme denemesi",
                null,
                null,
                "TODO",
                "HIGH"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(
                        task.getId(),
                        request,
                        anotherMember.getId(),
                        false
                )
        );

        assertThat(exception.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(taskRepository.findById(task.getId()))
                .isPresent()
                .get()
                .extracting(Task::getTitle)
                .isEqualTo("Owner task");
    }

    @Test
    void archivedProjectCannotUpdateTaskWithPostgres() {
        User owner = createUser();
        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        Task task = createTask(
                meeting,
                "Original task",
                TODAY,
                TaskStatus.TODO
        );

        project.setArchived(true);
        projectRepository.save(project);

        TaskRequestDto request = new TaskRequestDto(
                "Updated task",
                "Arşivli projede güncelleme denemesi",
                null,
                null,
                "TODO",
                "HIGH"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(
                        task.getId(),
                        request,
                        owner.getId(),
                        false
                )
        );

        assertThat(exception.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(taskRepository.findById(task.getId()))
                .isPresent()
                .get()
                .extracting(Task::getTitle)
                .isEqualTo("Original task");
    }

    @Test
    void archivedProjectCannotChangeTaskStatusWithPostgres() {
        User owner = createUser();
        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        Task task = createTask(
                meeting,
                "Assigned task",
                TODAY,
                TaskStatus.TODO
        );

        task.setAssigneeId(owner.getId());
        taskRepository.save(task);

        project.setArchived(true);
        projectRepository.save(project);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTaskStatus(
                        task.getId(),
                        "DONE",
                        owner.getId()
                )
        );

        assertThat(exception.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(taskRepository.findById(task.getId()))
                .isPresent()
                .get()
                .extracting(Task::getStatus)
                .isEqualTo(TaskStatus.TODO);
    }


    @Test
    void anotherMemberCannotChangeTaskStatusWithPostgres() {
        User owner = createUser();
        User assignee = createUser();
        User anotherMember = createUser();

        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        Task task = createTask(
                meeting,
                "Assigned task",
                TODAY,
                TaskStatus.TODO
        );

        task.setAssigneeId(assignee.getId());
        taskRepository.save(task);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTaskStatus(
                        task.getId(),
                        "DONE",
                        anotherMember.getId()
                )
        );

        assertThat(exception.getStatusCode())
                .isEqualTo(HttpStatus.FORBIDDEN);

        assertThat(taskRepository.findById(task.getId()))
                .isPresent()
                .get()
                .extracting(Task::getStatus)
                .isEqualTo(TaskStatus.TODO);
    }

    @Test
    void archivedMeetingCannotUpdateTaskWithPostgres() {
        User owner = createUser();
        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        Task task = createTask(
                meeting,
                "Original task",
                TODAY,
                TaskStatus.TODO
        );

        meeting.setArchived(true);
        meetingRepository.save(meeting);

        TaskRequestDto request = new TaskRequestDto(
                "Updated task",
                "Arşivli toplantıda güncelleme denemesi",
                null,
                null,
                "TODO",
                "HIGH"
        );

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(
                        task.getId(),
                        request,
                        owner.getId(),
                        false
                )
        );

        assertThat(exception.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(taskRepository.findById(task.getId()))
                .isPresent()
                .get()
                .extracting(Task::getTitle)
                .isEqualTo("Original task");
    }

    @Test
    void archivedMeetingCannotChangeTaskStatusWithPostgres() {
        User owner = createUser();
        Project project = createProject(owner);
        Meeting meeting = createMeeting(project, owner);

        Task task = createTask(
                meeting,
                "Assigned task",
                TODAY,
                TaskStatus.TODO
        );

        task.setAssigneeId(owner.getId());
        taskRepository.save(task);

        meeting.setArchived(true);
        meetingRepository.save(meeting);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTaskStatus(
                        task.getId(),
                        "DONE",
                        owner.getId()
                )
        );

        assertThat(exception.getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);

        assertThat(taskRepository.findById(task.getId()))
                .isPresent()
                .get()
                .extracting(Task::getStatus)
                .isEqualTo(TaskStatus.TODO);
    }
    @Test
    void dueDateRangeFilterWorksWithPostgres() {
        User user = createUser();
        Project project = createProject(user);
        Meeting meeting = createMeeting(project, user);

        Task yesterdayTask = createTask(
                meeting, "Yesterday task",
                TODAY.minusDays(1), TaskStatus.TODO
        );

        Task todayTask = createTask(
                meeting, "Today task",
                TODAY, TaskStatus.TODO
        );

        Task tomorrowTask = createTask(
                meeting, "Tomorrow task",
                TODAY.plusDays(1), TaskStatus.TODO
        );

        OffsetDateTime dueFrom = TODAY.atStartOfDay(TURKEY_ZONE)
                .toOffsetDateTime();

        OffsetDateTime dueTo = TODAY.plusDays(1)
                .atStartOfDay(TURKEY_ZONE)
                .toOffsetDateTime();

        var matchingTasks = taskRepository.findAll(
                TaskSpecification.withFilters(
                        project.getId(),
                        null,
                        null,
                        null,
                        dueFrom,
                        dueTo,
                        null,
                        null,
                        null
                )
        );

        assertThat(matchingTasks)
                .extracting(Task::getId)
                .contains(todayTask.getId())
                .doesNotContain(
                        yesterdayTask.getId(),
                        tomorrowTask.getId()
                );
    }


}