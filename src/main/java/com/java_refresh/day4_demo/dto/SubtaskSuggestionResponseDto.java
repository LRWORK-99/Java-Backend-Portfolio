package com.java_refresh.day4_demo.dto;

import java.util.List;

// fallbackMessage is null/empty when subtasks are present; populated when AI declined or returned nothing usable
public record SubtaskSuggestionResponseDto (List<String> subtasks, String fallbackMessage) {
}
