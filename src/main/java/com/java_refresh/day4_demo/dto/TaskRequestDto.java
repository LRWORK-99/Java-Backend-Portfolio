package com.java_refresh.day4_demo.dto;

import com.java_refresh.day4_demo.model.User;
import jakarta.validation.constraints.NotBlank;

public record TaskRequestDto(@NotBlank String title, boolean completed) {}
