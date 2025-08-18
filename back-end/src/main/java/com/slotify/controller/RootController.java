package com.slotify.controller;

import org.springframework.http.ResponseEntity;
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
    public ResponseEntity<Map<String, Object>> root() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "Slotify Backend");
        response.put("version", "1.0.0");
        response.put("status", "running");
        response.put("message", "Bem-vindo ao Slotify! Use os endpoints da API para interagir com o sistema.");
        response.put("endpoints", Map.of(
            "health", "/api/health",
            "establishment_login", "/api/establishment/login",
            "client_forgot_password", "/api/client/forgot-password",
            "establishment_forgot_password", "/api/establishment/forgot-password"
        ));
        
        return ResponseEntity.ok(response);
    }
}