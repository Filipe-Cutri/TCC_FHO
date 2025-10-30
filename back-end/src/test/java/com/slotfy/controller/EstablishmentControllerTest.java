package com.slotfy.controller;

import com.slotfy.model.*;
import com.slotfy.service.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EstablishmentController.class)
@Import({com.slotfy.config.SecurityConfig.class})
public class EstablishmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EstablishmentService establishmentService;

    @MockBean
    private AppointmentService appointmentService;

    @MockBean
    private ClientService clientService;

    @Autowired
    private ObjectMapper objectMapper;

    private Establishment createMockEstablishment(Long id, String name, EstablishmentStatus status) {
        Establishment establishment = new Establishment();
        establishment.setId(id);
        establishment.setName(name);
        establishment.setEmail("test@example.com");
        establishment.setStatus(status);
        establishment.setCategory("Beauty");
        establishment.setAddress("123 Main St");
        return establishment;
    }

    @Test
    @WithMockUser
    public void testListActiveEstablishments() throws Exception {
        List<Establishment> establishments = Arrays.asList(
            createMockEstablishment(1L, "Salon 1", EstablishmentStatus.ACTIVE),
            createMockEstablishment(2L, "Salon 2", EstablishmentStatus.ACTIVE)
        );
        
        when(establishmentService.getByStatus(EstablishmentStatus.ACTIVE))
                .thenReturn(establishments);

        mockMvc.perform(get("/api/establishment/list"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testListActiveEstablishmentsError() throws Exception {
        when(establishmentService.getByStatus(EstablishmentStatus.ACTIVE))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/list"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testGetEstablishment() throws Exception {
        Establishment establishment = createMockEstablishment(1L, "Salon 1", EstablishmentStatus.ACTIVE);
        
        when(establishmentService.findById(1L)).thenReturn(Optional.of(establishment));

        mockMvc.perform(get("/api/establishment/profile/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("Salon 1"));
    }

    @Test
    @WithMockUser
    public void testGetEstablishmentNotFound() throws Exception {
        when(establishmentService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/establishment/profile/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testCreateEstablishment() throws Exception {
        Establishment establishment = createMockEstablishment(1L, "New Salon", EstablishmentStatus.ACTIVE);
        
        when(establishmentService.createEstablishment(
                anyString(), anyString(), anyString(), anyString(), 
                anyString(), anyString(), anyString()))
                .thenReturn(establishment);

        Map<String, Object> request = new HashMap<>();
        request.put("name", "New Salon");
        request.put("email", "salon@example.com");
        request.put("phone", "1234567890");
        request.put("address", "123 Main St");
        request.put("description", "A beauty salon");
        request.put("category", "Beauty");
        request.put("cnpj", "12345678901234");

        mockMvc.perform(post("/api/establishment/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser
    public void testCreateEstablishmentMissingName() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("email", "salon@example.com");

        mockMvc.perform(post("/api/establishment/profile")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateEstablishment() throws Exception {
        Establishment establishment = createMockEstablishment(1L, "Updated Salon", EstablishmentStatus.ACTIVE);
        
        when(establishmentService.updateEstablishment(
                anyLong(), anyString(), anyString(), anyString(), anyString(), 
                anyString(), anyString(), anyString(), anyString()))
                .thenReturn(establishment);

        Map<String, Object> request = new HashMap<>();
        request.put("name", "Updated Salon");
        request.put("email", "updated@example.com");
        request.put("phone", "9876543210");
        request.put("address", "456 New St");
        request.put("description", "Updated description");
        request.put("category", "Beauty");
        request.put("cnpj", "12345678901234");
        request.put("workingHours", "9:00-18:00");

        mockMvc.perform(put("/api/establishment/profile/1")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    public void testUpdateStatus() throws Exception {
        Establishment establishment = createMockEstablishment(1L, "Salon 1", EstablishmentStatus.INACTIVE);
        
        when(establishmentService.updateStatus(1L, EstablishmentStatus.INACTIVE))
                .thenReturn(establishment);

        Map<String, String> request = new HashMap<>();
        request.put("status", "inactive");

        mockMvc.perform(put("/api/establishment/profile/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    public void testUpdateStatusMissingStatus() throws Exception {
        Map<String, String> request = new HashMap<>();

        mockMvc.perform(put("/api/establishment/profile/1/status")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateSettings() throws Exception {
        Establishment establishment = createMockEstablishment(1L, "Salon 1", EstablishmentStatus.ACTIVE);
        
        when(establishmentService.updateSettings(1L, "{\"theme\":\"dark\"}"))
                .thenReturn(establishment);

        Map<String, String> request = new HashMap<>();
        request.put("settings", "{\"theme\":\"dark\"}");

        mockMvc.perform(put("/api/establishment/profile/1/settings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    public void testUpdateImage() throws Exception {
        Establishment establishment = createMockEstablishment(1L, "Salon 1", EstablishmentStatus.ACTIVE);
        
        when(establishmentService.updateImage(1L, "http://example.com/image.jpg"))
                .thenReturn(establishment);

        Map<String, String> request = new HashMap<>();
        request.put("imageUrl", "http://example.com/image.jpg");

        mockMvc.perform(put("/api/establishment/profile/1/image")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    public void testGetEstablishmentsByStatus() throws Exception {
        List<Establishment> establishments = Arrays.asList(
            createMockEstablishment(1L, "Salon 1", EstablishmentStatus.ACTIVE)
        );
        
        when(establishmentService.getByStatus(EstablishmentStatus.ACTIVE))
                .thenReturn(establishments);

        mockMvc.perform(get("/api/establishment/profile/status/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @WithMockUser
    public void testGetEstablishmentsByCategory() throws Exception {
        List<Establishment> establishments = Arrays.asList(
            createMockEstablishment(1L, "Salon 1", EstablishmentStatus.ACTIVE)
        );
        
        when(establishmentService.getByCategory("Beauty"))
                .thenReturn(establishments);

        mockMvc.perform(get("/api/establishment/profile/category/Beauty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @WithMockUser
    public void testGetCategories() throws Exception {
        List<String> categories = Arrays.asList("Beauty", "Health", "Fitness");
        
        when(establishmentService.getCategories()).thenReturn(categories);

        mockMvc.perform(get("/api/establishment/profile/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(3));
    }

    @Test
    @WithMockUser
    public void testSearchEstablishments() throws Exception {
        List<Establishment> establishments = Arrays.asList(
            createMockEstablishment(1L, "Beauty Salon", EstablishmentStatus.ACTIVE)
        );
        
        when(establishmentService.searchEstablishments("Beauty"))
                .thenReturn(establishments);

        mockMvc.perform(get("/api/establishment/profile/search")
                        .param("term", "Beauty"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1));
    }

    @Test
    @WithMockUser
    public void testFindByEmail() throws Exception {
        Establishment establishment = createMockEstablishment(1L, "Salon 1", EstablishmentStatus.ACTIVE);
        
        when(establishmentService.findByEmail("test@example.com"))
                .thenReturn(Optional.of(establishment));

        mockMvc.perform(get("/api/establishment/profile/email/test@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser
    public void testFindByEmailNotFound() throws Exception {
        when(establishmentService.findByEmail("notfound@example.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(get("/api/establishment/profile/email/notfound@example.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testFindByCnpj() throws Exception {
        Establishment establishment = createMockEstablishment(1L, "Salon 1", EstablishmentStatus.ACTIVE);
        
        when(establishmentService.findByCnpj("12345678901234"))
                .thenReturn(Optional.of(establishment));

        mockMvc.perform(get("/api/establishment/profile/cnpj/12345678901234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    public void testActivateEstablishment() throws Exception {
        Establishment establishment = createMockEstablishment(1L, "Salon 1", EstablishmentStatus.ACTIVE);
        
        when(establishmentService.activateEstablishment(1L))
                .thenReturn(establishment);

        mockMvc.perform(put("/api/establishment/profile/1/activate")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    public void testDeactivateEstablishment() throws Exception {
        Establishment establishment = createMockEstablishment(1L, "Salon 1", EstablishmentStatus.INACTIVE);
        
        when(establishmentService.deactivateEstablishment(1L))
                .thenReturn(establishment);

        mockMvc.perform(put("/api/establishment/profile/1/deactivate")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    public void testGetStatistics() throws Exception {
        when(establishmentService.countByStatus(EstablishmentStatus.ACTIVE)).thenReturn(10L);
        when(establishmentService.countByStatus(EstablishmentStatus.INACTIVE)).thenReturn(5L);
        when(establishmentService.countByStatus(EstablishmentStatus.SUSPENDED)).thenReturn(2L);
        when(establishmentService.countByStatus(EstablishmentStatus.PENDING)).thenReturn(3L);

        mockMvc.perform(get("/api/establishment/profile/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.active").value(10))
                .andExpect(jsonPath("$.data.inactive").value(5))
                .andExpect(jsonPath("$.data.total").value(20));
    }

    @Test
    @WithMockUser
    public void testGetEstablishmentClients() throws Exception {
        List<Appointment> appointments = new ArrayList<>();
        Client client = new Client();
        client.setId(1L);
        client.setName("John Doe");
        client.setEmail("john@example.com");
        client.setPhone("1234567890");
        
        Appointment appointment = new Appointment();
        appointment.setClientId(1L);
        appointments.add(appointment);
        
        when(appointmentService.getByEstablishmentId(1L)).thenReturn(appointments);
        when(clientService.findById(1L)).thenReturn(Optional.of(client));

        mockMvc.perform(get("/api/establishment/clients")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testGetEstablishmentClientsError() throws Exception {
        when(appointmentService.getByEstablishmentId(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/clients")
                        .param("establishmentId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }
}
