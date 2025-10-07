package com.slotfy.service;

import com.slotfy.model.Professional;
import com.slotfy.model.ProfessionalStatus;
import com.slotfy.repository.ProfessionalRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Service class for Professional entity
 */
@Service
public class ProfessionalService extends BaseService<Professional, Long> {
    
    private final ProfessionalRepository professionalRepository;
    
    @Autowired
    public ProfessionalService(ProfessionalRepository professionalRepository) {
        super(professionalRepository);
        this.professionalRepository = professionalRepository;
    }
    
    /**
     * Get all professionals for an establishment
     */
    public List<Professional> getByEstablishmentId(Long establishmentId) {
        return professionalRepository.findByEstablishmentIdOrderByNameAsc(establishmentId);
    }
    
    /**
     * Get active professionals for an establishment
     */
    public List<Professional> getActiveByEstablishmentId(Long establishmentId) {
        return professionalRepository.findByEstablishmentIdAndStatusOrderByNameAsc(establishmentId, ProfessionalStatus.ACTIVE);
    }
    
    /**
     * Create a new professional
     */
    public Professional createProfessional(String name, String email, String phone, String specialties, Long establishmentId) {
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        if (establishmentId == null) {
            throw new IllegalArgumentException("ID do estabelecimento é obrigatório");
        }
        
        // Check if email already exists for this establishment
        if (email != null && !email.trim().isEmpty()) {
            Optional<Professional> existing = professionalRepository.findByEmailAndEstablishmentId(email, establishmentId);
            if (existing.isPresent()) {
                throw new IllegalArgumentException("Já existe um profissional com este email neste estabelecimento");
            }
        }
        
        Professional professional = new Professional(name.trim(), email, phone, specialties, establishmentId);
        return professionalRepository.save(professional);
    }
    
    /**
     * Update an existing professional
     */
    public Professional updateProfessional(Long professionalId, String name, String email, String phone, String specialties) {
        Optional<Professional> optionalProfessional = professionalRepository.findById(professionalId);
        if (optionalProfessional.isEmpty()) {
            throw new IllegalArgumentException("Profissional não encontrado");
        }
        
        Professional professional = optionalProfessional.get();
        
        // Validate input
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        // Check if email already exists for another professional in this establishment
        if (email != null && !email.trim().isEmpty()) {
            if (professionalRepository.existsByEmailAndEstablishmentIdAndIdNot(email, professional.getEstablishmentId(), professionalId)) {
                throw new IllegalArgumentException("Já existe outro profissional com este email neste estabelecimento");
            }
        }
        
        professional.setName(name.trim());
        professional.setEmail(email);
        professional.setPhone(phone);
        professional.setSpecialties(specialties);
        
        return professionalRepository.save(professional);
    }
    
    /**
     * Update professional status
     */
    public Professional updateStatus(Long professionalId, ProfessionalStatus status) {
        Optional<Professional> optionalProfessional = professionalRepository.findById(professionalId);
        if (optionalProfessional.isEmpty()) {
            throw new IllegalArgumentException("Profissional não encontrado");
        }
        
        Professional professional = optionalProfessional.get();
        professional.setStatus(status);
        
        return professionalRepository.save(professional);
    }
    
    /**
     * Update professional statistics
     */
    public Professional updateStatistics(Long professionalId, BigDecimal rating, BigDecimal satisfactionRate) {
        Optional<Professional> optionalProfessional = professionalRepository.findById(professionalId);
        if (optionalProfessional.isEmpty()) {
            throw new IllegalArgumentException("Profissional não encontrado");
        }
        
        Professional professional = optionalProfessional.get();
        if (rating != null) {
            professional.setRating(rating);
        }
        if (satisfactionRate != null) {
            professional.setSatisfactionRate(satisfactionRate);
        }
        
        return professionalRepository.save(professional);
    }
    
    /**
     * Increment appointment count for professional
     */
    public void incrementAppointmentCount(Long professionalId) {
        Optional<Professional> optionalProfessional = professionalRepository.findById(professionalId);
        if (optionalProfessional.isPresent()) {
            Professional professional = optionalProfessional.get();
            professional.incrementAppointments();
            professionalRepository.save(professional);
        }
    }
    
    /**
     * Get top rated professionals for an establishment
     */
    public List<Professional> getTopRatedProfessionals(Long establishmentId, BigDecimal minRating) {
        return professionalRepository.findTopRatedProfessionals(establishmentId, minRating);
    }
    
    /**
     * Search professionals by specialty
     */
    public List<Professional> searchBySpecialty(Long establishmentId, String specialty) {
        return professionalRepository.findByEstablishmentIdAndSpecialtiesContaining(establishmentId, specialty);
    }
    
    /**
     * Count professionals for establishment
     */
    public long countByEstablishment(Long establishmentId) {
        return professionalRepository.countByEstablishmentId(establishmentId);
    }
    
    /**
     * Count active professionals for establishment
     */
    public long countActiveByEstablishment(Long establishmentId) {
        return professionalRepository.countByEstablishmentIdAndStatus(establishmentId, ProfessionalStatus.ACTIVE);
    }
    
    /**
     * Delete professional (only if no appointments)
     */
    public void deleteProfessional(Long professionalId) {
        Optional<Professional> optionalProfessional = professionalRepository.findById(professionalId);
        if (optionalProfessional.isEmpty()) {
            throw new IllegalArgumentException("Profissional não encontrado");
        }
        
        // In a real application, you would check for existing appointments here
        // For now, we'll just delete the professional
        professionalRepository.deleteById(professionalId);
    }
    
    /**
     * Validate that a professional belongs to the specified establishment
     * This is critical for multi-establishment data isolation
     */
    public void validateProfessionalBelongsToEstablishment(Long professionalId, Long establishmentId) {
        Optional<Professional> optionalProfessional = professionalRepository.findById(professionalId);
        if (optionalProfessional.isEmpty()) {
            throw new IllegalArgumentException("Profissional não encontrado");
        }
        
        Professional professional = optionalProfessional.get();
        if (!professional.getEstablishmentId().equals(establishmentId)) {
            throw new SecurityException("Acesso negado: profissional não pertence ao estabelecimento");
        }
    }
    
    /**
     * Get professional by ID with establishment validation
     * Ensures multi-establishment data isolation
     */
    public Optional<Professional> findByIdAndEstablishment(Long professionalId, Long establishmentId) {
        Optional<Professional> optionalProfessional = professionalRepository.findById(professionalId);
        if (optionalProfessional.isEmpty()) {
            return Optional.empty();
        }
        
        Professional professional = optionalProfessional.get();
        if (!professional.getEstablishmentId().equals(establishmentId)) {
            // Return empty instead of throwing exception for GET operations
            return Optional.empty();
        }
        
        return optionalProfessional;
    }
    
    /**
     * Update professional with establishment validation
     * Ensures multi-establishment data isolation
     */
    public Professional updateProfessional(Long professionalId, String name, String email, String phone, String specialties, Long establishmentId) {
        validateProfessionalBelongsToEstablishment(professionalId, establishmentId);
        return updateProfessional(professionalId, name, email, phone, specialties);
    }
    
    /**
     * Update professional status with establishment validation
     * Ensures multi-establishment data isolation
     */
    public Professional updateStatus(Long professionalId, ProfessionalStatus status, Long establishmentId) {
        validateProfessionalBelongsToEstablishment(professionalId, establishmentId);
        return updateStatus(professionalId, status);
    }
    
    /**
     * Delete professional with establishment validation
     * Ensures multi-establishment data isolation
     */
    public void deleteProfessional(Long professionalId, Long establishmentId) {
        validateProfessionalBelongsToEstablishment(professionalId, establishmentId);
        deleteProfessional(professionalId);
    }
}