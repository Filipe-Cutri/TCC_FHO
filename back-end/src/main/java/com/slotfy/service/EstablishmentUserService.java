package com.slotfy.service;

import com.slotfy.model.EstablishmentUser;
import com.slotfy.model.UserRole;
import com.slotfy.repository.EstablishmentUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing establishment users
 */
@Service
public class EstablishmentUserService extends BaseService<EstablishmentUser, Long> {
    
    @Autowired
    private EstablishmentUserRepository establishmentUserRepository;
    
    @Autowired
    PasswordEncoder passwordEncoder;
    
    public EstablishmentUserService(EstablishmentUserRepository repository) {
        super(repository);
        this.establishmentUserRepository = repository;
    }
    
    /**
     * Authenticate user with email and password
     */
    public Optional<EstablishmentUser> authenticate(String email, String password) {
        Optional<EstablishmentUser> user = establishmentUserRepository.findByEmailAndActive(email, true);
        
        if (user.isPresent() && passwordEncoder.matches(password, user.get().getPassword())) {
            return user;
        }
        
        return Optional.empty();
    }
    
    /**
     * Find user by email
     */
    public Optional<EstablishmentUser> findByEmail(String email) {
        return establishmentUserRepository.findByEmail(email);
    }
    
    /**
     * Find users by establishment
     */
    public List<EstablishmentUser> findByEstablishment(Long establishmentId) {
        return establishmentUserRepository.findByEstablishmentIdAndActive(establishmentId, true);
    }
    
    /**
     * Find users by establishment and role
     */
    public List<EstablishmentUser> findByEstablishmentAndRole(Long establishmentId, UserRole role) {
        return establishmentUserRepository.findByEstablishmentIdAndRole(establishmentId, role);
    }
    
    /**
     * Check if email exists
     */
    public boolean emailExists(String email) {
        return establishmentUserRepository.existsByEmail(email);
    }
    
    /**
     * Create new user
     */
    public EstablishmentUser createUser(String name, String email, String password, UserRole role, Long establishmentId) {
        if (emailExists(email)) {
            throw new IllegalArgumentException("Email já está em uso");
        }
        
        String encodedPassword = passwordEncoder.encode(password);
        EstablishmentUser user = new EstablishmentUser(name, email, encodedPassword, role, establishmentId);
        return save(user);
    }
    
    /**
     * Update user password
     */
    public void updatePassword(String email, String newPassword) {
        Optional<EstablishmentUser> userOpt = findByEmail(email);
        if (userOpt.isPresent()) {
            EstablishmentUser user = userOpt.get();
            user.setPassword(passwordEncoder.encode(newPassword));
            save(user);
        }
    }
    
    /**
     * Deactivate user
     */
    public void deactivateUser(Long userId) {
        Optional<EstablishmentUser> user = findById(userId);
        if (user.isPresent()) {
            user.get().setActive(false);
            save(user.get());
        }
    }
}