
package com.tein.karardefteri.project.dto;

import java.time.Instant;
import java.util.UUID;

public record ProjectResponseDto(
        UUID id,
        String name,
        String description,
        UUID createdBy,
        boolean archived,
        Instant createdAt,
        Instant updatedAt
) {
}
