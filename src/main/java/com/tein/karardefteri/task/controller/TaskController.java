
package com.tein.karardefteri.task.controller;

import com.tein.karardefteri.task.dto.TaskRequestDto;
import com.tein.karardefteri.task.dto.TaskResponseDto;
import com.tein.karardefteri.task.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
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
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponseDto createTask(
            @PathVariable UUID meetingId,
            @Valid @RequestBody TaskRequestDto request
    ) {
        return taskService.createTask(meetingId, request);
    }

    @GetMapping
    public List<TaskResponseDto> getTasks(
            @PathVariable UUID meetingId
    ) {
        return taskService.getTasksByMeetingId(meetingId);
    }
}
