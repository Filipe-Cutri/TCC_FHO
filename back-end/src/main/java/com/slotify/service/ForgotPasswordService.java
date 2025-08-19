package com.slotify.service;

import com.slotify.model.Client;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class ForgotPasswordService extends BaseService {

    public ForgotPasswordService() {
        super(); // Call the default constructor
    }

    // In-memory storage for password reset tokens (in production, use database)
    private Map<String, PasswordResetToken> passwordResetTokens = new HashMap<>();
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private ClientService clientService;

    public boolean sendClientPasswordResetEmail(String email) {
        // Check if client email exists in database
        if (isValidClientEmail(email)) {
            String token = generateResetToken();
            storeResetToken(token, email, "CLIENT");
            
            String resetLink = "http://localhost:8080/reset-password?token=" + token;
            String emailBody = buildPasswordResetEmailBody(resetLink, "cliente");
            
            return emailService.sendEmail(email, "Redefinição de Senha - Slotify", emailBody);
        }
        
        return false;
    }

    public boolean sendEstablishmentPasswordResetEmail(String email) {
        // TODO: Check if establishment email exists in database
        // For now, we'll simulate this check
        if (isValidEstablishmentEmail(email)) {
            String token = generateResetToken();
            storeResetToken(token, email, "ESTABLISHMENT");
            
            String resetLink = "http://localhost:8080/reset-password?token=" + token;
            String emailBody = buildPasswordResetEmailBody(resetLink, "estabelecimento");
            
            return emailService.sendEmail(email, "Redefinição de Senha - Slotify", emailBody);
        }
        
        return false;
    }

    public boolean resetPassword(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokens.get(token);
        
        if (resetToken == null) {
            return false; // Token not found
        }
        
        if (resetToken.isExpired()) {
            passwordResetTokens.remove(token); // Clean up expired token
            return false; // Token expired
        }
        
        // Update password in database based on user type and email
        boolean passwordUpdated = updateUserPassword(resetToken.getEmail(), 
                                                   resetToken.getUserType(), newPassword);
        
        if (passwordUpdated) {
            passwordResetTokens.remove(token); // Remove used token
            return true;
        }
        
        return false;
    }

    private String generateResetToken() {
        return UUID.randomUUID().toString();
    }

    private void storeResetToken(String token, String email, String userType) {
        PasswordResetToken resetToken = new PasswordResetToken(token, email, userType);
        passwordResetTokens.put(token, resetToken);
    }

    private boolean isValidClientEmail(String email) {
        // Check if email exists in client table
        return clientService.emailExists(email);
    }

    private boolean isValidEstablishmentEmail(String email) {
        // TODO: Check if email exists in establishment table
        // For now, simulate that all emails are valid
        return email != null && email.contains("@");
    }

    private boolean updateUserPassword(String email, String userType, String newPassword) {
        try {
            if ("CLIENT".equals(userType)) {
                clientService.updatePassword(email, newPassword);
                return true;
            } else if ("ESTABLISHMENT".equals(userType)) {
                // TODO: Update establishment user password when EstablishmentUserService supports it
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    private String buildPasswordResetEmailBody(String resetLink, String userType) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="text-align: center; margin-bottom: 30px;">
                        <h2 style="color: #3b82f6;">Slotify - Redefinição de Senha</h2>
                    </div>
                    
                    <p>Olá,</p>
                    
                    <p>Você solicitou a redefinição de senha para sua conta de %s no Slotify.</p>
                    
                    <p>Para criar uma nova senha, clique no botão abaixo:</p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #3b82f6; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; display: inline-block;">
                            Redefinir Senha
                        </a>
                    </div>
                    
                    <p>Se o botão não funcionar, copie e cole o seguinte link no seu navegador:</p>
                    <p style="word-break: break-all; background-color: #f3f4f6; padding: 10px; border-radius: 4px;">%s</p>
                    
                    <p style="color: #dc2626; font-weight: bold;">Este link é válido por 24 horas.</p>
                    
                    <p>Se você não solicitou esta redefinição de senha, ignore este e-mail. Sua senha permanecerá inalterada.</p>
                    
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #e5e7eb;">
                    
                    <p style="text-align: center; color: #6b7280; font-size: 14px;">
                        © 2024 Slotify - Sistema de Agendamento Inteligente<br>
                        Este é um e-mail automático, não responda.
                    </p>
                </div>
            </body>
            </html>
            """, userType, resetLink, resetLink);
    }

    // Inner class for password reset token
    private static class PasswordResetToken {
        private final String token;
        private final String email;
        private final String userType;
        private final LocalDateTime createdAt;
        private static final int EXPIRY_HOURS = 24;

        public PasswordResetToken(String token, String email, String userType) {
            this.token = token;
            this.email = email;
            this.userType = userType;
            this.createdAt = LocalDateTime.now();
        }

        public String getToken() {
            return token;
        }

        public String getEmail() {
            return email;
        }

        public String getUserType() {
            return userType;
        }

        public boolean isExpired() {
            return ChronoUnit.HOURS.between(createdAt, LocalDateTime.now()) >= EXPIRY_HOURS;
        }
    }
}