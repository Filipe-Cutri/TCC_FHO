package com.slotfy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest({RootController.class, ApiInfoController.class})
@Import({com.slotfy.config.SecurityConfig.class})
public class RootControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testApiInfoEndpoint() throws Exception {
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("Slotfy Backend"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.status").value("running"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.endpoints").exists())
                .andExpect(jsonPath("$.endpoints.health").value("/api/health"))
                .andExpect(jsonPath("$.endpoints.establishment_login").value("/api/establishment/login"))
                .andExpect(jsonPath("$.endpoints.client_login").value("/api/client/login"));
    }

    @Test
    @WithMockUser
    public void testApiInfoEndpointReturnsAllEndpoints() throws Exception {
        mockMvc.perform(get("/api/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.endpoints.client_register").value("/api/client/register"))
                .andExpect(jsonPath("$.endpoints.establishment_register").value("/api/establishment/register"))
                .andExpect(jsonPath("$.endpoints.client_forgot_password").value("/api/client/forgot-password"))
                .andExpect(jsonPath("$.endpoints.establishment_forgot_password").value("/api/establishment/forgot-password"));
    }
}
