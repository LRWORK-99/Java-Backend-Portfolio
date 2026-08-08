package com.java_refresh.day4_demo.dto;

import java.util.List;

public record GeminiRequestDto(List<Content> contents) {

    public record Content(List<Part> parts) {}

    public record Part(String text) {}
}
