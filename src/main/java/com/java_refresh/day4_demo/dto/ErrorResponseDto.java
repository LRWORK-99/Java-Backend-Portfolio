package com.java_refresh.day4_demo.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponseDto(int status, String message, LocalDateTime timestamp, Map<String, String> fieldErrors) {

    public ErrorResponseDto(int status, String message) {
        this(status, message, LocalDateTime.now(), null);
    }
}
