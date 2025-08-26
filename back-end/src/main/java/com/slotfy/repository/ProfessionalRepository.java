package com.slotfy.repository;

import com.slotfy.model.Professional;
import com.slotfy.model.ProfessionalStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Professional entity
 */
@Repository
public interface ProfessionalRepository extends BaseRepository<Professional, Long> {
    
    /**
     * Find all professionals by establishment ID
     */
    List<Professional> findByEstablishmentIdOrderByNameAsc(Long establishmentId);
    
    /**
     * Find all active professionals by establishment ID
     */
    List<Professional> findByEstablishmentIdAndStatusOrderByNameAsc(Long establishmentId, ProfessionalStatus status);
    
    /**
     * Find professional by email and establishment ID
     */
    Optional<Professional> findByEmailAndEstablishmentId(String email, Long establishmentId);
    
    /**
     * Check if email exists for different professional in same establishment
     */
    boolean existsByEmailAndEstablishmentIdAndIdNot(String email, Long establishmentId, Long id);
    
    /**
     * Count professionals by establishment ID
     */
    long countByEstablishmentId(Long establishmentId);
    
    /**
     * Count active professionals by establishment ID
     */
    long countByEstablishmentIdAndStatus(Long establishmentId, ProfessionalStatus status);
    
    /**
     * Find professionals with ratings above threshold
     */
    @Query("SELECT p FROM Professional p WHERE p.establishmentId = :establishmentId AND p.rating >= :minRating ORDER BY p.rating DESC")
    List<Professional> findTopRatedProfessionals(@Param("establishmentId") Long establishmentId, @Param("minRating") java.math.BigDecimal minRating);
    
    /**
     * Find professionals by specialty containing text
     */
    @Query("SELECT p FROM Professional p WHERE p.establishmentId = :establishmentId AND LOWER(p.specialties) LIKE LOWER(CONCAT('%', :specialty, '%'))")
    List<Professional> findByEstablishmentIdAndSpecialtiesContaining(@Param("establishmentId") Long establishmentId, @Param("specialty") String specialty);
}