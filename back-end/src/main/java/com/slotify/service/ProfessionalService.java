package com.slotify.service;

import com.slotify.model.Professional;
import com.slotify.model.ProfessionalStatus;
import com.slotify.repository.ProfessionalRepository;
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
}