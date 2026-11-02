
package com.tein.karardefteri.task.service;

import com.tein.karardefteri.meeting.service.MeetingService;
import com.tein.karardefteri.user.service.UserService;
import com.tein.karardefteri.task.dto.TaskRequestDto;
import com.tein.karardefteri.task.dto.TaskResponseDto;
import com.tein.karardefteri.task.entity.Task;
import com.tein.karardefteri.task.entity.TaskPriority;
import com.tein.karardefteri.task.entity.TaskStatus;
import com.tein.karardefteri.task.repository.TaskRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final MeetingService meetingService;
    private final UserService userService;

    public TaskService(
            TaskRepository taskRepository,
            MeetingService meetingService,
            UserService userService
    ) {
        this.taskRepository = taskRepository;
        this.meetingService = meetingService;
        this.userService = userService;
    }

    public TaskResponseDto createTask(
            UUID meetingId,
            TaskRequestDto request
    ) {
        // Toplantının varlığını MeetingService üzerinden kontrol et.
        meetingService.getMeetingById(meetingId);

        if (request.title() == null || request.title().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Başlık boş olamaz"
            );
        }

        if (request.title().length() > 200) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Başlık en fazla 200 karakter olabilir"
            );
        }

        if (request.assigneeId() != null
                && !userService.userExists(request.assigneeId())) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Kullanıcı bulunamadı"
            );
        }

        TaskStatus status = parseStatus(request.status());
        TaskPriority priority = parsePriority(request.priority());

        Task task = new Task();
        task.setMeetingId(meetingId);
        task.setTitle(request.title().trim());
        task.setDescription(request.description());
        task.setAssigneeId(request.assigneeId());
        task.setDueDate(request.dueDate());
        task.setStatus(status);
        task.setPriority(priority);

        return toResponse(taskRepository.save(task));
    }

    public List<TaskResponseDto> getTasksByMeetingId(UUID meetingId) {
        // Olmayan toplantının görev listesi istenirse 404 döner.
        meetingService.getMeetingById(meetingId);

        return taskRepository.findByMeetingId(meetingId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private TaskStatus parseStatus(String value) {
        if (value == null || value.isBlank()) {
            return TaskStatus.TODO;
        }

        try {
            return TaskStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Durum TODO, IN_PROGRESS veya DONE olmalıdır"
            );
        }
    }

    private TaskPriority parsePriority(String value) {
        if (value == null || value.isBlank()) {
            return TaskPriority.MEDIUM;
        }

        try {
            return TaskPriority.valueOf(value);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Öncelik LOW, MEDIUM veya HIGH olmalıdır"
            );
        }
    }

    private TaskResponseDto toResponse(Task task) {
        return new TaskResponseDto(
                task.getId(),
                task.getMeetingId(),
                task.getTitle(),
                task.getDescription(),
                task.getAssigneeId(),
                task.getDueDate(),
                task.getStatus().name(),
                task.getPriority().name(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
