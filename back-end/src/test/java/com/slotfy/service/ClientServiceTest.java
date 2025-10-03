package com.slotfy.service;

import com.slotfy.model.Client;
import com.slotfy.repository.ClientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ClientService
 */
public class ClientServiceTest {

    @Mock
    private ClientRepository clientRepository;
    
    @Mock
    private PasswordEncoder passwordEncoder;

    private ClientService clientService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        clientService = new ClientService(clientRepository);
        // Inject the mocked password encoder into the service
        ReflectionTestUtils.setField(clientService, "passwordEncoder", passwordEncoder);
        // Mock password encoder to return a simple hash
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
    }

    @Test
    void testServiceCreation() {
        assertNotNull(clientService);
    }

    @Test
    void testRegisterClient_Success() {
        String name = "Test Client";
        String email = "test@example.com";
        String password = "password123";
        String phone = "1234567890";

        when(clientRepository.existsByEmail(email)).thenReturn(false);
        
        Client savedClient = new Client(name, email, "encodedPassword", phone);
        savedClient.setId(1L);
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        Client result = clientService.register(name, email, password, phone);

        assertNotNull(result);
        verify(clientRepository).existsByEmail(email);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void testRegisterClient_EmailAlreadyExists() {
        String email = "existing@example.com";
        
        when(clientRepository.existsByEmail(email)).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.register("Test", email, "password123", "1234567890");
        });
        
        assertEquals("Email já está em uso", exception.getMessage());
        verify(clientRepository).existsByEmail(email);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void testRegisterClient_InvalidEmail() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.register("Test", "invalid-email", "password123", "1234567890");
        });
        
        assertEquals("Email inválido", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void testRegisterClient_ShortPassword() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.register("Test", "test@example.com", "123", "1234567890");
        });
        
        assertEquals("Senha deve ter pelo menos 6 caracteres", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void testRegisterClient_WithEstablishmentId() {
        String name = "Test Client";
        String email = "test@example.com";
        String password = "password123";
        String phone = "1234567890";
        Long establishmentId = 5L;

        when(clientRepository.existsByEmail(email)).thenReturn(false);
        
        Client savedClient = new Client(name, email, "encodedPassword", phone);
        savedClient.setId(1L);
        savedClient.setSelectedEstablishmentId(establishmentId);
        when(clientRepository.save(any(Client.class))).thenReturn(savedClient);

        Client result = clientService.registerClient(name, email, password, phone, establishmentId);

        assertNotNull(result);
        verify(clientRepository).save(any(Client.class));
    }

    @Test
    void testUpdateSelectedEstablishment_Success() {
        Long clientId = 1L;
        Long establishmentId = 2L;

        Client client = new Client("Test", "test@example.com", "password", "1234567890");
        client.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        Client result = clientService.updateSelectedEstablishment(clientId, establishmentId);

        assertNotNull(result);
        verify(clientRepository).findById(clientId);
        verify(clientRepository).save(client);
    }

    @Test
    void testUpdateSelectedEstablishment_ClientNotFound() {
        Long clientId = 1L;
        Long establishmentId = 2L;

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.updateSelectedEstablishment(clientId, establishmentId);
        });
        
        assertEquals("Cliente não encontrado", exception.getMessage());
        verify(clientRepository).findById(clientId);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void testUpdatePassword_Success() {
        String email = "test@example.com";
        String newPassword = "newPassword123";

        Client client = new Client("Test", email, "oldPassword", "1234567890");
        client.setId(1L);

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        clientService.updatePassword(email, newPassword);

        verify(clientRepository).findByEmail(email);
        verify(clientRepository).save(client);
    }

    @Test
    void testUpdatePassword_ShortPassword() {
        String email = "test@example.com";
        String shortPassword = "123";

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.updatePassword(email, shortPassword);
        });
        
        assertEquals("Senha deve ter pelo menos 6 caracteres", exception.getMessage());
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void testDeactivateClient_Success() {
        Long clientId = 1L;
        Client client = new Client("Test", "test@example.com", "password", "1234567890");
        client.setId(clientId);
        client.setActive(true);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        clientService.deactivateClient(clientId);

        verify(clientRepository).findById(clientId);
        verify(clientRepository).save(client);
    }

    @Test
    void testUpdateProfile_Success() {
        Long clientId = 1L;
        String newName = "Updated Name";
        String newPhone = "9876543210";

        Client client = new Client("Old Name", "test@example.com", "password", "1234567890");
        client.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        Client result = clientService.updateProfile(clientId, newName, newPhone);

        assertNotNull(result);
        verify(clientRepository).findById(clientId);
        verify(clientRepository).save(client);
    }

    @Test
    void testUpdateProfile_ClientNotFound() {
        Long clientId = 1L;

        when(clientRepository.findById(clientId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            clientService.updateProfile(clientId, "New Name", "1234567890");
        });
        
        assertEquals("Cliente não encontrado", exception.getMessage());
        verify(clientRepository).findById(clientId);
        verify(clientRepository, never()).save(any(Client.class));
    }

    @Test
    void testUpdateProfile_WithNullName() {
        Long clientId = 1L;
        Client client = new Client("Old Name", "test@example.com", "password", "1234567890");
        client.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        Client result = clientService.updateProfile(clientId, null, "1234567890");

        assertNotNull(result);
        verify(clientRepository).save(client);
    }

    @Test
    void testUpdateProfile_WithEmptyPhone() {
        Long clientId = 1L;
        Client client = new Client("Test", "test@example.com", "password", "1234567890");
        client.setId(clientId);

        when(clientRepository.findById(clientId)).thenReturn(Optional.of(client));
        when(clientRepository.save(any(Client.class))).thenReturn(client);

        Client result = clientService.updateProfile(clientId, "Test", "");

        assertNotNull(result);
        verify(clientRepository).save(client);
    }

    @Test
    void testFindByEmail() {
        String email = "test@example.com";
        Client client = new Client("Test", email, "password", "1234567890");

        when(clientRepository.findByEmail(email)).thenReturn(Optional.of(client));

        Optional<Client> result = clientService.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        verify(clientRepository).findByEmail(email);
    }

    @Test
    void testFindByEmail_NotFound() {
        String email = "notfound@example.com";

        when(clientRepository.findByEmail(email)).thenReturn(Optional.empty());

        Optional<Client> result = clientService.findByEmail(email);

        assertFalse(result.isPresent());
        verify(clientRepository).findByEmail(email);
    }
}
