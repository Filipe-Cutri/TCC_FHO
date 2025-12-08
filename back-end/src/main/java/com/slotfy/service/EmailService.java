package com.slotfy.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${email.from.address}")
    private String fromEmail;

    @Value("${email.from.name}")
    private String fromName;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

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

    public boolean sendEmail(String to, String subject, String body) {
        // Validate input parameters
        if (to == null || to.trim().isEmpty()) {
            System.err.println("Erro: Endereço de email de destino é nulo ou vazio");
            return false;
        }
        
        if (subject == null || subject.trim().isEmpty()) {
            System.err.println("Erro: Assunto do email é nulo ou vazio");
            return false;
        }
        
        if (body == null || body.trim().isEmpty()) {
            System.err.println("Erro: Corpo do email é nulo ou vazio");
            return false;
        }
        
        if (fromEmail == null || fromEmail.trim().isEmpty()) {
            System.err.println("Erro: Email remetente não configurado");
            return false;
        }
        
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail, fromName);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true); // true indicates HTML content
            
            mailSender.send(message);
            System.out.println("Email enviado com sucesso para: " + maskEmail(to));
            return true;
            
        } catch (Exception e) {
            System.err.println("Erro ao enviar email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendPasswordResetEmail(String to, String resetLink) {
        // Validate input parameters
        if (to == null || to.trim().isEmpty()) {
            System.err.println("Erro: Endereço de email para reset de senha é nulo ou vazio");
            return false;
        }
        
        if (resetLink == null || resetLink.trim().isEmpty()) {
            System.err.println("Erro: Link de reset de senha é nulo ou vazio");
            return false;
        }
        
        String subject = "Redefinição de Senha - Slotfy";
        String body = buildPasswordResetEmailBody(resetLink);
        return sendEmail(to, subject, body);
    }

    private String buildPasswordResetEmailBody(String resetLink) {
        return String.format("""
            <html>
            <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333;">
                <div style="max-width: 600px; margin: 0 auto; padding: 20px;">
                    <div style="text-align: center; margin-bottom: 30px;">
                        <h2 style="color: #3b82f6;">Slotfy - Redefinição de Senha</h2>
                    </div>
                    
                    <p>Olá,</p>
                    
                    <p>Você solicitou a redefinição de senha para sua conta no Slotfy.</p>
                    
                    <p>Para criar uma nova senha, clique no botão abaixo:</p>
                    
                    <div style="text-align: center; margin: 30px 0;">
                        <a href="%s" style="background-color: #3b82f6; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; display: inline-block;">
                            Redefinir Senha
                        </a>
                    </div>
                    
                    <p>Se o botão não funcionar, copie e cole o seguinte link no seu navegador:</p>
                    <p style="word-break: break-all; background-color: #f3f4f6; padding: 10px; border-radius: 4px;">%s</p>
                    
                    <p style="color: #dc2626; font-weight: bold;">Este link é válido por 1 hora.</p>
                    
                    <p>Se você não solicitou esta redefinição de senha, ignore este e-mail.</p>
                    
                    <hr style="margin: 30px 0; border: none; border-top: 1px solid #e5e7eb;">
                    
                    <p style="text-align: center; color: #6b7280; font-size: 14px;">
                        © 2024 Slotfy - Sistema de Agendamento Inteligente<br>
                        Este é um e-mail automático, não responda.
                    </p>
                </div>
            </body>
            </html>
            """, resetLink, resetLink);
    }
}