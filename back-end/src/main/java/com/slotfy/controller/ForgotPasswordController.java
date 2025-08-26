package com.slotfy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.slotfy.service.ForgotPasswordService;

import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(originPatterns = "*")
public class ForgotPasswordController {

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    @PostMapping("/client/forgot-password")
    public ResponseEntity<Map<String, Object>> clientForgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "E-mail é obrigatório"));
            }
            
            boolean emailSent = forgotPasswordService.sendClientPasswordResetEmail(email);
            
            if (emailSent) {
                return ResponseEntity.ok()
                    .body(Map.of("success", true, "message", "Instruções enviadas para o e-mail"));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "E-mail não encontrado"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Erro interno do servidor"));
        }
    }

    @PostMapping("/establishment/forgot-password")
    public ResponseEntity<Map<String, Object>> establishmentForgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "E-mail é obrigatório"));
            }
            
            boolean emailSent = forgotPasswordService.sendEstablishmentPasswordResetEmail(email);
            
            if (emailSent) {
                return ResponseEntity.ok()
                    .body(Map.of("success", true, "message", "Instruções enviadas para o e-mail"));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "E-mail não encontrado"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Erro interno do servidor"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            
            if (token == null || newPassword == null || 
                token.trim().isEmpty() || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Token e nova senha são obrigatórios"));
            }
            
            boolean passwordReset = forgotPasswordService.resetPassword(token, newPassword);
            
            if (passwordReset) {
                return ResponseEntity.ok()
                    .body(Map.of("success", true, "message", "Senha alterada com sucesso"));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Token inválido ou expirado"));
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Erro interno do servidor"));
        }
    }
}