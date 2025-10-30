package com.slotfy.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * Health check controller to verify application status.
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {
    
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "UP");
        response.put("application", "Slotfy Backend");
        response.put("version", "1.0.0");
        
        return ResponseEntity.ok(response);
    }

    /**
     * Temporary debug endpoint to inspect HTTP headers.
     * Useful for verifying X-Forwarded-* headers from Railway proxy.
     * TODO: Remove this endpoint after verifying the fix works in production.
     */
    @GetMapping("/__headers")
    public ResponseEntity<Map<String, Object>> debugHeaders(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        Map<String, String> headers = new HashMap<>();
        
        // Collect all headers
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headers.put(headerName, request.getHeader(headerName));
        }
        
        response.put("headers", headers);
        response.put("requestURL", request.getRequestURL().toString());
        response.put("requestURI", request.getRequestURI());
        response.put("scheme", request.getScheme());
        response.put("serverName", request.getServerName());
        response.put("serverPort", request.getServerPort());
        response.put("remoteAddr", request.getRemoteAddr());
        response.put("isSecure", request.isSecure());
        
        return ResponseEntity.ok(response);
    }
}