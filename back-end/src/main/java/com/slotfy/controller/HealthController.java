package com.slotfy.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller to verify application status.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);
    private static final long startTime = System.currentTimeMillis();
    
    @Value("${app.version:unknown}")
    private String appVersion;
    
    @Value("${app.commit.hash:unknown}")
    private String commitHash;
    
    @Value("${spring.profiles.active:default}")
    private String activeProfile;
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        try {
            logger.info("Health check endpoint called");
            
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ok");
            response.put("timestamp", Instant.now().toString());
            response.put("service", "backend");
            response.put("version", appVersion);
            response.put("commit", commitHash);
            response.put("environment", activeProfile);
            response.put("uptime", System.currentTimeMillis() - startTime);
            
            // Memory information
            Runtime runtime = Runtime.getRuntime();
            Map<String, Object> memory = new HashMap<>();
            memory.put("total", runtime.totalMemory());
            memory.put("free", runtime.freeMemory());
            memory.put("used", runtime.totalMemory() - runtime.freeMemory());
            memory.put("max", runtime.maxMemory());
            response.put("memory", memory);
            
            logger.info("Health check successful: version={}, commit={}, environment={}", 
                       appVersion, commitHash, activeProfile);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in health check endpoint", e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("timestamp", Instant.now().toString());
            errorResponse.put("message", "Health check failed: " + e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}