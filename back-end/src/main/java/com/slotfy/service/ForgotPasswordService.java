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

    /**
     * Request password reset for client.
     * Always returns true to avoid revealing if email exists.
     */
    public boolean sendClientPasswordResetEmail(String email) {
        System.out.println("Tentando enviar email de reset para cliente: " + maskEmail(email));
        Optional<Client> clientOpt = clientRepository.findByEmail(email);
        
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            System.out.println("Cliente encontrado");
            String rawToken = generateSecureToken();
            String tokenHash = hashToken(rawToken);
            long expiry = System.currentTimeMillis() + TOKEN_EXPIRY_MS;
            
            client.setResetPasswordTokenHash(tokenHash);
            client.setResetPasswordExpiry(expiry);
            clientRepository.save(client);
            System.out.println("Token gerado e salvo para cliente");
            
            String resetLink = frontendUrl + "/pages/reset-password.html?email=" + email + "&token=" + rawToken;
            System.out.println("Enviando email de reset para cliente");
            boolean emailSent = emailService.sendPasswordResetEmail(email, resetLink);
            System.out.println("Email enviado: " + emailSent);
        } else {
            System.out.println("Cliente não encontrado");
        }
        
        // Always return true for security (don't reveal if email exists)
        return true;
    }

    /**
     * Request password reset for establishment user.
     * Always returns true to avoid revealing if email exists.
     */
    public boolean sendEstablishmentPasswordResetEmail(String email) {
        System.out.println("Tentando enviar email de reset para estabelecimento: " + maskEmail(email));
        Optional<EstablishmentUser> userOpt = establishmentUserRepository.findByEmail(email);
        
        if (userOpt.isPresent()) {
            EstablishmentUser user = userOpt.get();
            System.out.println("Estabelecimento encontrado");
            String rawToken = generateSecureToken();
            String tokenHash = hashToken(rawToken);
            long expiry = System.currentTimeMillis() + TOKEN_EXPIRY_MS;
            
            user.setResetPasswordTokenHash(tokenHash);
            user.setResetPasswordExpiry(expiry);
            establishmentUserRepository.save(user);
            System.out.println("Token gerado e salvo para estabelecimento");
            
            String resetLink = frontendUrl + "/pages/reset-password.html?email=" + email + "&token=" + rawToken;
            System.out.println("Enviando email de reset para estabelecimento");
            boolean emailSent = emailService.sendPasswordResetEmail(email, resetLink);
            System.out.println("Email enviado: " + emailSent);
        } else {
            System.out.println("Estabelecimento não encontrado");
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