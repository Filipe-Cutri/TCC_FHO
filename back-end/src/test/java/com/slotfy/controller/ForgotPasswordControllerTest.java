package com.slotfy.controller;

import com.slotfy.service.ForgotPasswordService;
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

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ForgotPasswordController.class)
@Import({com.slotfy.config.SecurityConfig.class})
public class ForgotPasswordControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ForgotPasswordService forgotPasswordService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testClientForgotPasswordSuccess() throws Exception {
        when(forgotPasswordService.sendClientPasswordResetEmail(anyString())).thenReturn(true);

        Map<String, String> request = new HashMap<>();
        request.put("email", "client@example.com");

        mockMvc.perform(post("/api/client/forgot-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Instruções enviadas para o e-mail"));
    }

    @Test
    @WithMockUser
    public void testClientForgotPasswordEmailNotFound() throws Exception {
        when(forgotPasswordService.sendClientPasswordResetEmail(anyString())).thenReturn(false);

        Map<String, String> request = new HashMap<>();
        request.put("email", "notfound@example.com");

        mockMvc.perform(post("/api/client/forgot-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("E-mail não encontrado"));
    }

    @Test
    @WithMockUser
    public void testClientForgotPasswordEmptyEmail() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "");

        mockMvc.perform(post("/api/client/forgot-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("E-mail é obrigatório"));
    }

    @Test
    @WithMockUser
    public void testClientForgotPasswordNullEmail() throws Exception {
        Map<String, String> request = new HashMap<>();

        mockMvc.perform(post("/api/client/forgot-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("E-mail é obrigatório"));
    }

    @Test
    @WithMockUser
    public void testClientForgotPasswordServiceException() throws Exception {
        when(forgotPasswordService.sendClientPasswordResetEmail(anyString()))
                .thenThrow(new RuntimeException("Service error"));

        Map<String, String> request = new HashMap<>();
        request.put("email", "error@example.com");

        mockMvc.perform(post("/api/client/forgot-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Erro interno do servidor"));
    }

    @Test
    @WithMockUser
    public void testEstablishmentForgotPasswordSuccess() throws Exception {
        when(forgotPasswordService.sendEstablishmentPasswordResetEmail(anyString())).thenReturn(true);

        Map<String, String> request = new HashMap<>();
        request.put("email", "establishment@example.com");

        mockMvc.perform(post("/api/establishment/forgot-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Instruções enviadas para o e-mail"));
    }

    @Test
    @WithMockUser
    public void testEstablishmentForgotPasswordEmailNotFound() throws Exception {
        when(forgotPasswordService.sendEstablishmentPasswordResetEmail(anyString())).thenReturn(false);

        Map<String, String> request = new HashMap<>();
        request.put("email", "notfound@example.com");

        mockMvc.perform(post("/api/establishment/forgot-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("E-mail não encontrado"));
    }

    @Test
    @WithMockUser
    public void testEstablishmentForgotPasswordEmptyEmail() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "");

        mockMvc.perform(post("/api/establishment/forgot-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("E-mail é obrigatório"));
    }

    @Test
    @WithMockUser
    public void testResetPasswordSuccess() throws Exception {
        when(forgotPasswordService.resetPassword(anyString(), anyString())).thenReturn(true);

        Map<String, String> request = new HashMap<>();
        request.put("token", "valid-token");
        request.put("newPassword", "newPassword123");

        mockMvc.perform(post("/api/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Senha alterada com sucesso"));
    }

    @Test
    @WithMockUser
    public void testResetPasswordInvalidToken() throws Exception {
        when(forgotPasswordService.resetPassword(anyString(), anyString())).thenReturn(false);

        Map<String, String> request = new HashMap<>();
        request.put("token", "invalid-token");
        request.put("newPassword", "newPassword123");

        mockMvc.perform(post("/api/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token inválido ou expirado"));
    }

    @Test
    @WithMockUser
    public void testResetPasswordEmptyToken() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("token", "");
        request.put("newPassword", "newPassword123");

        mockMvc.perform(post("/api/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token e nova senha são obrigatórios"));
    }

    @Test
    @WithMockUser
    public void testResetPasswordEmptyPassword() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("token", "valid-token");
        request.put("newPassword", "");

        mockMvc.perform(post("/api/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token e nova senha são obrigatórios"));
    }

    @Test
    @WithMockUser
    public void testResetPasswordNullFields() throws Exception {
        Map<String, String> request = new HashMap<>();

        mockMvc.perform(post("/api/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Token e nova senha são obrigatórios"));
    }

    @Test
    @WithMockUser
    public void testResetPasswordServiceException() throws Exception {
        when(forgotPasswordService.resetPassword(anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        Map<String, String> request = new HashMap<>();
        request.put("token", "valid-token");
        request.put("newPassword", "newPassword123");

        mockMvc.perform(post("/api/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Erro interno do servidor"));
    }
}
