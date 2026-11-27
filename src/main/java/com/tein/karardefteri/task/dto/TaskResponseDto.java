
package com.tein.karardefteri.task.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

@Schema(description = "Görev bilgilerini içeren API yanıt modeli.")
public record TaskResponseDto(

        @Schema(
                description = "Görevin benzersiz kayıt numarası.",
                example = "1"
        )
        Long id,

        @Schema(
                description = "Görevin bağlı olduğu toplantının UUID değeri.",
                example = "550e8400-e29b-41d4-a716-446655440000"
        )
        UUID meetingId,

        @Schema(
                description = "Görevin bağlı olduğu toplantının adı.",
                example = "Haftalık proje toplantısı"
        )
        String meetingTitle,

        @Schema(
                description = "Görevin bağlı olduğu projenin UUID değeri.",
                example = "650e8400-e29b-41d4-a716-446655440000"
        )
        UUID projectId,

        @Schema(
                description = "Görevin bağlı olduğu projenin adı.",
                example = "Karar Defteri API"
        )
        String projectName,

        @Schema(
                description = "Görevin başlığı.",
                example = "API testlerini tamamla"
        )
        String title,

        @Schema(
                description = "Görev açıklaması. Açıklama girilmemişse null olabilir.",
                example = "Görev tamamlandıktan sonra test sonuçları kontrol edilmeli.",
                nullable = true
        )
        String description,

        @Schema(
                description = "Görevin atandığı kullanıcının UUID değeri. Atama yapılmamışsa null olur.",
                example = "750e8400-e29b-41d4-a716-446655440000",
                nullable = true
        )
        UUID assigneeId,

        @Schema(
                description = "Görevin son teslim tarihi. Tarih belirlenmemişse null olur.",
                example = "2026-11-25T17:00:00+03:00",
                nullable = true
        )
        OffsetDateTime dueDate,

        @Schema(
                description = "Görev durumu: TODO (Başlanmadı), IN_PROGRESS (Devam ediyor), DONE (Tamamlandı).",
                example = "IN_PROGRESS",
                allowableValues = {"TODO", "IN_PROGRESS", "DONE"}
        )
        String status,

        @Schema(
                description = "Görev önceliği: LOW (Düşük), MEDIUM (Orta), HIGH (Yüksek).",
                example = "MEDIUM",
                allowableValues = {"LOW", "MEDIUM", "HIGH"}
        )
        String priority,

        @Schema(
                description = "Görevin oluşturulma zamanı.",
                example = "2026-11-19T10:30:00Z"
        )
        Instant createdAt,

        @Schema(
                description = "Görev bilgilerinin son güncellenme zamanı.",
                example = "2026-11-19T11:45:00Z"
        )
        Instant updatedAt
) {}
