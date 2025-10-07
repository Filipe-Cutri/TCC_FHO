package com.slotfy.service;

import com.slotfy.model.*;
import com.slotfy.repository.AppointmentRepository;
import com.slotfy.repository.ProfessionalRepository;
import com.slotfy.repository.ServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests to ensure multi-establishment data isolation
 * Critical security requirement: users from one establishment should NEVER
 * be able to access data from another establishment
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class EstablishmentIsolationTest {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private ProfessionalService professionalService;
    
    @Autowired
    private ServiceService serviceService;
    
    @Autowired
    private AppointmentRepository appointmentRepository;
    
    @Autowired
    private ProfessionalRepository professionalRepository;
    
    @Autowired
    private ServiceRepository serviceRepository;
    
    private Long establishment1Id = 1L;
    private Long establishment2Id = 2L;
    
    private Appointment appointment1;
    private Appointment appointment2;
    private Professional professional1;
    private Professional professional2;
    private Service service1;
    private Service service2;
    
    @BeforeEach
    void setUp() {
        // Create test data for establishment 1
        professional1 = new Professional("Professional 1", "prof1@test.com", "1111111111", "Corte", establishment1Id);
        professional1 = professionalRepository.save(professional1);
        
        service1 = new Service("Service 1", "Description 1", 30, new BigDecimal("50.00"), establishment1Id);
        service1 = serviceRepository.save(service1);
        
        appointment1 = new Appointment(100L, professional1.getId(), service1.getId(), establishment1Id, LocalDateTime.now().plusDays(1));
        appointment1.setClientName("Client 1");
        appointment1.setProfessionalName("Professional 1");
        appointment1.setServiceName("Service 1");
        appointment1 = appointmentRepository.save(appointment1);
        
        // Create test data for establishment 2
        professional2 = new Professional("Professional 2", "prof2@test.com", "2222222222", "Corte", establishment2Id);
        professional2 = professionalRepository.save(professional2);
        
        service2 = new Service("Service 2", "Description 2", 30, new BigDecimal("60.00"), establishment2Id);
        service2 = serviceRepository.save(service2);
        
        appointment2 = new Appointment(200L, professional2.getId(), service2.getId(), establishment2Id, LocalDateTime.now().plusDays(1));
        appointment2.setClientName("Client 2");
        appointment2.setProfessionalName("Professional 2");
        appointment2.setServiceName("Service 2");
        appointment2 = appointmentRepository.save(appointment2);
    }
    
    // ==================== APPOINTMENT ISOLATION TESTS ====================
    
    @Test
    @DisplayName("Should NOT find appointment from different establishment")
    void testAppointmentIsolation_FindById() {
        // Try to access appointment from establishment 1 using establishment 2's context
        Optional<Appointment> result = appointmentService.findByIdAndEstablishment(appointment1.getId(), establishment2Id);
        
        assertFalse(result.isPresent(), "Should not be able to access appointment from different establishment");
    }
    
    @Test
    @DisplayName("Should find appointment from same establishment")
    void testAppointmentIsolation_FindByIdSuccess() {
        // Access appointment from same establishment
        Optional<Appointment> result = appointmentService.findByIdAndEstablishment(appointment1.getId(), establishment1Id);
        
        assertTrue(result.isPresent(), "Should be able to access appointment from same establishment");
        assertEquals(appointment1.getId(), result.get().getId());
    }
    
    @Test
    @DisplayName("Should throw SecurityException when updating appointment from different establishment")
    void testAppointmentIsolation_UpdateStatus() {
        // Try to update appointment from establishment 1 using establishment 2's context
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            appointmentService.updateStatus(appointment1.getId(), AppointmentStatus.CONFIRMED, establishment2Id);
        });
        
        assertTrue(exception.getMessage().contains("Acesso negado"), 
            "Should throw SecurityException with access denied message");
    }
    
    @Test
    @DisplayName("Should successfully update appointment from same establishment")
    void testAppointmentIsolation_UpdateStatusSuccess() {
        // Update appointment from same establishment
        Appointment updated = appointmentService.updateStatus(appointment1.getId(), AppointmentStatus.CONFIRMED, establishment1Id);
        
        assertEquals(AppointmentStatus.CONFIRMED, updated.getStatus());
    }
    
    @Test
    @DisplayName("Should throw SecurityException when rescheduling appointment from different establishment")
    void testAppointmentIsolation_Reschedule() {
        LocalDateTime newDateTime = LocalDateTime.now().plusDays(2);
        
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            appointmentService.reschedule(appointment1.getId(), newDateTime, establishment2Id);
        });
        
        assertTrue(exception.getMessage().contains("Acesso negado"));
    }
    
    @Test
    @DisplayName("Should throw SecurityException when updating notes from different establishment")
    void testAppointmentIsolation_UpdateNotes() {
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            appointmentService.updateNotes(appointment1.getId(), "New notes", establishment2Id);
        });
        
        assertTrue(exception.getMessage().contains("Acesso negado"));
    }
    
    // ==================== PROFESSIONAL ISOLATION TESTS ====================
    
    @Test
    @DisplayName("Should NOT find professional from different establishment")
    void testProfessionalIsolation_FindById() {
        Optional<Professional> result = professionalService.findByIdAndEstablishment(professional1.getId(), establishment2Id);
        
        assertFalse(result.isPresent(), "Should not be able to access professional from different establishment");
    }
    
    @Test
    @DisplayName("Should find professional from same establishment")
    void testProfessionalIsolation_FindByIdSuccess() {
        Optional<Professional> result = professionalService.findByIdAndEstablishment(professional1.getId(), establishment1Id);
        
        assertTrue(result.isPresent(), "Should be able to access professional from same establishment");
        assertEquals(professional1.getId(), result.get().getId());
    }
    
    @Test
    @DisplayName("Should throw SecurityException when updating professional from different establishment")
    void testProfessionalIsolation_Update() {
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            professionalService.updateProfessional(professional1.getId(), "Updated Name", 
                "updated@test.com", "9999999999", "Updated specialties", establishment2Id);
        });
        
        assertTrue(exception.getMessage().contains("Acesso negado"));
    }
    
    @Test
    @DisplayName("Should successfully update professional from same establishment")
    void testProfessionalIsolation_UpdateSuccess() {
        Professional updated = professionalService.updateProfessional(professional1.getId(), "Updated Name",
            "prof1@test.com", "1111111111", "Updated specialties", establishment1Id);
        
        assertEquals("Updated Name", updated.getName());
    }
    
    @Test
    @DisplayName("Should throw SecurityException when updating professional status from different establishment")
    void testProfessionalIsolation_UpdateStatus() {
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            professionalService.updateStatus(professional1.getId(), ProfessionalStatus.INACTIVE, establishment2Id);
        });
        
        assertTrue(exception.getMessage().contains("Acesso negado"));
    }
    
    @Test
    @DisplayName("Should throw SecurityException when deleting professional from different establishment")
    void testProfessionalIsolation_Delete() {
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            professionalService.deleteProfessional(professional1.getId(), establishment2Id);
        });
        
        assertTrue(exception.getMessage().contains("Acesso negado"));
    }
    
    // ==================== SERVICE ISOLATION TESTS ====================
    
    @Test
    @DisplayName("Should NOT find service from different establishment")
    void testServiceIsolation_FindById() {
        Optional<Service> result = serviceService.findByIdAndEstablishment(service1.getId(), establishment2Id);
        
        assertFalse(result.isPresent(), "Should not be able to access service from different establishment");
    }
    
    @Test
    @DisplayName("Should find service from same establishment")
    void testServiceIsolation_FindByIdSuccess() {
        Optional<Service> result = serviceService.findByIdAndEstablishment(service1.getId(), establishment1Id);
        
        assertTrue(result.isPresent(), "Should be able to access service from same establishment");
        assertEquals(service1.getId(), result.get().getId());
    }
    
    @Test
    @DisplayName("Should throw SecurityException when updating service from different establishment")
    void testServiceIsolation_Update() {
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            serviceService.updateService(service1.getId(), "Updated Service", 
                "Updated description", 45, new BigDecimal("75.00"), "Category", establishment2Id);
        });
        
        assertTrue(exception.getMessage().contains("Acesso negado"));
    }
    
    @Test
    @DisplayName("Should successfully update service from same establishment")
    void testServiceIsolation_UpdateSuccess() {
        Service updated = serviceService.updateService(service1.getId(), "Updated Service",
            "Updated description", 45, new BigDecimal("75.00"), "Category", establishment1Id);
        
        assertEquals("Updated Service", updated.getName());
    }
    
    @Test
    @DisplayName("Should throw SecurityException when updating service status from different establishment")
    void testServiceIsolation_UpdateStatus() {
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            serviceService.updateStatus(service1.getId(), ServiceStatus.INACTIVE, establishment2Id);
        });
        
        assertTrue(exception.getMessage().contains("Acesso negado"));
    }
    
    @Test
    @DisplayName("Should throw SecurityException when deleting service from different establishment")
    void testServiceIsolation_Delete() {
        SecurityException exception = assertThrows(SecurityException.class, () -> {
            serviceService.deleteService(service1.getId(), establishment2Id);
        });
        
        assertTrue(exception.getMessage().contains("Acesso negado"));
    }
    
    // ==================== CROSS-ESTABLISHMENT VALIDATION TESTS ====================
    
    @Test
    @DisplayName("Should prevent accessing data across establishments in batch")
    void testCrossEstablishmentAccess() {
        // Verify all establishment 1 data is isolated from establishment 2
        assertFalse(appointmentService.findByIdAndEstablishment(appointment1.getId(), establishment2Id).isPresent());
        assertFalse(professionalService.findByIdAndEstablishment(professional1.getId(), establishment2Id).isPresent());
        assertFalse(serviceService.findByIdAndEstablishment(service1.getId(), establishment2Id).isPresent());
        
        // Verify all establishment 2 data is isolated from establishment 1
        assertFalse(appointmentService.findByIdAndEstablishment(appointment2.getId(), establishment1Id).isPresent());
        assertFalse(professionalService.findByIdAndEstablishment(professional2.getId(), establishment1Id).isPresent());
        assertFalse(serviceService.findByIdAndEstablishment(service2.getId(), establishment1Id).isPresent());
    }
}
