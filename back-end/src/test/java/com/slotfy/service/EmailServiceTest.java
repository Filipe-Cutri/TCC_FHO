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
}
