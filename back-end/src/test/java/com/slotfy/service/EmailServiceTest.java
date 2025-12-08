package com.slotfy.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import jakarta.mail.internet.MimeMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test class for EmailService with Gmail SMTP integration
 */
public class EmailServiceTest {

    private EmailService emailService;
    private JavaMailSender mailSender;

    @BeforeEach
    void setUp() {
        mailSender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@slotfy.com");
        ReflectionTestUtils.setField(emailService, "fromName", "Slotfy - Sistema de Agendamento");
    }

    @Test
    void testServiceCreation() {
        assertNotNull(emailService);
    }

    @Test
    void testSendEmail_ValidEmail() {
        String to = "test@example.com";
        String subject = "Test Email";
        String body = "<html><body><h1>Test Email Body</h1></body></html>";
        
        boolean result = emailService.sendEmail(to, subject, body);
        
        // Verify that mailSender was called
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        assertTrue(result);
    }

    @Test
    void testSendPasswordResetEmail() {
        String to = "test@example.com";
        String resetLink = "https://localhost:8443/pages/reset-password.html?token=test-token";
        
        boolean result = emailService.sendPasswordResetEmail(to, resetLink);
        
        // Verify that mailSender was called
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        assertTrue(result);
    }

    @Test
    void testSendEmail_NullEmail() {
        String subject = "Test Email";
        String body = "<html><body><h1>Test Email Body</h1></body></html>";
        
        // Should handle null email gracefully
        boolean result = emailService.sendEmail(null, subject, body);
        assertFalse(result);
    }

    @Test
    void testSendEmail_EmptyEmail() {
        String to = "";
        String subject = "Test Email";
        String body = "<html><body><h1>Test Email Body</h1></body></html>";
        
        // Should handle empty email gracefully
        boolean result = emailService.sendEmail(to, subject, body);
        assertFalse(result);
    }
    
    @Test
    void testSendEmail_ExceptionHandling() {
        // Setup mail sender to throw exception
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(MimeMessage.class));
        
        String to = "test@example.com";
        String subject = "Test Email";
        String body = "<html><body><h1>Test Email Body</h1></body></html>";
        
        // Should return false when exception occurs
        boolean result = emailService.sendEmail(to, subject, body);
        assertFalse(result);
    }
    
    @Test
    void testSendPasswordResetEmail_NullEmail() {
        boolean result = emailService.sendPasswordResetEmail(null, "http://test.com/reset");
        assertFalse(result);
    }
    
    @Test
    void testSendPasswordResetEmail_EmptyEmail() {
        boolean result = emailService.sendPasswordResetEmail("", "http://test.com/reset");
        assertFalse(result);
    }
    
    @Test
    void testSendEmail_NullSubject() {
        String to = "test@example.com";
        String body = "<html><body><h1>Test Email Body</h1></body></html>";
        
        // Should handle null subject gracefully
        boolean result = emailService.sendEmail(to, null, body);
        assertFalse(result);
    }
    
    @Test
    void testSendEmail_NullBody() {
        String to = "test@example.com";
        String subject = "Test Email";
        
        // Should handle null body gracefully
        boolean result = emailService.sendEmail(to, subject, null);
        assertFalse(result);
    }
    
    @Test
    void testSendEmail_NullFromEmail() {
        // Set null from email
        ReflectionTestUtils.setField(emailService, "fromEmail", null);
        
        String to = "test@example.com";
        String subject = "Test Email";
        String body = "<html><body><h1>Test Email Body</h1></body></html>";
        
        // Should return false with null from email
        boolean result = emailService.sendEmail(to, subject, body);
        assertFalse(result);
    }
    
    @Test
    void testSendPasswordResetEmail_WithSpecialCharactersInLink() {
        String to = "test@example.com";
        String resetLink = "https://example.com/reset?token=abc123&param=value%20encoded";
        
        boolean result = emailService.sendPasswordResetEmail(to, resetLink);
        
        // Verify that mailSender was called
        verify(mailSender, times(1)).send(any(MimeMessage.class));
        assertTrue(result);
    }
}
