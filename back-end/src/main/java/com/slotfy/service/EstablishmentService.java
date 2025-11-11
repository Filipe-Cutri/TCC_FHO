package com.slotfy.service;

import com.slotfy.model.Establishment;
import com.slotfy.model.EstablishmentStatus;
import com.slotfy.repository.EstablishmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service class for Establishment entity
 */
@Service
public class EstablishmentService extends BaseService<Establishment, Long> {
    
    private static final Logger logger = LoggerFactory.getLogger(EstablishmentService.class);
    private final EstablishmentRepository establishmentRepository;
    
    @Autowired
    public EstablishmentService(EstablishmentRepository establishmentRepository) {
        super(establishmentRepository);
        this.establishmentRepository = establishmentRepository;
    }
    
    /**
     * Find establishment by email
     */
    public Optional<Establishment> findByEmail(String email) {
        return establishmentRepository.findByEmail(email);
    }
    
    /**
     * Find establishment by CNPJ
     */
    public Optional<Establishment> findByCnpj(String cnpj) {
        return establishmentRepository.findByCnpj(cnpj);
    }
    
    /**
     * Get establishments by status
     */
    public List<Establishment> getByStatus(EstablishmentStatus status) {
        return establishmentRepository.findByStatusOrderByNameAsc(status);
    }
    
    /**
     * Get establishments by category
     */
    public List<Establishment> getByCategory(String category) {
        return establishmentRepository.findByCategoryOrderByNameAsc(category);
    }
    
    /**
     * Create a new establishment
     */
    public Establishment createEstablishment(String name, String email, String phone, String address, 
                                           String description, String category, String cnpj) {
        logger.debug("Iniciando criação de estabelecimento - Nome: {}, Email: {}, Categoria: {}", name, email, category);
        
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            logger.error("Validação falhou: Nome é obrigatório");
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        logger.debug("Verificando duplicidade de email: {}", email);
        // Check if email already exists
        if (email != null && !email.trim().isEmpty()) {
            Optional<Establishment> existingByEmail = establishmentRepository.findByEmail(email);
            if (existingByEmail.isPresent()) {
                logger.warn("Tentativa de cadastrar estabelecimento com email já existente: {} (ID existente: {})", 
                           email, existingByEmail.get().getId());
                throw new IllegalArgumentException("Já existe um estabelecimento com este email");
            }
        }
        
        logger.debug("Verificando duplicidade de CNPJ: {}", cnpj);
        // Check if CNPJ already exists
        if (cnpj != null && !cnpj.trim().isEmpty()) {
            Optional<Establishment> existingByCnpj = establishmentRepository.findByCnpj(cnpj);
            if (existingByCnpj.isPresent()) {
                logger.warn("Tentativa de cadastrar estabelecimento com CNPJ já existente: {} (ID existente: {})", 
                           cnpj, existingByCnpj.get().getId());
                throw new IllegalArgumentException("Já existe um estabelecimento com este CNPJ");
            }
        }
        
        logger.debug("Criando objeto Establishment com os dados fornecidos");
        Establishment establishment = new Establishment(name.trim(), email, phone, address);
        establishment.setDescription(description);
        establishment.setCategory(category);
        establishment.setCnpj(cnpj);
        
        logger.info("Salvando estabelecimento no banco de dados - Nome: {}", name.trim());
        try {
            Establishment saved = establishmentRepository.save(establishment);
            logger.info("Estabelecimento salvo com sucesso - ID: {}, Nome: {}, Status: {}", 
                       saved.getId(), saved.getName(), saved.getStatus());
            return saved;
        } catch (Exception e) {
            logger.error("Erro ao salvar estabelecimento no banco de dados", e);
            logger.error("Tipo de erro: {}, Mensagem: {}", e.getClass().getName(), e.getMessage());
            if (e.getCause() != null) {
                logger.error("Causa raiz: {} - {}", e.getCause().getClass().getName(), e.getCause().getMessage());
            }
            throw e;
        }
    }
    
    /**
     * Update establishment information
     */
    public Establishment updateEstablishment(Long establishmentId, String name, String email, String phone, 
                                           String address, String description, String category, String cnpj,
                                           String workingHours) {
        Optional<Establishment> optionalEstablishment = establishmentRepository.findById(establishmentId);
        if (optionalEstablishment.isEmpty()) {
            throw new IllegalArgumentException("Estabelecimento não encontrado");
        }
        
        Establishment establishment = optionalEstablishment.get();
        
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        // Check if email already exists for another establishment
        if (email != null && !email.trim().isEmpty()) {
            if (establishmentRepository.existsByEmailAndIdNot(email, establishmentId)) {
                throw new IllegalArgumentException("Já existe outro estabelecimento com este email");
            }
        }
        
        // Check if CNPJ already exists for another establishment
        if (cnpj != null && !cnpj.trim().isEmpty()) {
            if (establishmentRepository.existsByCnpjAndIdNot(cnpj, establishmentId)) {
                throw new IllegalArgumentException("Já existe outro estabelecimento com este CNPJ");
            }
        }
        
        establishment.setName(name.trim());
        establishment.setEmail(email);
        establishment.setPhone(phone);
        establishment.setAddress(address);
        establishment.setDescription(description);
        establishment.setCategory(category);
        establishment.setCnpj(cnpj);
        establishment.setWorkingHours(workingHours);
        
        return establishmentRepository.save(establishment);
    }
    
    /**
     * Update establishment status
     */
    public Establishment updateStatus(Long establishmentId, EstablishmentStatus status) {
        Optional<Establishment> optionalEstablishment = establishmentRepository.findById(establishmentId);
        if (optionalEstablishment.isEmpty()) {
            throw new IllegalArgumentException("Estabelecimento não encontrado");
        }
        
        Establishment establishment = optionalEstablishment.get();
        establishment.setStatus(status);
        
        return establishmentRepository.save(establishment);
    }
    
    /**
     * Update establishment settings
     */
    public Establishment updateSettings(Long establishmentId, String settings) {
        Optional<Establishment> optionalEstablishment = establishmentRepository.findById(establishmentId);
        if (optionalEstablishment.isEmpty()) {
            throw new IllegalArgumentException("Estabelecimento não encontrado");
        }
        
        Establishment establishment = optionalEstablishment.get();
        establishment.setSettings(settings);
        
        return establishmentRepository.save(establishment);
    }
    
    /**
     * Update establishment image
     */
    public Establishment updateImage(Long establishmentId, String imageUrl) {
        Optional<Establishment> optionalEstablishment = establishmentRepository.findById(establishmentId);
        if (optionalEstablishment.isEmpty()) {
            throw new IllegalArgumentException("Estabelecimento não encontrado");
        }
        
        Establishment establishment = optionalEstablishment.get();
        establishment.setImageUrl(imageUrl);
        
        return establishmentRepository.save(establishment);
    }
    
    /**
     * Get distinct categories
     */
    public List<String> getCategories() {
        return establishmentRepository.findDistinctCategories();
    }
    
    /**
     * Search establishments
     */
    public List<Establishment> searchEstablishments(String searchTerm) {
        return establishmentRepository.searchByNameOrDescription(searchTerm);
    }
    
    /**
     * Count establishments by status
     */
    public long countByStatus(EstablishmentStatus status) {
        return establishmentRepository.countByStatus(status);
    }
    
    /**
     * Activate establishment
     */
    public Establishment activateEstablishment(Long establishmentId) {
        return updateStatus(establishmentId, EstablishmentStatus.ACTIVE);
    }
    
    /**
     * Deactivate establishment
     */
    public Establishment deactivateEstablishment(Long establishmentId) {
        return updateStatus(establishmentId, EstablishmentStatus.INACTIVE);
    }
    
    /**
     * Suspend establishment
     */
    public Establishment suspendEstablishment(Long establishmentId) {
        return updateStatus(establishmentId, EstablishmentStatus.SUSPENDED);
    }
}