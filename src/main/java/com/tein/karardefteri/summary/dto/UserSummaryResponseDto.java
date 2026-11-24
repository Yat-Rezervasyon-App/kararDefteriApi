
package com.tein.karardefteri.summary.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Giriş yapan kullanıcının görev sayılarını içeren özet yanıtı.")
public record UserSummaryResponseDto(

        @Schema(
                description = "Kullanıcının tamamlanmamış (TODO veya IN_PROGRESS) görevlerinin toplam sayısı.",
                example = "7"
        )
        long openTaskCount,

        @Schema(
                description = "Kullanıcının son teslim tarihi geçmiş ve henüz tamamlanmamış görevlerinin sayısı.",
                example = "2"
        )
        long overdueTaskCount

) {
}
