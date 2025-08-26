package com.slotfy.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.NoRepositoryBean;

/**
 * Base repository interface for common database operations.
 * This interface can be extended by specific entity repositories.
 */
@NoRepositoryBean
public interface BaseRepository<T, ID> extends JpaRepository<T, ID> {
    
    // Common repository methods can be defined here
    
}