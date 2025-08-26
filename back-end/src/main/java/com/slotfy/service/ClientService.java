package com.slotfy.service;

import com.slotfy.model.Client;
import com.slotfy.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service for managing client users
 */
@Service
public class ClientService extends BaseService<Client, Long> {
    
    @Autowired
    private ClientRepository clientRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    public ClientService(ClientRepository repository) {
        super(repository);
        this.clientRepository = repository;
    }
    
    /**
     * Authenticate client with email and password
     */
    public Optional<Client> authenticate(String email, String password) {
        Optional<Client> client = clientRepository.findByEmailAndActive(email, true);
        
        if (client.isPresent() && passwordEncoder.matches(password, client.get().getPassword())) {
            return client;
        }
        
        return Optional.empty();
    }
    
    /**
     * Find client by email
     */
    public Optional<Client> findByEmail(String email) {
        return clientRepository.findByEmail(email);
    }
    
    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return clientRepository.existsByEmail(email);
    }
    
    /**
     * Register new client
     */
    public Client registerClient(String name, String email, String password, String phone) {
        if (emailExists(email)) {
            throw new IllegalArgumentException("Email já está em uso");
        }
        
        // Encode password before saving
        String encodedPassword = passwordEncoder.encode(password);
        
        Client client = new Client(name, email, encodedPassword, phone);
        return save(client);
    }
    
    /**
     * Update client password
     */
    public void updatePassword(String email, String newPassword) {
        Optional<Client> client = findByEmail(email);
        if (client.isPresent()) {
            String encodedPassword = passwordEncoder.encode(newPassword);
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
}