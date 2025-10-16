package com.slotfy.controller;

import com.slotfy.model.Client;
import com.slotfy.service.ClientService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClientAuthController.class)
@Import({com.slotfy.config.SecurityConfig.class})
public class ClientAuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClientService clientService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testLoginSuccess() throws Exception {
        Client mockClient = new Client("Test Client", "test@example.com", "hashedPassword", "1234567890");
        mockClient.setId(1L);
        
        when(clientService.authenticate(anyString(), anyString()))
                .thenReturn(Optional.of(mockClient));

        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("password", "password123");

        mockMvc.perform(post("/api/client/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Login realizado com sucesso"))
                .andExpect(jsonPath("$.client.name").value("Test Client"))
                .andExpect(jsonPath("$.client.email").value("test@example.com"))
                .andExpect(jsonPath("$.client.phone").value("1234567890"));
    }

    @Test
    @WithMockUser
    public void testLoginWithEstablishmentId() throws Exception {
        Client mockClient = new Client("Test Client", "test@example.com", "hashedPassword", "1234567890");
        mockClient.setId(1L);
        mockClient.setSelectedEstablishmentId(5L);
        
        when(clientService.authenticate(anyString(), anyString()))
                .thenReturn(Optional.of(mockClient));

        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("password", "password123");

        mockMvc.perform(post("/api/client/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.client.selectedEstablishmentId").value(5));
    }

    @Test
    @WithMockUser
    public void testLoginInvalidCredentials() throws Exception {
        when(clientService.authenticate(anyString(), anyString()))
                .thenReturn(Optional.empty());

        Map<String, String> request = new HashMap<>();
        request.put("email", "test@example.com");
        request.put("password", "wrongpassword");

        mockMvc.perform(post("/api/client/login")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email ou senha inválidos"));
    }

    @Test
    @WithMockUser
    public void testRegisterSuccess() throws Exception {
        Client mockClient = new Client("Test Client", "test@example.com", "hashedPassword", "1234567890");
        mockClient.setId(1L);
        
        when(clientService.registerClient(anyString(), anyString(), anyString(), anyString(), isNull()))
                .thenReturn(mockClient);

        Map<String, String> request = new HashMap<>();
        request.put("name", "Test Client");
        request.put("email", "test@example.com");
        request.put("password", "password123");
        request.put("phone", "1234567890");

        mockMvc.perform(post("/api/client/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Conta criada com sucesso"))
                .andExpect(jsonPath("$.client.name").value("Test Client"))
                .andExpect(jsonPath("$.client.email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    public void testRegisterWithEstablishmentId() throws Exception {
        Client mockClient = new Client("Test Client", "test@example.com", "hashedPassword", "1234567890");
        mockClient.setId(1L);
        mockClient.setSelectedEstablishmentId(5L);
        
        when(clientService.registerClient(anyString(), anyString(), anyString(), anyString(), eq(5L)))
                .thenReturn(mockClient);

        Map<String, String> request = new HashMap<>();
        request.put("name", "Test Client");
        request.put("email", "test@example.com");
        request.put("password", "password123");
        request.put("phone", "1234567890");
        request.put("establishmentId", "5");

        mockMvc.perform(post("/api/client/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.client.selectedEstablishmentId").value(5));
    }

    @Test
    @WithMockUser
    public void testRegisterInvalidInput() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("name", "");
        request.put("email", "test@example.com");
        request.put("password", "123"); // Too short

        mockMvc.perform(post("/api/client/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testRegisterInvalidEmail() throws Exception {
        when(clientService.registerClient(anyString(), anyString(), anyString(), anyString(), any()))
                .thenThrow(new RuntimeException("Email inválido"));

        Map<String, String> request = new HashMap<>();
        request.put("name", "Test Client");
        request.put("email", "invalid-email");
        request.put("password", "password123");
        request.put("phone", "1234567890");

        mockMvc.perform(post("/api/client/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Erro interno do servidor"));
    }

    @Test
    @WithMockUser
    public void testRegisterEmailAlreadyExists() throws Exception {
        when(clientService.registerClient(anyString(), anyString(), anyString(), anyString(), any()))
                .thenThrow(new IllegalArgumentException("Email já está em uso"));

        Map<String, String> request = new HashMap<>();
        request.put("name", "Test Client");
        request.put("email", "existing@example.com");
        request.put("password", "password123");
        request.put("phone", "1234567890");

        mockMvc.perform(post("/api/client/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Email já está em uso"));
    }

    @Test
    @WithMockUser
    public void testRegisterWithInvalidEstablishmentId() throws Exception {
        Client mockClient = new Client("Test Client", "test@example.com", "hashedPassword", "1234567890");
        mockClient.setId(1L);
        
        when(clientService.registerClient(anyString(), anyString(), anyString(), anyString(), isNull()))
                .thenReturn(mockClient);

        Map<String, String> request = new HashMap<>();
        request.put("name", "Test Client");
        request.put("email", "test@example.com");
        request.put("password", "password123");
        request.put("phone", "1234567890");
        request.put("establishmentId", "invalid");

        mockMvc.perform(post("/api/client/register")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    public void testUpdateSelectedEstablishmentSuccess() throws Exception {
        Client mockClient = new Client("Test Client", "test@example.com", "hashedPassword", "1234567890");
        mockClient.setId(1L);
        mockClient.setSelectedEstablishmentId(5L);
        
        when(clientService.updateSelectedEstablishment(eq(1L), eq(5L)))
                .thenReturn(mockClient);

        Map<String, String> request = new HashMap<>();
        request.put("clientId", "1");
        request.put("establishmentId", "5");

        mockMvc.perform(put("/api/client/establishment")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Estabelecimento selecionado com sucesso"))
                .andExpect(jsonPath("$.client.selectedEstablishmentId").value(5));
    }

    @Test
    @WithMockUser
    public void testUpdateSelectedEstablishmentMissingClientId() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("establishmentId", "5");

        mockMvc.perform(put("/api/client/establishment")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("ID do cliente é obrigatório"));
    }

    @Test
    @WithMockUser
    public void testUpdateSelectedEstablishmentEmptyClientId() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("clientId", "");
        request.put("establishmentId", "5");

        mockMvc.perform(put("/api/client/establishment")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("ID do cliente é obrigatório"));
    }

    @Test
    @WithMockUser
    public void testUpdateSelectedEstablishmentInvalidClientId() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("clientId", "invalid");
        request.put("establishmentId", "5");

        mockMvc.perform(put("/api/client/establishment")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("ID inválido"));
    }

    @Test
    @WithMockUser
    public void testUpdateSelectedEstablishmentClientNotFound() throws Exception {
        when(clientService.updateSelectedEstablishment(eq(999L), any()))
                .thenThrow(new IllegalArgumentException("Cliente não encontrado"));

        Map<String, String> request = new HashMap<>();
        request.put("clientId", "999");
        request.put("establishmentId", "5");

        mockMvc.perform(put("/api/client/establishment")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Cliente não encontrado"));
    }

    @Test
    @WithMockUser
    public void testUpdateSelectedEstablishmentWithNullEstablishmentId() throws Exception {
        Client mockClient = new Client("Test Client", "test@example.com", "hashedPassword", "1234567890");
        mockClient.setId(1L);
        
        when(clientService.updateSelectedEstablishment(eq(1L), isNull()))
                .thenReturn(mockClient);

        Map<String, String> request = new HashMap<>();
        request.put("clientId", "1");

        mockMvc.perform(put("/api/client/establishment")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    public void testUpdateSelectedEstablishmentServiceException() throws Exception {
        when(clientService.updateSelectedEstablishment(any(), any()))
                .thenThrow(new RuntimeException("Unexpected error"));

        Map<String, String> request = new HashMap<>();
        request.put("clientId", "1");
        request.put("establishmentId", "5");

        mockMvc.perform(put("/api/client/establishment")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Erro ao atualizar estabelecimento selecionado"));
    }
}
