package com.slotfy.service;

import com.slotfy.model.Appointment;
import com.slotfy.model.AppointmentStatus;
import com.slotfy.repository.AppointmentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Test to verify AppointmentService compilation and functionality after fixing JPQL queries
 */
public class AppointmentServiceTest {

    @Mock
    private AppointmentRepository repository;

    private AppointmentService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new AppointmentService(repository);
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
}