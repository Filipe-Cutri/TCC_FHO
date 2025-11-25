package com.slotfy.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for EmailService with SendGrid integration
 */
public class EmailServiceTest {

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService();
        // Set the SendGrid API key for testing
        ReflectionTestUtils.setField(emailService, "sendGridApiKey", "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");
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@slotfy.com");
        ReflectionTestUtils.setField(emailService, "fromName", "Slotfy - Sistema de Agendamento");
    }

    @Test
    void testServiceCreation() {
        assertNotNull(emailService);
    }

    @Test
    void testSendEmail_ValidEmail() {
        // Note: This test will actually attempt to send an email via SendGrid
        // In production, you might want to mock the SendGrid API
        String to = "test@example.com";
        String subject = "Test Email";
        String body = "<html><body><h1>Test Email Body</h1></body></html>";
        
        // The method should return true or false depending on SendGrid's response
        // We'll just verify it doesn't throw an exception
        assertDoesNotThrow(() -> {
            emailService.sendEmail(to, subject, body);
        });
    }

    @Test
    void testSendPasswordResetEmail() {
        String to = "test@example.com";
        String resetLink = "https://localhost:8443/pages/reset-password.html?token=test-token";
        
        // The method should return true or false depending on SendGrid's response
        // We'll just verify it doesn't throw an exception
        assertDoesNotThrow(() -> {
            emailService.sendPasswordResetEmail(to, resetLink);
        });
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
    void testSendEmail_InvalidApiKey() {
        // Setup with an invalid API key
        ReflectionTestUtils.setField(emailService, "sendGridApiKey", "invalid-key");
        
        String to = "test@example.com";
        String subject = "Test Email";
        String body = "<html><body><h1>Test Email Body</h1></body></html>";
        
        // Should return false with invalid API key
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
        
        // Should handle special characters in link
        assertDoesNotThrow(() -> {
            emailService.sendPasswordResetEmail(to, resetLink);
        });
    }
}
