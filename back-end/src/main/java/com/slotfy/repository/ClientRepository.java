package com.slotfy.repository;

import com.slotfy.model.Client;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Client entity
 */
@Repository
public interface ClientRepository extends BaseRepository<Client, Long> {
    
    /**
     * Find client by email
     */
    Optional<Client> findByEmail(String email);
    
    /**
     * Find client by email and active status
     */
    Optional<Client> findByEmailAndActive(String email, Boolean active);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
}