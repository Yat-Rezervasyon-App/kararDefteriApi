package com.tein.karardefteri.task.dto;

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
        String status,
        String priority,
        Instant createdAt,
        Instant updatedAt
) {}