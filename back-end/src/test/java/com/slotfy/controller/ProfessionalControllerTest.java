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
