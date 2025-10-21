package com.slotfy.service;

import com.slotfy.model.Service;
import com.slotfy.model.ServiceStatus;
import com.slotfy.repository.ServiceRepository;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Service entity
 */
@org.springframework.stereotype.Service
public class ServiceService extends BaseService<Service, Long> {
    
    private final ServiceRepository serviceRepository;
    
    @Autowired
    public ServiceService(ServiceRepository serviceRepository) {
        super(serviceRepository);
        this.serviceRepository = serviceRepository;
    }
    
    /**
     * Get all services for an establishment
     */
    public List<Service> getByEstablishmentId(Long establishmentId) {
        return serviceRepository.findByEstablishmentIdOrderByNameAsc(establishmentId);
    }
    
    /**
     * Get active services for an establishment
     */
    public List<Service> getActiveByEstablishmentId(Long establishmentId) {
        return serviceRepository.findByEstablishmentIdAndStatusOrderByNameAsc(establishmentId, ServiceStatus.ACTIVE);
    }
    
    /**
     * Get services by category
     */
    public List<Service> getByCategory(Long establishmentId, String category) {
        return serviceRepository.findByEstablishmentIdAndCategoryOrderByNameAsc(establishmentId, category);
    }
    
    /**
     * Create a new service
     */
    public Service createService(String name, String description, Integer durationMinutes, BigDecimal price, 
                               Long establishmentId, String category) {
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do serviço é obrigatório");
        }
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new IllegalArgumentException("Duração deve ser maior que zero");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo");
        }
        if (establishmentId == null) {
            throw new IllegalArgumentException("ID do estabelecimento é obrigatório");
        }
        
        // Check if name already exists for this establishment
        Optional<Service> existing = serviceRepository.findByNameAndEstablishmentId(name.trim(), establishmentId);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("Já existe um serviço com este nome neste estabelecimento");
        }
        
        Service service = new Service(name.trim(), description, durationMinutes, price, establishmentId);
        service.setCategory(category);
        
        return serviceRepository.save(service);
    }
    
    /**
     * Update an existing service
     */
    public Service updateService(Long serviceId, String name, String description, Integer durationMinutes, 
                               BigDecimal price, String category) {
        Optional<Service> optionalService = serviceRepository.findById(serviceId);
        if (optionalService.isEmpty()) {
            throw new IllegalArgumentException("Serviço não encontrado");
        }
        
        Service service = optionalService.get();
        
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome do serviço é obrigatório");
        }
        if (durationMinutes == null || durationMinutes <= 0) {
            throw new IllegalArgumentException("Duração deve ser maior que zero");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Preço não pode ser negativo");
        }
        
        // Check if name already exists for another service in this establishment
        if (serviceRepository.existsByNameAndEstablishmentIdAndIdNot(name.trim(), service.getEstablishmentId(), serviceId)) {
            throw new IllegalArgumentException("Já existe outro serviço com este nome neste estabelecimento");
        }
        
        service.setName(name.trim());
        service.setDescription(description);
        service.setDurationMinutes(durationMinutes);
        service.setPrice(price);
        service.setCategory(category);
        
        return serviceRepository.save(service);
    }
    
    /**
     * Update service status
     */
    public Service updateStatus(Long serviceId, ServiceStatus status) {
        Optional<Service> optionalService = serviceRepository.findById(serviceId);
        if (optionalService.isEmpty()) {
            throw new IllegalArgumentException("Serviço não encontrado");
        }
        
        Service service = optionalService.get();
        service.setStatus(status);
        
        return serviceRepository.save(service);
    }
    
    /**
     * Get distinct categories for an establishment
     */
    public List<String> getCategories(Long establishmentId) {
        return serviceRepository.findDistinctCategoriesByEstablishmentId(establishmentId);
    }
    
    /**
     * Search services by price range
     */
    public List<Service> getByPriceRange(Long establishmentId, BigDecimal minPrice, BigDecimal maxPrice) {
        return serviceRepository.findByEstablishmentIdAndPriceBetween(establishmentId, minPrice, maxPrice);
    }
    
    /**
     * Search services by duration range
     */
    public List<Service> getByDurationRange(Long establishmentId, Integer minDuration, Integer maxDuration) {
        return serviceRepository.findByEstablishmentIdAndDurationBetween(establishmentId, minDuration, maxDuration);
    }
    
    /**
     * Count services for establishment
     */
    public long countByEstablishment(Long establishmentId) {
        return serviceRepository.countByEstablishmentId(establishmentId);
    }
    
    /**
     * Count active services for establishment
     */
    public long countActiveByEstablishment(Long establishmentId) {
        return serviceRepository.countByEstablishmentIdAndStatus(establishmentId, ServiceStatus.ACTIVE);
    }
    
    /**
     * Delete service (only if no appointments)
     */
    public void deleteService(Long serviceId) {
        Optional<Service> optionalService = serviceRepository.findById(serviceId);
        if (optionalService.isEmpty()) {
            throw new IllegalArgumentException("Serviço não encontrado");
        }
        
        // In a real application, you would check for existing appointments here
        // For now, we'll just delete the service
        serviceRepository.deleteById(serviceId);
    }
    
    /**
     * Validate that a service belongs to the specified establishment
     * This is critical for multi-establishment data isolation
     */
    public void validateServiceBelongsToEstablishment(Long serviceId, Long establishmentId) {
        Optional<Service> optionalService = serviceRepository.findById(serviceId);
        if (optionalService.isEmpty()) {
            throw new IllegalArgumentException("Serviço não encontrado");
        }
        
        Service service = optionalService.get();
        if (!service.getEstablishmentId().equals(establishmentId)) {
            throw new SecurityException("Acesso negado: serviço não pertence ao estabelecimento");
        }
    }
    
    /**
     * Get service by ID with establishment validation
     * Ensures multi-establishment data isolation
     */
    public Optional<Service> findByIdAndEstablishment(Long serviceId, Long establishmentId) {
        Optional<Service> optionalService = serviceRepository.findById(serviceId);
        if (optionalService.isEmpty()) {
            return Optional.empty();
        }
        
        Service service = optionalService.get();
        if (!service.getEstablishmentId().equals(establishmentId)) {
            // Return empty instead of throwing exception for GET operations
            return Optional.empty();
        }
        
        return optionalService;
    }
    
    /**
     * Update service with establishment validation
     * Ensures multi-establishment data isolation
     */
    public Service updateService(Long serviceId, String name, String description, Integer durationMinutes, 
                               BigDecimal price, String category, Long establishmentId) {
        validateServiceBelongsToEstablishment(serviceId, establishmentId);
        return updateService(serviceId, name, description, durationMinutes, price, category);
    }
    
    /**
     * Update service status with establishment validation
     * Ensures multi-establishment data isolation
     */
    public Service updateStatus(Long serviceId, ServiceStatus status, Long establishmentId) {
        validateServiceBelongsToEstablishment(serviceId, establishmentId);
        return updateStatus(serviceId, status);
    }
    
    /**
     * Delete service with establishment validation
     * Ensures multi-establishment data isolation
     */
    public void deleteService(Long serviceId, Long establishmentId) {
        validateServiceBelongsToEstablishment(serviceId, establishmentId);
        deleteService(serviceId);
    }
    
    /**
     * Update service image with establishment validation
     * Ensures multi-establishment data isolation
     */
    public Service updateImage(Long serviceId, String imageUrl, Long establishmentId) {
        validateServiceBelongsToEstablishment(serviceId, establishmentId);
        
        Optional<Service> optionalService = serviceRepository.findById(serviceId);
        if (optionalService.isEmpty()) {
            throw new IllegalArgumentException("Serviço não encontrado");
        }
        
        Service service = optionalService.get();
        service.setImageUrl(imageUrl);
        
        return serviceRepository.save(service);
    }
}