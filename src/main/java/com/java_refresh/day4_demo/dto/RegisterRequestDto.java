package com.java_refresh.day4_demo.dto;

import jakarta.validation.constraints.NotBlank;

public record RegisterRequestDto(@NotBlank String username, @NotBlank String password) {}
