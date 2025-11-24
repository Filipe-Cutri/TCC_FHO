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
 * Test class for RootController view endpoints
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
                .andExpect(forwardedUrl("/index.html"));
    }

    @Test
    @WithMockUser
    public void testApiIndexEndpoint() throws Exception {
        mockMvc.perform(get("/api"))
                .andExpect(status().isOk())
                .andExpect(view().name("api-index.html"));
    }
}
