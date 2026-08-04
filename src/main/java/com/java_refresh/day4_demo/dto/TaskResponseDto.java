package com.java_refresh.day4_demo.dto;

public record TaskResponseDto(Long id, String title, boolean completed, String ownerUsername) {}
