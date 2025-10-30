package com.slotfy.service;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${sendgrid.api.key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from.email}")
    private String fromEmail;

    @Value("${sendgrid.from.name}")
    private String fromName;

    public EmailService() {
        super(); // Call the default constructor
    }

    public boolean sendEmail(String to, String subject, String body) {
        try {
            Email from = new Email(fromEmail, fromName);
            Email toEmail = new Email(to);
            Content content = new Content("text/html", body);
            Mail mail = new Mail(from, subject, toEmail, content);

            SendGrid sg = new SendGrid(sendGridApiKey);
            Request request = new Request();
            
            try {
                request.setMethod(Method.POST);
                request.setEndpoint("mail/send");
                request.setBody(mail.build());
                Response response = sg.api(request);
                
                // SendGrid returns 202 for successful acceptance
                if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                    System.out.println("Email enviado com sucesso para: " + to);
                    return true;
                } else {
                    System.err.println("Erro ao enviar email. Status: " + response.getStatusCode());
                    System.err.println("Resposta: " + response.getBody());
                    return false;
                }
            } catch (IOException ex) {
                System.err.println("Erro de IO ao enviar email: " + ex.getMessage());
                ex.printStackTrace();
                return false;
            }
            
        } catch (Exception e) {
            System.err.println("Erro ao enviar email: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean sendPasswordResetEmail(String to, String resetLink) {
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