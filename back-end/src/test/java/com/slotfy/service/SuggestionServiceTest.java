package com.slotfy.service;

import com.slotfy.dto.SchedulerSuggestRequest;
import com.slotfy.dto.SchedulerSuggestResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for SuggestionService
 */
@ExtendWith(MockitoExtension.class)
public class SuggestionServiceTest {
    
    @Mock
    private BedrockService bedrockService;
    
    @InjectMocks
    private SuggestionService suggestionService;
    
    private SchedulerSuggestRequest createValidRequest() {
        SchedulerSuggestRequest request = new SchedulerSuggestRequest();
        request.setUserId("user123");
        request.setTimezone("America/Sao_Paulo");
        request.setDuration(60);
        request.setBuffer(10);
        request.setMaxSuggestions(3);
        return request;
    }
    
    @Test
    void testGenerateSuggestions_ValidRequest() {
        // Arrange
        SchedulerSuggestRequest request = createValidRequest();
        
        List<SchedulerSuggestResponse.Suggestion> suggestions = new ArrayList<>();
        suggestions.add(new SchedulerSuggestResponse.Suggestion(
            "2025-11-03T09:00:00-03:00",
            "2025-11-03T10:00:00-03:00",
            "Good time",
            0.95
        ));
        
        SchedulerSuggestResponse mockResponse = new SchedulerSuggestResponse(suggestions);
        when(bedrockService.generateSuggestions(any())).thenReturn(mockResponse);
        
        // Act
        SchedulerSuggestResponse response = suggestionService.generateSuggestions(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.getSuggestions().size());
    }
    
    @Test
    void testGenerateSuggestions_MissingUserId() {
        // Arrange
        SchedulerSuggestRequest request = createValidRequest();
        request.setUserId(null);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            suggestionService.generateSuggestions(request);
        });
    }
    
    @Test
    void testGenerateSuggestions_EmptyUserId() {
        // Arrange
        SchedulerSuggestRequest request = createValidRequest();
        request.setUserId("");
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            suggestionService.generateSuggestions(request);
        });
    }
    
    @Test
    void testGenerateSuggestions_MissingTimezone() {
        // Arrange
        SchedulerSuggestRequest request = createValidRequest();
        request.setTimezone(null);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            suggestionService.generateSuggestions(request);
        });
    }
    
    @Test
    void testGenerateSuggestions_InvalidTimezone() {
        // Arrange
        SchedulerSuggestRequest request = createValidRequest();
        request.setTimezone("Invalid/Timezone");
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            suggestionService.generateSuggestions(request);
        });
    }
    
    @Test
    void testGenerateSuggestions_ZeroDuration() {
        // Arrange
        SchedulerSuggestRequest request = createValidRequest();
        request.setDuration(0);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            suggestionService.generateSuggestions(request);
        });
    }
    
    @Test
    void testGenerateSuggestions_NegativeDuration() {
        // Arrange
        SchedulerSuggestRequest request = createValidRequest();
        request.setDuration(-10);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            suggestionService.generateSuggestions(request);
        });
    }
    
    @Test
    void testGenerateSuggestions_NegativeBuffer() {
        // Arrange
        SchedulerSuggestRequest request = createValidRequest();
        request.setBuffer(-5);
        
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            suggestionService.generateSuggestions(request);
        });
    }
    
    @Test
    void testGenerateSuggestions_FilterConflictingWithBusySlots() {
        // Arrange
        SchedulerSuggestRequest request = createValidRequest();
        
        List<SchedulerSuggestRequest.TimeWindow> busySlots = new ArrayList<>();
        busySlots.add(new SchedulerSuggestRequest.TimeWindow(
            "2025-11-03T09:00:00-03:00",
            "2025-11-03T10:00:00-03:00"
        ));
        request.setBusySlots(busySlots);
        
        List<SchedulerSuggestResponse.Suggestion> suggestions = new ArrayList<>();
        // This suggestion conflicts with busy slot
        suggestions.add(new SchedulerSuggestResponse.Suggestion(
            "2025-11-03T09:00:00-03:00",
            "2025-11-03T10:00:00-03:00",
            "Conflicting",
            0.95
        ));
        // This one doesn't conflict
        suggestions.add(new SchedulerSuggestResponse.Suggestion(
            "2025-11-03T14:00:00-03:00",
            "2025-11-03T15:00:00-03:00",
            "Non-conflicting",
            0.85
        ));
        
        SchedulerSuggestResponse mockResponse = new SchedulerSuggestResponse(suggestions);
        when(bedrockService.generateSuggestions(any())).thenReturn(mockResponse);
        
        // Act
        SchedulerSuggestResponse response = suggestionService.generateSuggestions(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.getSuggestions().size());
        assertEquals("2025-11-03T14:00:00-03:00", response.getSuggestions().get(0).getStart());
    }
    
    @Test
    void testGenerateSuggestions_FilterOutsideAvailableWindows() {
        // Arrange
        SchedulerSuggestRequest request = createValidRequest();
        
        List<SchedulerSuggestRequest.TimeWindow> availableWindows = new ArrayList<>();
        availableWindows.add(new SchedulerSuggestRequest.TimeWindow(
            "2025-11-03T08:00:00-03:00",
            "2025-11-03T12:00:00-03:00"
        ));
        request.setAvailableWindows(availableWindows);
        
        List<SchedulerSuggestResponse.Suggestion> suggestions = new ArrayList<>();
        // This suggestion is within available window
        suggestions.add(new SchedulerSuggestResponse.Suggestion(
            "2025-11-03T09:00:00-03:00",
            "2025-11-03T10:00:00-03:00",
            "Within window",
            0.95
        ));
        // This one is outside available window
        suggestions.add(new SchedulerSuggestResponse.Suggestion(
            "2025-11-03T14:00:00-03:00",
            "2025-11-03T15:00:00-03:00",
            "Outside window",
            0.85
        ));
        
        SchedulerSuggestResponse mockResponse = new SchedulerSuggestResponse(suggestions);
        when(bedrockService.generateSuggestions(any())).thenReturn(mockResponse);
        
        // Act
        SchedulerSuggestResponse response = suggestionService.generateSuggestions(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.getSuggestions().size());
        assertEquals("2025-11-03T09:00:00-03:00", response.getSuggestions().get(0).getStart());
    }
    
    @Test
    void testGenerateSuggestions_SortByScore() {
        // Arrange
        SchedulerSuggestRequest request = createValidRequest();
        
        List<SchedulerSuggestResponse.Suggestion> suggestions = new ArrayList<>();
        suggestions.add(new SchedulerSuggestResponse.Suggestion(
            "2025-11-03T09:00:00-03:00",
            "2025-11-03T10:00:00-03:00",
            "Low score",
            0.70
        ));
        suggestions.add(new SchedulerSuggestResponse.Suggestion(
            "2025-11-03T14:00:00-03:00",
            "2025-11-03T15:00:00-03:00",
            "High score",
            0.95
        ));
        suggestions.add(new SchedulerSuggestResponse.Suggestion(
            "2025-11-03T16:00:00-03:00",
            "2025-11-03T17:00:00-03:00",
            "Medium score",
            0.85
        ));
        
        SchedulerSuggestResponse mockResponse = new SchedulerSuggestResponse(suggestions);
        when(bedrockService.generateSuggestions(any())).thenReturn(mockResponse);
        
        // Act
        SchedulerSuggestResponse response = suggestionService.generateSuggestions(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(3, response.getSuggestions().size());
        // Should be sorted by score descending
        assertEquals(0.95, response.getSuggestions().get(0).getScore());
        assertEquals(0.85, response.getSuggestions().get(1).getScore());
        assertEquals(0.70, response.getSuggestions().get(2).getScore());
    }
    
    @Test
    void testGenerateSuggestions_LimitToMaxSuggestions() {
        // Arrange
        SchedulerSuggestRequest request = createValidRequest();
        request.setMaxSuggestions(2);
        
        List<SchedulerSuggestResponse.Suggestion> suggestions = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            suggestions.add(new SchedulerSuggestResponse.Suggestion(
                String.format("2025-11-03T%02d:00:00-03:00", 9 + i),
                String.format("2025-11-03T%02d:00:00-03:00", 10 + i),
                "Suggestion " + i,
                0.9 - (i * 0.1)
            ));
        }
        
        SchedulerSuggestResponse mockResponse = new SchedulerSuggestResponse(suggestions);
        when(bedrockService.generateSuggestions(any())).thenReturn(mockResponse);
        
        // Act
        SchedulerSuggestResponse response = suggestionService.generateSuggestions(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(2, response.getSuggestions().size());
    }
    
    @Test
    void testGenerateSuggestions_FilterInvalidTimeRange() {
        // Arrange
        SchedulerSuggestRequest request = createValidRequest();
        
        List<SchedulerSuggestResponse.Suggestion> suggestions = new ArrayList<>();
        // Invalid: end before start
        suggestions.add(new SchedulerSuggestResponse.Suggestion(
            "2025-11-03T10:00:00-03:00",
            "2025-11-03T09:00:00-03:00",
            "Invalid",
            0.95
        ));
        // Valid suggestion
        suggestions.add(new SchedulerSuggestResponse.Suggestion(
            "2025-11-03T14:00:00-03:00",
            "2025-11-03T15:00:00-03:00",
            "Valid",
            0.85
        ));
        
        SchedulerSuggestResponse mockResponse = new SchedulerSuggestResponse(suggestions);
        when(bedrockService.generateSuggestions(any())).thenReturn(mockResponse);
        
        // Act
        SchedulerSuggestResponse response = suggestionService.generateSuggestions(request);
        
        // Assert
        assertNotNull(response);
        assertEquals(1, response.getSuggestions().size());
        assertEquals("Valid", response.getSuggestions().get(0).getReason());
    }
    
    @Test
    void testGenerateSuggestions_EmptyBedrockResponse() {
        // Arrange
        SchedulerSuggestRequest request = createValidRequest();
        
        SchedulerSuggestResponse mockResponse = new SchedulerSuggestResponse(new ArrayList<>());
        when(bedrockService.generateSuggestions(any())).thenReturn(mockResponse);
        
        // Act
        SchedulerSuggestResponse response = suggestionService.generateSuggestions(request);
        
        // Assert
        assertNotNull(response);
        assertTrue(response.getSuggestions().isEmpty());
    }
}
