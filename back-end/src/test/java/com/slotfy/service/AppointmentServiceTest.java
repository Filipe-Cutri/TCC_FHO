package com.slotfy.service;

import com.slotfy.model.Appointment;
import com.slotfy.model.AppointmentStatus;
import com.slotfy.model.Professional;
import com.slotfy.repository.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Test to verify AppointmentService compilation and functionality after fixing JPQL queries
 */
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository repository;

    @Mock
    private ServiceService serviceService;

    @Mock
    private ProfessionalService professionalService;

    private AppointmentService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AppointmentService(repository, serviceService, professionalService);
    }

    @Test
    void testServiceCreation() {
        // Test that the service can be created without compilation errors
        assertNotNull(service);
    }

    @Test
    void testGetTodayAppointments() {
        // Test that getTodayAppointments calls repository with correct parameters
        Long establishmentId = 1L;
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfDay = now.withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        
        List<Appointment> expectedAppointments = Collections.emptyList();
        when(repository.findTodayAppointments(eq(establishmentId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(expectedAppointments);
        
        List<Appointment> result = service.getTodayAppointments(establishmentId);
        
        assertNotNull(result);
        verify(repository).findTodayAppointments(eq(establishmentId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testCountTodayAppointments() {
        // Test that countTodayAppointments calls repository with correct parameters
        Long establishmentId = 1L;
        long expectedCount = 5L;
        
        when(repository.countTodayAppointments(eq(establishmentId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(expectedCount);
        
        long result = service.countTodayAppointments(establishmentId);
        
        assertEquals(expectedCount, result);
        verify(repository).countTodayAppointments(eq(establishmentId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testCountThisMonthAppointments() {
        // Test that countThisMonthAppointments calls repository with correct parameters
        Long establishmentId = 1L;
        long expectedCount = 20L;
        
        when(repository.countThisMonthAppointments(eq(establishmentId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(expectedCount);
        
        long result = service.countThisMonthAppointments(establishmentId);
        
        assertEquals(expectedCount, result);
        verify(repository).countThisMonthAppointments(eq(establishmentId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    void testIsTimeSlotAvailable_NoConflicts() {
        // Test that time slot availability check works when no conflicts exist
        Long professionalId = 1L;
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(1);
        
        // Mock empty list for potential conflicts
        when(repository.findPotentialConflictingAppointments(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        
        boolean result = service.isTimeSlotAvailable(professionalId, startTime, endTime);
        
        assertTrue(result);
    }

    @Test
    void testIsTimeSlotAvailable_WithConflicts() {
        // Test that time slot availability check works when conflicts exist
        Long professionalId = 1L;
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(1);
        
        // Create a conflicting appointment
        Appointment conflictingAppointment = new Appointment();
        conflictingAppointment.setAppointmentDateTime(startTime.plusMinutes(30));
        conflictingAppointment.setServiceDurationMinutes(60);
        
        when(repository.findPotentialConflictingAppointments(any(), any(), any()))
            .thenReturn(Arrays.asList(conflictingAppointment));
        
        boolean result = service.isTimeSlotAvailable(professionalId, startTime, endTime);
        
        assertFalse(result);
    }
    
    // Test createAppointment method
    @Test
    void testCreateAppointment_Success() {
        Long clientId = 1L;
        Long professionalId = 2L;
        Long serviceId = 3L;
        Long establishmentId = 4L;
        LocalDateTime appointmentDateTime = LocalDateTime.now().plusHours(2);
        String notes = "Test notes";
        String clientName = "Test Client";
        String professionalName = "Test Professional";
        String serviceName = "Test Service";
        Integer serviceDurationMinutes = 60;
        BigDecimal servicePrice = new BigDecimal("50.00");
        
        // Mock professional lookup
        Professional mockProfessional = new Professional();
        mockProfessional.setId(professionalId);
        mockProfessional.setEstablishmentId(establishmentId);
        when(professionalService.findById(professionalId)).thenReturn(Optional.of(mockProfessional));
        
        // Mock service lookup
        com.slotfy.model.Service mockService = new com.slotfy.model.Service();
        mockService.setId(serviceId);
        mockService.setEstablishmentId(establishmentId);
        when(serviceService.findById(serviceId)).thenReturn(Optional.of(mockService));
        
        // Mock no conflicts
        when(repository.findPotentialConflictingAppointments(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        
        Appointment savedAppointment = new Appointment(clientId, professionalId, serviceId, establishmentId, appointmentDateTime);
        savedAppointment.setId(1L);
        when(repository.save(any(Appointment.class))).thenReturn(savedAppointment);
        
        Appointment result = service.createAppointment(clientId, professionalId, serviceId, establishmentId,
                appointmentDateTime, notes, clientName, professionalName, serviceName, serviceDurationMinutes, servicePrice);
        
        assertNotNull(result);
        verify(repository).save(any(Appointment.class));
    }
    
    @Test
    void testCreateAppointment_NullClientId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createAppointment(null, 2L, 3L, 4L, LocalDateTime.now().plusHours(1), 
                    "notes", "client", "professional", "service", 60, new BigDecimal("50.00"));
        });
        assertEquals("ID do cliente é obrigatório", exception.getMessage());
    }
    
    @Test
    void testCreateAppointment_NullProfessionalId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createAppointment(1L, null, 3L, 4L, LocalDateTime.now().plusHours(1), 
                    "notes", "client", "professional", "service", 60, new BigDecimal("50.00"));
        });
        assertEquals("ID do profissional é obrigatório", exception.getMessage());
    }
    
    @Test
    void testCreateAppointment_NullServiceId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createAppointment(1L, 2L, null, 4L, LocalDateTime.now().plusHours(1), 
                    "notes", "client", "professional", "service", 60, new BigDecimal("50.00"));
        });
        assertEquals("ID do serviço é obrigatório", exception.getMessage());
    }
    
    @Test
    void testCreateAppointment_NullEstablishmentId() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createAppointment(1L, 2L, 3L, null, LocalDateTime.now().plusHours(1), 
                    "notes", "client", "professional", "service", 60, new BigDecimal("50.00"));
        });
        assertEquals("ID do estabelecimento é obrigatório", exception.getMessage());
    }
    
    @Test
    void testCreateAppointment_NullDateTime() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createAppointment(1L, 2L, 3L, 4L, null, 
                    "notes", "client", "professional", "service", 60, new BigDecimal("50.00"));
        });
        assertEquals("Data e hora são obrigatórias", exception.getMessage());
    }
    
    @Test
    void testCreateAppointment_PastDateTime() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createAppointment(1L, 2L, 3L, 4L, LocalDateTime.now().minusHours(1), 
                    "notes", "client", "professional", "service", 60, new BigDecimal("50.00"));
        });
        assertEquals("Não é possível agendar para uma data passada", exception.getMessage());
    }
    
    @Test
    void testCreateAppointment_WithConflicts() {
        LocalDateTime appointmentDateTime = LocalDateTime.now().plusHours(2);
        Long establishmentId = 4L;
        
        // Mock professional lookup
        Professional mockProfessional = new Professional();
        mockProfessional.setId(2L);
        mockProfessional.setEstablishmentId(establishmentId);
        when(professionalService.findById(2L)).thenReturn(Optional.of(mockProfessional));
        
        // Mock service lookup
        com.slotfy.model.Service mockService = new com.slotfy.model.Service();
        mockService.setId(3L);
        mockService.setEstablishmentId(establishmentId);
        when(serviceService.findById(3L)).thenReturn(Optional.of(mockService));
        
        // Mock conflicting appointment
        Appointment conflictingAppointment = new Appointment();
        conflictingAppointment.setAppointmentDateTime(appointmentDateTime);
        conflictingAppointment.setServiceDurationMinutes(60);
        
        when(repository.findPotentialConflictingAppointments(any(), any(), any()))
            .thenReturn(Arrays.asList(conflictingAppointment));
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createAppointment(1L, 2L, 3L, establishmentId, appointmentDateTime, 
                    "notes", "client", "professional", "service", 60, new BigDecimal("50.00"));
        });
        assertEquals("Já existe um agendamento para este profissional neste horário", exception.getMessage());
    }
    
    // Test update status methods
    @Test
    void testUpdateStatus_Success() {
        Long appointmentId = 1L;
        AppointmentStatus newStatus = AppointmentStatus.CONFIRMED;
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(repository.save(any(Appointment.class))).thenReturn(appointment);
        
        Appointment result = service.updateStatus(appointmentId, newStatus);
        
        assertNotNull(result);
        verify(repository).findById(appointmentId);
        verify(repository).save(appointment);
    }
    
    @Test
    void testUpdateStatus_AppointmentNotFound() {
        Long appointmentId = 1L;
        
        when(repository.findById(appointmentId)).thenReturn(Optional.empty());
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.updateStatus(appointmentId, AppointmentStatus.CONFIRMED);
        });
        assertEquals("Agendamento não encontrado", exception.getMessage());
    }
    
    @Test
    void testCancelAppointment() {
        Long appointmentId = 1L;
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(repository.save(any(Appointment.class))).thenReturn(appointment);
        
        Appointment result = service.cancelAppointment(appointmentId);
        
        assertNotNull(result);
        verify(repository).findById(appointmentId);
        verify(repository).save(appointment);
    }
    
    @Test
    void testConfirmAppointment() {
        Long appointmentId = 1L;
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(repository.save(any(Appointment.class))).thenReturn(appointment);
        
        Appointment result = service.confirmAppointment(appointmentId);
        
        assertNotNull(result);
        verify(repository).findById(appointmentId);
        verify(repository).save(appointment);
    }
    
    @Test
    void testCompleteAppointment() {
        Long appointmentId = 1L;
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setStatus(AppointmentStatus.CONFIRMED);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(repository.save(any(Appointment.class))).thenReturn(appointment);
        
        Appointment result = service.completeAppointment(appointmentId);
        
        assertNotNull(result);
        verify(repository).findById(appointmentId);
        verify(repository).save(appointment);
    }
    
    // Test updateNotes method
    @Test
    void testUpdateNotes_Success() {
        Long appointmentId = 1L;
        String newNotes = "Updated notes";
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setNotes("Old notes");
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(repository.save(any(Appointment.class))).thenReturn(appointment);
        
        Appointment result = service.updateNotes(appointmentId, newNotes);
        
        assertNotNull(result);
        verify(repository).findById(appointmentId);
        verify(repository).save(appointment);
    }
    
    @Test
    void testUpdateNotes_AppointmentNotFound() {
        Long appointmentId = 1L;
        
        when(repository.findById(appointmentId)).thenReturn(Optional.empty());
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.updateNotes(appointmentId, "New notes");
        });
        assertEquals("Agendamento não encontrado", exception.getMessage());
    }
    
    // Test reschedule method
    @Test
    void testReschedule_Success() {
        Long appointmentId = 1L;
        LocalDateTime newDateTime = LocalDateTime.now().plusHours(3);
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setProfessionalId(2L);
        appointment.setServiceDurationMinutes(60);
        appointment.setAppointmentDateTime(LocalDateTime.now().plusHours(1));
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(repository.findPotentialConflictingAppointments(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(repository.save(any(Appointment.class))).thenReturn(appointment);
        
        Appointment result = service.reschedule(appointmentId, newDateTime);
        
        assertNotNull(result);
        verify(repository).findById(appointmentId);
        verify(repository).save(appointment);
    }
    
    @Test
    void testReschedule_AppointmentNotFound() {
        Long appointmentId = 1L;
        LocalDateTime newDateTime = LocalDateTime.now().plusHours(3);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.empty());
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.reschedule(appointmentId, newDateTime);
        });
        assertEquals("Agendamento não encontrado", exception.getMessage());
    }
    
    @Test
    void testReschedule_PastDateTime() {
        Long appointmentId = 1L;
        LocalDateTime pastDateTime = LocalDateTime.now().minusHours(1);
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.reschedule(appointmentId, pastDateTime);
        });
        assertEquals("Não é possível reagendar para uma data passada", exception.getMessage());
    }
    
    @Test
    void testReschedule_WithConflicts() {
        Long appointmentId = 1L;
        LocalDateTime newDateTime = LocalDateTime.now().plusHours(3);
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setProfessionalId(2L);
        appointment.setServiceDurationMinutes(60);
        
        // Mock conflicting appointment (different ID)
        Appointment conflictingAppointment = new Appointment();
        conflictingAppointment.setId(2L);
        conflictingAppointment.setAppointmentDateTime(newDateTime);
        conflictingAppointment.setServiceDurationMinutes(60);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(repository.findPotentialConflictingAppointments(any(), any(), any()))
            .thenReturn(Arrays.asList(conflictingAppointment));
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.reschedule(appointmentId, newDateTime);
        });
        assertEquals("Já existe um agendamento para este profissional no novo horário", exception.getMessage());
    }
    
    // Test query methods
    @Test
    void testGetByEstablishmentId() {
        Long establishmentId = 1L;
        List<Appointment> expectedAppointments = Arrays.asList(new Appointment(), new Appointment());
        
        when(repository.findByEstablishmentIdOrderByAppointmentDateTimeDesc(establishmentId))
            .thenReturn(expectedAppointments);
        
        List<Appointment> result = service.getByEstablishmentId(establishmentId);
        
        assertEquals(expectedAppointments, result);
        verify(repository).findByEstablishmentIdOrderByAppointmentDateTimeDesc(establishmentId);
    }
    
    @Test
    void testGetByEstablishmentAndStatus() {
        Long establishmentId = 1L;
        AppointmentStatus status = AppointmentStatus.CONFIRMED;
        List<Appointment> expectedAppointments = Arrays.asList(new Appointment());
        
        when(repository.findByEstablishmentIdAndStatusOrderByAppointmentDateTimeAsc(establishmentId, status))
            .thenReturn(expectedAppointments);
        
        List<Appointment> result = service.getByEstablishmentAndStatus(establishmentId, status);
        
        assertEquals(expectedAppointments, result);
        verify(repository).findByEstablishmentIdAndStatusOrderByAppointmentDateTimeAsc(establishmentId, status);
    }
    
    @Test
    void testGetByProfessional() {
        Long professionalId = 1L;
        List<Appointment> expectedAppointments = Arrays.asList(new Appointment(), new Appointment());
        
        when(repository.findByProfessionalIdOrderByAppointmentDateTimeAsc(professionalId))
            .thenReturn(expectedAppointments);
        
        List<Appointment> result = service.getByProfessional(professionalId);
        
        assertEquals(expectedAppointments, result);
        verify(repository).findByProfessionalIdOrderByAppointmentDateTimeAsc(professionalId);
    }
    
    @Test
    void testGetByClient() {
        Long clientId = 1L;
        List<Appointment> expectedAppointments = Arrays.asList(new Appointment(), new Appointment());
        
        when(repository.findByClientIdOrderByAppointmentDateTimeDesc(clientId))
            .thenReturn(expectedAppointments);
        
        List<Appointment> result = service.getByClient(clientId);
        
        assertEquals(expectedAppointments, result);
        verify(repository).findByClientIdOrderByAppointmentDateTimeDesc(clientId);
    }
    
    @Test
    void testGetUpcomingAppointments() {
        Long establishmentId = 1L;
        List<Appointment> expectedAppointments = Arrays.asList(new Appointment());
        
        when(repository.findUpcomingAppointments(eq(establishmentId), any(LocalDateTime.class)))
            .thenReturn(expectedAppointments);
        
        List<Appointment> result = service.getUpcomingAppointments(establishmentId);
        
        assertEquals(expectedAppointments, result);
        verify(repository).findUpcomingAppointments(eq(establishmentId), any(LocalDateTime.class));
    }
    
    @Test
    void testGetByDateRange() {
        Long establishmentId = 1L;
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(7);
        List<Appointment> expectedAppointments = Arrays.asList(new Appointment());
        
        when(repository.findByEstablishmentIdAndDateRange(establishmentId, startDate, endDate))
            .thenReturn(expectedAppointments);
        
        List<Appointment> result = service.getByDateRange(establishmentId, startDate, endDate);
        
        assertEquals(expectedAppointments, result);
        verify(repository).findByEstablishmentIdAndDateRange(establishmentId, startDate, endDate);
    }
    
    // Test counting methods
    @Test
    void testCountByEstablishment() {
        Long establishmentId = 1L;
        long expectedCount = 10L;
        
        when(repository.countByEstablishmentId(establishmentId)).thenReturn(expectedCount);
        
        long result = service.countByEstablishment(establishmentId);
        
        assertEquals(expectedCount, result);
        verify(repository).countByEstablishmentId(establishmentId);
    }
    
    @Test
    void testCalculateMonthlyRevenue() {
        Long establishmentId = 1L;
        BigDecimal expectedRevenue = new BigDecimal("1500.00");
        
        when(repository.calculateMonthlyRevenue(eq(establishmentId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(expectedRevenue);
        
        BigDecimal result = service.calculateMonthlyRevenue(establishmentId);
        
        assertEquals(expectedRevenue, result);
        verify(repository).calculateMonthlyRevenue(eq(establishmentId), any(LocalDateTime.class), any(LocalDateTime.class));
    }
    
    @Test
    void testGetProfessionalPerformanceStats() {
        Long establishmentId = 1L;
        Object[] statsRow = {"Professional 1", 10L, new BigDecimal("500.00")};
        List<Object[]> expectedStats = Collections.singletonList(statsRow);
        
        when(repository.findProfessionalPerformanceStats(eq(establishmentId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(expectedStats);
        
        List<Object[]> result = service.getProfessionalPerformanceStats(establishmentId);
        
        assertEquals(expectedStats, result);
        verify(repository).findProfessionalPerformanceStats(eq(establishmentId), any(LocalDateTime.class), any(LocalDateTime.class));
    }
    
    // Test client-specific methods
    @Test
    void testGetClientUpcomingAppointments() {
        Long clientId = 1L;
        List<Appointment> expectedAppointments = Arrays.asList(new Appointment());
        
        when(repository.findByClientIdAndAppointmentDateTimeAfterOrderByAppointmentDateTimeAsc(eq(clientId), any(LocalDateTime.class)))
            .thenReturn(expectedAppointments);
        
        List<Appointment> result = service.getClientUpcomingAppointments(clientId);
        
        assertEquals(expectedAppointments, result);
        verify(repository).findByClientIdAndAppointmentDateTimeAfterOrderByAppointmentDateTimeAsc(eq(clientId), any(LocalDateTime.class));
    }
    
    @Test
    void testGetClientAppointments() {
        Long clientId = 1L;
        List<Appointment> expectedAppointments = Arrays.asList(new Appointment(), new Appointment());
        
        when(repository.findByClientIdOrderByAppointmentDateTimeDesc(clientId))
            .thenReturn(expectedAppointments);
        
        List<Appointment> result = service.getClientAppointments(clientId);
        
        assertEquals(expectedAppointments, result);
        verify(repository).findByClientIdOrderByAppointmentDateTimeDesc(clientId);
    }
    
    @Test
    void testGetClientAppointmentHistory() {
        Long clientId = 1L;
        List<Appointment> expectedAppointments = Arrays.asList(new Appointment());
        
        when(repository.findByClientIdAndAppointmentDateTimeBeforeOrderByAppointmentDateTimeDesc(eq(clientId), any(LocalDateTime.class)))
            .thenReturn(expectedAppointments);
        
        List<Appointment> result = service.getClientAppointmentHistory(clientId);
        
        assertEquals(expectedAppointments, result);
        verify(repository).findByClientIdAndAppointmentDateTimeBeforeOrderByAppointmentDateTimeDesc(eq(clientId), any(LocalDateTime.class));
    }
    
    @Test
    void testIsClientTimeSlotAvailable_NoConflicts() {
        Long clientId = 1L;
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(1);
        
        when(repository.findByClientIdAndAppointmentDateTimeBetween(eq(clientId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Collections.emptyList());
        
        boolean result = service.isClientTimeSlotAvailable(clientId, startTime, endTime);
        
        assertTrue(result);
        verify(repository).findByClientIdAndAppointmentDateTimeBetween(eq(clientId), any(LocalDateTime.class), any(LocalDateTime.class));
    }
    
    @Test
    void testIsClientTimeSlotAvailable_WithConflicts() {
        Long clientId = 1L;
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(1);
        
        Appointment conflictingAppointment = new Appointment();
        conflictingAppointment.setAppointmentDateTime(startTime.plusMinutes(30));
        conflictingAppointment.setServiceDurationMinutes(60);
        
        when(repository.findByClientIdAndAppointmentDateTimeBetween(eq(clientId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(conflictingAppointment));
        
        boolean result = service.isClientTimeSlotAvailable(clientId, startTime, endTime);
        
        assertFalse(result);
        verify(repository).findByClientIdAndAppointmentDateTimeBetween(eq(clientId), any(LocalDateTime.class), any(LocalDateTime.class));
    }
    
    @Test
    void testCreateClientAppointment() {
        Long clientId = 1L;
        Long professionalId = 2L;
        Long serviceId = 3L;
        Long establishmentId = 4L;
        LocalDateTime appointmentDateTime = LocalDateTime.now().plusHours(2);
        String notes = "Client appointment notes";
        
        // Mock professional lookup
        Professional mockProfessional = new Professional();
        mockProfessional.setId(professionalId);
        mockProfessional.setEstablishmentId(establishmentId);
        when(professionalService.findById(professionalId)).thenReturn(Optional.of(mockProfessional));
        
        // Mock service lookup
        com.slotfy.model.Service mockService = new com.slotfy.model.Service();
        mockService.setId(serviceId);
        mockService.setEstablishmentId(establishmentId);
        when(serviceService.findById(serviceId)).thenReturn(Optional.of(mockService));
        
        // Mock no conflicts
        when(repository.findPotentialConflictingAppointments(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        
        Appointment savedAppointment = new Appointment(clientId, professionalId, serviceId, establishmentId, appointmentDateTime);
        savedAppointment.setId(1L);
        when(repository.save(any(Appointment.class))).thenReturn(savedAppointment);
        
        Appointment result = service.createClientAppointment(clientId, professionalId, serviceId, establishmentId, appointmentDateTime, notes);
        
        assertNotNull(result);
        verify(repository).save(any(Appointment.class));
    }
    
    // Test edge cases and additional scenarios
    @Test
    void testCreateAppointment_WithNullDuration() {
        Long clientId = 1L;
        Long professionalId = 2L;
        Long serviceId = 3L;
        Long establishmentId = 4L;
        LocalDateTime appointmentDateTime = LocalDateTime.now().plusHours(2);
        
        // Mock professional lookup
        Professional mockProfessional = new Professional();
        mockProfessional.setId(professionalId);
        mockProfessional.setEstablishmentId(establishmentId);
        when(professionalService.findById(professionalId)).thenReturn(Optional.of(mockProfessional));
        
        // Mock service lookup
        com.slotfy.model.Service mockService = new com.slotfy.model.Service();
        mockService.setId(serviceId);
        mockService.setEstablishmentId(establishmentId);
        when(serviceService.findById(serviceId)).thenReturn(Optional.of(mockService));
        
        // Mock no conflicts
        when(repository.findPotentialConflictingAppointments(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        
        Appointment savedAppointment = new Appointment(clientId, professionalId, serviceId, establishmentId, appointmentDateTime);
        savedAppointment.setId(1L);
        when(repository.save(any(Appointment.class))).thenReturn(savedAppointment);
        
        // Test with null duration - should default to 60 minutes
        Appointment result = service.createAppointment(clientId, professionalId, serviceId, establishmentId,
                appointmentDateTime, "notes", "client", "professional", "service", null, new BigDecimal("50.00"));
        
        assertNotNull(result);
        verify(repository).save(any(Appointment.class));
    }
    
    @Test
    void testReschedule_WithNullDuration() {
        Long appointmentId = 1L;
        LocalDateTime newDateTime = LocalDateTime.now().plusHours(3);
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setProfessionalId(2L);
        appointment.setServiceDurationMinutes(null); // null duration
        appointment.setAppointmentDateTime(LocalDateTime.now().plusHours(1));
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(repository.findPotentialConflictingAppointments(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(repository.save(any(Appointment.class))).thenReturn(appointment);
        
        Appointment result = service.reschedule(appointmentId, newDateTime);
        
        assertNotNull(result);
        verify(repository).findById(appointmentId);
        verify(repository).save(appointment);
    }
    
    @Test
    void testIsClientTimeSlotAvailable_WithNullEndDateTime() {
        Long clientId = 1L;
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(1);
        
        Appointment appointmentWithNullDuration = new Appointment();
        appointmentWithNullDuration.setAppointmentDateTime(startTime.plusMinutes(30));
        appointmentWithNullDuration.setServiceDurationMinutes(null); // This will cause getEndDateTime() to return null
        
        when(repository.findByClientIdAndAppointmentDateTimeBetween(eq(clientId), any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(Arrays.asList(appointmentWithNullDuration));
        
        boolean result = service.isClientTimeSlotAvailable(clientId, startTime, endTime);
        
        assertFalse(result); // Should detect conflict even with null duration (defaults to 60 min)
        verify(repository).findByClientIdAndAppointmentDateTimeBetween(eq(clientId), any(LocalDateTime.class), any(LocalDateTime.class));
    }
    
    @Test 
    void testFindConflictingAppointments_WithNullEndDateTime() {
        Long professionalId = 1L;
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(1);
        
        Appointment appointmentWithNullDuration = new Appointment();
        appointmentWithNullDuration.setAppointmentDateTime(startTime.plusMinutes(30));
        appointmentWithNullDuration.setServiceDurationMinutes(null);
        
        when(repository.findPotentialConflictingAppointments(any(), any(), any()))
            .thenReturn(Arrays.asList(appointmentWithNullDuration));
        
        boolean result = service.isTimeSlotAvailable(professionalId, startTime, endTime);
        
        assertFalse(result); // Should detect conflict even with null duration (defaults to 60 min)
    }
    
    @Test
    void testReschedule_ExcludesSelfFromConflicts() {
        Long appointmentId = 1L;
        LocalDateTime newDateTime = LocalDateTime.now().plusHours(3);
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setProfessionalId(2L);
        appointment.setServiceDurationMinutes(60);
        appointment.setAppointmentDateTime(LocalDateTime.now().plusHours(1));
        
        // Mock the same appointment as a potential conflict (should be excluded)
        Appointment selfConflict = new Appointment();
        selfConflict.setId(appointmentId); // Same ID - should be excluded
        selfConflict.setAppointmentDateTime(newDateTime);
        selfConflict.setServiceDurationMinutes(60);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(repository.findPotentialConflictingAppointments(any(), any(), any()))
            .thenReturn(Arrays.asList(selfConflict));
        when(repository.save(any(Appointment.class))).thenReturn(appointment);
        
        Appointment result = service.reschedule(appointmentId, newDateTime);
        
        assertNotNull(result); // Should succeed as self-conflict is excluded
        verify(repository).findById(appointmentId);
        verify(repository).save(appointment);
    }
    
    @Test
    void testCreateAppointment_DefaultDurationUsed() {
        LocalDateTime appointmentDateTime = LocalDateTime.now().plusHours(2);
        Long establishmentId = 4L;
        
        // Mock professional lookup
        Professional mockProfessional = new Professional();
        mockProfessional.setId(2L);
        mockProfessional.setEstablishmentId(establishmentId);
        when(professionalService.findById(2L)).thenReturn(Optional.of(mockProfessional));
        
        // Mock service lookup
        com.slotfy.model.Service mockService = new com.slotfy.model.Service();
        mockService.setId(3L);
        mockService.setEstablishmentId(establishmentId);
        when(serviceService.findById(3L)).thenReturn(Optional.of(mockService));
        
        // Mock an appointment that would conflict if default duration (60 min) is used
        Appointment existingAppointment = new Appointment();
        existingAppointment.setAppointmentDateTime(appointmentDateTime.plusMinutes(30));
        existingAppointment.setServiceDurationMinutes(60);
        
        when(repository.findPotentialConflictingAppointments(any(), any(), any()))
            .thenReturn(Arrays.asList(existingAppointment));
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createAppointment(1L, 2L, 3L, establishmentId, appointmentDateTime, 
                    "notes", "client", "professional", "service", null, new BigDecimal("50.00"));
        });
        assertEquals("Já existe um agendamento para este profissional neste horário", exception.getMessage());
    }
    
    @Test
    void testIsTimeSlotAvailable_EdgeOverlap() {
        Long professionalId = 1L;
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(1);
        
        // Create an appointment that ends exactly when the new one starts (no overlap)
        Appointment noOverlapAppointment = new Appointment();
        noOverlapAppointment.setAppointmentDateTime(startTime.minusHours(1));
        noOverlapAppointment.setServiceDurationMinutes(60);
        
        when(repository.findPotentialConflictingAppointments(any(), any(), any()))
            .thenReturn(Arrays.asList(noOverlapAppointment));
        
        boolean result = service.isTimeSlotAvailable(professionalId, startTime, endTime);
        
        assertTrue(result); // Should be available as appointments don't overlap
    }
    
    @Test
    void testIsTimeSlotAvailable_ExactOverlap() {
        Long professionalId = 1L;
        LocalDateTime startTime = LocalDateTime.now().plusHours(1);
        LocalDateTime endTime = startTime.plusHours(1);
        
        // Create an appointment with exact same time slot
        Appointment exactOverlapAppointment = new Appointment();
        exactOverlapAppointment.setAppointmentDateTime(startTime);
        exactOverlapAppointment.setServiceDurationMinutes(60);
        
        when(repository.findPotentialConflictingAppointments(any(), any(), any()))
            .thenReturn(Arrays.asList(exactOverlapAppointment));
        
        boolean result = service.isTimeSlotAvailable(professionalId, startTime, endTime);
        
        assertFalse(result); // Should detect exact overlap
    }
    
    // Test multi-establishment data isolation methods
    @Test
    void testValidateAppointmentBelongsToEstablishment_Success() {
        Long appointmentId = 1L;
        Long establishmentId = 4L;
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setEstablishmentId(establishmentId);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        
        // Should not throw exception
        assertDoesNotThrow(() -> {
            service.validateAppointmentBelongsToEstablishment(appointmentId, establishmentId);
        });
        
        verify(repository).findById(appointmentId);
    }
    
    @Test
    void testValidateAppointmentBelongsToEstablishment_NotFound() {
        Long appointmentId = 1L;
        Long establishmentId = 4L;
        
        when(repository.findById(appointmentId)).thenReturn(Optional.empty());
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.validateAppointmentBelongsToEstablishment(appointmentId, establishmentId);
        });
        assertEquals("Agendamento não encontrado", exception.getMessage());
    }
    
    @Test
    void testValidateAppointmentBelongsToEstablishment_WrongEstablishment() {
        Long appointmentId = 1L;
        Long requestedEstablishmentId = 4L;
        Long actualEstablishmentId = 5L;
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setEstablishmentId(actualEstablishmentId);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        
        Exception exception = assertThrows(SecurityException.class, () -> {
            service.validateAppointmentBelongsToEstablishment(appointmentId, requestedEstablishmentId);
        });
        assertEquals("Acesso negado: agendamento não pertence ao estabelecimento", exception.getMessage());
    }
    
    @Test
    void testFindByIdAndEstablishment_Success() {
        Long appointmentId = 1L;
        Long establishmentId = 4L;
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setEstablishmentId(establishmentId);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        
        Optional<Appointment> result = service.findByIdAndEstablishment(appointmentId, establishmentId);
        
        assertTrue(result.isPresent());
        assertEquals(appointmentId, result.get().getId());
    }
    
    @Test
    void testFindByIdAndEstablishment_NotFound() {
        Long appointmentId = 1L;
        Long establishmentId = 4L;
        
        when(repository.findById(appointmentId)).thenReturn(Optional.empty());
        
        Optional<Appointment> result = service.findByIdAndEstablishment(appointmentId, establishmentId);
        
        assertFalse(result.isPresent());
    }
    
    @Test
    void testFindByIdAndEstablishment_WrongEstablishment() {
        Long appointmentId = 1L;
        Long requestedEstablishmentId = 4L;
        Long actualEstablishmentId = 5L;
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setEstablishmentId(actualEstablishmentId);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        
        Optional<Appointment> result = service.findByIdAndEstablishment(appointmentId, requestedEstablishmentId);
        
        assertFalse(result.isPresent()); // Returns empty instead of throwing exception for GET operations
    }
    
    // Test updateStatus with establishment validation
    @Test
    void testUpdateStatus_WithEstablishmentValidation_Success() {
        Long appointmentId = 1L;
        Long establishmentId = 4L;
        AppointmentStatus newStatus = AppointmentStatus.CONFIRMED;
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setEstablishmentId(establishmentId);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(repository.save(any(Appointment.class))).thenReturn(appointment);
        
        Appointment result = service.updateStatus(appointmentId, newStatus, establishmentId);
        
        assertNotNull(result);
        verify(repository, times(2)).findById(appointmentId); // Once for validation, once for update
        verify(repository).save(appointment);
    }
    
    @Test
    void testUpdateStatus_WithEstablishmentValidation_WrongEstablishment() {
        Long appointmentId = 1L;
        Long requestedEstablishmentId = 4L;
        Long actualEstablishmentId = 5L;
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setEstablishmentId(actualEstablishmentId);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        
        Exception exception = assertThrows(SecurityException.class, () -> {
            service.updateStatus(appointmentId, AppointmentStatus.CONFIRMED, requestedEstablishmentId);
        });
        assertEquals("Acesso negado: agendamento não pertence ao estabelecimento", exception.getMessage());
    }
    
    // Test reschedule with establishment validation
    @Test
    void testReschedule_WithEstablishmentValidation_Success() {
        Long appointmentId = 1L;
        Long establishmentId = 4L;
        LocalDateTime newDateTime = LocalDateTime.now().plusHours(3);
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setEstablishmentId(establishmentId);
        appointment.setProfessionalId(2L);
        appointment.setServiceDurationMinutes(60);
        appointment.setAppointmentDateTime(LocalDateTime.now().plusHours(1));
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(repository.findPotentialConflictingAppointments(any(), any(), any()))
            .thenReturn(Collections.emptyList());
        when(repository.save(any(Appointment.class))).thenReturn(appointment);
        
        Appointment result = service.reschedule(appointmentId, newDateTime, establishmentId);
        
        assertNotNull(result);
    }
    
    @Test
    void testReschedule_WithEstablishmentValidation_WrongEstablishment() {
        Long appointmentId = 1L;
        Long requestedEstablishmentId = 4L;
        Long actualEstablishmentId = 5L;
        LocalDateTime newDateTime = LocalDateTime.now().plusHours(3);
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setEstablishmentId(actualEstablishmentId);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        
        Exception exception = assertThrows(SecurityException.class, () -> {
            service.reschedule(appointmentId, newDateTime, requestedEstablishmentId);
        });
        assertEquals("Acesso negado: agendamento não pertence ao estabelecimento", exception.getMessage());
    }
    
    // Test updateNotes with establishment validation
    @Test
    void testUpdateNotes_WithEstablishmentValidation_Success() {
        Long appointmentId = 1L;
        Long establishmentId = 4L;
        String newNotes = "Updated notes";
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setEstablishmentId(establishmentId);
        appointment.setNotes("Old notes");
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(repository.save(any(Appointment.class))).thenReturn(appointment);
        
        Appointment result = service.updateNotes(appointmentId, newNotes, establishmentId);
        
        assertNotNull(result);
    }
    
    @Test
    void testUpdateNotes_WithEstablishmentValidation_WrongEstablishment() {
        Long appointmentId = 1L;
        Long requestedEstablishmentId = 4L;
        Long actualEstablishmentId = 5L;
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setEstablishmentId(actualEstablishmentId);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        
        Exception exception = assertThrows(SecurityException.class, () -> {
            service.updateNotes(appointmentId, "New notes", requestedEstablishmentId);
        });
        assertEquals("Acesso negado: agendamento não pertence ao estabelecimento", exception.getMessage());
    }
    
    // Test cancelClientAppointment
    @Test
    void testCancelClientAppointment_Success() {
        Long appointmentId = 1L;
        Long clientId = 2L;
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setClientId(clientId);
        appointment.setStatus(AppointmentStatus.SCHEDULED);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        when(repository.save(any(Appointment.class))).thenReturn(appointment);
        
        Appointment result = service.cancelClientAppointment(appointmentId, clientId);
        
        assertNotNull(result);
        verify(repository).save(appointment);
    }
    
    @Test
    void testCancelClientAppointment_NotFound() {
        Long appointmentId = 1L;
        Long clientId = 2L;
        
        when(repository.findById(appointmentId)).thenReturn(Optional.empty());
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.cancelClientAppointment(appointmentId, clientId);
        });
        assertEquals("Agendamento não encontrado", exception.getMessage());
    }
    
    @Test
    void testCancelClientAppointment_WrongClient() {
        Long appointmentId = 1L;
        Long requestedClientId = 2L;
        Long actualClientId = 3L;
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setClientId(actualClientId);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        
        Exception exception = assertThrows(SecurityException.class, () -> {
            service.cancelClientAppointment(appointmentId, requestedClientId);
        });
        assertEquals("Você não tem permissão para cancelar este agendamento", exception.getMessage());
    }
    
    @Test
    void testCancelClientAppointment_AlreadyCancelled() {
        Long appointmentId = 1L;
        Long clientId = 2L;
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setClientId(clientId);
        appointment.setStatus(AppointmentStatus.CANCELLED);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.cancelClientAppointment(appointmentId, clientId);
        });
        assertEquals("Este agendamento já está cancelado", exception.getMessage());
    }
    
    @Test
    void testCancelClientAppointment_AlreadyCompleted() {
        Long appointmentId = 1L;
        Long clientId = 2L;
        
        Appointment appointment = new Appointment();
        appointment.setId(appointmentId);
        appointment.setClientId(clientId);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        
        when(repository.findById(appointmentId)).thenReturn(Optional.of(appointment));
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.cancelClientAppointment(appointmentId, clientId);
        });
        assertEquals("Não é possível cancelar um agendamento já finalizado", exception.getMessage());
    }
    
    // Test getAppointmentsNeedingReminders
    @Test
    void testGetAppointmentsNeedingReminders() {
        LocalDateTime now = LocalDateTime.now();
        
        // Create appointments with various conditions
        Appointment reminderNeeded = new Appointment();
        reminderNeeded.setAppointmentDateTime(now.plusHours(12)); // Within 24 hour window
        reminderNeeded.setStatus(AppointmentStatus.SCHEDULED);
        
        Appointment confirmed = new Appointment();
        confirmed.setAppointmentDateTime(now.plusHours(20));
        confirmed.setStatus(AppointmentStatus.CONFIRMED);
        
        Appointment tooFarAway = new Appointment();
        tooFarAway.setAppointmentDateTime(now.plusHours(48)); // Beyond 24 hour window
        tooFarAway.setStatus(AppointmentStatus.SCHEDULED);
        
        Appointment alreadyPassed = new Appointment();
        alreadyPassed.setAppointmentDateTime(now.minusHours(1)); // In the past
        alreadyPassed.setStatus(AppointmentStatus.SCHEDULED);
        
        Appointment cancelled = new Appointment();
        cancelled.setAppointmentDateTime(now.plusHours(12));
        cancelled.setStatus(AppointmentStatus.CANCELLED);
        
        when(repository.findAll()).thenReturn(Arrays.asList(
            reminderNeeded, confirmed, tooFarAway, alreadyPassed, cancelled
        ));
        
        List<Appointment> result = service.getAppointmentsNeedingReminders();
        
        // Should include reminderNeeded and confirmed, exclude others
        assertEquals(2, result.size());
        verify(repository).findAll();
    }
    
    // Test createAppointment with service/professional not belonging to establishment
    @Test
    void testCreateAppointment_ProfessionalNotFound() {
        LocalDateTime appointmentDateTime = LocalDateTime.now().plusHours(2);
        Long establishmentId = 4L;
        
        when(professionalService.findById(2L)).thenReturn(Optional.empty());
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createAppointment(1L, 2L, 3L, establishmentId, appointmentDateTime, 
                    "notes", "client", "professional", "service", 60, new BigDecimal("50.00"));
        });
        assertEquals("Profissional não encontrado", exception.getMessage());
    }
    
    @Test
    void testCreateAppointment_ProfessionalWrongEstablishment() {
        LocalDateTime appointmentDateTime = LocalDateTime.now().plusHours(2);
        Long establishmentId = 4L;
        Long differentEstablishmentId = 5L;
        
        Professional mockProfessional = new Professional();
        mockProfessional.setId(2L);
        mockProfessional.setEstablishmentId(differentEstablishmentId);
        when(professionalService.findById(2L)).thenReturn(Optional.of(mockProfessional));
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createAppointment(1L, 2L, 3L, establishmentId, appointmentDateTime, 
                    "notes", "client", "professional", "service", 60, new BigDecimal("50.00"));
        });
        assertEquals("Profissional não pertence ao estabelecimento selecionado", exception.getMessage());
    }
    
    @Test
    void testCreateAppointment_ServiceNotFound() {
        LocalDateTime appointmentDateTime = LocalDateTime.now().plusHours(2);
        Long establishmentId = 4L;
        
        Professional mockProfessional = new Professional();
        mockProfessional.setId(2L);
        mockProfessional.setEstablishmentId(establishmentId);
        when(professionalService.findById(2L)).thenReturn(Optional.of(mockProfessional));
        
        when(serviceService.findById(3L)).thenReturn(Optional.empty());
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createAppointment(1L, 2L, 3L, establishmentId, appointmentDateTime, 
                    "notes", "client", "professional", "service", 60, new BigDecimal("50.00"));
        });
        assertEquals("Serviço não encontrado", exception.getMessage());
    }
    
    @Test
    void testCreateAppointment_ServiceWrongEstablishment() {
        LocalDateTime appointmentDateTime = LocalDateTime.now().plusHours(2);
        Long establishmentId = 4L;
        Long differentEstablishmentId = 5L;
        
        Professional mockProfessional = new Professional();
        mockProfessional.setId(2L);
        mockProfessional.setEstablishmentId(establishmentId);
        when(professionalService.findById(2L)).thenReturn(Optional.of(mockProfessional));
        
        com.slotfy.model.Service mockService = new com.slotfy.model.Service();
        mockService.setId(3L);
        mockService.setEstablishmentId(differentEstablishmentId);
        when(serviceService.findById(3L)).thenReturn(Optional.of(mockService));
        
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            service.createAppointment(1L, 2L, 3L, establishmentId, appointmentDateTime, 
                    "notes", "client", "professional", "service", 60, new BigDecimal("50.00"));
        });
        assertEquals("Serviço não pertence ao estabelecimento selecionado", exception.getMessage());
    }
}