
package com.tein.karardefteri.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Görev durumunu güncelleme isteği")
public record TaskStatusUpdateRequestDto(

        @Schema(
                description = "Yeni görev durumu",
                example = "IN_PROGRESS",
                allowableValues = {"TODO", "IN_PROGRESS", "DONE"},
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Görev durumu boş olamaz")
        @Pattern(
                regexp = "^(TODO|IN_PROGRESS|DONE)$",
                message = "Durum TODO, IN_PROGRESS veya DONE olmalıdır"
        )
        String status
) {}
