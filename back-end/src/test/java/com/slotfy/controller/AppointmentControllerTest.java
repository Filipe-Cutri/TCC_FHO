package com.slotfy.controller;

import com.slotfy.model.Appointment;
import com.slotfy.model.AppointmentStatus;
import com.slotfy.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentController.class)
@Import({com.slotfy.config.SecurityConfig.class})
public class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @Autowired
    private ObjectMapper objectMapper;

    private Appointment createMockAppointment(Long id, Long establishmentId, AppointmentStatus status) {
        Appointment appointment = new Appointment();
        appointment.setId(id);
        appointment.setClientId(1L);
        appointment.setProfessionalId(1L);
        appointment.setServiceId(1L);
        appointment.setEstablishmentId(establishmentId);
        appointment.setAppointmentDateTime(LocalDateTime.now().plusDays(1));
        appointment.setStatus(status);
        appointment.setServicePrice(new BigDecimal("100.00"));
        appointment.setServiceDurationMinutes(60);
        return appointment;
    }

    @Test
    @WithMockUser
    public void testGetAppointments() throws Exception {
        List<Appointment> appointments = Arrays.asList(
            createMockAppointment(1L, 1L, AppointmentStatus.SCHEDULED),
            createMockAppointment(2L, 1L, AppointmentStatus.CONFIRMED)
        );
        
        when(appointmentService.getByEstablishmentId(1L)).thenReturn(appointments);

        mockMvc.perform(get("/api/establishment/appointments")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testGetAppointmentsError() throws Exception {
        when(appointmentService.getByEstablishmentId(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/appointments")
                        .param("establishmentId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testGetAppointmentsByStatus() throws Exception {
        List<Appointment> appointments = Arrays.asList(
            createMockAppointment(1L, 1L, AppointmentStatus.SCHEDULED)
        );
        
        when(appointmentService.getByEstablishmentAndStatus(1L, AppointmentStatus.SCHEDULED))
                .thenReturn(appointments);

        mockMvc.perform(get("/api/establishment/appointments/status/scheduled")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @WithMockUser
    public void testGetAppointmentsByStatusInvalidStatus() throws Exception {
        mockMvc.perform(get("/api/establishment/appointments/status/invalid")
                        .param("establishmentId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testGetTodayAppointments() throws Exception {
        List<Appointment> appointments = Arrays.asList(
            createMockAppointment(1L, 1L, AppointmentStatus.SCHEDULED)
        );
        
        when(appointmentService.getTodayAppointments(1L)).thenReturn(appointments);

        mockMvc.perform(get("/api/establishment/appointments/today")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @WithMockUser
    public void testGetUpcomingAppointments() throws Exception {
        List<Appointment> appointments = Arrays.asList(
            createMockAppointment(1L, 1L, AppointmentStatus.CONFIRMED)
        );
        
        when(appointmentService.getUpcomingAppointments(1L)).thenReturn(appointments);

        mockMvc.perform(get("/api/establishment/appointments/upcoming")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @WithMockUser
    public void testGetAppointmentsByDateRange() throws Exception {
        List<Appointment> appointments = Arrays.asList(
            createMockAppointment(1L, 1L, AppointmentStatus.SCHEDULED)
        );
        
        LocalDateTime startDate = LocalDateTime.now();
        LocalDateTime endDate = startDate.plusDays(7);
        
        when(appointmentService.getByDateRange(eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(appointments);

        mockMvc.perform(get("/api/establishment/appointments/range")
                        .param("establishmentId", "1")
                        .param("startDate", startDate.toString())
                        .param("endDate", endDate.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @WithMockUser
    public void testGetAppointmentsByProfessional() throws Exception {
        List<Appointment> appointments = Arrays.asList(
            createMockAppointment(1L, 1L, AppointmentStatus.SCHEDULED)
        );
        
        when(appointmentService.getByProfessional(1L)).thenReturn(appointments);

        mockMvc.perform(get("/api/establishment/appointments/professional/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @WithMockUser
    public void testGetAppointment() throws Exception {
        Appointment appointment = createMockAppointment(1L, 1L, AppointmentStatus.SCHEDULED);
        
        when(appointmentService.findByIdAndEstablishment(1L, 1L))
                .thenReturn(Optional.of(appointment));

        mockMvc.perform(get("/api/establishment/appointments/1")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser
    public void testGetAppointmentNotFound() throws Exception {
        when(appointmentService.findByIdAndEstablishment(1L, 1L))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/establishment/appointments/1")
                        .param("establishmentId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testCreateAppointment() throws Exception {
        Appointment appointment = createMockAppointment(1L, 1L, AppointmentStatus.SCHEDULED);
        
        when(appointmentService.createAppointment(
                anyLong(), anyLong(), anyLong(), anyLong(), any(LocalDateTime.class),
                any(), any(), any(), any(), any(), any()))
                .thenReturn(appointment);

        Map<String, Object> request = new HashMap<>();
        request.put("clientId", 1L);
        request.put("professionalId", 1L);
        request.put("serviceId", 1L);
        request.put("establishmentId", 1L);
        request.put("appointmentDateTime", LocalDateTime.now().plusDays(1).toString());
        request.put("notes", "Test appointment");
        request.put("serviceDurationMinutes", 60);
        request.put("servicePrice", "100.00");

        mockMvc.perform(post("/api/establishment/appointments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser
    public void testCreateAppointmentMissingFields() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("clientId", 1L);
        // Missing required fields

        mockMvc.perform(post("/api/establishment/appointments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateStatus() throws Exception {
        Appointment appointment = createMockAppointment(1L, 1L, AppointmentStatus.CONFIRMED);
        
        when(appointmentService.updateStatus(1L, AppointmentStatus.CONFIRMED, 1L))
                .thenReturn(appointment);

        Map<String, String> request = new HashMap<>();
        request.put("status", "confirmed");

        mockMvc.perform(put("/api/establishment/appointments/1/status")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    public void testUpdateStatusMissingStatus() throws Exception {
        Map<String, String> request = new HashMap<>();

        mockMvc.perform(put("/api/establishment/appointments/1/status")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateStatusSecurityException() throws Exception {
        when(appointmentService.updateStatus(1L, AppointmentStatus.CONFIRMED, 1L))
                .thenThrow(new SecurityException("Access denied"));

        Map<String, String> request = new HashMap<>();
        request.put("status", "confirmed");

        mockMvc.perform(put("/api/establishment/appointments/1/status")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testReschedule() throws Exception {
        Appointment appointment = createMockAppointment(1L, 1L, AppointmentStatus.SCHEDULED);
        
        when(appointmentService.reschedule(eq(1L), any(LocalDateTime.class), eq(1L)))
                .thenReturn(appointment);

        Map<String, String> request = new HashMap<>();
        request.put("newDateTime", LocalDateTime.now().plusDays(2).toString());

        mockMvc.perform(put("/api/establishment/appointments/1/reschedule")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    public void testUpdateNotes() throws Exception {
        Appointment appointment = createMockAppointment(1L, 1L, AppointmentStatus.SCHEDULED);
        
        when(appointmentService.updateNotes(1L, "New notes", 1L))
                .thenReturn(appointment);

        Map<String, String> request = new HashMap<>();
        request.put("notes", "New notes");

        mockMvc.perform(put("/api/establishment/appointments/1/notes")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    public void testCancelAppointment() throws Exception {
        Appointment appointment = createMockAppointment(1L, 1L, AppointmentStatus.CANCELLED);
        
        when(appointmentService.updateStatus(1L, AppointmentStatus.CANCELLED, 1L))
                .thenReturn(appointment);

        mockMvc.perform(put("/api/establishment/appointments/1/cancel")
                        .with(csrf())
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    public void testConfirmAppointment() throws Exception {
        Appointment appointment = createMockAppointment(1L, 1L, AppointmentStatus.CONFIRMED);
        
        when(appointmentService.updateStatus(1L, AppointmentStatus.CONFIRMED, 1L))
                .thenReturn(appointment);

        mockMvc.perform(put("/api/establishment/appointments/1/confirm")
                        .with(csrf())
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    public void testCompleteAppointment() throws Exception {
        Appointment appointment = createMockAppointment(1L, 1L, AppointmentStatus.COMPLETED);
        
        when(appointmentService.updateStatus(1L, AppointmentStatus.COMPLETED, 1L))
                .thenReturn(appointment);

        mockMvc.perform(put("/api/establishment/appointments/1/complete")
                        .with(csrf())
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    public void testCheckAvailability() throws Exception {
        when(appointmentService.isTimeSlotAvailable(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(true);

        LocalDateTime startTime = LocalDateTime.now().plusDays(1);
        LocalDateTime endTime = startTime.plusHours(1);

        mockMvc.perform(get("/api/establishment/appointments/availability")
                        .param("professionalId", "1")
                        .param("startTime", startTime.toString())
                        .param("endTime", endTime.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.available").value(true));
    }

    @Test
    @WithMockUser
    public void testGetStatistics() throws Exception {
        when(appointmentService.countByEstablishment(1L)).thenReturn(100L);
        when(appointmentService.countTodayAppointments(1L)).thenReturn(5L);
        when(appointmentService.countThisMonthAppointments(1L)).thenReturn(30L);
        when(appointmentService.calculateMonthlyRevenue(1L))
                .thenReturn(new BigDecimal("5000.00"));

        mockMvc.perform(get("/api/establishment/appointments/statistics")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(100))
                .andExpect(jsonPath("$.data.today").value(5))
                .andExpect(jsonPath("$.data.thisMonth").value(30));
    }

    @Test
    @WithMockUser
    public void testGetPerformanceStats() throws Exception {
        List<Object[]> performanceStats = new ArrayList<>();
        
        when(appointmentService.getProfessionalPerformanceStats(1L))
                .thenReturn(performanceStats);

        mockMvc.perform(get("/api/establishment/appointments/performance")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testGetClientHistory() throws Exception {
        List<Appointment> appointments = Arrays.asList(
            createMockAppointment(1L, 1L, AppointmentStatus.COMPLETED)
        );
        
        when(appointmentService.getClientAppointmentHistory(1L))
                .thenReturn(appointments);

        mockMvc.perform(get("/api/establishment/appointments/client/1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @WithMockUser
    public void testGetClientUpcoming() throws Exception {
        List<Appointment> appointments = Arrays.asList(
            createMockAppointment(1L, 1L, AppointmentStatus.CONFIRMED)
        );
        
        when(appointmentService.getClientUpcomingAppointments(1L))
                .thenReturn(appointments);

        mockMvc.perform(get("/api/establishment/appointments/client/1/upcoming"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1));
    }
}
