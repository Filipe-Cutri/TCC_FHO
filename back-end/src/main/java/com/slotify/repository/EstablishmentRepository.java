package com.slotify.repository;

import com.slotify.model.Establishment;
import com.slotify.model.EstablishmentStatus;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Establishment entity
 */
@Repository
public interface EstablishmentRepository extends BaseRepository<Establishment, Long> {
    
    /**
     * Find establishment by email
     */
    Optional<Establishment> findByEmail(String email);
    
    /**
     * Find establishment by CNPJ
     */
    Optional<Establishment> findByCnpj(String cnpj);
    
    /**
     * Find establishments by status
     */
    List<Establishment> findByStatusOrderByNameAsc(EstablishmentStatus status);
    
    /**
     * Find establishments by category
     */
    List<Establishment> findByCategoryOrderByNameAsc(String category);
    
    /**
     * Check if email exists for different establishment
     */
    boolean existsByEmailAndIdNot(String email, Long id);
    
    /**
     * Check if CNPJ exists for different establishment
     */
    boolean existsByCnpjAndIdNot(String cnpj, Long id);
    
    /**
     * Find distinct categories
     */
    @Query("SELECT DISTINCT e.category FROM Establishment e WHERE e.category IS NOT NULL ORDER BY e.category")
    List<String> findDistinctCategories();
    
    /**
     * Search establishments by name or description
     */
    @Query("SELECT e FROM Establishment e WHERE e.status = 'ACTIVE' AND (LOWER(e.name) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :searchTerm, '%'))) ORDER BY e.name ASC")
    List<Establishment> searchByNameOrDescription(@Param("searchTerm") String searchTerm);
    
    /**
     * Count establishments by status
     */
    long countByStatus(EstablishmentStatus status);
}