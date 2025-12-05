package com.slotfy.service;

import com.slotfy.model.Client;
import com.slotfy.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for managing client users
 */
@Service
public class ClientService extends BaseAuthService<Client, ClientRepository> {
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    @Autowired
    private EmailService emailService;
    
    @Value("${frontend.url}")
    private String frontendUrl;
    
    public ClientService(ClientRepository repository) {
        super(repository);
        this.clientRepository = repository;
    }
    
    @Override
    protected String getEntityPassword(Client entity) {
        return entity.getPassword();
    }
    
    @Override
    protected Optional<Client> findActiveByEmail(String email) {
        return clientRepository.findByEmailAndActive(email, true);
    }
    
    @Override
    protected boolean checkEmailExists(String email) {
        return clientRepository.existsByEmail(email);
    }
    
    /**
     * Find client by email
     */
    @Override
    public Optional<Client> findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }
    
    /**
     * Register new client
     */
    @Override
    public Client register(String name, String email, String password, String... additionalParams) {
        if (existsByEmail(email)) {
            throw new IllegalArgumentException("Email já está em uso");
        }
        
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException("Email inválido");
        }
        
        if (!isValidPassword(password)) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
        }
        
        String phone = additionalParams.length > 0 ? additionalParams[0] : null;
        Long establishmentId = additionalParams.length > 1 && additionalParams[1] != null 
            ? Long.parseLong(additionalParams[1]) : null;
        
        String encodedPassword = hashPassword(password);
        
        Client client = new Client(name, email, encodedPassword, phone);
        client.setSelectedEstablishmentId(establishmentId);
        return save(client);
    }
    
    /**
     * Register new client (backwards compatibility)
     */
    public Client registerClient(String name, String email, String password, String phone) {
        return register(name, email, password, phone);
    }
    
    /**
     * Register new client with establishment
     */
    public Client registerClient(String name, String email, String password, String phone, Long establishmentId) {
        return register(name, email, password, phone, establishmentId != null ? establishmentId.toString() : null);
    }
    
    /**
     * Update client's selected establishment
     */
    public Client updateSelectedEstablishment(Long clientId, Long establishmentId) {
        Optional<Client> clientOpt = findById(clientId);
        if (!clientOpt.isPresent()) {
            throw new IllegalArgumentException("Cliente não encontrado");
        }
        
        Client client = clientOpt.get();
        client.setSelectedEstablishmentId(establishmentId);
        return save(client);
    }
    
    /**
     * Update client password
     */
    public void updatePassword(String email, String newPassword) {
        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
        }
        
        Optional<Client> client = findByEmail(email);
        if (client.isPresent()) {
            String encodedPassword = hashPassword(newPassword);
            client.get().setPassword(encodedPassword);
            save(client.get());
        }
    }
    
    /**
     * Deactivate client
     */
    public void deactivateClient(Long clientId) {
        Optional<Client> client = findById(clientId);
        if (client.isPresent()) {
            client.get().setActive(false);
            save(client.get());
        }
    }
    
    /**
     * Update client profile
     */
    public Client updateProfile(Long clientId, String name, String phone) {
        Optional<Client> clientOpt = findById(clientId);
        if (!clientOpt.isPresent()) {
            throw new IllegalArgumentException("Cliente não encontrado");
        }
        
        Client client = clientOpt.get();
        
        if (name != null && !name.trim().isEmpty()) {
            client.setName(name.trim());
        }
        
        if (phone != null) {
            client.setPhone(phone.trim().isEmpty() ? null : phone.trim());
        }
        
        return save(client);
    }
    
    /**
     * Initiate password reset process by generating token and sending email
     */
    public boolean initiatePasswordReset(String email) {
        Optional<Client> clientOpt = findByEmail(email);
        if (!clientOpt.isPresent()) {
            // Return false but don't reveal that email doesn't exist for security
            return false;
        }
        
        Client client = clientOpt.get();
        if (!client.getActive()) {
            throw new IllegalArgumentException("Conta desativada");
        }
        
        // Generate reset token
        String token = passwordResetService.generateToken();
        String hashedToken = passwordResetService.hashToken(token);
        Long expiry = passwordResetService.getTokenExpiry();
        
        // Save token to database
        client.setResetPasswordTokenHash(hashedToken);
        client.setResetPasswordExpiry(expiry);
        save(client);
        
        // Build reset link - this should point to the frontend reset page
        String resetLink = buildResetLink(token, "client");
        
        // Send email
        return emailService.sendPasswordResetEmail(client.getEmail(), resetLink);
    }
    
    /**
     * Reset password using token
     */
    public boolean resetPassword(String token, String newPassword) {
        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
        }
        
        String hashedToken = passwordResetService.hashToken(token);
        Optional<Client> clientOpt = clientRepository.findByResetPasswordTokenHash(hashedToken);
        
        if (!clientOpt.isPresent()) {
            throw new IllegalArgumentException("Token de redefinição inválido");
        }
        
        Client client = clientOpt.get();
        
        // Check if token is expired
        if (passwordResetService.isTokenExpired(client.getResetPasswordExpiry())) {
            // Clear expired token
            client.setResetPasswordTokenHash(null);
            client.setResetPasswordExpiry(null);
            save(client);
            throw new IllegalArgumentException("Token de redefinição expirado");
        }
        
        // Update password
        String encodedPassword = hashPassword(newPassword);
        client.setPassword(encodedPassword);
        
        // Clear reset token
        client.setResetPasswordTokenHash(null);
        client.setResetPasswordExpiry(null);
        
        save(client);
        return true;
    }
    
    /**
     * Build password reset link for frontend
     */
    private String buildResetLink(String token, String userType) {
        return String.format("%s/pages/reset-password.html?token=%s&type=%s", frontendUrl, token, userType);
    }
}