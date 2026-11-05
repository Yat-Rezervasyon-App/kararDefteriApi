
package com.tein.karardefteri.task;

import com.tein.karardefteri.meeting.entity.Meeting;
import com.tein.karardefteri.meeting.service.MeetingService;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private MeetingService meetingService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskService taskService;

    private UUID meetingId;

    @BeforeEach
    void setUp() {
        meetingId = UUID.randomUUID();
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

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(new Meeting());

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

        verify(meetingService).getMeetingById(meetingId);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void getTasksByMeetingId_shouldReturnEmptyList() {
        when(meetingService.getMeetingById(meetingId))
                .thenReturn(new Meeting());

        when(taskRepository.findByMeetingId(meetingId))
                .thenReturn(List.of());

        List<TaskResponseDto> result =
                taskService.getTasksByMeetingId(meetingId);

        assertNotNull(result);
        assertTrue(result.isEmpty());

        verify(taskRepository).findByMeetingId(meetingId);
    }

    @Test
    void getTaskById_shouldReturnTaskWhenFound() {
        Task task = new Task();
        task.setMeetingId(meetingId);
        task.setTitle("Test görevi");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        TaskResponseDto response =
                taskService.getTaskById(1L);

        assertNotNull(response);
        assertEquals(meetingId, response.meetingId());
        assertEquals("Test görevi", response.title());
        assertEquals("TODO", response.status());
        assertEquals("MEDIUM", response.priority());
    }

    @Test
    void getTaskById_shouldThrow404WhenTaskDoesNotExist() {
        when(taskRepository.findById(99L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.getTaskById(99L)
        );

        assertEquals(
                HttpStatus.NOT_FOUND,
                exception.getStatusCode()
        );
    }

    @Test
    void updateTask_shouldAllowMeetingOwner() {
        UUID ownerId = UUID.randomUUID();

        Meeting meeting = new Meeting();
        meeting.setCreatedBy(ownerId);

        Task task = new Task();
        task.setMeetingId(meetingId);
        task.setTitle("Eski başlık");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);

        TaskRequestDto request = new TaskRequestDto(
                "Yeni başlık",
                "Yeni açıklama",
                null,
                null,
                null,
                "HIGH"
        );

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDto response = taskService.updateTask(
                1L, request, ownerId, false
        );

        assertEquals("Yeni başlık", response.title());
        assertEquals("HIGH", response.priority());
        verify(taskRepository).save(task);
    }

    @Test
    void updateTask_shouldAllowAdmin() {
        UUID adminId = UUID.randomUUID();

        Meeting meeting = new Meeting();
        meeting.setCreatedBy(UUID.randomUUID());

        Task task = new Task();
        task.setMeetingId(meetingId);
        task.setTitle("Eski başlık");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);

        TaskRequestDto request = new TaskRequestDto(
                "Admin güncelledi",
                "Açıklama",
                null,
                null,
                null,
                "HIGH"
        );

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDto response = taskService.updateTask(
                1L, request, adminId, true
        );

        assertEquals("Admin güncelledi", response.title());
        assertEquals("HIGH", response.priority());
        verify(taskRepository).save(task);
    }

    @Test
    void updateTask_shouldThrow403WhenUserIsNotMeetingOwnerOrAdmin() {
        UUID anotherUserId = UUID.randomUUID();

        Meeting meeting = new Meeting();
        meeting.setCreatedBy(UUID.randomUUID());

        Task task = new Task();
        task.setMeetingId(meetingId);
        task.setTitle("Eski başlık");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);

        TaskRequestDto request = new TaskRequestDto(
                "Yeni başlık",
                "Açıklama",
                null,
                null,
                null,
                "HIGH"
        );

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(
                        1L, request, anotherUserId, false
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_shouldThrow400WhenPriorityIsBlank() {
        UUID ownerId = UUID.randomUUID();

        Meeting meeting = new Meeting();
        meeting.setCreatedBy(ownerId);

        Task task = new Task();
        task.setMeetingId(meetingId);
        task.setTitle("Test görevi");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);

        TaskRequestDto request = new TaskRequestDto(
                "Yeni başlık",
                "Açıklama",
                null,
                null,
                null,
                " "
        );

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(
                        1L, request, ownerId, false
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_shouldThrow400WhenPriorityIsInvalid() {
        UUID ownerId = UUID.randomUUID();

        Meeting meeting = new Meeting();
        meeting.setCreatedBy(ownerId);

        Task task = new Task();
        task.setMeetingId(meetingId);
        task.setTitle("Test görevi");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);

        TaskRequestDto request = new TaskRequestDto(
                "Yeni başlık",
                "Açıklama",
                null,
                null,
                null,
                "URGENT"
        );

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(meeting);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTask(
                        1L, request, ownerId, false
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskStatus_shouldThrow403WhenUserIsNotAssignee() {
        UUID assigneeId = UUID.randomUUID();
        UUID anotherUserId = UUID.randomUUID();

        Task task = new Task();
        task.setMeetingId(meetingId);
        task.setAssigneeId(assigneeId);
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTaskStatus(
                        1L,
                        "DONE",
                        anotherUserId
                )
        );

        assertEquals(
                HttpStatus.FORBIDDEN,
                exception.getStatusCode()
        );

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTaskStatus_shouldThrow400WhenStatusIsInvalid() {
        UUID assigneeId = UUID.randomUUID();

        Task task = new Task();
        task.setMeetingId(meetingId);
        task.setAssigneeId(assigneeId);
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);

        when(taskRepository.findById(1L))
                .thenReturn(Optional.of(task));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.updateTaskStatus(
                        1L,
                        "INVALID_STATUS",
                        assigneeId
                )
        );

        assertEquals(
                HttpStatus.BAD_REQUEST,
                exception.getStatusCode()
        );

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_shouldThrow409WhenAssigneeIsInactive() {
        UUID ownerId = UUID.randomUUID();
        UUID inactiveUserId = UUID.randomUUID();

        Meeting meeting = new Meeting();
        meeting.setCreatedBy(ownerId);

        Task task = new Task();
        task.setMeetingId(meetingId);
        task.setTitle("Test görevi");
        task.setStatus(TaskStatus.TODO);
        task.setPriority(TaskPriority.MEDIUM);

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
                () -> taskService.updateTask(
                        1L, request, ownerId, false
                )
        );

        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );

        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_shouldThrow409WhenAssigneeIsInactive(){
        UUID inactiveUserId = UUID.randomUUID();

        TaskRequestDto request = new TaskRequestDto(
                "Test görevi",
                "Açıklama",
                inactiveUserId,
                null,
                null,
                null
        );

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(new Meeting());

        when(userService.isUserActive(inactiveUserId))
                .thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                ()-> taskService.createTask(meetingId, request)
        );
        assertEquals(
                HttpStatus.CONFLICT,
                exception.getStatusCode()
        );
        verify(taskRepository, never()).save(any(Task.class));
    }
}
