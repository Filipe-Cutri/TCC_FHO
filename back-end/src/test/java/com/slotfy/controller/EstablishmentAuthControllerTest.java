package com.slotfy.controller;

import com.slotfy.model.EstablishmentUser;
import com.slotfy.model.UserRole;
import com.slotfy.service.EstablishmentUserService;
import com.slotfy.service.EstablishmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EstablishmentAuthController.class)
@Import({com.slotfy.config.SecurityConfig.class})
public class EstablishmentAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EstablishmentUserService establishmentUserService;

    @MockBean
    private EstablishmentService establishmentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testRegisterSuccess() throws Exception {
        // Arrange
        EstablishmentUser mockUser = new EstablishmentUser("Test User", "test@example.com", "hashedPassword", UserRole.ADMIN, 1L);
        mockUser.setId(1L);
        
        when(establishmentUserService.createUser(anyString(), anyString(), anyString(), any(UserRole.class), anyLong()))
                .thenReturn(mockUser);

        Map<String, String> request = new HashMap<>();
        request.put("name", "Test User");
        request.put("email", "test@example.com");
        request.put("password", "password123");
        request.put("establishmentId", "1");

        // Act & Assert
        mockMvc.perform(post("/api/establishment/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Conta criada com sucesso"))
                .andExpect(jsonPath("$.user.name").value("Test User"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    public void testLoginSuccess() throws Exception {
        // Arrange
        EstablishmentUser mockUser = new EstablishmentUser("Test User", "test@example.com", "hashedPassword", UserRole.ADMIN, 1L);
        mockUser.setId(1L);
        
        when(establishmentUserService.authenticate(anyString(), anyString()))
                .thenReturn(Optional.of(mockUser));

        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("password", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/establishment/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login realizado com sucesso"))
                .andExpect(jsonPath("$.user.name").value("Test User"))
                .andExpect(jsonPath("$.user.email").value("test@example.com"))
                .andExpect(jsonPath("$.user.role").value("admin"));
    }

    @Test
    @WithMockUser
    public void testRegisterInvalidInput() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("name", "");
        request.put("email", "test@example.com");
        request.put("password", "123"); // Too short

        mockMvc.perform(post("/api/establishment/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testRegisterCompleteSuccess() throws Exception {
        // Arrange - Mock establishment creation
        com.slotfy.model.Establishment mockEstablishment = new com.slotfy.model.Establishment(
                "Test Establishment", 
                "establishment@example.com", 
                "1234567890", 
                "Test Address"
        );
        mockEstablishment.setId(1L);
        mockEstablishment.setCategory("Barbearia");

        // Mock establishment user creation
        EstablishmentUser mockUser = new EstablishmentUser(
                "Test Establishment", 
                "establishment@example.com", 
                "hashedPassword", 
                UserRole.ADMIN, 
                1L
        );
        mockUser.setId(1L);

        when(establishmentService.createEstablishment(
                anyString(), anyString(), anyString(), any(), any(), anyString(), any()))
                .thenReturn(mockEstablishment);
        
        when(establishmentUserService.createUser(
                anyString(), anyString(), anyString(), any(UserRole.class), anyLong()))
                .thenReturn(mockUser);

        Map<String, String> request = new HashMap<>();
        request.put("tipoEstabelecimento", "barbearia");
        request.put("nomeEstabelecimento", "Test Establishment");
        request.put("email", "establishment@example.com");
        request.put("telefone", "1234567890");
        request.put("senha", "password123");

        // Act & Assert
        mockMvc.perform(post("/api/establishment/register-complete")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Estabelecimento registrado com sucesso!"))
                .andExpect(jsonPath("$.establishmentId").value(1))
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.establishmentName").value("Test Establishment"))
                .andExpect(jsonPath("$.userEmail").value("establishment@example.com"));
    }

    @Test
    @WithMockUser
    public void testRegisterCompleteValidationError() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("tipoEstabelecimento", "");
        request.put("nomeEstabelecimento", "");
        request.put("email", "invalid-email");
        request.put("telefone", "");
        request.put("senha", "123"); // Too short

        mockMvc.perform(post("/api/establishment/register-complete")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}