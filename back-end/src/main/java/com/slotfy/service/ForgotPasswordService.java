package com.slotfy.service;

import com.slotfy.model.Client;
import com.slotfy.model.EstablishmentUser;
import com.slotfy.repository.ClientRepository;
import com.slotfy.repository.EstablishmentUserRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Optional;

@Service
public class ForgotPasswordService {

    private static final int TOKEN_BYTES = 32;
    private static final long TOKEN_EXPIRY_HOURS = 1;
    private static final long TOKEN_EXPIRY_MS = TOKEN_EXPIRY_HOURS * 60 * 60 * 1000;
    
    @Autowired
    private EmailService emailService;
    
    @Autowired
    private EstablishmentUserService establishmentUserService;
    
    @Autowired
    private ClientService clientService;
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private EstablishmentUserRepository establishmentUserRepository;
    
    @Value("${frontend.url}")
    private String frontendUrl;

    private final SecureRandom secureRandom = new SecureRandom();

    public ForgotPasswordService() {
        super();
    }

    /**
     * Request password reset for client.
     * Always returns true to avoid revealing if email exists.
     */
    public boolean sendClientPasswordResetEmail(String email) {
        Optional<Client> clientOpt = clientRepository.findByEmail(email);
        
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            String rawToken = generateSecureToken();
            String tokenHash = hashToken(rawToken);
            long expiry = System.currentTimeMillis() + TOKEN_EXPIRY_MS;
            
            client.setResetPasswordTokenHash(tokenHash);
            client.setResetPasswordExpiry(expiry);
            clientRepository.save(client);
            
            String resetLink = frontendUrl + "/reset-password?token=" + rawToken;
            emailService.sendPasswordResetEmail(email, resetLink);
        }
        
        // Always return true for security (don't reveal if email exists)
        return true;
    }

    /**
     * Request password reset for establishment user.
     * Always returns true to avoid revealing if email exists.
     */
    public boolean sendEstablishmentPasswordResetEmail(String email) {
        Optional<EstablishmentUser> userOpt = establishmentUserRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            EstablishmentUser user = userOpt.get();
            String rawToken = generateSecureToken();
            String tokenHash = hashToken(rawToken);
            long expiry = System.currentTimeMillis() + TOKEN_EXPIRY_MS;
            
            user.setResetPasswordTokenHash(tokenHash);
            user.setResetPasswordExpiry(expiry);
            establishmentUserRepository.save(user);
            
            String resetLink = frontendUrl + "/reset-password?token=" + rawToken;
            emailService.sendPasswordResetEmail(email, resetLink);
        }
        
        // Always return true for security (don't reveal if email exists)
        return true;
    }

    /**
     * Reset password using token and email.
     * Returns false if token is invalid or expired.
     */
    public boolean resetPassword(String email, String rawToken, String newPassword) {
        String tokenHash = hashToken(rawToken);
        
        // Try to find client first
        Optional<Client> clientOpt = clientRepository.findByResetPasswordTokenHash(tokenHash);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            
            // Verify email matches and token is not expired
            if (client.getEmail().equals(email) && !isTokenExpired(client.getResetPasswordExpiry())) {
                clientService.updatePassword(email, newPassword);
                
                // Invalidate token after use
                client.setResetPasswordTokenHash(null);
                client.setResetPasswordExpiry(null);
                clientRepository.save(client);
                
                return true;
            }
        }
        
        // Try to find establishment user
        Optional<EstablishmentUser> userOpt = establishmentUserRepository.findByResetPasswordTokenHash(tokenHash);
        if (userOpt.isPresent()) {
            EstablishmentUser user = userOpt.get();
            
            // Verify email matches and token is not expired
            if (user.getEmail().equals(email) && !isTokenExpired(user.getResetPasswordExpiry())) {
                establishmentUserService.updatePassword(email, newPassword);
                
                // Invalidate token after use
                user.setResetPasswordTokenHash(null);
                user.setResetPasswordExpiry(null);
                establishmentUserRepository.save(user);
                
                return true;
            }
        }
        
        return false;
    }

    /**
     * Generate a cryptographically secure random token (32 bytes as hex string)
     */
    private String generateSecureToken() {
        byte[] randomBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(randomBytes);
        return bytesToHex(randomBytes);
    }

    /**
     * Hash token using SHA-256
     */
    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Check if token is expired
     */
    private boolean isTokenExpired(Long expiryTimestamp) {
        if (expiryTimestamp == null) {
            return true;
        }
        return System.currentTimeMillis() > expiryTimestamp;
    }

    /**
     * Convert byte array to hex string
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}