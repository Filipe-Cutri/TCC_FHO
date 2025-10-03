package com.slotfy.service;

import com.slotfy.model.Professional;
import com.slotfy.model.ProfessionalStatus;
import com.slotfy.repository.ProfessionalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ProfessionalService
 */
public class ProfessionalServiceTest {

    @Mock
    private ProfessionalRepository professionalRepository;

    private ProfessionalService professionalService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        professionalService = new ProfessionalService(professionalRepository);
    }

    @Test
    void testServiceCreation() {
        assertNotNull(professionalService);
    }

    @Test
    void testGetByEstablishmentId() {
        Long establishmentId = 1L;
        List<Professional> expectedProfessionals = Arrays.asList(
            new Professional("Prof 1", "prof1@example.com", "123456", "Specialty 1", establishmentId),
            new Professional("Prof 2", "prof2@example.com", "789012", "Specialty 2", establishmentId)
        );

        when(professionalRepository.findByEstablishmentIdOrderByNameAsc(establishmentId))
            .thenReturn(expectedProfessionals);

        List<Professional> result = professionalService.getByEstablishmentId(establishmentId);

        assertEquals(expectedProfessionals, result);
        verify(professionalRepository).findByEstablishmentIdOrderByNameAsc(establishmentId);
    }

    @Test
    void testGetActiveByEstablishmentId() {
        Long establishmentId = 1L;
        List<Professional> activeProfessionals = Arrays.asList(
            new Professional("Active Prof", "active@example.com", "123456", "Specialty", establishmentId)
        );

        when(professionalRepository.findByEstablishmentIdAndStatusOrderByNameAsc(establishmentId, ProfessionalStatus.ACTIVE))
            .thenReturn(activeProfessionals);

        List<Professional> result = professionalService.getActiveByEstablishmentId(establishmentId);

        assertEquals(activeProfessionals, result);
        verify(professionalRepository).findByEstablishmentIdAndStatusOrderByNameAsc(establishmentId, ProfessionalStatus.ACTIVE);
    }

    @Test
    void testCreateProfessional_Success() {
        String name = "New Professional";
        String email = "newprof@example.com";
        String phone = "1234567890";
        String specialties = "Specialty 1, Specialty 2";
        Long establishmentId = 1L;

        when(professionalRepository.findByEmailAndEstablishmentId(email, establishmentId))
            .thenReturn(Optional.empty());

        Professional savedProfessional = new Professional(name, email, phone, specialties, establishmentId);
        savedProfessional.setId(1L);
        when(professionalRepository.save(any(Professional.class))).thenReturn(savedProfessional);

        Professional result = professionalService.createProfessional(name, email, phone, specialties, establishmentId);

        assertNotNull(result);
        verify(professionalRepository).save(any(Professional.class));
    }

    @Test
    void testCreateProfessional_NullName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            professionalService.createProfessional(null, "email@example.com", "123", "Specialty", 1L);
        });
        
        assertEquals("Nome é obrigatório", exception.getMessage());
        verify(professionalRepository, never()).save(any(Professional.class));
    }

    @Test
    void testCreateProfessional_EmptyName() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            professionalService.createProfessional("   ", "email@example.com", "123", "Specialty", 1L);
        });
        
        assertEquals("Nome é obrigatório", exception.getMessage());
        verify(professionalRepository, never()).save(any(Professional.class));
    }

    @Test
    void testCreateProfessional_NullEstablishmentId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            professionalService.createProfessional("Name", "email@example.com", "123", "Specialty", null);
        });
        
        assertEquals("ID do estabelecimento é obrigatório", exception.getMessage());
        verify(professionalRepository, never()).save(any(Professional.class));
    }

    @Test
    void testCreateProfessional_EmailAlreadyExists() {
        String email = "existing@example.com";
        Long establishmentId = 1L;

        Professional existing = new Professional("Existing", email, "123", "Specialty", establishmentId);
        when(professionalRepository.findByEmailAndEstablishmentId(email, establishmentId))
            .thenReturn(Optional.of(existing));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            professionalService.createProfessional("New", email, "456", "Specialty", establishmentId);
        });
        
        assertEquals("Já existe um profissional com este email neste estabelecimento", exception.getMessage());
        verify(professionalRepository, never()).save(any(Professional.class));
    }

    @Test
    void testUpdateProfessional_Success() {
        Long professionalId = 1L;
        Long establishmentId = 1L;
        String newName = "Updated Name";
        String newEmail = "updated@example.com";
        String newPhone = "9876543210";
        String newSpecialties = "New Specialties";

        Professional professional = new Professional("Old Name", "old@example.com", "123", "Old", establishmentId);
        professional.setId(professionalId);

        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));
        when(professionalRepository.existsByEmailAndEstablishmentIdAndIdNot(newEmail, establishmentId, professionalId))
            .thenReturn(false);
        when(professionalRepository.save(any(Professional.class))).thenReturn(professional);

        Professional result = professionalService.updateProfessional(professionalId, newName, newEmail, newPhone, newSpecialties);

        assertNotNull(result);
        verify(professionalRepository).save(professional);
    }

    @Test
    void testUpdateProfessional_NotFound() {
        Long professionalId = 1L;

        when(professionalRepository.findById(professionalId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            professionalService.updateProfessional(professionalId, "Name", "email@example.com", "123", "Specialty");
        });
        
        assertEquals("Profissional não encontrado", exception.getMessage());
        verify(professionalRepository, never()).save(any(Professional.class));
    }

    @Test
    void testUpdateProfessional_NullName() {
        Long professionalId = 1L;
        Professional professional = new Professional("Old Name", "old@example.com", "123", "Specialty", 1L);
        professional.setId(professionalId);

        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            professionalService.updateProfessional(professionalId, null, "email@example.com", "123", "Specialty");
        });
        
        assertEquals("Nome é obrigatório", exception.getMessage());
        verify(professionalRepository, never()).save(any(Professional.class));
    }

    @Test
    void testUpdateProfessional_EmailAlreadyExists() {
        Long professionalId = 1L;
        Long establishmentId = 1L;
        String duplicateEmail = "duplicate@example.com";

        Professional professional = new Professional("Name", "old@example.com", "123", "Specialty", establishmentId);
        professional.setId(professionalId);

        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));
        when(professionalRepository.existsByEmailAndEstablishmentIdAndIdNot(duplicateEmail, establishmentId, professionalId))
            .thenReturn(true);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            professionalService.updateProfessional(professionalId, "Name", duplicateEmail, "123", "Specialty");
        });
        
        assertEquals("Já existe outro profissional com este email neste estabelecimento", exception.getMessage());
        verify(professionalRepository, never()).save(any(Professional.class));
    }

    @Test
    void testUpdateStatus_Success() {
        Long professionalId = 1L;
        Professional professional = new Professional("Name", "email@example.com", "123", "Specialty", 1L);
        professional.setId(professionalId);

        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));
        when(professionalRepository.save(any(Professional.class))).thenReturn(professional);

        Professional result = professionalService.updateStatus(professionalId, ProfessionalStatus.INACTIVE);

        assertNotNull(result);
        verify(professionalRepository).save(professional);
    }

    @Test
    void testUpdateStatus_NotFound() {
        Long professionalId = 1L;

        when(professionalRepository.findById(professionalId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            professionalService.updateStatus(professionalId, ProfessionalStatus.INACTIVE);
        });
        
        assertEquals("Profissional não encontrado", exception.getMessage());
        verify(professionalRepository, never()).save(any(Professional.class));
    }

    @Test
    void testUpdateStatistics_Success() {
        Long professionalId = 1L;
        BigDecimal rating = new BigDecimal("4.5");
        BigDecimal satisfactionRate = new BigDecimal("85.5");

        Professional professional = new Professional("Name", "email@example.com", "123", "Specialty", 1L);
        professional.setId(professionalId);

        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));
        when(professionalRepository.save(any(Professional.class))).thenReturn(professional);

        Professional result = professionalService.updateStatistics(professionalId, rating, satisfactionRate);

        assertNotNull(result);
        verify(professionalRepository).save(professional);
    }

    @Test
    void testUpdateStatistics_NotFound() {
        Long professionalId = 1L;

        when(professionalRepository.findById(professionalId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            professionalService.updateStatistics(professionalId, new BigDecimal("4.5"), new BigDecimal("85.5"));
        });
        
        assertEquals("Profissional não encontrado", exception.getMessage());
        verify(professionalRepository, never()).save(any(Professional.class));
    }

    @Test
    void testIncrementAppointmentCount() {
        Long professionalId = 1L;
        Professional professional = new Professional("Name", "email@example.com", "123", "Specialty", 1L);
        professional.setId(professionalId);

        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));
        when(professionalRepository.save(any(Professional.class))).thenReturn(professional);

        professionalService.incrementAppointmentCount(professionalId);

        verify(professionalRepository).save(professional);
    }

    @Test
    void testGetTopRatedProfessionals() {
        Long establishmentId = 1L;
        BigDecimal minRating = new BigDecimal("4.0");
        List<Professional> topRated = Arrays.asList(
            new Professional("Top Prof", "top@example.com", "123", "Specialty", establishmentId)
        );

        when(professionalRepository.findTopRatedProfessionals(establishmentId, minRating))
            .thenReturn(topRated);

        List<Professional> result = professionalService.getTopRatedProfessionals(establishmentId, minRating);

        assertEquals(topRated, result);
        verify(professionalRepository).findTopRatedProfessionals(establishmentId, minRating);
    }

    @Test
    void testSearchBySpecialty() {
        Long establishmentId = 1L;
        String specialty = "Massage";
        List<Professional> specialists = Arrays.asList(
            new Professional("Specialist", "spec@example.com", "123", "Massage, Therapy", establishmentId)
        );

        when(professionalRepository.findByEstablishmentIdAndSpecialtiesContaining(establishmentId, specialty))
            .thenReturn(specialists);

        List<Professional> result = professionalService.searchBySpecialty(establishmentId, specialty);

        assertEquals(specialists, result);
        verify(professionalRepository).findByEstablishmentIdAndSpecialtiesContaining(establishmentId, specialty);
    }

    @Test
    void testCountByEstablishment() {
        Long establishmentId = 1L;
        long expectedCount = 5L;

        when(professionalRepository.countByEstablishmentId(establishmentId)).thenReturn(expectedCount);

        long result = professionalService.countByEstablishment(establishmentId);

        assertEquals(expectedCount, result);
        verify(professionalRepository).countByEstablishmentId(establishmentId);
    }

    @Test
    void testCountActiveByEstablishment() {
        Long establishmentId = 1L;
        long expectedCount = 3L;

        when(professionalRepository.countByEstablishmentIdAndStatus(establishmentId, ProfessionalStatus.ACTIVE))
            .thenReturn(expectedCount);

        long result = professionalService.countActiveByEstablishment(establishmentId);

        assertEquals(expectedCount, result);
        verify(professionalRepository).countByEstablishmentIdAndStatus(establishmentId, ProfessionalStatus.ACTIVE);
    }

    @Test
    void testDeleteProfessional_Success() {
        Long professionalId = 1L;
        Professional professional = new Professional("Name", "email@example.com", "123", "Specialty", 1L);
        professional.setId(professionalId);

        when(professionalRepository.findById(professionalId)).thenReturn(Optional.of(professional));
        doNothing().when(professionalRepository).deleteById(professionalId);

        professionalService.deleteProfessional(professionalId);

        verify(professionalRepository).deleteById(professionalId);
    }

    @Test
    void testDeleteProfessional_NotFound() {
        Long professionalId = 1L;

        when(professionalRepository.findById(professionalId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            professionalService.deleteProfessional(professionalId);
        });
        
        assertEquals("Profissional não encontrado", exception.getMessage());
        verify(professionalRepository, never()).deleteById(anyLong());
    }
}
