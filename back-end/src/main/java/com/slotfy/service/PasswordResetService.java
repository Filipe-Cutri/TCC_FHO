package com.slotfy.service;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Service for handling password reset tokens
 */
@Service
public class PasswordResetService {
    
    private static final long TOKEN_VALIDITY_HOURS = 1;
    private static final int TOKEN_LENGTH = 32;
    
    /**
     * Generate a secure random token
     */
    public String generateToken() {
        SecureRandom random = new SecureRandom();
        byte[] tokenBytes = new byte[TOKEN_LENGTH];
        random.nextBytes(tokenBytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
    }
    
    /**
     * Hash a token for secure storage
     */
    public String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }
    
    /**
     * Get token expiry timestamp (current time + validity hours)
     */
    public Long getTokenExpiry() {
        return System.currentTimeMillis() + (TOKEN_VALIDITY_HOURS * 60 * 60 * 1000);
    }
    
    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(Long expiryTimestamp) {
        return expiryTimestamp == null || System.currentTimeMillis() > expiryTimestamp;
    }
    
    /**
     * Verify that a plain token matches a hashed token
     */
    public boolean verifyToken(String plainToken, String hashedToken) {
        String hashOfPlainToken = hashToken(plainToken);
        return hashOfPlainToken.equals(hashedToken);
    }
}
