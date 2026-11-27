
package com.tein.karardefteri.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TaskRequestDto(

        @Schema(
                description = "Görevin başlığı. Zorunludur ve en fazla 200 karakter olabilir.",
                example = "API testlerini tamamla",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "Başlık boş olamaz")
        @Size(
                max = 200,
                message = "Başlık en fazla 200 karakter olabilir"
        )
        String title,

        @Schema(
                description = "Görev hakkında isteğe bağlı açıklama.",
                example = "Görev tanımlandıktan sonra test sonuçları kontrol edilmeli.",
                nullable = true
        )
        String description,

        @Schema(
                description = "Görevin atanacağı kullanıcının UUID değeri. Yeni görev oluşturulurken boş bırakılırsa görev atanmamış olur. PUT ile güncellemede null mevcut atamayı korur.",
                example = "550e8400-e29b-41d4-a716-446655440000",
                nullable = true
        )
        UUID assigneeId,

        @Schema(
                description = "Görevin tamamlanması gereken son tarih. Yeni görevde boş bırakılabilir; PUT ile güncellemede null son tarihi temizler.",
                example = "2026-11-25T17:00:00+03:00",
                nullable = true
        )
        OffsetDateTime dueDate,

        @Schema(
                description = "Görev durumu. TODO: Başlanmadı, IN_PROGRESS: Devam ediyor, DONE: Tamamlandı. Gönderilmezse veya null ise TODO olur.",
                example = "TODO",
                allowableValues = {"TODO", "IN_PROGRESS", "DONE"}
        )
        @Pattern(
                regexp = "^(TODO|IN_PROGRESS|DONE)$",
                message = "Durum TODO, IN_PROGRESS veya DONE olmalıdır"
        )
        String status,

        @Schema(
                description = "Görev önceliği. LOW: Düşük, MEDIUM: Orta, HIGH: Yüksek. Gönderilmezse veya null ise MEDIUM olur.",
                example = "MEDIUM",
                allowableValues = {"LOW", "MEDIUM", "HIGH"}
        )
        @Pattern(
                regexp = "^(LOW|MEDIUM|HIGH)$",
                message = "Öncelik LOW, MEDIUM veya HIGH olmalıdır"
        )
        String priority
) {}
