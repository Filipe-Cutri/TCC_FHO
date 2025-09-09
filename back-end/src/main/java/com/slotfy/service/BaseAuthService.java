package com.slotfy.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

/**
 * Base authentication service with common authentication logic
 */
public abstract class BaseAuthService<T, R> extends BaseService<T, Long> implements AuthenticatableService<T> {
    
    @Autowired
    protected PasswordEncoder passwordEncoder;
    
    protected R authRepository;
    
    public BaseAuthService(R repository) {
        this.authRepository = repository;
    }
    
    /**
     * Validate email format
     */
    protected boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }
    
    /**
     * Validate password strength
     */
    protected boolean isValidPassword(String password) {
        return password != null && password.length() >= 6;
    }
    
    /**
     * Hash password before saving
     */
    protected String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }
    
    /**
     * Verify password against hash
     */
    protected boolean verifyPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }
    
    /**
     * Get entity password (to be implemented by concrete classes)
     */
    protected abstract String getEntityPassword(T entity);
    
    /**
     * Find active entity by email (to be implemented by concrete classes)
     */
    protected abstract Optional<T> findActiveByEmail(String email);
    
    /**
     * Check email existence (to be implemented by concrete classes)
     */
    protected abstract boolean checkEmailExists(String email);
    
    /**
     * Common authentication logic
     */
    @Override
    public Optional<T> authenticate(String email, String password) {
        if (!isValidEmail(email) || !isValidPassword(password)) {
            return Optional.empty();
        }
        
        Optional<T> entity = findActiveByEmail(email);
        
        if (entity.isPresent() && verifyPassword(password, getEntityPassword(entity.get()))) {
            return entity;
        }
        
        return Optional.empty();
    }
    
    /**
     * Common email existence check
     */
    @Override
    public boolean existsByEmail(String email) {
        if (!isValidEmail(email)) {
            return false;
        }
        return checkEmailExists(email);
    }
}