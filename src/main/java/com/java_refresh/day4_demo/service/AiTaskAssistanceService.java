package com.java_refresh.day4_demo.service;

import com.java_refresh.day4_demo.dto.GeminiRequestDto;
import com.java_refresh.day4_demo.dto.GeminiResponseDto;
import com.java_refresh.day4_demo.dto.SubtaskSuggestionResponseDto;
import com.java_refresh.day4_demo.model.Task;
import jakarta.servlet.http.Part;
import org.springframework.boot.json.JsonParseException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.List;

@Service
public class AiTaskAssistanceService {

    private final RestClient geminiRestClient;
    private final JsonMapper jsonMapper;


    public AiTaskAssistanceService(RestClient geminiRestClient, JsonMapper jsonMapper) {
        this.geminiRestClient = geminiRestClient;
        this.jsonMapper = jsonMapper;
    }

    public SubtaskSuggestionResponseDto suggestSubtasks(Task task) {
        String prompt = buildPrompt(task);

        GeminiRequestDto geminiRequestDto = new GeminiRequestDto(
                List.of(new GeminiRequestDto.Content(
                        List.of(new GeminiRequestDto.Part(prompt))
                ))
        );

        GeminiResponseDto geminiResponseDto;

        try {
            geminiResponseDto = geminiRestClient.post()
                    .body(geminiRequestDto)
                    .retrieve()
                    .body(GeminiResponseDto.class);
        } catch (RestClientException ex) {
            return new SubtaskSuggestionResponseDto(List.of(), "AI service unavailable, please try again later.");
        }

        if (geminiResponseDto == null || geminiResponseDto.candidates() == null || geminiResponseDto.candidates().isEmpty()
            || geminiResponseDto.candidates().getFirst().content().parts().isEmpty()) {
            return new SubtaskSuggestionResponseDto(List.of(), "AI returned no suggestions.");
        }

        String rawText = geminiResponseDto.candidates().get(0).content().parts().get(0).text();

        List<String> subTasks;

        try{
            subTasks = jsonMapper.readValue(rawText, new TypeReference<List<String>>() {});
        } catch (JacksonException ex) {
            return new SubtaskSuggestionResponseDto(List.of(), "AI response could not be parsed.");
        }

        if (subTasks == null || subTasks.isEmpty()) {
            return new SubtaskSuggestionResponseDto(List.of(), "AI returned no usable suggestions.");
        }

        return new SubtaskSuggestionResponseDto(subTasks, null);

    }

    private String buildPrompt(Task task) {
        return """
        You are helping break down a task into actionable subtasks.
        Task title: %s

        Return ONLY a JSON array of short subtask strings, nothing else.
        Example: ["Research options", "Draft outline", "Review with team"]
        """.formatted(task.getTitle());
    }

}
