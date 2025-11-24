package com.slotfy.controller;

import com.slotfy.dto.SchedulerConfirmRequest;
import com.slotfy.dto.SchedulerSuggestRequest;
import com.slotfy.dto.SchedulerSuggestResponse;
import com.slotfy.service.SuggestionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SchedulerController.class)
@Import({com.slotfy.config.SecurityConfig.class})
public class SchedulerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private SuggestionService suggestionService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testSuggestSuccess() throws Exception {
        SchedulerSuggestRequest request = new SchedulerSuggestRequest();
        request.setUserId("user123");
        request.setDuration(60);
        request.setTimezone("America/Sao_Paulo");

        SchedulerSuggestResponse.Suggestion suggestion = new SchedulerSuggestResponse.Suggestion(
            "2024-01-01T10:00:00Z",
            "2024-01-01T11:00:00Z",
            "Morning slot",
            0.9
        );

        SchedulerSuggestResponse response = new SchedulerSuggestResponse(
            Collections.singletonList(suggestion)
        );

        when(suggestionService.generateSuggestions(any(SchedulerSuggestRequest.class)))
            .thenReturn(response);

        mockMvc.perform(post("/api/scheduler/suggest")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.suggestions").isArray())
            .andExpect(jsonPath("$.suggestions[0].start").value("2024-01-01T10:00:00Z"));
    }

    @Test
    @WithMockUser
    public void testSuggestInvalidRequest() throws Exception {
        SchedulerSuggestRequest request = new SchedulerSuggestRequest();
        request.setUserId("user123");

        when(suggestionService.generateSuggestions(any(SchedulerSuggestRequest.class)))
            .thenThrow(new IllegalArgumentException("Duration is required"));

        mockMvc.perform(post("/api/scheduler/suggest")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("Duration is required"));
    }

    @Test
    @WithMockUser
    public void testSuggestInternalError() throws Exception {
        SchedulerSuggestRequest request = new SchedulerSuggestRequest();
        request.setUserId("user123");
        request.setDuration(60);

        when(suggestionService.generateSuggestions(any(SchedulerSuggestRequest.class)))
            .thenThrow(new RuntimeException("Internal error"));

        mockMvc.perform(post("/api/scheduler/suggest")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser
    public void testConfirmSuccess() throws Exception {
        // Use future dates for the test
        LocalDateTime futureStart = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime futureEnd = futureStart.plusHours(1);
        
        SchedulerConfirmRequest request = new SchedulerConfirmRequest(
            "user123",
            futureStart.format(DateTimeFormatter.ISO_DATE_TIME),
            futureEnd.format(DateTimeFormatter.ISO_DATE_TIME)
        );

        mockMvc.perform(post("/api/scheduler/confirm")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.message").value("Time slot confirmed successfully"));
    }

    @Test
    @WithMockUser
    public void testConfirmMissingUserId() throws Exception {
        SchedulerConfirmRequest request = new SchedulerConfirmRequest(
            null,
            "2024-01-01T10:00:00",
            "2024-01-01T11:00:00"
        );

        mockMvc.perform(post("/api/scheduler/confirm")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("userId is required"));
    }

    @Test
    @WithMockUser
    public void testConfirmMissingStartTime() throws Exception {
        SchedulerConfirmRequest request = new SchedulerConfirmRequest(
            "user123",
            null,
            "2024-01-01T11:00:00"
        );

        mockMvc.perform(post("/api/scheduler/confirm")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("start time is required"));
    }

    @Test
    @WithMockUser
    public void testConfirmMissingEndTime() throws Exception {
        SchedulerConfirmRequest request = new SchedulerConfirmRequest(
            "user123",
            "2024-01-01T10:00:00",
            null
        );

        mockMvc.perform(post("/api/scheduler/confirm")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("end time is required"));
    }
}
