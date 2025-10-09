package com.slotfy.service;

import com.slotfy.dto.AIRecommendationRequest;
import com.slotfy.dto.AIRecommendationResponse;
import com.slotfy.model.*;
import com.slotfy.repository.AppointmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AISchedulingService
 */
@ExtendWith(MockitoExtension.class)
public class AISchedulingServiceTest {
    
    @Mock
    private ServiceService serviceService;
    
    @Mock
    private ProfessionalService professionalService;
    
    @Mock
    private EstablishmentService establishmentService;
    
    @Mock
    private AppointmentService appointmentService;
    
    @Mock
    private AppointmentRepository appointmentRepository;
    
    @InjectMocks
    private AISchedulingService aiSchedulingService;
    
    private AIRecommendationRequest request;
    private Establishment establishment;
    private List<Service> services;
    private List<Professional> professionals;
    
    @BeforeEach
    void setUp() {
        // Setup request
        request = new AIRecommendationRequest();
        request.setClientId(1L);
        request.setEstablishmentId(1L);
        
        AIRecommendationRequest.ClientPreferences preferences = new AIRecommendationRequest.ClientPreferences();
        preferences.setPreferredTimes(new String[]{"afternoon"});
        preferences.setBudget("medium");
        request.setPreferences(preferences);
        
        // Setup establishment
        establishment = new Establishment();
        establishment.setId(1L);
        establishment.setName("Barbearia Premium");
        establishment.setCategory("Barbearia");
        
        // Setup services
        services = new ArrayList<>();
        Service service1 = new Service("Corte Masculino", "Corte tradicional", 30, new BigDecimal("40.00"), 1L);
        service1.setId(1L);
        service1.setStatus(ServiceStatus.ACTIVE);
        services.add(service1);
        
        Service service2 = new Service("Corte + Barba", "Combo completo", 60, new BigDecimal("65.00"), 1L);
        service2.setId(2L);
        service2.setStatus(ServiceStatus.ACTIVE);
        services.add(service2);
        
        // Setup professionals
        professionals = new ArrayList<>();
        Professional prof1 = new Professional("João Silva", "joao@example.com", "11999999999", "Corte masculino", 1L);
        prof1.setId(1L);
        prof1.setRating(new BigDecimal("4.8"));
        prof1.setSatisfactionRate(new BigDecimal("95.5"));
        prof1.setStatus(ProfessionalStatus.ACTIVE);
        professionals.add(prof1);
        
        Professional prof2 = new Professional("Carlos Santos", "carlos@example.com", "11988888888", "Barba", 1L);
        prof2.setId(2L);
        prof2.setRating(new BigDecimal("4.5"));
        prof2.setSatisfactionRate(new BigDecimal("90.0"));
        prof2.setStatus(ProfessionalStatus.ACTIVE);
        professionals.add(prof2);
    }
    
    @Test
    void testGenerateRecommendations_Success() {
        // Arrange
        when(serviceService.getActiveByEstablishmentId(anyLong())).thenReturn(services);
        when(professionalService.getActiveByEstablishmentId(anyLong())).thenReturn(professionals);
        when(establishmentService.findById(anyLong())).thenReturn(Optional.of(establishment));
        when(appointmentService.getByClient(anyLong())).thenReturn(new ArrayList<>());
        when(appointmentService.isTimeSlotAvailable(anyLong(), any(), any())).thenReturn(true);
        
        // Act
        List<AIRecommendationResponse> recommendations = aiSchedulingService.generateRecommendations(request);
        
        // Assert
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        assertTrue(recommendations.size() <= 5, "Should return at most 5 recommendations");
        
        // Verify first recommendation
        AIRecommendationResponse firstRec = recommendations.get(0);
        assertNotNull(firstRec.getService());
        assertNotNull(firstRec.getProfessional());
        assertNotNull(firstRec.getEstablishment());
        assertNotNull(firstRec.getDate());
        assertNotNull(firstRec.getTime());
        assertNotNull(firstRec.getPrice());
        assertNotNull(firstRec.getConfidence());
        assertNotNull(firstRec.getReason());
        
        // Verify confidence score is within valid range
        assertTrue(firstRec.getConfidence() >= 0 && firstRec.getConfidence() <= 100);
    }
    
    @Test
    void testGenerateRecommendations_NoServices() {
        // Arrange
        when(serviceService.getActiveByEstablishmentId(anyLong())).thenReturn(new ArrayList<>());
        
        // Act
        List<AIRecommendationResponse> recommendations = aiSchedulingService.generateRecommendations(request);
        
        // Assert
        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty(), "Should return empty list when no services available");
    }
    
    @Test
    void testGenerateRecommendations_NoProfessionals() {
        // Arrange
        when(serviceService.getActiveByEstablishmentId(anyLong())).thenReturn(services);
        when(professionalService.getActiveByEstablishmentId(anyLong())).thenReturn(new ArrayList<>());
        
        // Act
        List<AIRecommendationResponse> recommendations = aiSchedulingService.generateRecommendations(request);
        
        // Assert
        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty(), "Should return empty list when no professionals available");
    }
    
    @Test
    void testGenerateRecommendations_NoEstablishment() {
        // Arrange
        when(serviceService.getActiveByEstablishmentId(anyLong())).thenReturn(services);
        when(professionalService.getActiveByEstablishmentId(anyLong())).thenReturn(professionals);
        when(establishmentService.findById(anyLong())).thenReturn(Optional.empty());
        
        // Act
        List<AIRecommendationResponse> recommendations = aiSchedulingService.generateRecommendations(request);
        
        // Assert
        assertNotNull(recommendations);
        assertTrue(recommendations.isEmpty(), "Should return empty list when establishment not found");
    }
    
    @Test
    void testGenerateRecommendations_WithClientHistory() {
        // Arrange
        List<Appointment> clientHistory = new ArrayList<>();
        Appointment pastAppointment = new Appointment();
        pastAppointment.setServiceId(1L); // Had service 1 before
        clientHistory.add(pastAppointment);
        
        when(serviceService.getActiveByEstablishmentId(anyLong())).thenReturn(services);
        when(professionalService.getActiveByEstablishmentId(anyLong())).thenReturn(professionals);
        when(establishmentService.findById(anyLong())).thenReturn(Optional.of(establishment));
        when(appointmentService.getByClient(anyLong())).thenReturn(clientHistory);
        when(appointmentService.isTimeSlotAvailable(anyLong(), any(), any())).thenReturn(true);
        
        // Act
        List<AIRecommendationResponse> recommendations = aiSchedulingService.generateRecommendations(request);
        
        // Assert
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        
        // Verify that recommendations with past service have higher confidence
        boolean hasHistoryMatch = recommendations.stream()
            .anyMatch(r -> r.getReason().contains("já utilizou este serviço"));
        
        assertTrue(hasHistoryMatch || recommendations.isEmpty(), 
            "Should prioritize services from client history");
    }
    
    @Test
    void testGenerateRecommendations_HighConfidenceWithGoodProfessional() {
        // Arrange
        when(serviceService.getActiveByEstablishmentId(anyLong())).thenReturn(services);
        when(professionalService.getActiveByEstablishmentId(anyLong())).thenReturn(professionals);
        when(establishmentService.findById(anyLong())).thenReturn(Optional.of(establishment));
        when(appointmentService.getByClient(anyLong())).thenReturn(new ArrayList<>());
        when(appointmentService.isTimeSlotAvailable(anyLong(), any(), any())).thenReturn(true);
        
        // Act
        List<AIRecommendationResponse> recommendations = aiSchedulingService.generateRecommendations(request);
        
        // Assert
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        
        // First professional has rating 4.8, should get high confidence
        AIRecommendationResponse topRec = recommendations.get(0);
        assertTrue(topRec.getConfidence() >= 50, 
            "High-rated professional should have confidence >= 50");
    }
    
    @Test
    void testGenerateRecommendations_AfternoonPreference() {
        // Arrange
        when(serviceService.getActiveByEstablishmentId(anyLong())).thenReturn(services);
        when(professionalService.getActiveByEstablishmentId(anyLong())).thenReturn(professionals);
        when(establishmentService.findById(anyLong())).thenReturn(Optional.of(establishment));
        when(appointmentService.getByClient(anyLong())).thenReturn(new ArrayList<>());
        when(appointmentService.isTimeSlotAvailable(anyLong(), any(), any())).thenReturn(true);
        
        // Act
        List<AIRecommendationResponse> recommendations = aiSchedulingService.generateRecommendations(request);
        
        // Assert
        assertNotNull(recommendations);
        assertFalse(recommendations.isEmpty());
        
        // Verify that recommended times are in afternoon range (14-16)
        boolean hasAfternoonTime = recommendations.stream()
            .anyMatch(r -> {
                String time = r.getTime();
                int hour = Integer.parseInt(time.split(":")[0]);
                return hour >= 14 && hour < 17;
            });
        
        assertTrue(hasAfternoonTime, "Should include afternoon time slots based on preference");
    }
}
