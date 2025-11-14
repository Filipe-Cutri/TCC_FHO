package com.slotfy.controller;

import com.slotfy.service.EmailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmailTestController.class)
@Import({com.slotfy.config.SecurityConfig.class})
public class EmailTestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailService emailService;

    @Test
    @WithMockUser
    public void testSendTestEmailSuccess() throws Exception {
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/test/email/send-test")
                .with(csrf())
                .param("to", "test@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Email de teste enviado com sucesso para test@example.com"));
    }

    @Test
    @WithMockUser
    public void testSendTestEmailFailure() throws Exception {
        when(emailService.sendEmail(anyString(), anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/test/email/send-test")
                .with(csrf())
                .param("to", "test@example.com"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testSendTestEmailException() throws Exception {
        when(emailService.sendEmail(anyString(), anyString(), anyString()))
            .thenThrow(new RuntimeException("Email service error"));

        mockMvc.perform(post("/api/test/email/send-test")
                .with(csrf())
                .param("to", "test@example.com"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser
    public void testSendResetPasswordEmailSuccess() throws Exception {
        when(emailService.sendPasswordResetEmail(anyString(), anyString())).thenReturn(true);

        mockMvc.perform(post("/api/test/email/send-reset-password")
                .with(csrf())
                .param("to", "test@example.com"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    public void testSendResetPasswordEmailFailure() throws Exception {
        when(emailService.sendPasswordResetEmail(anyString(), anyString())).thenReturn(false);

        mockMvc.perform(post("/api/test/email/send-reset-password")
                .with(csrf())
                .param("to", "test@example.com"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testGetStatus() throws Exception {
        mockMvc.perform(get("/api/test/email/status")
                .with(csrf()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("ok"))
            .andExpect(jsonPath("$.message").value("Email test controller is active"));
    }
}
