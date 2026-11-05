package com.tein.karardefteri.decision.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class DecisionCreateRequest {

    @NotBlank(message = "Karar içeriği boş olamaz.")
    @Size(max = 5000, message = "Karar içeriği en fazla 5000 karakter olabilir.")
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}