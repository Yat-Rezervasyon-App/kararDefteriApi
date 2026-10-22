
package com.tein.karardefteri.task.controller;

import com.tein.karardefteri.task.dto.TaskRequestDto;
import com.tein.karardefteri.task.dto.TaskResponseDto;
import com.tein.karardefteri.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/meetings/{meetingId}/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<TaskResponseDto> createTask(
            @PathVariable UUID meetingId,
            @Valid @RequestBody TaskRequestDto requestDto) {

        TaskResponseDto createdTask =
                taskService.createTask(meetingId, requestDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(createdTask);
    }

    @GetMapping
    public ResponseEntity<List<TaskResponseDto>> getTasksByMeetingId(
            @PathVariable UUID meetingId) {

        List<TaskResponseDto> tasks =
                taskService.getTasksByMeetingId(meetingId);

        return ResponseEntity.ok(tasks);
    }
}
