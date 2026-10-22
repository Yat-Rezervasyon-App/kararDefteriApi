
package com.tein.karardefteri.task.dto;

import com.tein.karardefteri.task.entity.TaskPriority;
import com.tein.karardefteri.task.entity.TaskStatus;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskResponseDto(
        Long id,
        UUID meetingId,
        String title,
        String description,
        UUID assigneeId,
        OffsetDateTime dueDate,
        TaskStatus status,
        TaskPriority priority,
        Instant createdAt,
        Instant updatedAt
) {}
