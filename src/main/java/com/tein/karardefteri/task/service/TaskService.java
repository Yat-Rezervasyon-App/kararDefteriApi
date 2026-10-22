
package com.tein.karardefteri.task.service;

import com.tein.karardefteri.task.dto.TaskRequestDto;
import com.tein.karardefteri.task.dto.TaskResponseDto;
import com.tein.karardefteri.task.entity.Task;
import com.tein.karardefteri.task.entity.TaskStatus;
import com.tein.karardefteri.task.entity.TaskPriority;
import com.tein.karardefteri.task.repository.TaskRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Transactional
    public TaskResponseDto createTask(UUID meetingId, TaskRequestDto requestDto) {
        Task task = new Task();

        task.setMeetingId(meetingId);
        task.setTitle(requestDto.title());
        task.setDescription(requestDto.description());
        task.setAssigneeId(requestDto.assigneeId());
        task.setDueDate(requestDto.dueDate());

        if (requestDto.status() != null && !requestDto.status().isBlank()) {
            task.setStatus(TaskStatus.valueOf(requestDto.status()));
        }

        if (requestDto.priority() != null && !requestDto.priority().isBlank()) {
            task.setPriority(TaskPriority.valueOf(requestDto.priority()));
        }

        Task savedTask = taskRepository.save(task);
        return mapToDto(savedTask);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDto> getTasksByMeetingId(UUID meetingId) {
        return taskRepository.findByMeetingId(meetingId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    private TaskResponseDto mapToDto(Task task) {
        return new TaskResponseDto(
                task.getId(),
                task.getMeetingId(),
                task.getTitle(),
                task.getDescription(),
                task.getAssigneeId(),
                task.getDueDate(),
                task.getStatus(),
                task.getPriority(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
