package com.slotfy.controller;

import com.slotfy.model.Professional;
import com.slotfy.model.ProfessionalStatus;
import com.slotfy.service.ProfessionalService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ProfessionalController.class)
@Import({com.slotfy.config.SecurityConfig.class})
public class ProfessionalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProfessionalService professionalService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testGetProfessionals() throws Exception {
        List<Professional> professionals = Arrays.asList(
            createMockProfessional(1L, "Professional 1", "prof1@example.com"),
            createMockProfessional(2L, "Professional 2", "prof2@example.com")
        );
        
        when(professionalService.getByEstablishmentId(1L)).thenReturn(professionals);

        mockMvc.perform(get("/api/establishment/professionals")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testGetProfessionalsError() throws Exception {
        when(professionalService.getByEstablishmentId(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/professionals")
                        .param("establishmentId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testGetActiveProfessionals() throws Exception {
        List<Professional> professionals = Arrays.asList(
            createMockProfessional(1L, "Professional 1", "prof1@example.com")
        );
        
        when(professionalService.getActiveByEstablishmentId(1L)).thenReturn(professionals);

        mockMvc.perform(get("/api/establishment/professionals/active")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testGetActiveProfessionalsError() throws Exception {
        when(professionalService.getActiveByEstablishmentId(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/professionals/active")
                        .param("establishmentId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testGetProfessional() throws Exception {
        Professional professional = createMockProfessional(1L, "Professional 1", "prof1@example.com");
        
        when(professionalService.findByIdAndEstablishment(1L, 1L)).thenReturn(Optional.of(professional));

        mockMvc.perform(get("/api/establishment/professionals/1")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser
    public void testGetProfessionalNotFound() throws Exception {
        when(professionalService.findByIdAndEstablishment(1L, 1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/establishment/professionals/1")
                        .param("establishmentId", "1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testGetProfessionalError() throws Exception {
        when(professionalService.findByIdAndEstablishment(1L, 1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/professionals/1")
                        .param("establishmentId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testCreateProfessionalSuccess() throws Exception {
        Professional professional = createMockProfessional(1L, "New Professional", "new@example.com");
        
        when(professionalService.createProfessional(anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(professional);

        Map<String, Object> request = new HashMap<>();
        request.put("name", "New Professional");
        request.put("email", "new@example.com");
        request.put("phone", "1234567890");
        request.put("specialties", "Haircut");
        request.put("establishmentId", "1");

        mockMvc.perform(post("/api/establishment/professionals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profissional criado com sucesso"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser
    public void testCreateProfessionalMissingName() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("email", "new@example.com");
        request.put("phone", "1234567890");
        request.put("establishmentId", "1");

        mockMvc.perform(post("/api/establishment/professionals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Nome é obrigatório"));
    }

    @Test
    @WithMockUser
    public void testCreateProfessionalEmptyName() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "  ");
        request.put("email", "new@example.com");
        request.put("phone", "1234567890");
        request.put("establishmentId", "1");

        mockMvc.perform(post("/api/establishment/professionals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Nome é obrigatório"));
    }

    @Test
    @WithMockUser
    public void testCreateProfessionalMissingEstablishmentId() throws Exception {
        Map<String, Object> request = new HashMap<>();
        request.put("name", "New Professional");
        request.put("email", "new@example.com");
        request.put("phone", "1234567890");

        mockMvc.perform(post("/api/establishment/professionals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("ID do estabelecimento é obrigatório"));
    }

    @Test
    @WithMockUser
    public void testCreateProfessionalError() throws Exception {
        when(professionalService.createProfessional(anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> request = new HashMap<>();
        request.put("name", "New Professional");
        request.put("email", "new@example.com");
        request.put("phone", "1234567890");
        request.put("establishmentId", "1");

        mockMvc.perform(post("/api/establishment/professionals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateProfessionalError() throws Exception {
        when(professionalService.updateProfessional(anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> request = new HashMap<>();
        request.put("name", "Updated Professional");

        mockMvc.perform(put("/api/establishment/professionals/1")
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
        Professional professional = createMockProfessional(1L, "Professional 1", "prof1@example.com");
        
        when(professionalService.updateStatus(anyLong(), any(ProfessionalStatus.class), anyLong()))
                .thenReturn(professional);

        Map<String, String> request = new HashMap<>();
        request.put("status", "active");

        mockMvc.perform(put("/api/establishment/professionals/1/status")
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

        mockMvc.perform(put("/api/establishment/professionals/1/status")
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

        mockMvc.perform(put("/api/establishment/professionals/1/status")
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
        when(professionalService.updateStatus(anyLong(), any(ProfessionalStatus.class), anyLong()))
                .thenThrow(new SecurityException("Access denied"));

        Map<String, String> request = new HashMap<>();
        request.put("status", "active");

        mockMvc.perform(put("/api/establishment/professionals/1/status")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateStatusInvalidArgument() throws Exception {
        when(professionalService.updateStatus(anyLong(), any(ProfessionalStatus.class), anyLong()))
                .thenThrow(new IllegalArgumentException("Invalid status"));

        Map<String, String> request = new HashMap<>();
        request.put("status", "INVALID");

        mockMvc.perform(put("/api/establishment/professionals/1/status")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateStatusError() throws Exception {
        when(professionalService.updateStatus(anyLong(), any(ProfessionalStatus.class), anyLong()))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, String> request = new HashMap<>();
        request.put("status", "active");

        mockMvc.perform(put("/api/establishment/professionals/1/status")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateProfessionalSuccess() throws Exception {
        Professional professional = createMockProfessional(1L, "Updated Professional", "updated@example.com");
        
        when(professionalService.updateProfessional(anyLong(), anyString(), anyString(), anyString(), anyString(), anyLong()))
                .thenReturn(professional);

        Map<String, Object> request = new HashMap<>();
        request.put("name", "Updated Professional");
        request.put("email", "updated@example.com");
        request.put("phone", "1234567890");
        request.put("specialties", "Haircut, Shaving");

        mockMvc.perform(put("/api/establishment/professionals/1")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profissional atualizado com sucesso"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser
    public void testUpdateProfessionalSecurityException() throws Exception {
        when(professionalService.updateProfessional(anyLong(), any(), any(), any(), any(), anyLong()))
                .thenThrow(new SecurityException("Access denied"));

        Map<String, Object> request = new HashMap<>();
        request.put("name", "Updated Professional");

        mockMvc.perform(put("/api/establishment/professionals/1")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateProfessionalIllegalArgument() throws Exception {
        when(professionalService.updateProfessional(anyLong(), any(), any(), any(), any(), anyLong()))
                .thenThrow(new IllegalArgumentException("Invalid data"));

        Map<String, Object> request = new HashMap<>();
        request.put("name", "Updated Professional");

        mockMvc.perform(put("/api/establishment/professionals/1")
                        .with(csrf())
                        .param("establishmentId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateStatisticsSuccess() throws Exception {
        Professional professional = createMockProfessional(1L, "Professional 1", "prof1@example.com");
        
        when(professionalService.updateStatistics(anyLong(), any(), any()))
                .thenReturn(professional);

        Map<String, Object> request = new HashMap<>();
        request.put("rating", "4.5");
        request.put("satisfactionRate", "95.0");

        mockMvc.perform(put("/api/establishment/professionals/1/statistics")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Estatísticas atualizadas com sucesso"));
    }

    @Test
    @WithMockUser
    public void testUpdateStatisticsIllegalArgument() throws Exception {
        when(professionalService.updateStatistics(anyLong(), any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid statistics"));

        Map<String, Object> request = new HashMap<>();
        request.put("rating", "4.5");

        mockMvc.perform(put("/api/establishment/professionals/1/statistics")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateStatisticsError() throws Exception {
        when(professionalService.updateStatistics(anyLong(), any(), any()))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> request = new HashMap<>();
        request.put("rating", "4.5");

        mockMvc.perform(put("/api/establishment/professionals/1/statistics")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testGetTopRatedProfessionals() throws Exception {
        List<Professional> professionals = Arrays.asList(
            createMockProfessional(1L, "Top Professional", "top@example.com")
        );
        
        when(professionalService.getTopRatedProfessionals(eq(1L), any(java.math.BigDecimal.class)))
                .thenReturn(professionals);

        mockMvc.perform(get("/api/establishment/professionals/top-rated")
                        .param("establishmentId", "1")
                        .param("minRating", "4.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testGetTopRatedProfessionalsError() throws Exception {
        when(professionalService.getTopRatedProfessionals(eq(1L), any(java.math.BigDecimal.class)))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/professionals/top-rated")
                        .param("establishmentId", "1")
                        .param("minRating", "4.0"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testSearchBySpecialty() throws Exception {
        List<Professional> professionals = Arrays.asList(
            createMockProfessional(1L, "Specialist", "specialist@example.com")
        );
        
        when(professionalService.searchBySpecialty(1L, "Haircut")).thenReturn(professionals);

        mockMvc.perform(get("/api/establishment/professionals/search")
                        .param("establishmentId", "1")
                        .param("specialty", "Haircut"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testSearchBySpecialtyError() throws Exception {
        when(professionalService.searchBySpecialty(1L, "Haircut"))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/professionals/search")
                        .param("establishmentId", "1")
                        .param("specialty", "Haircut"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testDeleteProfessionalSuccess() throws Exception {
        mockMvc.perform(delete("/api/establishment/professionals/1")
                        .with(csrf())
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Profissional removido com sucesso"));
    }

    @Test
    @WithMockUser
    public void testDeleteProfessionalSecurityException() throws Exception {
        doThrow(new SecurityException("Access denied")).when(professionalService).deleteProfessional(1L, 1L);

        mockMvc.perform(delete("/api/establishment/professionals/1")
                        .with(csrf())
                        .param("establishmentId", "1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testDeleteProfessionalIllegalArgument() throws Exception {
        doThrow(new IllegalArgumentException("Professional not found")).when(professionalService).deleteProfessional(1L, 1L);

        mockMvc.perform(delete("/api/establishment/professionals/1")
                        .with(csrf())
                        .param("establishmentId", "1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testDeleteProfessionalError() throws Exception {
        doThrow(new RuntimeException("Database error")).when(professionalService).deleteProfessional(1L, 1L);

        mockMvc.perform(delete("/api/establishment/professionals/1")
                        .with(csrf())
                        .param("establishmentId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testUpdateImageSuccess() throws Exception {
        Professional professional = createMockProfessional(1L, "Professional 1", "prof1@example.com");
        
        when(professionalService.updateImage(1L, "http://example.com/image.jpg", 1L)).thenReturn(professional);

        Map<String, String> request = new HashMap<>();
        request.put("imageUrl", "http://example.com/image.jpg");

        mockMvc.perform(put("/api/establishment/professionals/1/image")
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
        when(professionalService.updateImage(1L, "http://example.com/image.jpg", 1L))
                .thenThrow(new SecurityException("Access denied"));

        Map<String, String> request = new HashMap<>();
        request.put("imageUrl", "http://example.com/image.jpg");

        mockMvc.perform(put("/api/establishment/professionals/1/image")
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
        when(professionalService.updateImage(1L, "http://example.com/image.jpg", 1L))
                .thenThrow(new IllegalArgumentException("Professional not found"));

        Map<String, String> request = new HashMap<>();
        request.put("imageUrl", "http://example.com/image.jpg");

        mockMvc.perform(put("/api/establishment/professionals/1/image")
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
        when(professionalService.updateImage(1L, "http://example.com/image.jpg", 1L))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, String> request = new HashMap<>();
        request.put("imageUrl", "http://example.com/image.jpg");

        mockMvc.perform(put("/api/establishment/professionals/1/image")
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
        when(professionalService.countByEstablishment(1L)).thenReturn(10L);
        when(professionalService.countActiveByEstablishment(1L)).thenReturn(7L);

        mockMvc.perform(get("/api/establishment/professionals/statistics")
                        .param("establishmentId", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.total").value(10))
                .andExpect(jsonPath("$.data.active").value(7))
                .andExpect(jsonPath("$.data.inactive").value(3));
    }

    @Test
    @WithMockUser
    public void testGetStatisticsError() throws Exception {
        when(professionalService.countByEstablishment(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/establishment/professionals/statistics")
                        .param("establishmentId", "1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testCreateProfessionalIllegalArgument() throws Exception {
        when(professionalService.createProfessional(anyString(), any(), any(), any(), anyLong()))
                .thenThrow(new IllegalArgumentException("Invalid data"));

        Map<String, Object> request = new HashMap<>();
        request.put("name", "New Professional");
        request.put("email", "new@example.com");
        request.put("phone", "1234567890");
        request.put("establishmentId", "1");

        mockMvc.perform(post("/api/establishment/professionals")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    private Professional createMockProfessional(Long id, String name, String email) {
        Professional professional = new Professional();
        professional.setId(id);
        professional.setName(name);
        professional.setEmail(email);
        professional.setPhone("1234567890");
        professional.setStatus(ProfessionalStatus.ACTIVE);
        return professional;
    }
}
