
package com.tein.karardefteri.task.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskRequestDto(
       @NotBlank(message = "Başlık boş olamaz")
       @Size(max=200,message="Başlık en fazla 200 karakter olmalıdır")

       String title,
       String description,
       UUID assigneeId,
       OffsetDateTime dueDate,
       String status,
       String priority
) {}
