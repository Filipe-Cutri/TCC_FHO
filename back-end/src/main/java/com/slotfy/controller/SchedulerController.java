package com.slotfy.controller;

import com.slotfy.dto.ApiResponse;
import com.slotfy.dto.SchedulerConfirmRequest;
import com.slotfy.dto.SchedulerSuggestRequest;
import com.slotfy.dto.SchedulerSuggestResponse;
import com.slotfy.service.SuggestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Map;

/**
 * REST controller for scheduling suggestions
 */
@RestController
@RequestMapping("/api/scheduler")
public class SchedulerController {
    
    private static final Logger logger = LoggerFactory.getLogger(SchedulerController.class);
    
    @Autowired
    private SuggestionService suggestionService;
    
    /**
     * Generate scheduling suggestions
     * POST /api/scheduler/suggest
     */
    @PostMapping("/suggest")
    public ResponseEntity<?> suggest(@RequestBody SchedulerSuggestRequest request) {
        try {
            logger.info("Received suggest request for userId: {}", request.getUserId());
            
            SchedulerSuggestResponse response = suggestionService.generateSuggestions(request);
            
            logger.info("Generated {} suggestions for userId: {}", 
                       response.getSuggestions().size(), request.getUserId());
            
            return ResponseEntity.ok(response);
            
        } catch (IllegalArgumentException e) {
            logger.warn("Invalid request: {}", e.getMessage());
            return ResponseEntity
                .badRequest()
                .body(ApiResponse.error(e.getMessage()));
                
        } catch (Exception e) {
            logger.error("Error generating suggestions", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to generate suggestions"));
        }
    }
    
    /**
     * Confirm a selected time slot
     * POST /api/scheduler/confirm
     * 
     * Validates the time slot availability and returns confirmation status.
     * Actual appointment creation should be done through the appointment endpoints.
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> confirm(@RequestBody SchedulerConfirmRequest request) {
        try {
            logger.info("Received confirm request for userId: {} (start: {}, end: {})", 
                       request.getUserId(), request.getStart(), request.getEnd());
            
            // Validate input
            if (request.getUserId() == null || request.getUserId().isEmpty()) {
                return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("userId is required"));
            }
            
            if (request.getStart() == null || request.getStart().isEmpty()) {
                return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("start time is required"));
            }
            
            if (request.getEnd() == null || request.getEnd().isEmpty()) {
                return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("end time is required"));
            }
            
            // Parse and validate the time slot
            LocalDateTime startTime;
            LocalDateTime endTime;
            try {
                startTime = LocalDateTime.parse(request.getStart(), DateTimeFormatter.ISO_DATE_TIME);
                endTime = LocalDateTime.parse(request.getEnd(), DateTimeFormatter.ISO_DATE_TIME);
            } catch (DateTimeParseException e) {
                return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Invalid date/time format. Use ISO 8601 format (e.g., 2024-01-15T10:00:00)"));
            }
            
            // Validate that end time is after start time
            if (!endTime.isAfter(startTime)) {
                return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("End time must be after start time"));
            }
            
            // Validate that the time slot is in the future
            if (startTime.isBefore(LocalDateTime.now())) {
                return ResponseEntity
                    .badRequest()
                    .body(ApiResponse.error("Cannot confirm a time slot in the past"));
            }
            
            // Return confirmation with slot details
            // The actual appointment creation should be done via POST /api/client/appointments/book
            // or POST /api/appointments with full booking details
            Map<String, Object> confirmationData = Map.of(
                "userId", request.getUserId(),
                "startTime", startTime.toString(),
                "endTime", endTime.toString(),
                "durationMinutes", java.time.Duration.between(startTime, endTime).toMinutes(),
                "message", "Time slot validated. Proceed with appointment booking via /api/client/appointments/book"
            );
            
            logger.info("Time slot confirmed for userId: {} from {} to {}", 
                       request.getUserId(), startTime, endTime);
            
            return ResponseEntity.ok(ApiResponse.success("Time slot confirmed successfully", confirmationData));
            
        } catch (Exception e) {
            logger.error("Error confirming appointment", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to confirm appointment"));
        }
    }
}
