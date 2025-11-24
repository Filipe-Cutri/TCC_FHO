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
import static org.mockito.Mockito.doThrow;
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

    @Test
    @WithMockUser
    public void testGetServicesByCategory() throws Exception {
        List<Service> services = Arrays.asList(
            createMockService(1L, "Haircut", new BigDecimal("50.00"))
        );
        
        when(serviceService.getByCategory(1L, "Haircut")).thenReturn(services);

        mockMvc.perform(get("/api/establishment/services/category/Haircut")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testGetServicesByCategoryError() throws Exception {
        when(serviceService.getByCategory(1L, "Haircut"))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/services/category/Haircut")
                        .param("establishmentId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testGetCategories() throws Exception {
        List<String> categories = Arrays.asList("Haircut", "Shaving", "Massage");
        
        when(serviceService.getCategories(1L)).thenReturn(categories);

        mockMvc.perform(get("/api/establishment/services/categories")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(3))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testGetCategoriesError() throws Exception {
        when(serviceService.getCategories(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/services/categories")
                        .param("establishmentId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testSearchByPriceRange() throws Exception {
        List<Service> services = Arrays.asList(
            createMockService(1L, "Service 1", new BigDecimal("50.00"))
        );
        
        when(serviceService.getByPriceRange(eq(1L), any(BigDecimal.class), any(BigDecimal.class)))
                .thenReturn(services);

        mockMvc.perform(get("/api/establishment/services/search/price")
                        .param("establishmentId", "1")
                        .param("minPrice", "10.00")
                        .param("maxPrice", "100.00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testSearchByPriceRangeError() throws Exception {
        when(serviceService.getByPriceRange(eq(1L), any(BigDecimal.class), any(BigDecimal.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/services/search/price")
                        .param("establishmentId", "1")
                        .param("minPrice", "10.00")
                        .param("maxPrice", "100.00"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testSearchByDurationRange() throws Exception {
        List<Service> services = Arrays.asList(
            createMockService(1L, "Service 1", new BigDecimal("50.00"))
        );
        
        when(serviceService.getByDurationRange(1L, 30, 120)).thenReturn(services);

        mockMvc.perform(get("/api/establishment/services/search/duration")
                        .param("establishmentId", "1")
                        .param("minDuration", "30")
                        .param("maxDuration", "120"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testSearchByDurationRangeError() throws Exception {
        when(serviceService.getByDurationRange(1L, 30, 120))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/services/search/duration")
                        .param("establishmentId", "1")
                        .param("minDuration", "30")
                        .param("maxDuration", "120"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testDeleteServiceSuccess() throws Exception {
        mockMvc.perform(delete("/api/establishment/services/1")
                        .with(csrf())
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Serviço removido com sucesso"));
    }

    @Test
    @WithMockUser
    public void testDeleteServiceSecurityException() throws Exception {
        doThrow(new SecurityException("Access denied")).when(serviceService).deleteService(1L, 1L);

        mockMvc.perform(delete("/api/establishment/services/1")
                        .with(csrf())
                        .param("establishmentId", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testDeleteServiceIllegalArgument() throws Exception {
        doThrow(new IllegalArgumentException("Service not found")).when(serviceService).deleteService(1L, 1L);

        mockMvc.perform(delete("/api/establishment/services/1")
                        .with(csrf())
                        .param("establishmentId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testDeleteServiceError() throws Exception {
        doThrow(new RuntimeException("Database error")).when(serviceService).deleteService(1L, 1L);

        mockMvc.perform(delete("/api/establishment/services/1")
                        .with(csrf())
                        .param("establishmentId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateImageSuccess() throws Exception {
        Service service = createMockService(1L, "Service 1", new BigDecimal("50.00"));
        
        when(serviceService.updateImage(1L, "http://example.com/image.jpg", 1L)).thenReturn(service);

        Map<String, String> request = new HashMap<>();
        request.put("imageUrl", "http://example.com/image.jpg");

        mockMvc.perform(put("/api/establishment/services/1/image")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Imagem atualizada com sucesso"));
    }

    @Test
    @WithMockUser
    public void testUpdateImageSecurityException() throws Exception {
        when(serviceService.updateImage(1L, "http://example.com/image.jpg", 1L))
                .thenThrow(new SecurityException("Access denied"));

        Map<String, String> request = new HashMap<>();
        request.put("imageUrl", "http://example.com/image.jpg");

        mockMvc.perform(put("/api/establishment/services/1/image")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateImageIllegalArgument() throws Exception {
        when(serviceService.updateImage(1L, "http://example.com/image.jpg", 1L))
                .thenThrow(new IllegalArgumentException("Service not found"));

        Map<String, String> request = new HashMap<>();
        request.put("imageUrl", "http://example.com/image.jpg");

        mockMvc.perform(put("/api/establishment/services/1/image")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateImageError() throws Exception {
        when(serviceService.updateImage(1L, "http://example.com/image.jpg", 1L))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, String> request = new HashMap<>();
        request.put("imageUrl", "http://example.com/image.jpg");

        mockMvc.perform(put("/api/establishment/services/1/image")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testGetStatistics() throws Exception {
        when(serviceService.countByEstablishment(1L)).thenReturn(10L);
        when(serviceService.countActiveByEstablishment(1L)).thenReturn(7L);
        when(serviceService.getCategories(1L)).thenReturn(Arrays.asList("Haircut", "Shaving"));

        mockMvc.perform(get("/api/establishment/services/statistics")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(10))
                .andExpect(jsonPath("$.data.active").value(7))
                .andExpect(jsonPath("$.data.inactive").value(3))
                .andExpect(jsonPath("$.data.categories").value(2));
    }

    @Test
    @WithMockUser
    public void testGetStatisticsError() throws Exception {
        when(serviceService.countByEstablishment(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/services/statistics")
                        .param("establishmentId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateServiceSuccess() throws Exception {
        Service service = createMockService(1L, "Updated Service", new BigDecimal("100.00"));
        
        when(serviceService.updateService(anyLong(), anyString(), anyString(), anyInt(), any(BigDecimal.class), anyString(), anyLong()))
                .thenReturn(service);

        Map<String, Object> request = new HashMap<>();
        request.put("name", "Updated Service");
        request.put("description", "Updated description");
        request.put("durationMinutes", 90);
        request.put("price", "100.00");
        request.put("category", "Haircut");

        mockMvc.perform(put("/api/establishment/services/1")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Serviço atualizado com sucesso"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser
    public void testUpdateServiceSecurityException() throws Exception {
        when(serviceService.updateService(anyLong(), any(), any(), any(), any(), any(), anyLong()))
                .thenThrow(new SecurityException("Access denied"));

        Map<String, Object> request = new HashMap<>();
        request.put("name", "Updated Service");

        mockMvc.perform(put("/api/establishment/services/1")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateServiceIllegalArgument() throws Exception {
        when(serviceService.updateService(anyLong(), any(), any(), any(), any(), any(), anyLong()))
                .thenThrow(new IllegalArgumentException("Service not found"));

        Map<String, Object> request = new HashMap<>();
        request.put("name", "Updated Service");

        mockMvc.perform(put("/api/establishment/services/1")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testCreateServiceIllegalArgument() throws Exception {
        when(serviceService.createService(anyString(), any(), anyInt(), any(BigDecimal.class), anyLong(), any()))
                .thenThrow(new IllegalArgumentException("Invalid data"));

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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateStatusIllegalArgument() throws Exception {
        when(serviceService.updateStatus(anyLong(), any(ServiceStatus.class), anyLong()))
                .thenThrow(new IllegalArgumentException("Invalid status"));

        Map<String, String> request = new HashMap<>();
        request.put("status", "INVALID");

        mockMvc.perform(put("/api/establishment/services/1/status")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
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
