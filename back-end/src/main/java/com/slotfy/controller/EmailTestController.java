package com.slotfy.controller;

import com.slotfy.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller para testar a funcionalidade de email
 * APENAS PARA DESENVOLVIMENTO - Remover em produção
 */
@RestController
@RequestMapping("/api/test/email")
public class EmailTestController {

    @Autowired
    private EmailService emailService;

    /**
     * Endpoint de teste para enviar email simples
     * DELETE THIS IN PRODUCTION
     */
    @PostMapping("/send-test")
    public ResponseEntity<?> sendTestEmail(@RequestParam String to) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String subject = "Email de Teste - Slotfy";
            String body = """
                <html>
                <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                    <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                        <div style="text-align: center; margin-bottom: 30px;">
                            <h2 style="color: #3b82f6;">Teste de Email - Slotfy</h2>
                        </div>
                        
                        <p>Olá!</p>
                        
                        <p>Este é um email de teste para verificar a integração com SendGrid.</p>
                        
                        <p>Se você recebeu este email, significa que a integração está funcionando corretamente!</p>
                        
                        <hr style="margin: 30px 0; border: none; border-top: 1px solid #e5e7eb;">
                        
                        <p style="text-align: center; color: #6b7280; font-size: 14px;">
                            © 2024 Slotfy - Sistema de Agendamento Inteligente<br>
                            Este é um email de teste.
                        </p>
                    </div>
                </body>
                </html>
                """;
            
            boolean sent = emailService.sendEmail(to, subject, body);
            
            if (sent) {
                response.put("success", true);
                response.put("message", "Email de teste enviado com sucesso para " + to);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Falha ao enviar email. Verifique os logs.");
                return ResponseEntity.status(500).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro ao enviar email: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint de teste para enviar email de redefinição de senha
     * DELETE THIS IN PRODUCTION
     */
    @PostMapping("/send-reset-password")
    public ResponseEntity<?> sendResetPasswordEmail(@RequestParam String to) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String resetLink = "https://localhost:8443/pages/reset-password.html?token=TEST-TOKEN-123";
            boolean sent = emailService.sendPasswordResetEmail(to, resetLink);
            
            if (sent) {
                response.put("success", true);
                response.put("message", "Email de redefinição de senha enviado com sucesso para " + to);
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "Falha ao enviar email. Verifique os logs.");
                return ResponseEntity.status(500).body(response);
            }
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "Erro ao enviar email: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint de verificação
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "ok");
        response.put("message", "Email test controller is active");
        response.put("warning", "Este controller deve ser removido em produção");
        return ResponseEntity.ok(response);
    }
}
