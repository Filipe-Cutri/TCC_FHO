package com.slotify.repository;

import com.slotify.model.EstablishmentUser;
import com.slotify.model.UserRole;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for EstablishmentUser entity
 */
@Repository
public interface EstablishmentUserRepository extends BaseRepository<EstablishmentUser, Long> {
    
    /**
     * Find user by email
     */
    Optional<EstablishmentUser> findByEmail(String email);
    
    /**
     * Find user by email and active status
     */
    Optional<EstablishmentUser> findByEmailAndActive(String email, Boolean active);
    
    /**
     * Find users by establishment ID
     */
    List<EstablishmentUser> findByEstablishmentId(Long establishmentId);
    
    /**
     * Find users by establishment ID and role
     */
    List<EstablishmentUser> findByEstablishmentIdAndRole(Long establishmentId, UserRole role);
    
    /**
     * Find active users by establishment ID
     */
    List<EstablishmentUser> findByEstablishmentIdAndActive(Long establishmentId, Boolean active);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
}