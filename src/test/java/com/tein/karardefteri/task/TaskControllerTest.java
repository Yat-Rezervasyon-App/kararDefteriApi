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

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
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

        Page<TaskResponseDto> taskPage =
                new PageImpl<>(List.of(task));

        when(session.getAttribute("userId"))
                .thenReturn(loggedInUserId);

        when(taskService.getMyTasks(
                loggedInUserId,
                null,
                null,
                null,
                null,
                0,
                20
        )).thenReturn(taskPage);

        ResponseEntity<Page<TaskResponseDto>> response =
                taskController.getMyTasks(
                        null,
                        null,
                        null,
                        null,
                        0,
                        20,
                        session
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getContent().size());

        assertEquals(
                loggedInUserId,
                response.getBody().getContent().get(0).assigneeId()
        );

        verify(taskService).getMyTasks(
                loggedInUserId,
                null,
                null,
                null,
                null,
                0,
                20
        );
    }

    @Test
    void getMyTasks_shouldReturn401WhenUserIsNotLoggedIn() {

        when(session.getAttribute("userId"))
                .thenReturn(null);

        ResponseEntity<Page<TaskResponseDto>> response =
                taskController.getMyTasks(
                        null,
                        null,
                        null,
                        null,
                        0,
                        20,
                        session
                );

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

        when(taskService.getMyTasks(
                loggedInUserId,
                null,
                null,
                null,
                null,
                0,
                20
        )).thenReturn(new PageImpl<>(List.of()));

        ResponseEntity<Page<TaskResponseDto>> response =
                taskController.getMyTasks(
                        null,
                        null,
                        null,
                        null,
                        0,
                        20,
                        session
                );

        assertEquals(HttpStatus.OK, response.getStatusCode());

        verify(taskService).getMyTasks(
                loggedInUserId,
                null,
                null,
                null,
                null,
                0,
                20
        );
    }

    @Test
    void getMyTasks_shouldReturn401WhenUserIdIsInvalid() {

        when(session.getAttribute("userId"))
                .thenReturn("gecersiz-user-id");

        ResponseEntity<Page<TaskResponseDto>> response =
                taskController.getMyTasks(
                        null,
                        null,
                        null,
                        null,
                        0,
                        20,
                        session
                );

        assertEquals(
                HttpStatus.UNAUTHORIZED,
                response.getStatusCode()
        );

        assertNull(response.getBody());

        verifyNoInteractions(taskService);
    }
}