package com.tein.karardefteri.meeting.dto;

import com.tein.karardefteri.project.dto.ProjectResponseDto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record MeetingDetailResponseDto(
        UUID id,
        UUID projectId,
        String title,
        OffsetDateTime meetingAt,
        String description,
        String notes,
        UUID createdBy,
        boolean archived,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt,
        ProjectResponseDto project
) {
}