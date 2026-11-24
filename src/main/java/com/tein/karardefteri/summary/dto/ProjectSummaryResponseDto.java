
package com.tein.karardefteri.summary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Bir projeye ait toplantı, karar ve görev sayılarını içeren özet yanıtı.")
public record ProjectSummaryResponseDto(

        @Schema(
                description = "Projeye bağlı toplam toplantı sayısı.",
                example = "12"
        )
        long meetingCount,

        @Schema(
                description = "Projeye bağlı aktif karar sayısı.",
                example = "8"
        )
        long activeDecisionCount,

        @Schema(
                description = "Tamamlanmamış (TODO veya IN_PROGRESS) görevlerin toplam sayısı.",
                example = "15"
        )
        long openTaskCount,

        @Schema(
                description = "Son teslim tarihi geçmiş ve henüz tamamlanmamış görevlerin sayısı.",
                example = "3"
        )
        long overdueTaskCount
) {
}
