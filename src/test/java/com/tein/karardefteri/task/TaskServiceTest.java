package com.tein.karardefteri.task;

import com.tein.karardefteri.meeting.entity.Meeting;
import com.tein.karardefteri.meeting.service.MeetingService;
import com.tein.karardefteri.project.entity.Project;
import com.tein.karardefteri.project.service.ProjectService;
import com.tein.karardefteri.task.dto.TaskRequestDto;
import com.tein.karardefteri.task.dto.TaskResponseDto;
import com.tein.karardefteri.task.entity.Task;
import com.tein.karardefteri.task.entity.TaskPriority;
import com.tein.karardefteri.task.entity.TaskStatus;
import com.tein.karardefteri.task.repository.TaskRepository;
import com.tein.karardefteri.task.service.TaskService;
import com.tein.karardefteri.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private MeetingService meetingService;

    @Mock
    private UserService userService;

    @Mock
    private ProjectService projectService;

    @InjectMocks
    private TaskService taskService;

    private UUID meetingId;
    private UUID projectId;
    private Project project;

    @BeforeEach
    void setUp() {
        meetingId = UUID.randomUUID();
        projectId = UUID.randomUUID();

        project = new Project();
        project.setName("Test projesi");

        lenient().when(projectService.getProjectById(projectId))
                .thenReturn(project);

        Meeting defaultMeeting = new Meeting();
        defaultMeeting.setProjectId(projectId);
        defaultMeeting.setTitle("Test toplantısı");

        lenient().when(meetingService.getMeetingById(any(UUID.class)))
                .thenReturn(defaultMeeting);
    }

    private Meeting createMeeting(UUID ownerId) {
        Meeting meeting = new Meeting();
        meeting.setProjectId(projectId);
        meeting.setTitle("Test toplantısı");
        meeting.setCreatedBy(ownerId);
        return meeting;
    }

    private Task createTask() {
        Task task = new Task();
        task.setMeetingId(meetingId);
        task.setTitle("Test görevi");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);
        return task;
    }

    private TaskRequestDto createRequest(String title, String priority) {
        return new TaskRequestDto(
                title,
                "Açıklama",
                null,
                null,
                null,
                priority
        );
    }

    @Test
    void createTask_shouldSaveTask() {
        TaskRequestDto request = new TaskRequestDto(
                "Test görevi",
                "Açıklama",
                null,
                null,
                null,
                null
        );

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> {
                    Task task = invocation.getArgument(0);

                    if (task.getStatus() == null) {
                        task.setStatus(TaskStatus.TODO);
                    }

                    if (task.getPriority() == null) {
                        task.setPriority(TaskPriority.MEDIUM);
                    }

                    return task;
                });

        TaskResponseDto response =
                taskService.createTask(meetingId, request);

        assertNotNull(response);
        assertEquals("Test görevi", response.title());
        assertEquals("TODO", response.status());
        assertEquals("MEDIUM", response.priority());
        assertEquals("Test toplantısı", response.meetingTitle());
        assertEquals("Test projesi", response.projectName());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void getTasksByMeetingId_shouldReturnEmptyList() {
        when(taskRepository.findByMeetingId(meetingId))
                .thenReturn(List.of());

        List<TaskResponseDto> result =
                taskService.getTasksByMeetingId(meetingId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository).findByMeetingId(meetingId);
    }

    @Test
    void getMyTasks_shouldReturnAssignedTasks() {
        UUID userId = UUID.randomUUID();

        Task task = createTask();
        task.setAssigneeId(userId);

        when(taskRepository.findByAssigneeId(userId))
                .thenReturn(List.of(task));

        List<TaskResponseDto> result =
                taskService.getMyTasks(userId);

        assertEquals(1, result.size());
        assertEquals("Test görevi", result.get(0).title());
        assertEquals(userId, result.get(0).assigneeId());

        verify(taskRepository).findByAssigneeId(userId);
    }

    @Test
    void getTaskById_shouldReturnTaskWhenFound() {
        Task task = createTask();

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        TaskResponseDto response =
                taskService.getTaskById(1L);

        assertNotNull(response);
        assertEquals(meetingId, response.meetingId());
        assertEquals("Test görevi", response.title());
        assertEquals("TODO", response.status());
        assertEquals("MEDIUM", response.priority());
        assertEquals("Test toplantısı", response.meetingTitle());
        assertEquals("Test projesi", response.projectName());
    }

    @Test
    void getTaskById_shouldThrow404WhenTaskDoesNotExist() {
        when(taskRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.getTaskById(99L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    void updateTask_shouldAllowMeetingOwner() {
        UUID ownerId = UUID.randomUUID();

        Meeting meeting = createMeeting(ownerId);
        Task task = createTask();

        TaskRequestDto request = createRequest("Yeni başlık", "HIGH");

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDto response =
                taskService.updateTask(1L, request, ownerId, false);

        assertEquals("Yeni başlık", response.title());
        assertEquals("HIGH", response.priority());

        verify(taskRepository).save(task);
    }

    @Test
    void updateTask_shouldAllowAdmin() {
        UUID adminId = UUID.randomUUID();

        Meeting meeting = createMeeting(UUID.randomUUID());
        Task task = createTask();

        TaskRequestDto request = createRequest("Admin güncelledi", "HIGH");

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDto response =
                taskService.updateTask(1L, request, adminId, true);

        assertEquals("Admin güncelledi", response.title());
        assertEquals("HIGH", response.priority());

        verify(taskRepository).save(task);
    }

    @Test
    void updateTask_shouldThrow403WhenUserIsNotMeetingOwnerOrAdmin() {
        UUID anotherUserId = UUID.randomUUID();

        Meeting meeting = createMeeting(UUID.randomUUID());
        Task task = createTask();

        TaskRequestDto request = createRequest("Yeni başlık", "HIGH");

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(1L, request, anotherUserId, false)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_shouldThrow400WhenPriorityIsBlank() {
        UUID ownerId = UUID.randomUUID();

        Meeting meeting = createMeeting(ownerId);
        Task task = createTask();

        TaskRequestDto request = createRequest("Yeni başlık", " ");

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(1L, request, ownerId, false)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_shouldThrow400WhenPriorityIsInvalid() {
        UUID ownerId = UUID.randomUUID();

        Meeting meeting = createMeeting(ownerId);
        Task task = createTask();

        TaskRequestDto request = createRequest("Yeni başlık", "URGENT");

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(1L, request, ownerId, false)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskStatus_shouldThrow403WhenUserIsNotAssignee() {
        UUID assigneeId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();

        Task task = createTask();
        task.setAssigneeId(assigneeId);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTaskStatus(1L, "DONE", anotherUserId)
        );

        assertEquals(HttpStatus.FORBIDDEN, exception.getStatusCode());

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskStatus_shouldThrow400WhenStatusIsInvalid() {
        UUID assigneeId = UUID.randomUUID();

        Task task = createTask();
        task.setAssigneeId(assigneeId);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTaskStatus(
                        1L, "INVALID_STATUS", assigneeId
                )
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_shouldThrow409WhenAssigneeIsInactive() {
        UUID ownerId = UUID.randomUUID();
        UUID inactiveUserId = UUID.randomUUID();

        Meeting meeting = createMeeting(ownerId);
        Task task = createTask();

        TaskRequestDto request = new TaskRequestDto(
                "Güncellenmiş görev",
                "Açıklama",
                inactiveUserId,
                null,
                null,
                "HIGH"
        );

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        when(userService.isUserActive(inactiveUserId))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(1L, request, ownerId, false)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_shouldThrow409WhenMeetingIsArchived() {
        Meeting meeting = createMeeting(UUID.randomUUID());
        meeting.setArchived(true);

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        TaskRequestDto request = createRequest("Yeni görev", "HIGH");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.createTask(meetingId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_shouldThrow409WhenProjectIsArchived() {
        Meeting meeting = createMeeting(UUID.randomUUID());

        Project archivedProject = new Project();
        archivedProject.setName("Arşivli proje");
        archivedProject.setArchived(true);

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        when(projectService.getProjectById(projectId))
                .thenReturn(archivedProject);

        TaskRequestDto request = createRequest("Yeni görev", "HIGH");

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.createTask(meetingId, request)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void getTasksByMeetingId_shouldAllowReadingWhenMeetingIsArchived() {
        UUID archivedMeetingId = UUID.randomUUID();

        Meeting archivedMeeting = createMeeting(UUID.randomUUID());
        archivedMeeting.setArchived(true);

        Task task = new Task();
        task.setMeetingId(archivedMeetingId);
        task.setTitle("Arşivli toplantının görevi");

        when(meetingService.getMeetingById(archivedMeetingId))
                .thenReturn(archivedMeeting);

        when(taskRepository.findByMeetingId(archivedMeetingId))
                .thenReturn(List.of(task));

        List<TaskResponseDto> result =
                taskService.getTasksByMeetingId(archivedMeetingId);

        assertEquals(1, result.size());
        assertEquals("Arşivli toplantının görevi", result.get(0).title());

        verify(taskRepository).findByMeetingId(archivedMeetingId);
    }

    @Test
    void getTasksByMeetingId_shouldAllowReadingWhenProjectIsArchived() {
        Meeting meeting = createMeeting(UUID.randomUUID());

        Project archivedProject = new Project();
        archivedProject.setName("Arşivli proje");
        archivedProject.setArchived(true);

        Task task = createTask();

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        when(projectService.getProjectById(projectId))
                .thenReturn(archivedProject);

        when(taskRepository.findByMeetingId(meetingId))
                .thenReturn(List.of(task));

        List<TaskResponseDto> result =
                taskService.getTasksByMeetingId(meetingId);

        assertEquals(1, result.size());
        assertEquals("Test görevi", result.get(0).title());
        assertEquals("Arşivli proje", result.get(0).projectName());

        verify(taskRepository).findByMeetingId(meetingId);
    }

    @Test
    void updateTask_shouldThrow409WhenMeetingIsArchived() {
        UUID ownerId = UUID.randomUUID();

        Meeting meeting = createMeeting(ownerId);
        meeting.setArchived(true);

        Task task = createTask();
        TaskRequestDto request = createRequest("Yeni başlık", "HIGH");

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(1L, request, ownerId, false)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_shouldThrow409WhenProjectIsArchived() {
        UUID ownerId = UUID.randomUUID();

        Meeting meeting = createMeeting(ownerId);
        Task task = createTask();

        Project archivedProject = new Project();
        archivedProject.setName("Arşivli proje");
        archivedProject.setArchived(true);

        TaskRequestDto request = createRequest("Yeni başlık", "HIGH");

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        when(projectService.getProjectById(projectId))
                .thenReturn(archivedProject);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(1L, request, ownerId, false)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskStatus_shouldThrow409WhenMeetingIsArchived() {
        UUID assigneeId = UUID.randomUUID();

        Meeting meeting = createMeeting(UUID.randomUUID());
        meeting.setArchived(true);

        Task task = createTask();
        task.setAssigneeId(assigneeId);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTaskStatus(1L, "DONE", assigneeId)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskStatus_shouldThrow409WhenProjectIsArchived() {
        UUID assigneeId = UUID.randomUUID();

        Meeting meeting = createMeeting(UUID.randomUUID());
        Task task = createTask();
        task.setAssigneeId(assigneeId);

        Project archivedProject = new Project();
        archivedProject.setName("Arşivli proje");
        archivedProject.setArchived(true);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        when(projectService.getProjectById(projectId))
                .thenReturn(archivedProject);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTaskStatus(1L, "DONE", assigneeId)
        );

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_shouldKeepExistingAssigneeWhenAssigneeIdIsNull() {
        UUID ownerId = UUID.randomUUID();
        UUID existingAssigneeId = UUID.randomUUID();

        Meeting meeting = createMeeting(ownerId);

        Task task = createTask();
        task.setAssigneeId(existingAssigneeId);

        TaskRequestDto request = createRequest(
                "Güncellenmiş başlık",
                "HIGH"
        );

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        when(projectService.getProjectById(projectId))
                .thenReturn(project);

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDto response = taskService.updateTask(
                1L,
                request,
                ownerId,
                false
        );

        assertEquals(existingAssigneeId, task.getAssigneeId());
        assertEquals(existingAssigneeId, response.assigneeId());
    }
}
