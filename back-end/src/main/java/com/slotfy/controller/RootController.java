package com.slotfy.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Root controller to handle requests to the root path.
 */
@RestController
public class RootController {
    
    @GetMapping("/")
    public ResponseEntity<Map<String, Object>> index() {
        // Return API information for root path
        // In production, frontend is served separately
        Map<String, Object> response = new HashMap<>();
        response.put("application", "Slotfy Backend");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("message", "Backend API está funcionando. O frontend é servido separadamente.");
        response.put("api_info", "/api/info");
        response.put("health_check", "/api/health");
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/favicon.ico")
    public ResponseEntity<Void> favicon() {
        // Return 204 No Content for favicon requests
        // Frontend handles its own favicon
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
    
    @GetMapping("/api")
    public ResponseEntity<Map<String, Object>> apiIndex() {
        // Redirect to /api/info for consistency
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Use /api/info para informações da API");
        response.put("api_info_endpoint", "/api/info");
        
        return ResponseEntity.ok(response);
    }
}

/**
 * API Info controller to provide API information
 */
@RestController
class ApiInfoController {
    
    @GetMapping("/api/info")
    public ResponseEntity<Map<String, Object>> apiInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "Slotfy Backend");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("message", "Bem-vindo ao Slotfy! Use os endpoints da API para interagir com o sistema.");
        response.put("endpoints", Map.of(
            "health", "/api/health",
            "establishment_login", "/api/establishment/login",
            "client_login", "/api/client/login",
            "client_register", "/api/client/register",
            "establishment_register", "/api/establishment/register",
            "client_forgot_password", "/api/client/forgot-password",
            "establishment_forgot_password", "/api/establishment/forgot-password"
        ));
        
        return ResponseEntity.ok(response);
    }
}