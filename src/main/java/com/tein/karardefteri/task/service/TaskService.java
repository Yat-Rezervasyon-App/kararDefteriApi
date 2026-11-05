
package com.tein.karardefteri.task.service;

import com.tein.karardefteri.project.service.ProjectService;
import com.tein.karardefteri.meeting.entity.Meeting;
import com.tein.karardefteri.meeting.service.MeetingService;
import com.tein.karardefteri.task.dto.TaskRequestDto;
import com.tein.karardefteri.task.dto.TaskResponseDto;
import com.tein.karardefteri.task.entity.Task;
import com.tein.karardefteri.task.entity.TaskPriority;
import com.tein.karardefteri.task.entity.TaskStatus;
import com.tein.karardefteri.task.repository.TaskRepository;
import com.tein.karardefteri.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final MeetingService meetingService;
    private final UserService userService;
    private final ProjectService projectService;

    public TaskService(
            TaskRepository taskRepository,
            MeetingService meetingService,
            UserService userService,
            ProjectService projectService) {
            UserService userService) {

        this.taskRepository = taskRepository;
        this.meetingService = meetingService;
        this.userService = userService;
        this.projectService = projectService;
    }

    // Toplantı ve bağlı projenin arşiv durumunu kontrol etme
    private void checkTaskParentsAreActive(Meeting meeting) {

        if (meeting.isArchived()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Archived meeting cannot be modified");
        }

        var project = projectService.getProjectById(
                meeting.getProjectId());

        if (project.isArchived()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Archived project tasks cannot be modified");
        }
    }

    // Görev oluşturma
    @Transactional
    public TaskResponseDto createTask(
            UUID meetingId,
            TaskRequestDto requestDto) {

        Meeting meeting = meetingService.getMeetingById(meetingId);

        checkTaskParentsAreActive(meeting);

        // Atanacak kullanıcı aktif mi?
        if (requestDto.assigneeId() != null
                && !userService.isUserActive(requestDto.assigneeId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Assigned user does not exist or is inactive");
        meetingService.getMeetingById(meetingId);
        checkAssigneeIsActive(requestDto.assigneeId());

        if (requestDto.title() == null || requestDto.title().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Task title cannot be empty"
            );
        }

        Task task = new Task();

        task.setMeetingId(meetingId);
        task.setTitle(requestDto.title().trim());
        task.setDescription(requestDto.description());
        if (requestDto.assigneeId() != null) {
            task.setAssigneeId(requestDto.assigneeId());
        }
        task.setDueDate(requestDto.dueDate());

        if (requestDto.status() != null
                && !requestDto.status().isBlank()) {

            task.setStatus(parseStatus(requestDto.status()));
        }

        if (requestDto.priority() != null
                && !requestDto.priority().isBlank()) {

        if (requestDto.status() == null
                || requestDto.status().isBlank()) {
            task.setStatus(TaskStatus.TODO);
        } else {
            task.setStatus(parseStatus(requestDto.status()));
        }

        if (requestDto.priority() == null
                || requestDto.priority().isBlank()) {
            task.setPriority(TaskPriority.MEDIUM);
        } else {
            task.setPriority(parsePriority(requestDto.priority()));
        }

        return mapToDto(taskRepository.save(task));
    }

    // Toplantıya ait görevleri listeleme
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getTasksByMeetingId(UUID meetingId) {

        meetingService.getMeetingById(meetingId);

        return taskRepository.findByMeetingId(meetingId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // Giriş yapan kullanıcının görevlerini listeleme
    @Transactional(readOnly = true)
    public List<TaskResponseDto> getMyTasks(UUID userId) {

        return taskRepository.findByAssigneeId(userId)
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    // ID ile görev getirme
    @Transactional(readOnly = true)
    public TaskResponseDto getTaskById(Long id) {

        return mapToDto(findTaskById(id));
    }

    // Görev bilgilerini güncelleme
    @Transactional
    public TaskResponseDto updateTask(
            Long id,
            TaskRequestDto requestDto,
            UUID currentUserId,
            boolean isAdmin) {

        Task task = findTaskById(id);

        Meeting meeting = meetingService.getMeetingById(
                task.getMeetingId());

        // Önce yetki kontrolü yapılır.
        if (!isAdmin
                && (currentUserId == null
                || !currentUserId.equals(meeting.getCreatedBy()))) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the meeting owner or ADMIN can update the task");
        }

        // Yetkiden sonra toplantı ve proje arşiv kontrolü yapılır.
        checkTaskParentsAreActive(meeting);

        if (requestDto.priority() == null
                || requestDto.priority().isBlank()) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Task priority is required");
        }

        TaskPriority priority = parsePriority(requestDto.priority());

        // Yeni atanacak kullanıcı aktif mi?
        // Aynı mevcut atama korunuyorsa tekrar kontrol edilmez.
        if (requestDto.assigneeId() != null
                && !requestDto.assigneeId().equals(task.getAssigneeId())
                && !userService.isUserActive(requestDto.assigneeId())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Assigned user does not exist or is inactive");
        }

        task.setTitle(requestDto.title());
        task.setDescription(requestDto.description());
        if (requestDto.assigneeId() != null) {
            task.setAssigneeId(requestDto.assigneeId());
        }
        checkAssigneeIsActive(requestDto.assigneeId());

        task.setTitle(requestDto.title().trim());
        task.setDescription(requestDto.description());
        task.setAssigneeId(requestDto.assigneeId());
        task.setDueDate(requestDto.dueDate());
        task.setPriority(priority);

        return mapToDto(taskRepository.save(task));
    }

    // Görev durumunu güncelleme
    @Transactional
    public TaskResponseDto updateTaskStatus(
            Long id,
            String status,
            UUID currentUserId) {

        Task task = findTaskById(id);

        Meeting meeting = meetingService.getMeetingById(
                task.getMeetingId());

        // Önce göreve atanmış kullanıcının yetkisi kontrol edilir.
        if (currentUserId == null
                || !currentUserId.equals(task.getAssigneeId())) {

            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Only the assigned user can update task status");
        }

        // Yetkiden sonra arşiv kontrolü yapılır.
        checkTaskParentsAreActive(meeting);

                    "Only the assigned user can update task status"
            );
        }

        task.setStatus(parseStatus(status));

        return mapToDto(taskRepository.save(task));
    }

    // String değerini TaskStatus enum değerine dönüştürme
    private void checkAssigneeIsActive(UUID assigneeId) {

        if (assigneeId != null
                && !userService.isUserActive(assigneeId)) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Assigned user does not exist or is inactive"
            );
        }
    }

    private TaskStatus parseStatus(String status) {

        if (status == null || status.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Task status is required");
        }

        try {
            return TaskStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid task status");
        }
    }

    // String değerini TaskPriority enum değerine dönüştürme
                    "Task status is required"
            );
        }

        try {
            return TaskStatus.valueOf(status.trim());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid task status"
            );
        }
    }

    private TaskPriority parsePriority(String priority) {

        if (priority == null || priority.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Task priority is required");
        }

        try {
            return TaskPriority.valueOf(priority);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid task priority");
        }
    }

    // ID ile görev bulma
                    "Task priority is required"
            );
        }

        try {
            return TaskPriority.valueOf(priority.trim());
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid task priority"
            );
        }
    }

    private Task findTaskById(Long id) {

        return taskRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Task not found"));
    }

    // Entity nesnesini Response DTO'ya dönüştürme
    private TaskResponseDto mapToDto(Task task) {

        Meeting meeting = meetingService.getMeetingById(
                task.getMeetingId());

        var project = projectService.getProjectById(
                meeting.getProjectId());

                        "Task not found"
                ));
    }

    private TaskResponseDto mapToDto(Task task) {

        return new TaskResponseDto(
                task.getId(),
                task.getMeetingId(),
                meeting.getTitle(),
                project.getId(),
                project.getName(),
                task.getTitle(),
                task.getDescription(),
                task.getAssigneeId(),
                task.getDueDate(),
                task.getStatus() != null
                        ? task.getStatus().name()
                        : null,
                task.getPriority() != null
                        ? task.getPriority().name()
                        : null,
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
