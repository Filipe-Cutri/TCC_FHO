package com.slotfy.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * Root controller to handle requests to the root path and serve the frontend.
 */
@Controller
public class RootController {
    
    @GetMapping("/")
    public String index() {
        return "index.html";
    }
    
    @GetMapping("/api/info")
    @RequestMapping("/api/info")
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

/**
 * API Info controller for backward compatibility
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