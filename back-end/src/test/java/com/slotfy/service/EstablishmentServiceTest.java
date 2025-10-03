package com.slotfy.service;

import com.slotfy.model.Establishment;
import com.slotfy.model.EstablishmentStatus;
import com.slotfy.repository.EstablishmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for EstablishmentService
 */
public class EstablishmentServiceTest {

    @Mock
    private EstablishmentRepository establishmentRepository;

    private EstablishmentService establishmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        establishmentService = new EstablishmentService(establishmentRepository);
    }

    @Test
    void testServiceCreation() {
        assertNotNull(establishmentService);
    }

    @Test
    void testFindByEmail() {
        String email = "test@example.com";
        Establishment establishment = new Establishment("Test Establishment", email, "123456789", "123 Main St");

        when(establishmentRepository.findByEmail(email)).thenReturn(Optional.of(establishment));

        Optional<Establishment> result = establishmentService.findByEmail(email);

        assertTrue(result.isPresent());
        assertEquals(email, result.get().getEmail());
        verify(establishmentRepository).findByEmail(email);
    }

    @Test
    void testFindByCnpj() {
        String cnpj = "12345678901234";
        Establishment establishment = new Establishment("Test Establishment", "test@example.com", "123456789", "123 Main St");
        establishment.setCnpj(cnpj);

        when(establishmentRepository.findByCnpj(cnpj)).thenReturn(Optional.of(establishment));

        Optional<Establishment> result = establishmentService.findByCnpj(cnpj);

        assertTrue(result.isPresent());
        verify(establishmentRepository).findByCnpj(cnpj);
    }

    @Test
    void testGetByStatus() {
        EstablishmentStatus status = EstablishmentStatus.ACTIVE;
        List<Establishment> establishments = Arrays.asList(
            new Establishment("Est 1", "est1@example.com", "111", "Address 1"),
            new Establishment("Est 2", "est2@example.com", "222", "Address 2")
        );

        when(establishmentRepository.findByStatusOrderByNameAsc(status))
            .thenReturn(establishments);

        List<Establishment> result = establishmentService.getByStatus(status);

        assertEquals(establishments, result);
        verify(establishmentRepository).findByStatusOrderByNameAsc(status);
    }

    @Test
    void testGetByCategory() {
        String category = "Salon";
        List<Establishment> establishments = Arrays.asList(
            new Establishment("Salon 1", "salon1@example.com", "111", "Address 1")
        );

        when(establishmentRepository.findByCategoryOrderByNameAsc(category))
            .thenReturn(establishments);

        List<Establishment> result = establishmentService.getByCategory(category);

        assertEquals(establishments, result);
        verify(establishmentRepository).findByCategoryOrderByNameAsc(category);
    }

    @Test
    void testCreateEstablishment_Success() {
        String name = "New Establishment";
        String email = "new@example.com";
        String phone = "123456789";
        String address = "123 Main St";
        String description = "Description";
        String category = "Salon";
        String cnpj = "12345678901234";

        when(establishmentRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(establishmentRepository.findByCnpj(cnpj)).thenReturn(Optional.empty());

        Establishment savedEstablishment = new Establishment(name, email, phone, address);
        savedEstablishment.setId(1L);
        when(establishmentRepository.save(any(Establishment.class))).thenReturn(savedEstablishment);

        Establishment result = establishmentService.createEstablishment(name, email, phone, address, description, category, cnpj);

        assertNotNull(result);
        verify(establishmentRepository).save(any(Establishment.class));
    }

    @Test
    void testCreateEstablishment_NullName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            establishmentService.createEstablishment(null, "email@example.com", "123", "Address", "Desc", "Cat", "12345678901234");
        });
        
        assertEquals("Nome é obrigatório", exception.getMessage());
        verify(establishmentRepository, never()).save(any(Establishment.class));
    }

    @Test
    void testCreateEstablishment_EmptyName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            establishmentService.createEstablishment("   ", "email@example.com", "123", "Address", "Desc", "Cat", "12345678901234");
        });
        
        assertEquals("Nome é obrigatório", exception.getMessage());
        verify(establishmentRepository, never()).save(any(Establishment.class));
    }

    @Test
    void testCreateEstablishment_EmailAlreadyExists() {
        String email = "existing@example.com";

        Establishment existing = new Establishment("Existing", email, "123", "Address");
        when(establishmentRepository.findByEmail(email)).thenReturn(Optional.of(existing));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            establishmentService.createEstablishment("New", email, "456", "New Address", "Desc", "Cat", "12345678901234");
        });
        
        assertEquals("Já existe um estabelecimento com este email", exception.getMessage());
        verify(establishmentRepository, never()).save(any(Establishment.class));
    }

    @Test
    void testCreateEstablishment_CnpjAlreadyExists() {
        String cnpj = "12345678901234";

        when(establishmentRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        Establishment existing = new Establishment("Existing", "existing@example.com", "123", "Address");
        when(establishmentRepository.findByCnpj(cnpj)).thenReturn(Optional.of(existing));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            establishmentService.createEstablishment("New", "new@example.com", "456", "New Address", "Desc", "Cat", cnpj);
        });
        
        assertEquals("Já existe um estabelecimento com este CNPJ", exception.getMessage());
        verify(establishmentRepository, never()).save(any(Establishment.class));
    }

    @Test
    void testUpdateEstablishment_Success() {
        Long establishmentId = 1L;
        String newName = "Updated Establishment";
        String newEmail = "updated@example.com";
        String newPhone = "987654321";
        String newAddress = "456 New St";
        String newDescription = "New Description";
        String newCategory = "New Category";
        String newCnpj = "98765432109876";
        String newWorkingHours = "9-18";

        Establishment establishment = new Establishment("Old Name", "old@example.com", "123", "Old Address");
        establishment.setId(establishmentId);

        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
        when(establishmentRepository.existsByEmailAndIdNot(newEmail, establishmentId)).thenReturn(false);
        when(establishmentRepository.existsByCnpjAndIdNot(newCnpj, establishmentId)).thenReturn(false);
        when(establishmentRepository.save(any(Establishment.class))).thenReturn(establishment);

        Establishment result = establishmentService.updateEstablishment(establishmentId, newName, newEmail, newPhone, 
            newAddress, newDescription, newCategory, newCnpj, newWorkingHours);

        assertNotNull(result);
        verify(establishmentRepository).save(establishment);
    }

    @Test
    void testUpdateEstablishment_NotFound() {
        Long establishmentId = 1L;

        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            establishmentService.updateEstablishment(establishmentId, "Name", "email@example.com", "123", 
                "Address", "Desc", "Cat", "12345678901234", "9-18");
        });
        
        assertEquals("Estabelecimento não encontrado", exception.getMessage());
        verify(establishmentRepository, never()).save(any(Establishment.class));
    }

    @Test
    void testUpdateEstablishment_NullName() {
        Long establishmentId = 1L;
        Establishment establishment = new Establishment("Old Name", "old@example.com", "123", "Old Address");
        establishment.setId(establishmentId);

        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            establishmentService.updateEstablishment(establishmentId, null, "email@example.com", "123", 
                "Address", "Desc", "Cat", "12345678901234", "9-18");
        });
        
        assertEquals("Nome é obrigatório", exception.getMessage());
        verify(establishmentRepository, never()).save(any(Establishment.class));
    }

    @Test
    void testUpdateEstablishment_EmailAlreadyExists() {
        Long establishmentId = 1L;
        String duplicateEmail = "duplicate@example.com";

        Establishment establishment = new Establishment("Name", "old@example.com", "123", "Address");
        establishment.setId(establishmentId);

        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
        when(establishmentRepository.existsByEmailAndIdNot(duplicateEmail, establishmentId)).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            establishmentService.updateEstablishment(establishmentId, "Name", duplicateEmail, "123", 
                "Address", "Desc", "Cat", "12345678901234", "9-18");
        });
        
        assertEquals("Já existe outro estabelecimento com este email", exception.getMessage());
        verify(establishmentRepository, never()).save(any(Establishment.class));
    }

    @Test
    void testUpdateEstablishment_CnpjAlreadyExists() {
        Long establishmentId = 1L;
        String duplicateCnpj = "98765432109876";

        Establishment establishment = new Establishment("Name", "email@example.com", "123", "Address");
        establishment.setId(establishmentId);

        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
        when(establishmentRepository.existsByEmailAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(establishmentRepository.existsByCnpjAndIdNot(duplicateCnpj, establishmentId)).thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            establishmentService.updateEstablishment(establishmentId, "Name", "email@example.com", "123", 
                "Address", "Desc", "Cat", duplicateCnpj, "9-18");
        });
        
        assertEquals("Já existe outro estabelecimento com este CNPJ", exception.getMessage());
        verify(establishmentRepository, never()).save(any(Establishment.class));
    }

    @Test
    void testUpdateStatus_Success() {
        Long establishmentId = 1L;
        Establishment establishment = new Establishment("Name", "email@example.com", "123", "Address");
        establishment.setId(establishmentId);

        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
        when(establishmentRepository.save(any(Establishment.class))).thenReturn(establishment);

        Establishment result = establishmentService.updateStatus(establishmentId, EstablishmentStatus.INACTIVE);

        assertNotNull(result);
        verify(establishmentRepository).save(establishment);
    }

    @Test
    void testUpdateStatus_NotFound() {
        Long establishmentId = 1L;

        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            establishmentService.updateStatus(establishmentId, EstablishmentStatus.INACTIVE);
        });
        
        assertEquals("Estabelecimento não encontrado", exception.getMessage());
        verify(establishmentRepository, never()).save(any(Establishment.class));
    }

    @Test
    void testUpdateSettings_Success() {
        Long establishmentId = 1L;
        String settings = "{\"key\":\"value\"}";

        Establishment establishment = new Establishment("Name", "email@example.com", "123", "Address");
        establishment.setId(establishmentId);

        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
        when(establishmentRepository.save(any(Establishment.class))).thenReturn(establishment);

        Establishment result = establishmentService.updateSettings(establishmentId, settings);

        assertNotNull(result);
        verify(establishmentRepository).save(establishment);
    }

    @Test
    void testUpdateImage_Success() {
        Long establishmentId = 1L;
        String imageUrl = "https://example.com/image.jpg";

        Establishment establishment = new Establishment("Name", "email@example.com", "123", "Address");
        establishment.setId(establishmentId);

        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
        when(establishmentRepository.save(any(Establishment.class))).thenReturn(establishment);

        Establishment result = establishmentService.updateImage(establishmentId, imageUrl);

        assertNotNull(result);
        verify(establishmentRepository).save(establishment);
    }

    @Test
    void testGetCategories() {
        List<String> categories = Arrays.asList("Salon", "Spa", "Clinic");

        when(establishmentRepository.findDistinctCategories()).thenReturn(categories);

        List<String> result = establishmentService.getCategories();

        assertEquals(categories, result);
        verify(establishmentRepository).findDistinctCategories();
    }

    @Test
    void testSearchEstablishments() {
        String searchTerm = "salon";
        List<Establishment> establishments = Arrays.asList(
            new Establishment("Hair Salon", "salon@example.com", "123", "Address")
        );

        when(establishmentRepository.searchByNameOrDescription(searchTerm))
            .thenReturn(establishments);

        List<Establishment> result = establishmentService.searchEstablishments(searchTerm);

        assertEquals(establishments, result);
        verify(establishmentRepository).searchByNameOrDescription(searchTerm);
    }

    @Test
    void testCountByStatus() {
        EstablishmentStatus status = EstablishmentStatus.ACTIVE;
        long expectedCount = 5L;

        when(establishmentRepository.countByStatus(status)).thenReturn(expectedCount);

        long result = establishmentService.countByStatus(status);

        assertEquals(expectedCount, result);
        verify(establishmentRepository).countByStatus(status);
    }

    @Test
    void testActivateEstablishment() {
        Long establishmentId = 1L;
        Establishment establishment = new Establishment("Name", "email@example.com", "123", "Address");
        establishment.setId(establishmentId);

        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
        when(establishmentRepository.save(any(Establishment.class))).thenReturn(establishment);

        Establishment result = establishmentService.activateEstablishment(establishmentId);

        assertNotNull(result);
        verify(establishmentRepository).save(establishment);
    }

    @Test
    void testDeactivateEstablishment() {
        Long establishmentId = 1L;
        Establishment establishment = new Establishment("Name", "email@example.com", "123", "Address");
        establishment.setId(establishmentId);

        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
        when(establishmentRepository.save(any(Establishment.class))).thenReturn(establishment);

        Establishment result = establishmentService.deactivateEstablishment(establishmentId);

        assertNotNull(result);
        verify(establishmentRepository).save(establishment);
    }

    @Test
    void testSuspendEstablishment() {
        Long establishmentId = 1L;
        Establishment establishment = new Establishment("Name", "email@example.com", "123", "Address");
        establishment.setId(establishmentId);

        when(establishmentRepository.findById(establishmentId)).thenReturn(Optional.of(establishment));
        when(establishmentRepository.save(any(Establishment.class))).thenReturn(establishment);

        Establishment result = establishmentService.suspendEstablishment(establishmentId);

        assertNotNull(result);
        verify(establishmentRepository).save(establishment);
    }
}
