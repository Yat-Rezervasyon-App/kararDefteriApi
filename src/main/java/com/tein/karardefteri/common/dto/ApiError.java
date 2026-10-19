package com.tein.karardefteri.common.dto;

import java.util.Map;

public record ApiError(String code, String message, Map<String, String> fieldErrors) {
    public ApiError { fieldErrors = Map.copyOf(fieldErrors); }

    public ApiError(String code, String message) { this(code, message, Map.of()); }
}
