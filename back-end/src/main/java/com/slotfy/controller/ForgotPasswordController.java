package com.slotfy.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import com.slotfy.service.ForgotPasswordService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(originPatterns = "*")
public class ForgotPasswordController {

    @Autowired
    private ForgotPasswordService forgotPasswordService;

    // Simple rate limiting: track last request time per IP
    private final ConcurrentHashMap<String, Long> rateLimitMap = new ConcurrentHashMap<>();
    private static final long RATE_LIMIT_MS = 60000; // 1 minute between requests

    /**
     * Mask email address for logging (show first 2 chars and domain)
     */
    private String maskEmail(String email) {
        if (email == null || email.length() < 3 || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        String username = email.substring(0, atIndex);
        String domain = email.substring(atIndex);
        
        if (username.length() <= 2) {
            return username.charAt(0) + "***" + domain;
        }
        return username.substring(0, 2) + "***" + domain;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(
            @RequestBody Map<String, String> request,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor,
            @RequestHeader(value = "X-Real-IP", required = false) String realIp) {
        
        try {
            String email = request.get("email");
            System.out.println("Requisição de recuperação de senha recebida: " + maskEmail(email));
            
            if (email == null || email.trim().isEmpty()) {
                System.err.println("Erro: Email não fornecido na requisição");
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "E-mail é obrigatório"));
            }
            
            // Basic rate limiting
            String clientIp = getClientIp(forwardedFor, realIp);
            if (!checkRateLimit(clientIp)) {
                System.err.println("Rate limit excedido");
                return ResponseEntity.status(429)
                    .body(Map.of("success", false, "message", "Muitas requisições. Tente novamente em 1 minuto."));
            }
            
            // Try client first, then establishment
            System.out.println("Processando recuperação de senha");
            forgotPasswordService.sendClientPasswordResetEmail(email);
            forgotPasswordService.sendEstablishmentPasswordResetEmail(email);
            System.out.println("Processamento concluído");
            
            // Always return generic success message for security
            return ResponseEntity.ok()
                .body(Map.of("success", true, "message", "Se o e-mail existir, as instruções de redefinição foram enviadas"));
            
        } catch (Exception e) {
            System.err.println("Erro ao processar recuperação de senha: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                .body(Map.of("success", false, "message", "Erro interno do servidor"));
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String token = request.get("token");
            String newPassword = request.get("newPassword");
            
            if (email == null || token == null || newPassword == null || 
                email.trim().isEmpty() || token.trim().isEmpty() || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "E-mail, token e nova senha são obrigatórios"));
            }
            
            boolean passwordReset = forgotPasswordService.resetPassword(email, token, newPassword);
            
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

    private String getClientIp(String forwardedFor, String realIp) {
        if (forwardedFor != null && !forwardedFor.isEmpty()) {
            return forwardedFor.split(",")[0].trim();
        }
        if (realIp != null && !realIp.isEmpty()) {
            return realIp;
        }
        return "unknown";
    }

    private boolean checkRateLimit(String clientIp) {
        long now = System.currentTimeMillis();
        Long lastRequest = rateLimitMap.get(clientIp);
        
        if (lastRequest != null && (now - lastRequest) < RATE_LIMIT_MS) {
            return false;
        }
        
        rateLimitMap.put(clientIp, now);
        
        // Cleanup old entries (older than 5 minutes)
        rateLimitMap.entrySet().removeIf(entry -> (now - entry.getValue()) > 300000);
        
        return true;
    }
}