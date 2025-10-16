package com.slotfy.controller;

import com.slotfy.model.AppointmentStatus;
import com.slotfy.service.AppointmentService;
import com.slotfy.service.ProfessionalService;
import com.slotfy.service.ServiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DashboardController.class)
@Import({com.slotfy.config.SecurityConfig.class})
public class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private ProfessionalService professionalService;

    @MockBean
    private ServiceService serviceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testGetDashboardOverview() throws Exception {
        when(appointmentService.countByEstablishment(anyLong())).thenReturn(100L);
        when(appointmentService.countTodayAppointments(anyLong())).thenReturn(5L);
        when(appointmentService.countThisMonthAppointments(anyLong())).thenReturn(30L);
        when(appointmentService.calculateMonthlyRevenue(anyLong())).thenReturn(new BigDecimal("3000.00"));
        when(professionalService.countByEstablishment(anyLong())).thenReturn(10L);
        when(professionalService.countActiveByEstablishment(anyLong())).thenReturn(8L);
        when(serviceService.countByEstablishment(anyLong())).thenReturn(20L);
        when(serviceService.countActiveByEstablishment(anyLong())).thenReturn(18L);

        mockMvc.perform(get("/api/establishment/dashboard/overview")
                .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.appointments.total").value(100))
                .andExpect(jsonPath("$.data.appointments.today").value(5))
                .andExpect(jsonPath("$.data.appointments.thisMonth").value(30))
                .andExpect(jsonPath("$.data.professionals.total").value(10))
                .andExpect(jsonPath("$.data.professionals.active").value(8))
                .andExpect(jsonPath("$.data.services.total").value(20))
                .andExpect(jsonPath("$.data.services.active").value(18));
    }

    @Test
    @WithMockUser
    public void testGetDashboardOverviewException() throws Exception {
        when(appointmentService.countByEstablishment(anyLong())).thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/dashboard/overview")
                .param("establishmentId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Erro interno do servidor"));
    }

    @Test
    @WithMockUser
    public void testGetTodayAppointments() throws Exception {
        when(appointmentService.getTodayAppointments(anyLong())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/establishment/dashboard/today-appointments")
                .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    @WithMockUser
    public void testGetTodayAppointmentsException() throws Exception {
        when(appointmentService.getTodayAppointments(anyLong())).thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/establishment/dashboard/today-appointments")
                .param("establishmentId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testGetUpcomingAppointments() throws Exception {
        when(appointmentService.getUpcomingAppointments(anyLong())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/establishment/dashboard/upcoming-appointments")
                .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(0))
                .andExpect(jsonPath("$.total").value(0));
    }

    @Test
    @WithMockUser
    public void testGetTopProfessionals() throws Exception {
        when(professionalService.getTopRatedProfessionals(anyLong(), any(BigDecimal.class)))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/establishment/dashboard/top-professionals")
                .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    @WithMockUser
    public void testGetProfessionalPerformance() throws Exception {
        when(appointmentService.getProfessionalPerformanceStats(anyLong()))
                .thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/establishment/dashboard/professional-performance")
                .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(0));
    }

    @Test
    @WithMockUser
    public void testGetServiceCategories() throws Exception {
        when(serviceService.getCategories(anyLong())).thenReturn(List.of("Haircut", "Massage"));
        when(serviceService.getByCategory(anyLong(), anyString())).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/api/establishment/dashboard/service-categories")
                .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(2));
    }

    @Test
    @WithMockUser
    public void testGetQuickActions() throws Exception {
        when(appointmentService.getByEstablishmentAndStatus(anyLong(), any(AppointmentStatus.class)))
                .thenReturn(new ArrayList<>());
        when(professionalService.countActiveByEstablishment(anyLong())).thenReturn(5L);
        when(serviceService.countActiveByEstablishment(anyLong())).thenReturn(10L);

        mockMvc.perform(get("/api/establishment/dashboard/quick-actions")
                .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.pendingAppointments").value(0))
                .andExpect(jsonPath("$.data.activeProfessionals").value(5))
                .andExpect(jsonPath("$.data.activeServices").value(10))
                .andExpect(jsonPath("$.data.needsAttention").value(false));
    }

    @Test
    @WithMockUser
    public void testGetMonthlyTrends() throws Exception {
        when(appointmentService.countThisMonthAppointments(anyLong())).thenReturn(30L);
        when(appointmentService.calculateMonthlyRevenue(anyLong())).thenReturn(new BigDecimal("5000.00"));

        mockMvc.perform(get("/api/establishment/dashboard/monthly-trends")
                .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.thisMonth.appointments").value(30))
                .andExpect(jsonPath("$.data.thisMonth.revenue").exists())
                .andExpect(jsonPath("$.data.trend.appointmentsTrend").value("up"));
    }
}
