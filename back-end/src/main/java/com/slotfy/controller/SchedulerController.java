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
     * Note: This is a stub implementation. Integration with actual calendar
     * will be implemented in a future iteration.
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
            
            // Stub: Return success
            // TODO: Integrate with actual calendar system
            return ResponseEntity.ok(ApiResponse.success("Appointment confirmed successfully", null));
            
        } catch (Exception e) {
            logger.error("Error confirming appointment", e);
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error("Failed to confirm appointment"));
        }
    }
}
