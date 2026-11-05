package com.tein.karardefteri.task;

import com.tein.karardefteri.task.controller.TaskController;
import com.tein.karardefteri.task.dto.TaskResponseDto;
import com.tein.karardefteri.task.service.TaskService;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @Mock
    private HttpSession session;

    @InjectMocks
    private TaskController taskController;

    @Test
    void getMyTasks_shouldReturnOnlyLoggedInUsersTasks() {
        UUID loggedInUserId = UUID.randomUUID();

        TaskResponseDto task = new TaskResponseDto(
                1L,
                UUID.randomUUID(),
                "Test toplantısı",
                UUID.randomUUID(),
                "Test projesi",
                "Kullanıcının görevi",
                "Açıklama",
                loggedInUserId,
                null,
                "TODO",
                "MEDIUM",
                null,
                null
        );

        when(session.getAttribute("userId"))
                .thenReturn(loggedInUserId);

        when(taskService.getMyTasks(loggedInUserId))
                .thenReturn(List.of(task));

        ResponseEntity<List<TaskResponseDto>> response =
                taskController.getMyTasks(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals(
                loggedInUserId,
                response.getBody().get(0).assigneeId()
        );

        verify(taskService).getMyTasks(loggedInUserId);
    }

    @Test
    void getMyTasks_shouldReturn401WhenUserIsNotLoggedIn() {
        when(session.getAttribute("userId"))
                .thenReturn(null);

        ResponseEntity<List<TaskResponseDto>> response =
                taskController.getMyTasks(session);

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                response.getStatusCode()
        );

        assertNull(response.getBody());

        verifyNoInteractions(taskService);
    }

    @Test
    void getMyTasks_shouldAcceptUserIdAsString() {
        UUID loggedInUserId = UUID.randomUUID();

        when(session.getAttribute("userId"))
                .thenReturn(loggedInUserId.toString());

        when(taskService.getMyTasks(loggedInUserId))
                .thenReturn(List.of());

        ResponseEntity<List<TaskResponseDto>> response =
                taskController.getMyTasks(session);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(taskService).getMyTasks(loggedInUserId);
    }

    @Test
    void getMyTasks_shouldReturn401WhenUserIdIsInvalid() {
        when(session.getAttribute("userId"))
                .thenReturn("gecersiz-user-id");

        ResponseEntity<List<TaskResponseDto>> response =
                taskController.getMyTasks(session);

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                response.getStatusCode()
        );

        assertNull(response.getBody());

        verifyNoInteractions(taskService);
    }
}