package com.tein.karardefteri.task.controller;

import com.tein.karardefteri.task.dto.TaskRequestDto;
import com.tein.karardefteri.task.dto.TaskResponseDto;
import com.tein.karardefteri.task.service.TaskService;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    // Görev oluşturma.
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

    // Toplantıya ait görevleri listeleme.
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

    // Giriş yapan kullanıcının görevlerini filtreleyerek ve sayfalayarak getirir.
    @GetMapping("/api/users/me/tasks")
    public ResponseEntity<Page<TaskResponseDto>> getMyTasks(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) Boolean overdue,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {

        UUID userId = getUserId(session);

        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                taskService.getMyTasks(
                        userId,
                        projectId,
                        status,
                        priority,
                        overdue,
                        page,
                        size
                )
        );
    }

    // Tüm görevleri filtreleyerek ve sayfalayarak getirir.
    @GetMapping("/api/tasks")
    public ResponseEntity<Page<TaskResponseDto>> getTasks(
            @RequestParam(required = false) UUID projectId,
            @RequestParam(required = false) UUID assigneeId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) OffsetDateTime dueFrom,
            @RequestParam(required = false) OffsetDateTime dueTo,
            @RequestParam(required = false) Boolean overdue,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpSession session) {

        if (getUserId(session) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(
                taskService.getTasks(
                        projectId,
                        assigneeId,
                        status,
                        priority,
                        dueFrom,
                        dueTo,
                        overdue,
                        page,
                        size
                )
        );
    }

    // ID ile görev getirme.
    @GetMapping("/api/tasks/{id}")
    public ResponseEntity<TaskResponseDto> getTaskById(
            @PathVariable Long id,
            HttpSession session) {

        if (getUserId(session) == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    // Görev içeriğini ve atamasını güncelleme.
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

    // Görev durumunu güncelleme.
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

        TaskResponseDto updatedTask = taskService.updateTaskStatus(
                id,
                status,
                userId
        );

        return ResponseEntity.ok(updatedTask);
    }

    // Session üzerinden kullanıcı ID'sini alır.
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