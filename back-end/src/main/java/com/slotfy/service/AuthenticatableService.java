package com.slotfy.service;

import java.util.Optional;

/**
 * Interface for services that support authentication
 */
public interface AuthenticatableService<T> {
    
    /**
     * Authenticate entity with email and password
     */
    Optional<T> authenticate(String email, String password);
    
    /**
     * Find entity by email
     */
    Optional<T> findByEmail(String email);
    
    /**
     * Check if email exists
     */
    boolean existsByEmail(String email);
    
    /**
     * Register new entity
     */
    T register(String name, String email, String password, String... additionalParams);
}