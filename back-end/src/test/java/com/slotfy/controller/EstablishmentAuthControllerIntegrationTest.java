package com.slotfy.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slotfy.dto.EstablishmentRegisterRequest;
import com.slotfy.model.Establishment;
import com.slotfy.model.EstablishmentUser;
import com.slotfy.repository.EstablishmentRepository;
import com.slotfy.repository.EstablishmentUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EstablishmentAuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EstablishmentRepository establishmentRepository;

    @Autowired
    private EstablishmentUserRepository establishmentUserRepository;

    @AfterEach
    void cleanup() {
        // Clean up test data
        establishmentUserRepository.deleteAll();
        establishmentRepository.deleteAll();
    }

    @Test
    void testRegisterComplete_Success() throws Exception {
        // Prepare request
        EstablishmentRegisterRequest request = new EstablishmentRegisterRequest(
                "barbearia",
                "Barbearia Test",
                "test@example.com",
                "11999999999",
                "password123"
        );

        // Execute request
        String response = mockMvc.perform(post("/api/establishment/register-complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Estabelecimento registrado com sucesso!"))
                .andExpect(jsonPath("$.establishmentId").exists())
                .andExpect(jsonPath("$.userId").exists())
                .andReturn()
                .getResponse()
                .getContentAsString();

        System.out.println("Response: " + response);

        // Verify establishment was created
        Establishment establishment = establishmentRepository.findByEmail("test@example.com").orElse(null);
        assertNotNull(establishment, "Establishment should be created");
        assertEquals("Barbearia Test", establishment.getName());
        assertEquals("Barbearia", establishment.getCategory());
        assertEquals("11999999999", establishment.getPhone());

        // Verify admin user was created
        EstablishmentUser adminUser = establishmentUserRepository.findByEmail("test@example.com").orElse(null);
        assertNotNull(adminUser, "Admin user should be created");
        assertEquals("Barbearia Test", adminUser.getName());
        assertEquals(establishment.getId(), adminUser.getEstablishmentId());
    }

    @Test
    void testRegisterComplete_DuplicateEstablishmentEmail() throws Exception {
        // Create an establishment first
        EstablishmentRegisterRequest firstRequest = new EstablishmentRegisterRequest(
                "barbearia",
                "First Barbearia",
                "duplicate@example.com",
                "11999999991",
                "password123"
        );

        mockMvc.perform(post("/api/establishment/register-complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstRequest)))
                .andExpect(status().isOk());

        // Try to create another establishment with the same email
        EstablishmentRegisterRequest secondRequest = new EstablishmentRegisterRequest(
                "salao",
                "Second Salao",
                "duplicate@example.com",
                "11999999992",
                "password456"
        );

        mockMvc.perform(post("/api/establishment/register-complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void testRegisterComplete_ValidationErrors() throws Exception {
        // Request with missing fields
        EstablishmentRegisterRequest request = new EstablishmentRegisterRequest(
                "",  // empty type
                "",  // empty name
                "invalid-email",  // invalid email
                "",  // empty phone
                "123"  // password too short
        );

        mockMvc.perform(post("/api/establishment/register-complete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}
