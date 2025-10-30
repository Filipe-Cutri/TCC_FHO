package com.slotfy.service;

import com.slotfy.dto.SchedulerSuggestRequest;
import com.slotfy.dto.SchedulerSuggestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for BedrockService
 */
@ExtendWith(MockitoExtension.class)
public class BedrockServiceTest {
    
    private BedrockService bedrockService;
    
    @BeforeEach
    void setUp() {
        bedrockService = new BedrockService();
    }
    
    @Test
    void testBuildPrompt_WithAllFields() {
        // Arrange
        SchedulerSuggestRequest request = new SchedulerSuggestRequest();
        request.setUserId("user123");
        request.setTimezone("America/Sao_Paulo");
        request.setDuration(60);
        request.setBuffer(10);
        request.setPreferences("morning preferred");
        request.setMaxSuggestions(3);
        
        List<SchedulerSuggestRequest.TimeWindow> availableWindows = new ArrayList<>();
        availableWindows.add(new SchedulerSuggestRequest.TimeWindow("2025-11-03T08:00:00-03:00", "2025-11-03T18:00:00-03:00"));
        request.setAvailableWindows(availableWindows);
        
        List<SchedulerSuggestRequest.TimeWindow> busySlots = new ArrayList<>();
        busySlots.add(new SchedulerSuggestRequest.TimeWindow("2025-11-03T12:00:00-03:00", "2025-11-03T13:00:00-03:00"));
        request.setBusySlots(busySlots);
        
        // Act
        String prompt = bedrockService.buildPrompt(request);
        
        // Assert
        assertNotNull(prompt);
        assertTrue(prompt.contains("timezone: America/Sao_Paulo"));
        assertTrue(prompt.contains("duration (minutos): 60"));
        assertTrue(prompt.contains("buffer (minutos): 10"));
        assertTrue(prompt.contains("preferences: morning preferred"));
        assertTrue(prompt.contains("maxSuggestions: 3"));
        assertTrue(prompt.contains("availableWindows:"));
        assertTrue(prompt.contains("busySlots:"));
        assertTrue(prompt.contains("suggestions"));
    }
    
    @Test
    void testBuildPrompt_WithMinimalFields() {
        // Arrange
        SchedulerSuggestRequest request = new SchedulerSuggestRequest();
        request.setUserId("user123");
        request.setTimezone("UTC");
        request.setDuration(30);
        
        // Act
        String prompt = bedrockService.buildPrompt(request);
        
        // Assert
        assertNotNull(prompt);
        assertTrue(prompt.contains("timezone: UTC"));
        assertTrue(prompt.contains("duration (minutos): 30"));
        assertTrue(prompt.contains("buffer (minutos): 0"));
        assertTrue(prompt.contains("availableWindows: [] (considerar dia inteiro)"));
        assertTrue(prompt.contains("busySlots: [] (sem bloqueios)"));
        assertTrue(prompt.contains("maxSuggestions: 3")); // default value
    }
    
    @Test
    void testExtractJson_ValidJson() {
        // Arrange
        String response = "Some text before {\"suggestions\": [{\"start\": \"2025-11-03T09:00:00-03:00\", \"end\": \"2025-11-03T10:00:00-03:00\", \"reason\": \"Test\", \"score\": 0.9}]} some text after";
        
        // Act
        String json = bedrockService.extractJson(response);
        
        // Assert
        assertNotNull(json);
        assertTrue(json.startsWith("{"));
        assertTrue(json.endsWith("}"));
        assertTrue(json.contains("suggestions"));
    }
    
    @Test
    void testExtractJson_OnlyJson() {
        // Arrange
        String response = "{\"suggestions\": []}";
        
        // Act
        String json = bedrockService.extractJson(response);
        
        // Assert
        assertEquals(response, json);
    }
    
    @Test
    void testExtractJson_NoJson() {
        // Arrange
        String response = "No JSON here";
        
        // Act
        String json = bedrockService.extractJson(response);
        
        // Assert
        assertNull(json);
    }
    
    @Test
    void testExtractJson_EmptyString() {
        // Arrange
        String response = "";
        
        // Act
        String json = bedrockService.extractJson(response);
        
        // Assert
        assertNull(json);
    }
    
    @Test
    void testParseResponse_ValidResponse() {
        // Arrange
        String response = "{\"suggestions\": [{\"start\": \"2025-11-03T09:00:00-03:00\", \"end\": \"2025-11-03T10:00:00-03:00\", \"reason\": \"Test reason\", \"score\": 0.95}]}";
        
        // Act
        SchedulerSuggestResponse result = bedrockService.parseResponse(response);
        
        // Assert
        assertNotNull(result);
        assertNotNull(result.getSuggestions());
        assertEquals(1, result.getSuggestions().size());
        
        SchedulerSuggestResponse.Suggestion suggestion = result.getSuggestions().get(0);
        assertEquals("2025-11-03T09:00:00-03:00", suggestion.getStart());
        assertEquals("2025-11-03T10:00:00-03:00", suggestion.getEnd());
        assertEquals("Test reason", suggestion.getReason());
        assertEquals(0.95, suggestion.getScore(), 0.001);
    }
    
    @Test
    void testParseResponse_MultipleValidSuggestions() {
        // Arrange
        String response = "{\"suggestions\": [" +
            "{\"start\": \"2025-11-03T09:00:00-03:00\", \"end\": \"2025-11-03T10:00:00-03:00\", \"reason\": \"Reason 1\", \"score\": 0.95}," +
            "{\"start\": \"2025-11-03T14:00:00-03:00\", \"end\": \"2025-11-03T15:00:00-03:00\", \"reason\": \"Reason 2\", \"score\": 0.85}" +
            "]}";
        
        // Act
        SchedulerSuggestResponse result = bedrockService.parseResponse(response);
        
        // Assert
        assertNotNull(result);
        assertEquals(2, result.getSuggestions().size());
    }
    
    @Test
    void testParseResponse_EmptySuggestions() {
        // Arrange
        String response = "{\"suggestions\": []}";
        
        // Act
        SchedulerSuggestResponse result = bedrockService.parseResponse(response);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getSuggestions().isEmpty());
    }
    
    @Test
    void testParseResponse_InvalidJson() {
        // Arrange
        String response = "Not a valid JSON";
        
        // Act
        SchedulerSuggestResponse result = bedrockService.parseResponse(response);
        
        // Assert
        assertNotNull(result);
        assertTrue(result.getSuggestions().isEmpty());
    }
    
    @Test
    void testParseResponse_MissingFields() {
        // Arrange
        String response = "{\"suggestions\": [{\"start\": \"2025-11-03T09:00:00-03:00\", \"end\": \"2025-11-03T10:00:00-03:00\"}]}";
        
        // Act
        SchedulerSuggestResponse result = bedrockService.parseResponse(response);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getSuggestions().size());
        
        SchedulerSuggestResponse.Suggestion suggestion = result.getSuggestions().get(0);
        assertEquals("", suggestion.getReason()); // defaults to empty string
        assertEquals(0.0, suggestion.getScore(), 0.001); // defaults to 0.0
    }
    
    @Test
    void testParseResponse_WithSurroundingText() {
        // Arrange
        String response = "Here is the JSON: {\"suggestions\": [{\"start\": \"2025-11-03T09:00:00-03:00\", \"end\": \"2025-11-03T10:00:00-03:00\", \"reason\": \"Test\", \"score\": 0.9}]} and some more text";
        
        // Act
        SchedulerSuggestResponse result = bedrockService.parseResponse(response);
        
        // Assert
        assertNotNull(result);
        assertEquals(1, result.getSuggestions().size());
    }
}
