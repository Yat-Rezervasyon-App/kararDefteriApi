
package com.tein.karardefteri.task;

import com.tein.karardefteri.meeting.service.MeetingService;
import com.tein.karardefteri.task.dto.TaskRequestDto;
import com.tein.karardefteri.task.dto.TaskResponseDto;
import com.tein.karardefteri.task.entity.Task;
import com.tein.karardefteri.task.repository.TaskRepository;
import com.tein.karardefteri.task.service.TaskService;
import com.tein.karardefteri.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
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

    private TaskService taskService;
    private UUID meetingId;

    @BeforeEach
    void setUp() {
        taskService = new TaskService(
                taskRepository,
                meetingService,
                userService
        );
        meetingId = UUID.randomUUID();
    }

    @Test
    void createTask_shouldCreateTaskSuccessfully() {
        TaskRequestDto request = new TaskRequestDto(
                "Test görevi", "Açıklama", null, null, null, null
        );

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(null);
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TaskResponseDto response = taskService.createTask(meetingId, request);

        assertEquals(meetingId, response.meetingId());
        assertEquals("Test görevi", response.title());
        assertEquals("TODO", response.status());
        assertEquals("MEDIUM", response.priority());

        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_shouldRejectBlankTitle() {
        TaskRequestDto request = new TaskRequestDto(
                "   ", null, null, null, null, null
        );

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(null);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.createTask(meetingId, request)
        );

        assertEquals(400, exception.getStatusCode().value());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_shouldReturn404WhenMeetingDoesNotExist() {
        TaskRequestDto request = new TaskRequestDto(
                "Test görevi", null, null, null, null, null
        );

        when(meetingService.getMeetingById(meetingId))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Toplantı bulunamadı"
                ));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.createTask(meetingId, request)
        );

        assertEquals(404, exception.getStatusCode().value());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void createTask_shouldReturn404WhenAssigneeDoesNotExist() {
        UUID assigneeId = UUID.randomUUID();

        TaskRequestDto request = new TaskRequestDto(
                "Test görevi", null, assigneeId, null, null, null
        );

        when(meetingService.getMeetingById(meetingId))
                .thenReturn(null);
        when(userService.userExists(assigneeId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.createTask(meetingId, request)
        );

        assertEquals(404, exception.getStatusCode().value());
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void getTasksByMeetingId_shouldReturnTasks() {
        when(meetingService.getMeetingById(meetingId))
                .thenReturn(null);
        when(taskRepository.findByMeetingId(meetingId))
                .thenReturn(List.of());

        List<TaskResponseDto> response =
                taskService.getTasksByMeetingId(meetingId);

        assertNotNull(response);
        assertTrue(response.isEmpty());

        verify(taskRepository).findByMeetingId(meetingId);
    }

    @Test
    void getTasksByMeetingId_shouldReturn404WhenMeetingDoesNotExist() {
        when(meetingService.getMeetingById(meetingId))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND,
                        "Toplantı bulunamadı"
                ));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> taskService.getTasksByMeetingId(meetingId)
        );

        assertEquals(404, exception.getStatusCode().value());
        verify(taskRepository, never())
                .findByMeetingId(any(UUID.class));
    }
}
