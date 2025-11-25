package com.slotfy.service;

import com.slotfy.model.Client;
import com.slotfy.model.EstablishmentUser;
import com.slotfy.model.UserRole;
import com.slotfy.repository.ClientRepository;
import com.slotfy.repository.EstablishmentUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ForgotPasswordService
 */
@ExtendWith(MockitoExtension.class)
public class ForgotPasswordServiceTest {

    @Mock
    private EmailService emailService;

    @Mock
    private ClientService clientService;

    @Mock
    private EstablishmentUserService establishmentUserService;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private EstablishmentUserRepository establishmentUserRepository;

    @InjectMocks
    private ForgotPasswordService forgotPasswordService;

    private Client testClient;
    private EstablishmentUser testEstablishmentUser;

    @BeforeEach
    void setUp() {
        // Set frontend URL for testing
        ReflectionTestUtils.setField(forgotPasswordService, "frontendUrl", "http://localhost:3000");

        // Create test client
        testClient = new Client();
        testClient.setId(1L);
        testClient.setName("Test Client");
        testClient.setEmail("client@example.com");
        testClient.setPassword("hashedPassword");

        // Create test establishment user
        testEstablishmentUser = new EstablishmentUser();
        testEstablishmentUser.setId(1L);
        testEstablishmentUser.setName("Test User");
        testEstablishmentUser.setEmail("establishment@example.com");
        testEstablishmentUser.setPassword("hashedPassword");
        testEstablishmentUser.setRole(UserRole.ADMIN);
    }

    @Test
    void testSendClientPasswordResetEmail_Success() {
        // Arrange
        when(clientRepository.findByEmail("client@example.com")).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(emailService.sendPasswordResetEmail(anyString(), anyString())).thenReturn(true);

        // Act
        boolean result = forgotPasswordService.sendClientPasswordResetEmail("client@example.com");

        // Assert
        assertTrue(result);
        verify(clientRepository).save(argThat(client -> 
            client.getResetPasswordTokenHash() != null && 
            client.getResetPasswordExpiry() != null
        ));
        verify(emailService).sendPasswordResetEmail(eq("client@example.com"), anyString());
    }

    @Test
    void testSendClientPasswordResetEmail_EmailNotFound_StillReturnsTrue() {
        // Arrange
        when(clientRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        boolean result = forgotPasswordService.sendClientPasswordResetEmail("nonexistent@example.com");

        // Assert - Should return true for security (don't reveal if email exists)
        assertTrue(result);
        verify(clientRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void testSendEstablishmentPasswordResetEmail_Success() {
        // Arrange
        when(establishmentUserRepository.findByEmail("establishment@example.com"))
            .thenReturn(Optional.of(testEstablishmentUser));
        when(establishmentUserRepository.save(any(EstablishmentUser.class)))
            .thenReturn(testEstablishmentUser);
        when(emailService.sendPasswordResetEmail(anyString(), anyString())).thenReturn(true);

        // Act
        boolean result = forgotPasswordService.sendEstablishmentPasswordResetEmail("establishment@example.com");

        // Assert
        assertTrue(result);
        verify(establishmentUserRepository).save(argThat(user -> 
            user.getResetPasswordTokenHash() != null && 
            user.getResetPasswordExpiry() != null
        ));
        verify(emailService).sendPasswordResetEmail(eq("establishment@example.com"), anyString());
    }
    
    @Test
    void testSendEstablishmentPasswordResetEmail_EmailNotFound_StillReturnsTrue() {
        // Arrange
        when(establishmentUserRepository.findByEmail("nonexistent@example.com")).thenReturn(Optional.empty());

        // Act
        boolean result = forgotPasswordService.sendEstablishmentPasswordResetEmail("nonexistent@example.com");

        // Assert - Should return true for security (don't reveal if email exists)
        assertTrue(result);
        verify(establishmentUserRepository, never()).save(any());
        verify(emailService, never()).sendPasswordResetEmail(anyString(), anyString());
    }

    @Test
    void testResetPassword_ValidToken_Client() {
        // Arrange
        String rawToken = "validtoken123";
        String email = "client@example.com";
        String newPassword = "newPassword123";
        String tokenHash = "somehash123"; // Simulated hash
        
        testClient.setResetPasswordTokenHash(tokenHash);
        testClient.setResetPasswordExpiry(System.currentTimeMillis() + 3600000); // 1 hour from now
        
        when(clientRepository.findByResetPasswordTokenHash(anyString()))
            .thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        doNothing().when(clientService).updatePassword(eq(email), eq(newPassword));

        // Act
        boolean result = forgotPasswordService.resetPassword(email, rawToken, newPassword);

        // Assert
        assertTrue(result);
        verify(clientService).updatePassword(email, newPassword);
        verify(clientRepository).save(argThat(client ->
            client.getResetPasswordTokenHash() == null &&
            client.getResetPasswordExpiry() == null
        ));
    }

    @Test
    void testResetPassword_ValidToken_EstablishmentUser() {
        // Arrange
        String rawToken = "validtoken123";
        String email = "establishment@example.com";
        String newPassword = "newPassword123";
        String tokenHash = "somehash123"; // Simulated hash
        
        testEstablishmentUser.setResetPasswordTokenHash(tokenHash);
        testEstablishmentUser.setResetPasswordExpiry(System.currentTimeMillis() + 3600000);
        
        when(clientRepository.findByResetPasswordTokenHash(anyString())).thenReturn(Optional.empty());
        when(establishmentUserRepository.findByResetPasswordTokenHash(anyString()))
            .thenReturn(Optional.of(testEstablishmentUser));
        when(establishmentUserRepository.save(any(EstablishmentUser.class)))
            .thenReturn(testEstablishmentUser);
        doNothing().when(establishmentUserService).updatePassword(eq(email), eq(newPassword));

        // Act
        boolean result = forgotPasswordService.resetPassword(email, rawToken, newPassword);

        // Assert
        assertTrue(result);
        verify(establishmentUserService).updatePassword(email, newPassword);
        verify(establishmentUserRepository).save(argThat(user ->
            user.getResetPasswordTokenHash() == null &&
            user.getResetPasswordExpiry() == null
        ));
    }

    @Test
    void testResetPassword_InvalidToken() {
        // Arrange
        when(clientRepository.findByResetPasswordTokenHash(anyString())).thenReturn(Optional.empty());
        when(establishmentUserRepository.findByResetPasswordTokenHash(anyString()))
            .thenReturn(Optional.empty());

        // Act
        boolean result = forgotPasswordService.resetPassword(
            "client@example.com", "invalidtoken", "newPassword123");

        // Assert
        assertFalse(result);
        verify(clientService, never()).updatePassword(anyString(), anyString());
        verify(establishmentUserService, never()).updatePassword(anyString(), anyString());
    }

    @Test
    void testResetPassword_ExpiredToken() {
        // Arrange
        testClient.setResetPasswordTokenHash("somehash");
        testClient.setResetPasswordExpiry(System.currentTimeMillis() - 1000); // Expired 1 second ago
        
        when(clientRepository.findByResetPasswordTokenHash(anyString()))
            .thenReturn(Optional.of(testClient));

        // Act
        boolean result = forgotPasswordService.resetPassword(
            "client@example.com", "expiredtoken", "newPassword123");

        // Assert
        assertFalse(result);
        verify(clientService, never()).updatePassword(anyString(), anyString());
        verify(clientRepository, never()).save(any());
    }

    @Test
    void testResetPassword_EmailMismatch() {
        // Arrange
        testClient.setEmail("client@example.com");
        testClient.setResetPasswordTokenHash("somehash");
        testClient.setResetPasswordExpiry(System.currentTimeMillis() + 3600000);
        
        when(clientRepository.findByResetPasswordTokenHash(anyString()))
            .thenReturn(Optional.of(testClient));

        // Act - Using different email
        boolean result = forgotPasswordService.resetPassword(
            "different@example.com", "token", "newPassword123");

        // Assert
        assertFalse(result);
        verify(clientService, never()).updatePassword(anyString(), anyString());
    }

    @Test
    void testTokenIsSecurelyGenerated() {
        // Arrange
        when(clientRepository.findByEmail("client@example.com")).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(emailService.sendPasswordResetEmail(anyString(), anyString())).thenReturn(true);

        // Act
        forgotPasswordService.sendClientPasswordResetEmail("client@example.com");

        // Assert - Verify that the token hash is a hex string (SHA-256 produces 64 hex characters)
        verify(clientRepository).save(argThat(client -> {
            String hash = client.getResetPasswordTokenHash();
            return hash != null && hash.matches("[0-9a-f]{64}");
        }));
    }

    @Test
    void testTokenExpiryIsSetCorrectly() {
        // Arrange
        when(clientRepository.findByEmail("client@example.com")).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(emailService.sendPasswordResetEmail(anyString(), anyString())).thenReturn(true);

        long beforeRequest = System.currentTimeMillis();

        // Act
        forgotPasswordService.sendClientPasswordResetEmail("client@example.com");

        long afterRequest = System.currentTimeMillis();

        // Assert - Verify expiry is approximately 1 hour from now
        verify(clientRepository).save(argThat(client -> {
            Long expiry = client.getResetPasswordExpiry();
            if (expiry == null) return false;
            
            // Expected expiry should be 1 hour (3600000 ms) from request time
            long expectedMin = beforeRequest + 3600000;
            long expectedMax = afterRequest + 3600000;
            
            return expiry >= expectedMin && expiry <= expectedMax;
        }));
    }
    
    @Test
    void testResetPassword_NullTokenExpiry_Client() {
        // Arrange
        testClient.setResetPasswordTokenHash("somehash");
        testClient.setResetPasswordExpiry(null); // null expiry
        
        when(clientRepository.findByResetPasswordTokenHash(anyString()))
            .thenReturn(Optional.of(testClient));

        // Act
        boolean result = forgotPasswordService.resetPassword(
            "client@example.com", "token", "newPassword123");

        // Assert
        assertFalse(result);
        verify(clientService, never()).updatePassword(anyString(), anyString());
    }
    
    @Test
    void testResetPassword_NullTokenExpiry_EstablishmentUser() {
        // Arrange
        testEstablishmentUser.setResetPasswordTokenHash("somehash");
        testEstablishmentUser.setResetPasswordExpiry(null); // null expiry
        
        when(clientRepository.findByResetPasswordTokenHash(anyString())).thenReturn(Optional.empty());
        when(establishmentUserRepository.findByResetPasswordTokenHash(anyString()))
            .thenReturn(Optional.of(testEstablishmentUser));

        // Act
        boolean result = forgotPasswordService.resetPassword(
            "establishment@example.com", "token", "newPassword123");

        // Assert
        assertFalse(result);
        verify(establishmentUserService, never()).updatePassword(anyString(), anyString());
    }
    
    @Test
    void testResetPassword_ExpiredToken_EstablishmentUser() {
        // Arrange
        testEstablishmentUser.setResetPasswordTokenHash("somehash");
        testEstablishmentUser.setResetPasswordExpiry(System.currentTimeMillis() - 1000); // Expired
        
        when(clientRepository.findByResetPasswordTokenHash(anyString())).thenReturn(Optional.empty());
        when(establishmentUserRepository.findByResetPasswordTokenHash(anyString()))
            .thenReturn(Optional.of(testEstablishmentUser));

        // Act
        boolean result = forgotPasswordService.resetPassword(
            "establishment@example.com", "token", "newPassword123");

        // Assert
        assertFalse(result);
        verify(establishmentUserService, never()).updatePassword(anyString(), anyString());
    }
    
    @Test
    void testResetPassword_EmailMismatch_EstablishmentUser() {
        // Arrange
        testEstablishmentUser.setResetPasswordTokenHash("somehash");
        testEstablishmentUser.setResetPasswordExpiry(System.currentTimeMillis() + 3600000);
        
        when(clientRepository.findByResetPasswordTokenHash(anyString())).thenReturn(Optional.empty());
        when(establishmentUserRepository.findByResetPasswordTokenHash(anyString()))
            .thenReturn(Optional.of(testEstablishmentUser));

        // Act - Using different email
        boolean result = forgotPasswordService.resetPassword(
            "different@example.com", "token", "newPassword123");

        // Assert
        assertFalse(result);
        verify(establishmentUserService, never()).updatePassword(anyString(), anyString());
    }
    
    @Test
    void testResetLinkContainsToken() {
        // Arrange
        when(clientRepository.findByEmail("client@example.com")).thenReturn(Optional.of(testClient));
        when(clientRepository.save(any(Client.class))).thenReturn(testClient);
        when(emailService.sendPasswordResetEmail(anyString(), anyString())).thenReturn(true);

        // Act
        forgotPasswordService.sendClientPasswordResetEmail("client@example.com");

        // Assert - Verify the reset link contains the token
        verify(emailService).sendPasswordResetEmail(
            eq("client@example.com"), 
            argThat(link -> link.contains("token=") && link.startsWith("http://localhost:3000/reset-password"))
        );
    }
    
    @Test
    void testConstructor() {
        // Test that the constructor works properly
        ForgotPasswordService service = new ForgotPasswordService();
        assertNotNull(service);
    }
}
