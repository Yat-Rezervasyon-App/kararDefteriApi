package com.tein.karardefteri.summary.dto;

public record ProjectSummaryResponseDto(
        long meetingCount,
        long activeDecisionCount,
        long openTaskCount,
        long overdueTaskCount
) {
}