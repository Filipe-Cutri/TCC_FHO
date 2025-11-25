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
    public void testForgotPasswordSuccess() throws Exception {
        when(forgotPasswordService.sendClientPasswordResetEmail(anyString())).thenReturn(true);
        when(forgotPasswordService.sendEstablishmentPasswordResetEmail(anyString())).thenReturn(true);

        Map<String, String> request = new HashMap<>();
        request.put("email", "user@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .with(csrf())
                .header("X-Real-IP", "192.168.1.1")  // Unique IP for this test
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Se o e-mail existir, as instruções de redefinição foram enviadas"));
    }

    @Test
    @WithMockUser
    public void testForgotPasswordEmptyEmail() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "");

        mockMvc.perform(post("/api/auth/forgot-password")
                .with(csrf())
                .header("X-Real-IP", "192.168.1.2")  // Unique IP for this test
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("E-mail é obrigatório"));
    }

    @Test
    @WithMockUser
    public void testForgotPasswordNullEmail() throws Exception {
        Map<String, String> request = new HashMap<>();

        mockMvc.perform(post("/api/auth/forgot-password")
                .with(csrf())
                .header("X-Real-IP", "192.168.1.3")  // Unique IP for this test
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("E-mail é obrigatório"));
    }

    @Test
    @WithMockUser
    public void testForgotPasswordServiceException() throws Exception {
        when(forgotPasswordService.sendClientPasswordResetEmail(anyString()))
                .thenThrow(new RuntimeException("Service error"));

        Map<String, String> request = new HashMap<>();
        request.put("email", "error@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .with(csrf())
                .header("X-Real-IP", "192.168.1.4")  // Unique IP for this test
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Erro interno do servidor"));
    }

    @Test
    @WithMockUser
    public void testResetPasswordSuccess() throws Exception {
        when(forgotPasswordService.resetPassword(anyString(), anyString(), anyString())).thenReturn(true);

        Map<String, String> request = new HashMap<>();
        request.put("email", "user@example.com");
        request.put("token", "valid-token");
        request.put("newPassword", "newPassword123");

        mockMvc.perform(post("/api/auth/reset-password")
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
        when(forgotPasswordService.resetPassword(anyString(), anyString(), anyString())).thenReturn(false);

        Map<String, String> request = new HashMap<>();
        request.put("email", "user@example.com");
        request.put("token", "invalid-token");
        request.put("newPassword", "newPassword123");

        mockMvc.perform(post("/api/auth/reset-password")
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
        request.put("email", "user@example.com");
        request.put("token", "");
        request.put("newPassword", "newPassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("E-mail, token e nova senha são obrigatórios"));
    }

    @Test
    @WithMockUser
    public void testResetPasswordEmptyPassword() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "user@example.com");
        request.put("token", "valid-token");
        request.put("newPassword", "");

        mockMvc.perform(post("/api/auth/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("E-mail, token e nova senha são obrigatórios"));
    }

    @Test
    @WithMockUser
    public void testResetPasswordNullFields() throws Exception {
        Map<String, String> request = new HashMap<>();

        mockMvc.perform(post("/api/auth/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("E-mail, token e nova senha são obrigatórios"));
    }

    @Test
    @WithMockUser
    public void testResetPasswordServiceException() throws Exception {
        when(forgotPasswordService.resetPassword(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Service error"));

        Map<String, String> request = new HashMap<>();
        request.put("email", "user@example.com");
        request.put("token", "valid-token");
        request.put("newPassword", "newPassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Erro interno do servidor"));
    }
    
    @Test
    @WithMockUser
    public void testForgotPasswordWithForwardedForHeader() throws Exception {
        when(forgotPasswordService.sendClientPasswordResetEmail(anyString())).thenReturn(true);
        when(forgotPasswordService.sendEstablishmentPasswordResetEmail(anyString())).thenReturn(true);

        Map<String, String> request = new HashMap<>();
        request.put("email", "user@example.com");

        mockMvc.perform(post("/api/auth/forgot-password")
                .with(csrf())
                .header("X-Forwarded-For", "10.0.0.1, 192.168.1.100")  // Multiple IPs in forwarded header
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser
    public void testForgotPasswordWithNoHeaders() throws Exception {
        when(forgotPasswordService.sendClientPasswordResetEmail(anyString())).thenReturn(true);
        when(forgotPasswordService.sendEstablishmentPasswordResetEmail(anyString())).thenReturn(true);

        Map<String, String> request = new HashMap<>();
        request.put("email", "user2@example.com");

        // No X-Forwarded-For or X-Real-IP headers
        mockMvc.perform(post("/api/auth/forgot-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
    
    @Test
    @WithMockUser
    public void testForgotPasswordWithWhitespaceEmail() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "   ");

        mockMvc.perform(post("/api/auth/forgot-password")
                .with(csrf())
                .header("X-Real-IP", "192.168.1.20")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("E-mail é obrigatório"));
    }
    
    @Test
    @WithMockUser
    public void testResetPasswordEmptyEmail() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "");
        request.put("token", "valid-token");
        request.put("newPassword", "newPassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("E-mail, token e nova senha são obrigatórios"));
    }
    
    @Test
    @WithMockUser
    public void testResetPasswordWhitespaceEmail() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "   ");
        request.put("token", "valid-token");
        request.put("newPassword", "newPassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("E-mail, token e nova senha são obrigatórios"));
    }
    
    @Test
    @WithMockUser
    public void testResetPasswordWhitespaceToken() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "user@example.com");
        request.put("token", "   ");
        request.put("newPassword", "newPassword123");

        mockMvc.perform(post("/api/auth/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("E-mail, token e nova senha são obrigatórios"));
    }
    
    @Test
    @WithMockUser
    public void testResetPasswordWhitespaceNewPassword() throws Exception {
        Map<String, String> request = new HashMap<>();
        request.put("email", "user@example.com");
        request.put("token", "valid-token");
        request.put("newPassword", "   ");

        mockMvc.perform(post("/api/auth/reset-password")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("E-mail, token e nova senha são obrigatórios"));
    }
}
