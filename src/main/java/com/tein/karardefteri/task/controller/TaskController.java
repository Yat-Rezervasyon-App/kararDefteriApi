package com.tein.karardefteri.task.controller;

import com.tein.karardefteri.task.dto.TaskRequestDto;
import com.tein.karardefteri.task.dto.TaskResponseDto;
import com.tein.karardefteri.task.service.TaskService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Görev oluşturma
    @PostMapping("/api/meetings/{meetingId}/tasks")
    public ResponseEntity<TaskResponseDto> createTask(
            @PathVariable UUID meetingId,
            @Valid @RequestBody TaskRequestDto requestDto,
            HttpSession session) {

        UUID userId = getUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TaskResponseDto createdTask =
                taskService.createTask(meetingId, requestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(createdTask);
    }

    // Toplantıya ait görevleri listeleme
    @GetMapping("/api/meetings/{meetingId}/tasks")
    public ResponseEntity<List<TaskResponseDto>> getTasksByMeetingId(
            @PathVariable UUID meetingId,
            HttpSession session) {

        if (getUserId(session) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                taskService.getTasksByMeetingId(meetingId)
        );
    }

    // ID ile görev getirme
    @GetMapping("/api/tasks/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(
            @PathVariable Long id,
            HttpSession session) {

        if (getUserId(session) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                taskService.getTaskById(id)
        );
    }

    // Görev bilgilerini güncelleme
    @PutMapping("/api/tasks/{id}")
    public ResponseEntity<TaskResponseDto> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDto requestDto,
            HttpSession session) {

        UUID userId = getUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean isAdmin =
                "ADMIN".equals(session.getAttribute("role"));

        TaskResponseDto updatedTask = taskService.updateTask(
                id,
                requestDto,
                userId,
                isAdmin
        );

        return ResponseEntity.ok(updatedTask);
    }

    // Görev durumunu güncelleme
    @PatchMapping("/api/tasks/{id}/status")
    public ResponseEntity<TaskResponseDto> updateTaskStatus(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            HttpSession session) {

        UUID userId = getUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String status = request == null
                ? null
                : request.get("status");

        TaskResponseDto updatedTask =
                taskService.updateTaskStatus(
                        id,
                        status,
                        userId
                );

        return ResponseEntity.ok(updatedTask);
    }

    // Session üzerinden kullanıcı ID'sini alma
    private UUID getUserId(HttpSession session) {

        Object userId = session.getAttribute("userId");

        if (userId instanceof UUID) {
            return (UUID) userId;
        }

        if (userId instanceof String) {
            try {
                return UUID.fromString((String) userId);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        return null;
    }
}