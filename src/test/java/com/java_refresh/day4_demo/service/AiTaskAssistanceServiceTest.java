package com.java_refresh.day4_demo.service;

import com.java_refresh.day4_demo.dto.GeminiRequestDto;
import com.java_refresh.day4_demo.dto.GeminiResponseDto;
import com.java_refresh.day4_demo.dto.SubtaskSuggestionResponseDto;
import com.java_refresh.day4_demo.model.Task;
import com.java_refresh.day4_demo.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiTaskAssistanceServiceTest {

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.RequestBodyUriSpec requestBodyUriSpec;

    @Mock
    private RestClient.RequestBodySpec requestBodySpec;

    @Mock
    private RestClient.ResponseSpec responseSpec;

    private AiTaskAssistanceService aiTaskAssistanceService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("testUser", "hashedPassword", "USER");
        testUser.setId(1L);

        JsonMapper jsonMapper = JsonMapper.builder().build();

        aiTaskAssistanceService = new AiTaskAssistanceService(restClient, jsonMapper);
    }

    @Test
    void suggestSubtasks_returnsSubtasks_whenGeminiRespondsSuccessfully() {
        Task task = new Task("Plan event", false, testUser);

        // Wire up each link of the chain to return the next mock
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(GeminiRequestDto.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        String fakeJsonFromModel = "[\"Step 1\", \"Step 2\"]";
        GeminiResponseDto fakeResponse = new GeminiResponseDto(List.of(
                new GeminiResponseDto.Candidate(new GeminiResponseDto.Content(List.of(
                        new GeminiResponseDto.Part(fakeJsonFromModel)
                )))
        ));

        when(responseSpec.body(GeminiResponseDto.class)).thenReturn(fakeResponse);

        SubtaskSuggestionResponseDto result = aiTaskAssistanceService.suggestSubtasks(task);

        assertEquals(List.of("Step 1", "Step 2"), result.subtasks());
        assertNull(result.fallbackMessage());
    }

    @Test
    void suggestSubtasks_returnsFallback_whenRestClientThrows() {
        Task task = new Task("Plan event", false, testUser);

        // Wire up each link of the chain to return the next mock
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(GeminiRequestDto.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenThrow(new RestClientException("Gemini unreachable"));

        SubtaskSuggestionResponseDto result = aiTaskAssistanceService.suggestSubtasks(task);

        assertTrue(result.subtasks().isEmpty());
        assertEquals("AI service unavailable, please try again later.", result.fallbackMessage());

    }

    @Test
    void suggestSubtasks_returnsFallback_whenCandidatesEmpty() {

        Task task = new Task("Plan event", false, testUser);

        // Wire up each link of the chain to return the next mock
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(GeminiRequestDto.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        GeminiResponseDto fakeResponse = new GeminiResponseDto(List.of(
                new GeminiResponseDto.Candidate(new GeminiResponseDto.Content(List.of()
                ))
        ));

        when(responseSpec.body(GeminiResponseDto.class)).thenReturn(fakeResponse);

        SubtaskSuggestionResponseDto result = aiTaskAssistanceService.suggestSubtasks(task);

        assertTrue(result.subtasks().isEmpty());
        assertEquals("AI returned no suggestions.", result.fallbackMessage());

    }

    @Test
    void suggestSubtasks_returnsFallback_whenResponseIsMalformedJson() {
        Task task = new Task("Plan event", false, testUser);

        // Wire up each link of the chain to return the next mock
        when(restClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.body(any(GeminiRequestDto.class))).thenReturn(requestBodySpec);
        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        String malformedJson = "Not a valid JSON format";
        GeminiResponseDto fakeResponse = new GeminiResponseDto(List.of(
                new GeminiResponseDto.Candidate(new GeminiResponseDto.Content(List.of(
                        new GeminiResponseDto.Part(malformedJson)
                )))
        ));

        when(responseSpec.body(GeminiResponseDto.class)).thenReturn(fakeResponse);

        SubtaskSuggestionResponseDto result = aiTaskAssistanceService.suggestSubtasks(task);

        assertTrue(result.subtasks().isEmpty());
        assertEquals("AI response could not be parsed.", result.fallbackMessage());

    }
}
