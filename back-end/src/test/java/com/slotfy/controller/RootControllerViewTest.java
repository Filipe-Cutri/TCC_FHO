package com.slotfy.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test class for RootController endpoints
 */
@WebMvcTest(RootController.class)
@Import({com.slotfy.config.SecurityConfig.class})
public class RootControllerViewTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser
    public void testIndexEndpoint() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("Slotfy Backend"))
                .andExpect(jsonPath("$.version").value("1.0.0"))
                .andExpect(jsonPath("$.status").value("running"))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.api_info").value("/api/info"))
                .andExpect(jsonPath("$.health_check").value("/api/health"));
    }

    @Test
    @WithMockUser
    public void testApiIndexEndpoint() throws Exception {
        mockMvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.api_info_endpoint").value("/api/info"));
    }
    
    @Test
    @WithMockUser
    public void testFaviconEndpoint() throws Exception {
        mockMvc.perform(get("/favicon.ico"))
                .andExpect(status().isNoContent());
    }
}
