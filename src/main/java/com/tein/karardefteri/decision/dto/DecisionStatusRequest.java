package com.tein.karardefteri.decision.dto;

import com.tein.karardefteri.decision.entity.DecisionStatus;
import jakarta.validation.constraints.NotNull;

public class DecisionStatusRequest {

    @NotNull(message = "Karar durumu boş olamaz.")
    private DecisionStatus status;

    public DecisionStatus getStatus() {
        return status;
    }

    public void setStatus(DecisionStatus status) {
        this.status = status;
    }
}