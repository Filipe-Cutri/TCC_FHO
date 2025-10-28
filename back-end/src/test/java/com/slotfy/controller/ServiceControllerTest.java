package com.slotfy.controller;

import com.slotfy.model.Service;
import com.slotfy.model.ServiceStatus;
import com.slotfy.service.ServiceService;
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
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ServiceController.class)
@Import({com.slotfy.config.SecurityConfig.class})
public class ServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ServiceService serviceService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testGetServices() throws Exception {
        List<Service> services = Arrays.asList(
            createMockService(1L, "Service 1", new BigDecimal("50.00")),
            createMockService(2L, "Service 2", new BigDecimal("75.00"))
        );
        
        when(serviceService.getByEstablishmentId(1L)).thenReturn(services);

        mockMvc.perform(get("/api/establishment/services")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testGetServicesError() throws Exception {
        when(serviceService.getByEstablishmentId(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/services")
                        .param("establishmentId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testGetActiveServices() throws Exception {
        List<Service> services = Arrays.asList(
            createMockService(1L, "Service 1", new BigDecimal("50.00"))
        );
        
        when(serviceService.getActiveByEstablishmentId(1L)).thenReturn(services);

        mockMvc.perform(get("/api/establishment/services/active")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testGetActiveServicesError() throws Exception {
        when(serviceService.getActiveByEstablishmentId(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/services/active")
                        .param("establishmentId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testGetService() throws Exception {
        Service service = createMockService(1L, "Service 1", new BigDecimal("50.00"));
        
        when(serviceService.findByIdAndEstablishment(1L, 1L)).thenReturn(Optional.of(service));

        mockMvc.perform(get("/api/establishment/services/1")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser
    public void testGetServiceNotFound() throws Exception {
        when(serviceService.findByIdAndEstablishment(1L, 1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/establishment/services/1")
                        .param("establishmentId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testGetServiceError() throws Exception {
        when(serviceService.findByIdAndEstablishment(1L, 1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/services/1")
                        .param("establishmentId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testCreateServiceSuccess() throws Exception {
        Service service = createMockService(1L, "New Service", new BigDecimal("100.00"));
        
        when(serviceService.createService(anyString(), anyString(), anyInt(), any(BigDecimal.class), anyLong(), anyString()))
                .thenReturn(service);

        Map<String, Object> request = new HashMap<>();
        request.put("name", "New Service");
        request.put("description", "Service description");
        request.put("durationMinutes", 60);
        request.put("price", "100.00");
        request.put("establishmentId", "1");
        request.put("category", "Haircut");

        mockMvc.perform(post("/api/establishment/services")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Serviço criado com sucesso"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser
    public void testCreateServiceMissingName() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("description", "Service description");
        request.put("durationMinutes", 60);
        request.put("price", "100.00");
        request.put("establishmentId", "1");

        mockMvc.perform(post("/api/establishment/services")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Nome do serviço é obrigatório"));
    }

    @Test
    @WithMockUser
    public void testCreateServiceEmptyName() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "  ");
        request.put("description", "Service description");
        request.put("durationMinutes", 60);
        request.put("price", "100.00");
        request.put("establishmentId", "1");

        mockMvc.perform(post("/api/establishment/services")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Nome do serviço é obrigatório"));
    }

    @Test
    @WithMockUser
    public void testCreateServiceInvalidDuration() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "New Service");
        request.put("description", "Service description");
        request.put("durationMinutes", 0);
        request.put("price", "100.00");
        request.put("establishmentId", "1");

        mockMvc.perform(post("/api/establishment/services")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Duração deve ser maior que zero"));
    }

    @Test
    @WithMockUser
    public void testCreateServiceNegativePrice() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "New Service");
        request.put("description", "Service description");
        request.put("durationMinutes", 60);
        request.put("price", "-10.00");
        request.put("establishmentId", "1");

        mockMvc.perform(post("/api/establishment/services")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Preço não pode ser negativo"));
    }

    @Test
    @WithMockUser
    public void testCreateServiceMissingEstablishmentId() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "New Service");
        request.put("description", "Service description");
        request.put("durationMinutes", 60);
        request.put("price", "100.00");

        mockMvc.perform(post("/api/establishment/services")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("ID do estabelecimento é obrigatório"));
    }

    @Test
    @WithMockUser
    public void testCreateServiceError() throws Exception {
        when(serviceService.createService(anyString(), anyString(), anyInt(), any(BigDecimal.class), anyLong(), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> request = new HashMap<>();
        request.put("name", "New Service");
        request.put("description", "Service description");
        request.put("durationMinutes", 60);
        request.put("price", "100.00");
        request.put("establishmentId", "1");

        mockMvc.perform(post("/api/establishment/services")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateServiceError() throws Exception {
        when(serviceService.updateService(anyLong(), anyString(), anyString(), anyInt(), any(BigDecimal.class), anyString(), anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> request = new HashMap<>();
        request.put("name", "Updated Service");

        mockMvc.perform(put("/api/establishment/services/1")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateStatusSuccess() throws Exception {
        Service service = createMockService(1L, "Service 1", new BigDecimal("50.00"));
        
        when(serviceService.updateStatus(anyLong(), any(ServiceStatus.class), anyLong()))
                .thenReturn(service);

        Map<String, String> request = new HashMap<>();
        request.put("status", "active");

        mockMvc.perform(put("/api/establishment/services/1/status")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Status atualizado com sucesso"));
    }

    @Test
    @WithMockUser
    public void testUpdateStatusMissingStatus() throws Exception {
        Map<String, String> request = new HashMap<>();

        mockMvc.perform(put("/api/establishment/services/1/status")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Status é obrigatório"));
    }

    @Test
    @WithMockUser
    public void testUpdateStatusEmptyStatus() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("status", "  ");

        mockMvc.perform(put("/api/establishment/services/1/status")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Status é obrigatório"));
    }

    @Test
    @WithMockUser
    public void testUpdateStatusSecurityException() throws Exception {
        when(serviceService.updateStatus(anyLong(), any(ServiceStatus.class), anyLong()))
                .thenThrow(new SecurityException("Access denied"));

        Map<String, String> request = new HashMap<>();
        request.put("status", "active");

        mockMvc.perform(put("/api/establishment/services/1/status")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateStatusError() throws Exception {
        when(serviceService.updateStatus(anyLong(), any(ServiceStatus.class), anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, String> request = new HashMap<>();
        request.put("status", "active");

        mockMvc.perform(put("/api/establishment/services/1/status")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    private Service createMockService(Long id, String name, BigDecimal price) {
        Service service = new Service();
        service.setId(id);
        service.setName(name);
        service.setPrice(price);
        service.setDurationMinutes(60);
        service.setStatus(ServiceStatus.ACTIVE);
        return service;
    }
}
