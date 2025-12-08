package com.slotfy.service;

import com.slotfy.model.Client;
import com.slotfy.model.Establishment;
import com.slotfy.repository.ClientRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
    private EstablishmentService establishmentService;
    
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
        
        // Validate establishment exists and is active (if provided)
        if (establishmentId != null) {
            Optional<Establishment> establishmentOpt = establishmentService.findById(establishmentId);
            if (!establishmentOpt.isPresent()) {
                throw new IllegalArgumentException("Estabelecimento não encontrado");
            }
            
            Establishment establishment = establishmentOpt.get();
            if (!establishment.isActive()) {
                throw new IllegalArgumentException("Estabelecimento não está ativo. Por favor, escolha outro estabelecimento.");
            }
        }
        
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
        
        // Validate establishment exists and is active (if establishmentId is provided)
        if (establishmentId != null) {
            Optional<Establishment> establishmentOpt = establishmentService.findById(establishmentId);
            if (!establishmentOpt.isPresent()) {
                throw new IllegalArgumentException("Estabelecimento não encontrado");
            }
            
            Establishment establishment = establishmentOpt.get();
            if (!establishment.isActive()) {
                throw new IllegalArgumentException("Estabelecimento não está ativo");
            }
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
}