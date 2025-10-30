package com.slotfy.service;

import com.slotfy.dto.SchedulerSuggestRequest;
import com.slotfy.dto.SchedulerSuggestResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for handling scheduling suggestions with validation
 */
@Service
public class SuggestionService {
    
    private static final Logger logger = LoggerFactory.getLogger(SuggestionService.class);
    
    @Autowired
    private BedrockService bedrockService;
    
    /**
     * Generate and validate scheduling suggestions
     */
    public SchedulerSuggestResponse generateSuggestions(SchedulerSuggestRequest request) {
        // Validate input
        validateRequest(request);
        
        // Generate suggestions using Bedrock
        SchedulerSuggestResponse response = bedrockService.generateSuggestions(request);
        
        // Validate and filter suggestions
        List<SchedulerSuggestResponse.Suggestion> validSuggestions = validateSuggestions(
            response.getSuggestions(),
            request
        );
        
        // Sort by score descending and limit to maxSuggestions
        validSuggestions = validSuggestions.stream()
            .sorted((s1, s2) -> Double.compare(s2.getScore(), s1.getScore()))
            .limit(request.getMaxSuggestions())
            .collect(Collectors.toList());
        
        return new SchedulerSuggestResponse(validSuggestions);
    }
    
    /**
     * Validate request parameters
     */
    private void validateRequest(SchedulerSuggestRequest request) {
        if (request.getUserId() == null || request.getUserId().isEmpty()) {
            throw new IllegalArgumentException("userId is required");
        }
        
        if (request.getTimezone() == null || request.getTimezone().isEmpty()) {
            throw new IllegalArgumentException("timezone is required");
        }
        
        // Validate timezone format (basic check)
        if (!isValidTimezone(request.getTimezone())) {
            throw new IllegalArgumentException("Invalid timezone format");
        }
        
        if (request.getDuration() == null || request.getDuration() <= 0) {
            throw new IllegalArgumentException("duration must be greater than 0");
        }
        
        if (request.getBuffer() != null && request.getBuffer() < 0) {
            throw new IllegalArgumentException("buffer cannot be negative");
        }
    }
    
    /**
     * Basic timezone validation
     */
    private boolean isValidTimezone(String timezone) {
        try {
            java.time.ZoneId.of(timezone);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Validate suggestions against business rules
     */
    private List<SchedulerSuggestResponse.Suggestion> validateSuggestions(
            List<SchedulerSuggestResponse.Suggestion> suggestions,
            SchedulerSuggestRequest request) {
        
        if (suggestions == null || suggestions.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<SchedulerSuggestResponse.Suggestion> valid = new ArrayList<>();
        
        for (SchedulerSuggestResponse.Suggestion suggestion : suggestions) {
            if (isValidSuggestion(suggestion, request)) {
                valid.add(suggestion);
            } else {
                logger.warn("Filtered out invalid suggestion: {} to {}", 
                           suggestion.getStart(), suggestion.getEnd());
            }
        }
        
        return valid;
    }
    
    /**
     * Check if a suggestion is valid
     */
    private boolean isValidSuggestion(
            SchedulerSuggestResponse.Suggestion suggestion,
            SchedulerSuggestRequest request) {
        
        try {
            // Parse times
            ZonedDateTime start = ZonedDateTime.parse(suggestion.getStart(), DateTimeFormatter.ISO_DATE_TIME);
            ZonedDateTime end = ZonedDateTime.parse(suggestion.getEnd(), DateTimeFormatter.ISO_DATE_TIME);
            
            // Check that end is after start
            if (!end.isAfter(start)) {
                logger.warn("Invalid suggestion: end time must be after start time");
                return false;
            }
            
            // Check duration matches expected (with buffer)
            int buffer = request.getBuffer() != null ? request.getBuffer() : 0;
            long expectedDuration = request.getDuration() + (2 * buffer);
            long actualDuration = java.time.Duration.between(start, end).toMinutes();
            
            if (actualDuration < request.getDuration()) {
                logger.warn("Invalid suggestion: duration too short ({} < {})", 
                           actualDuration, request.getDuration());
                return false;
            }
            
            // Check no conflicts with busy slots
            if (hasConflictWithBusySlots(start, end, request.getBusySlots(), buffer)) {
                logger.warn("Invalid suggestion: conflicts with busy slots");
                return false;
            }
            
            // Check within available windows (if specified)
            if (request.getAvailableWindows() != null && !request.getAvailableWindows().isEmpty()) {
                if (!isWithinAvailableWindows(start, end, request.getAvailableWindows())) {
                    logger.warn("Invalid suggestion: outside available windows");
                    return false;
                }
            }
            
            return true;
            
        } catch (DateTimeParseException e) {
            logger.error("Invalid datetime format in suggestion", e);
            return false;
        }
    }
    
    /**
     * Check if suggestion conflicts with busy slots
     */
    private boolean hasConflictWithBusySlots(
            ZonedDateTime start,
            ZonedDateTime end,
            List<SchedulerSuggestRequest.TimeWindow> busySlots,
            int buffer) {
        
        if (busySlots == null || busySlots.isEmpty()) {
            return false;
        }
        
        // Add buffer to the suggestion time range
        ZonedDateTime bufferedStart = start.minusMinutes(buffer);
        ZonedDateTime bufferedEnd = end.plusMinutes(buffer);
        
        for (SchedulerSuggestRequest.TimeWindow busySlot : busySlots) {
            try {
                ZonedDateTime busyStart = ZonedDateTime.parse(busySlot.getStart(), DateTimeFormatter.ISO_DATE_TIME);
                ZonedDateTime busyEnd = ZonedDateTime.parse(busySlot.getEnd(), DateTimeFormatter.ISO_DATE_TIME);
                
                // Check if time ranges overlap
                if (timesOverlap(bufferedStart, bufferedEnd, busyStart, busyEnd)) {
                    return true;
                }
            } catch (DateTimeParseException e) {
                logger.error("Invalid datetime in busy slot", e);
            }
        }
        
        return false;
    }
    
    /**
     * Check if suggestion is within available windows
     */
    private boolean isWithinAvailableWindows(
            ZonedDateTime start,
            ZonedDateTime end,
            List<SchedulerSuggestRequest.TimeWindow> availableWindows) {
        
        for (SchedulerSuggestRequest.TimeWindow window : availableWindows) {
            try {
                ZonedDateTime windowStart = ZonedDateTime.parse(window.getStart(), DateTimeFormatter.ISO_DATE_TIME);
                ZonedDateTime windowEnd = ZonedDateTime.parse(window.getEnd(), DateTimeFormatter.ISO_DATE_TIME);
                
                // Check if suggestion is completely within this window
                if (!start.isBefore(windowStart) && !end.isAfter(windowEnd)) {
                    return true;
                }
            } catch (DateTimeParseException e) {
                logger.error("Invalid datetime in available window", e);
            }
        }
        
        return false;
    }
    
    /**
     * Check if two time ranges overlap
     */
    private boolean timesOverlap(ZonedDateTime start1, ZonedDateTime end1, 
                                  ZonedDateTime start2, ZonedDateTime end2) {
        return start1.isBefore(end2) && end1.isAfter(start2);
    }
}
