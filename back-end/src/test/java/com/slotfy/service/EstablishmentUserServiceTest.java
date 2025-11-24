package com.slotfy.service;

import com.slotfy.model.EstablishmentUser;
import com.slotfy.model.UserRole;
import com.slotfy.repository.EstablishmentUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Test to verify EstablishmentUserService compilation and basic functionality
 */
public class EstablishmentUserServiceTest {

    @Mock
    private EstablishmentUserRepository repository;
    
    @Mock
    private PasswordEncoder passwordEncoder;

    private EstablishmentUserService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new EstablishmentUserService(repository);
        // Manually inject the mock passwordEncoder since we can't use @Autowired in unit tests
        service.passwordEncoder = passwordEncoder;
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
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        when(repository.save(any(EstablishmentUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        EstablishmentUser created = service.createUser("Test User", email, "password", UserRole.ADMIN, 1L);
        
        assertNotNull(created);
        verify(repository).existsByEmail(email);
        verify(passwordEncoder).encode("password");
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

    @Test
    void testRegisterMethod() {
        // Test the register method with additional params
        String email = "newuser@test.com";
        when(repository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(repository.save(any(EstablishmentUser.class))).thenAnswer(invocation -> invocation.getArgument(0));
        
        EstablishmentUser registered = service.register("New User", email, "password123", "ADMIN", "1");
        
        assertNotNull(registered);
        assertEquals("New User", registered.getName());
        assertEquals(email, registered.getEmail());
        verify(repository).existsByEmail(email);
    }

    @Test
    void testRegisterMethodMissingParams() {
        // Test that register throws exception when params are missing
        assertThrows(IllegalArgumentException.class, () -> {
            service.register("New User", "test@test.com", "password123", "ADMIN");
        });
    }

    @Test
    void testFindByEmailMethod() {
        // Test the findByEmail method
        String email = "find@test.com";
        EstablishmentUser user = new EstablishmentUser("Found User", email, "password", UserRole.ADMIN, 1L);
        when(repository.findByEmail(email)).thenReturn(Optional.of(user));
        
        Optional<EstablishmentUser> found = service.findByEmail(email);
        
        assertTrue(found.isPresent());
        assertEquals(email, found.get().getEmail());
        verify(repository).findByEmail(email);
    }

    @Test
    void testFindByEstablishmentMethod() {
        // Test the findByEstablishment method
        Long establishmentId = 1L;
        List<EstablishmentUser> users = Arrays.asList(
            new EstablishmentUser("User 1", "user1@test.com", "password", UserRole.ADMIN, establishmentId),
            new EstablishmentUser("User 2", "user2@test.com", "password", UserRole.STAFF, establishmentId)
        );
        when(repository.findByEstablishmentIdAndActive(establishmentId, true)).thenReturn(users);
        
        List<EstablishmentUser> found = service.findByEstablishment(establishmentId);
        
        assertEquals(2, found.size());
        verify(repository).findByEstablishmentIdAndActive(establishmentId, true);
    }

    @Test
    void testFindByEstablishmentAndRoleMethod() {
        // Test the findByEstablishmentAndRole method
        Long establishmentId = 1L;
        UserRole role = UserRole.ADMIN;
        List<EstablishmentUser> users = Arrays.asList(
            new EstablishmentUser("Admin User", "admin@test.com", "password", role, establishmentId)
        );
        when(repository.findByEstablishmentIdAndRole(establishmentId, role)).thenReturn(users);
        
        List<EstablishmentUser> found = service.findByEstablishmentAndRole(establishmentId, role);
        
        assertEquals(1, found.size());
        assertEquals(role, found.get(0).getRole());
        verify(repository).findByEstablishmentIdAndRole(establishmentId, role);
    }

    @Test
    void testUpdatePasswordMethod() {
        // Test the updatePassword method
        String email = "update@test.com";
        EstablishmentUser user = new EstablishmentUser("User", email, "oldPassword", UserRole.ADMIN, 1L);
        when(repository.findByEmail(email)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newPassword")).thenReturn("encodedNewPassword");
        when(repository.save(any(EstablishmentUser.class))).thenReturn(user);
        
        service.updatePassword(email, "newPassword");
        
        verify(repository).findByEmail(email);
        verify(passwordEncoder).encode("newPassword");
        verify(repository).save(any(EstablishmentUser.class));
    }

    @Test
    void testUpdatePasswordInvalidPassword() {
        // Test that updatePassword throws exception for invalid password
        assertThrows(IllegalArgumentException.class, () -> {
            service.updatePassword("test@test.com", "short");
        });
    }

    @Test
    void testUpdatePasswordUserNotFound() {
        // Test updatePassword when user not found - should not throw
        String email = "notfound@test.com";
        when(repository.findByEmail(email)).thenReturn(Optional.empty());
        
        // Should not throw, just do nothing
        service.updatePassword(email, "newPassword123");
        
        verify(repository).findByEmail(email);
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void testCreateUserEmailAlreadyExists() {
        // Test that createUser throws exception when email exists
        String email = "exists@test.com";
        when(repository.existsByEmail(email)).thenReturn(true);
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.createUser("Test User", email, "password123", UserRole.ADMIN, 1L);
        });
    }

    @Test
    void testCreateUserInvalidEmail() {
        // Test that createUser throws exception for invalid email
        String email = "invalid-email";
        when(repository.existsByEmail(email)).thenReturn(false);
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.createUser("Test User", email, "password123", UserRole.ADMIN, 1L);
        });
    }

    @Test
    void testCreateUserInvalidPassword() {
        // Test that createUser throws exception for invalid password
        String email = "test@test.com";
        when(repository.existsByEmail(email)).thenReturn(false);
        
        assertThrows(IllegalArgumentException.class, () -> {
            service.createUser("Test User", email, "short", UserRole.ADMIN, 1L);
        });
    }

    @Test
    void testDeactivateUserNotFound() {
        // Test that deactivateUser handles non-existent user
        Long userId = 999L;
        when(repository.findById(userId)).thenReturn(Optional.empty());
        
        // Should not throw, just do nothing
        service.deactivateUser(userId);
        
        verify(repository).findById(userId);
        verify(repository, never()).save(any());
    }

    @Test
    void testCreateUserDatabaseError() {
        // Test that database errors are propagated
        String email = "error@test.com";
        when(repository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encodedPassword");
        when(repository.save(any(EstablishmentUser.class))).thenThrow(new RuntimeException("DB error"));
        
        assertThrows(RuntimeException.class, () -> {
            service.createUser("Test User", email, "password123", UserRole.ADMIN, 1L);
        });
    }
}