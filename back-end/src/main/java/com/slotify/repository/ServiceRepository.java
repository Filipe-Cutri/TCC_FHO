package com.slotify.repository;

import com.slotify.model.Service;
import com.slotify.model.ServiceStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Service entity
 */
@Repository
public interface ServiceRepository extends BaseRepository<Service, Long> {
    
    /**
     * Find all services by establishment ID
     */
    List<Service> findByEstablishmentIdOrderByNameAsc(Long establishmentId);
    
    /**
     * Find all active services by establishment ID
     */
    List<Service> findByEstablishmentIdAndStatusOrderByNameAsc(Long establishmentId, ServiceStatus status);
    
    /**
     * Find services by category and establishment
     */
    List<Service> findByEstablishmentIdAndCategoryOrderByNameAsc(Long establishmentId, String category);
    
    /**
     * Find service by name and establishment ID
     */
    Optional<Service> findByNameAndEstablishmentId(String name, Long establishmentId);
    
    /**
     * Check if name exists for different service in same establishment
     */
    boolean existsByNameAndEstablishmentIdAndIdNot(String name, Long establishmentId, Long id);
    
    /**
     * Count services by establishment ID
     */
    long countByEstablishmentId(Long establishmentId);
    
    /**
     * Count active services by establishment ID
     */
    long countByEstablishmentIdAndStatus(Long establishmentId, ServiceStatus status);
    
    /**
     * Find distinct categories by establishment
     */
    @Query("SELECT DISTINCT s.category FROM Service s WHERE s.establishmentId = :establishmentId AND s.category IS NOT NULL ORDER BY s.category")
    List<String> findDistinctCategoriesByEstablishmentId(@Param("establishmentId") Long establishmentId);
    
    /**
     * Find services by price range
     */
    @Query("SELECT s FROM Service s WHERE s.establishmentId = :establishmentId AND s.price BETWEEN :minPrice AND :maxPrice ORDER BY s.price ASC")
    List<Service> findByEstablishmentIdAndPriceBetween(@Param("establishmentId") Long establishmentId, 
                                                       @Param("minPrice") java.math.BigDecimal minPrice, 
                                                       @Param("maxPrice") java.math.BigDecimal maxPrice);
    
    /**
     * Find services by duration range
     */
    @Query("SELECT s FROM Service s WHERE s.establishmentId = :establishmentId AND s.durationMinutes BETWEEN :minDuration AND :maxDuration ORDER BY s.durationMinutes ASC")
    List<Service> findByEstablishmentIdAndDurationBetween(@Param("establishmentId") Long establishmentId, 
                                                         @Param("minDuration") Integer minDuration, 
                                                         @Param("maxDuration") Integer maxDuration);
}