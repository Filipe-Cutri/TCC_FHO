package com.slotfy.service;

import com.slotfy.model.EstablishmentUser;
import com.slotfy.model.UserRole;
import com.slotfy.repository.EstablishmentUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

/**
 * Test to verify EstablishmentUserService compilation and basic functionality
 */
public class EstablishmentUserServiceTest {

    @Mock
    private EstablishmentUserRepository repository;

    private EstablishmentUserService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new EstablishmentUserService(repository);
    }

    @Test
    void testServiceCreation() {
        // Test that the service can be created without compilation errors
        assertNotNull(service);
    }

    @Test
    void testSaveMethod() {
        // Test that save method works (inherited from BaseService)
        EstablishmentUser user = new EstablishmentUser("Test", "test@test.com", "password", UserRole.ADMIN, 1L);
        when(repository.save(user)).thenReturn(user);
        
        EstablishmentUser saved = service.save(user);
        
        assertNotNull(saved);
        verify(repository).save(user);
    }

    @Test 
    void testFindByIdMethod() {
        // Test that findById method works (inherited from BaseService)
        Long userId = 1L;
        EstablishmentUser user = new EstablishmentUser("Test", "test@test.com", "password", UserRole.ADMIN, 1L);
        when(repository.findById(userId)).thenReturn(Optional.of(user));
        
        Optional<EstablishmentUser> found = service.findById(userId);
        
        assertTrue(found.isPresent());
        verify(repository).findById(userId);
    }

    @Test
    void testCreateUserMethod() {
        // Test the createUser method that caused compilation issues
        String email = "test@test.com";
        when(repository.existsByEmail(email)).thenReturn(false);
        when(repository.save(any(EstablishmentUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        EstablishmentUser created = service.createUser("Test User", email, "password", UserRole.ADMIN, 1L);
        
        assertNotNull(created);
        verify(repository).existsByEmail(email);
        verify(repository).save(any(EstablishmentUser.class));
    }

    @Test
    void testDeactivateUserMethod() {
        // Test the deactivateUser method that caused compilation issues
        Long userId = 1L;
        EstablishmentUser user = new EstablishmentUser("Test", "test@test.com", "password", UserRole.ADMIN, 1L);
        when(repository.findById(userId)).thenReturn(Optional.of(user));
        when(repository.save(user)).thenReturn(user);
        
        service.deactivateUser(userId);
        
        verify(repository).findById(userId);
        verify(repository).save(user);
    }
}