package com.slotfy.controller;

import com.slotfy.service.AuthenticatableService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Base authentication controller with common auth endpoints
 */
public abstract class BaseAuthController<T> {
    
    protected abstract AuthenticatableService<T> getAuthService();
    protected abstract Map<String, Object> createUserResponse(T user);
    protected abstract String getUserTypeName();
    
    /**
     * Common login logic
     */
    protected ResponseEntity<Map<String, Object>> performLogin(Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            if (email == null || password == null || 
                email.trim().isEmpty() || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false, 
                        "message", "Email e senha são obrigatórios"
                    ));
            }
            
            Optional<T> userOpt = getAuthService().authenticate(email, password);
            
            if (userOpt.isPresent()) {
                T user = userOpt.get();
                
                return ResponseEntity.ok()
                    .body(Map.of(
                        "success", true,
                        "message", "Login realizado com sucesso",
                        getUserTypeName(), createUserResponse(user)
                    ));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Email ou senha inválidos"
                    ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Common validation for registration data
     */
    protected ResponseEntity<Map<String, Object>> validateRegistrationData(String name, String email, String password) {
        if (name == null || email == null || password == null ||
            name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", "Nome, email e senha são obrigatórios"
                ));
        }
        
        if (password.length() < 6) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", "Senha deve ter pelo menos 6 caracteres"
                ));
        }
        
        return null; // No validation errors
    }
    
    /**
     * Handle registration errors
     */
    protected ResponseEntity<Map<String, Object>> handleRegistrationError(Exception e) {
        if (e instanceof IllegalArgumentException) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } else {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
}