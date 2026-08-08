package com.java_refresh.day4_demo.dto;

import java.util.List;

public record GeminiResponseDto(List<Candidate> candidates) {

    public record Candidate(Content content) {}
    public record Content(List<Part> parts) {}
    public record Part(String text) {}
}
