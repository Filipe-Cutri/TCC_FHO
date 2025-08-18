package com.slotify.service;

import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;

/**
 * Base service abstract class for common business logic operations.
 * This class can be extended by specific entity services.
 */
public abstract class BaseService<T, ID> {
    
    protected com.slotify.repository.BaseRepository<T, ID> repository;
    
    /**
     * Constructor for dependency injection
     */
    public BaseService(com.slotify.repository.BaseRepository<T, ID> repository) {
        this.repository = repository;
    }
    
    /**
     * Default constructor for services that don't use repository
     */
    public BaseService() {
    }
    
    /**
     * Save an entity
     */
    public T save(T entity) {
        return repository.save(entity);
    }
    
    /**
     * Find entity by ID
     */
    public Optional<T> findById(ID id) {
        return repository.findById(id);
    }
    
    /**
     * Delete entity by ID
     */
    public void deleteById(ID id) {
        repository.deleteById(id);
    }
    
    /**
     * Check if entity exists by ID
     */
    public boolean existsById(ID id) {
        return repository.existsById(id);
    }
    
}