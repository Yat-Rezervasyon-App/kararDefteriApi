
package com.tein.karardefteri.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskRequestDto(

        @NotBlank(message = "Başlık boş olamaz")
        @Size(max = 200, message = "Başlık en fazla 200 karakter olabilir")
        String title,

        String description,

        UUID assigneeId,

        OffsetDateTime dueDate,

        @Pattern(
                regexp = "TODO|IN_PROGRESS|DONE",
                message = "Durum TODO, IN_PROGRESS veya DONE olmalıdır"
        )
        String status,

        @Pattern(
                regexp = "LOW|MEDIUM|HIGH",
                message = "Öncelik LOW, MEDIUM veya HIGH olmalıdır"
        )
        String priority

) {}
