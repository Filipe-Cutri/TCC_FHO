package com.slotfy.controller;

import com.slotfy.model.Professional;
import com.slotfy.model.Service;
import com.slotfy.service.FileStorageService;
import com.slotfy.service.ProfessionalService;
import com.slotfy.service.ServiceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class FileUploadControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private FileStorageService fileStorageService;
    
    @MockBean
    private ProfessionalService professionalService;
    
    @MockBean
    private ServiceService serviceService;
    
    @Test
    void testUploadProfessionalImage_Success() throws Exception {
        // Given
        Long professionalId = 1L;
        Long establishmentId = 1L;
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "professional.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );
        
        Professional professional = new Professional();
        professional.setId(professionalId);
        professional.setName("Test Professional");
        professional.setEstablishmentId(establishmentId);
        professional.setImageUrl("/uploads/professionals/test.jpg");
        
        when(fileStorageService.storeFile(any(), eq("professionals")))
            .thenReturn("professionals/test.jpg");
        when(professionalService.updateImage(eq(professionalId), anyString(), eq(establishmentId)))
            .thenReturn(professional);
        
        // When & Then
        mockMvc.perform(multipart("/api/files/professional/{id}/upload", professionalId)
                .file(file)
                .param("establishmentId", establishmentId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Imagem enviada com sucesso"))
            .andExpect(jsonPath("$.imageUrl").value("/uploads/professionals/test.jpg"));
    }
    
    @Test
    void testUploadServiceImage_Success() throws Exception {
        // Given
        Long serviceId = 1L;
        Long establishmentId = 1L;
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "service.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );
        
        Service service = new Service();
        service.setId(serviceId);
        service.setName("Test Service");
        service.setEstablishmentId(establishmentId);
        service.setPrice(BigDecimal.valueOf(50.00));
        service.setDurationMinutes(30);
        service.setImageUrl("/uploads/services/test.jpg");
        
        when(fileStorageService.storeFile(any(), eq("services")))
            .thenReturn("services/test.jpg");
        when(serviceService.updateImage(eq(serviceId), anyString(), eq(establishmentId)))
            .thenReturn(service);
        
        // When & Then
        mockMvc.perform(multipart("/api/files/service/{id}/upload", serviceId)
                .file(file)
                .param("establishmentId", establishmentId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.message").value("Imagem enviada com sucesso"))
            .andExpect(jsonPath("$.imageUrl").value("/uploads/services/test.jpg"));
    }
    
    @Test
    void testUploadProfessionalImage_InvalidFile() throws Exception {
        // Given
        Long professionalId = 1L;
        Long establishmentId = 1L;
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "professional.txt",
            "text/plain",
            "fake content".getBytes()
        );
        
        when(fileStorageService.storeFile(any(), eq("professionals")))
            .thenThrow(new IllegalArgumentException("Apenas arquivos JPG e PNG são permitidos"));
        
        // When & Then
        mockMvc.perform(multipart("/api/files/professional/{id}/upload", professionalId)
                .file(file)
                .param("establishmentId", establishmentId.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Apenas arquivos JPG e PNG são permitidos"));
    }
    
    @Test
    void testUploadServiceImage_InvalidFile() throws Exception {
        // Given
        Long serviceId = 1L;
        Long establishmentId = 1L;
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "service.gif",
            "image/gif",
            "fake content".getBytes()
        );
        
        when(fileStorageService.storeFile(any(), eq("services")))
            .thenThrow(new IllegalArgumentException("Apenas arquivos JPG e PNG são permitidos"));
        
        // When & Then
        mockMvc.perform(multipart("/api/files/service/{id}/upload", serviceId)
                .file(file)
                .param("establishmentId", establishmentId.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false));
    }
    
    @Test
    void testUploadProfessionalImage_Unauthorized() throws Exception {
        // Given
        Long professionalId = 1L;
        Long establishmentId = 1L;
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "professional.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );
        
        when(fileStorageService.storeFile(any(), eq("professionals")))
            .thenReturn("professionals/test.jpg");
        when(professionalService.updateImage(eq(professionalId), anyString(), eq(establishmentId)))
            .thenThrow(new SecurityException("Acesso negado"));
        
        // When & Then
        mockMvc.perform(multipart("/api/files/professional/{id}/upload", professionalId)
                .file(file)
                .param("establishmentId", establishmentId.toString()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Acesso negado"));
    }
    
    @Test
    void testUploadServiceImage_Unauthorized() throws Exception {
        // Given
        Long serviceId = 1L;
        Long establishmentId = 1L;
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "service.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );
        
        when(fileStorageService.storeFile(any(), eq("services")))
            .thenReturn("services/test.jpg");
        when(serviceService.updateImage(eq(serviceId), anyString(), eq(establishmentId)))
            .thenThrow(new SecurityException("Acesso negado ao serviço"));
        
        // When & Then
        mockMvc.perform(multipart("/api/files/service/{id}/upload", serviceId)
                .file(file)
                .param("establishmentId", establishmentId.toString()))
            .andExpect(status().isForbidden())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Acesso negado ao serviço"));
    }
    
    @Test
    void testUploadProfessionalImage_InternalError() throws Exception {
        // Given
        Long professionalId = 1L;
        Long establishmentId = 1L;
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "professional.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );
        
        when(fileStorageService.storeFile(any(), eq("professionals")))
            .thenThrow(new RuntimeException("Erro interno ao salvar arquivo"));
        
        // When & Then
        mockMvc.perform(multipart("/api/files/professional/{id}/upload", professionalId)
                .file(file)
                .param("establishmentId", establishmentId.toString()))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void testUploadServiceImage_InternalError() throws Exception {
        // Given
        Long serviceId = 1L;
        Long establishmentId = 1L;
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "service.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );
        
        when(fileStorageService.storeFile(any(), eq("services")))
            .thenThrow(new RuntimeException("Erro interno ao salvar arquivo"));
        
        // When & Then
        mockMvc.perform(multipart("/api/files/service/{id}/upload", serviceId)
                .file(file)
                .param("establishmentId", establishmentId.toString()))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").exists());
    }
    
    @Test
    void testUploadProfessionalImage_FileTooLarge() throws Exception {
        // Given
        Long professionalId = 1L;
        Long establishmentId = 1L;
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "professional.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );
        
        when(fileStorageService.storeFile(any(), eq("professionals")))
            .thenThrow(new IllegalArgumentException("Arquivo muito grande. Tamanho máximo: 5MB"));
        
        // When & Then
        mockMvc.perform(multipart("/api/files/professional/{id}/upload", professionalId)
                .file(file)
                .param("establishmentId", establishmentId.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Arquivo muito grande. Tamanho máximo: 5MB"));
    }
    
    @Test
    void testUploadServiceImage_FileTooLarge() throws Exception {
        // Given
        Long serviceId = 1L;
        Long establishmentId = 1L;
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "service.jpg",
            "image/jpeg",
            "fake image content".getBytes()
        );
        
        when(fileStorageService.storeFile(any(), eq("services")))
            .thenThrow(new IllegalArgumentException("Arquivo muito grande. Tamanho máximo: 5MB"));
        
        // When & Then
        mockMvc.perform(multipart("/api/files/service/{id}/upload", serviceId)
                .file(file)
                .param("establishmentId", establishmentId.toString()))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.message").value("Arquivo muito grande. Tamanho máximo: 5MB"));
    }
    
    @Test
    void testUploadProfessionalImage_PngFile() throws Exception {
        // Given
        Long professionalId = 1L;
        Long establishmentId = 1L;
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "professional.png",
            "image/png",
            "fake png content".getBytes()
        );
        
        Professional professional = new Professional();
        professional.setId(professionalId);
        professional.setName("Test Professional");
        professional.setEstablishmentId(establishmentId);
        professional.setImageUrl("/uploads/professionals/test.png");
        
        when(fileStorageService.storeFile(any(), eq("professionals")))
            .thenReturn("professionals/test.png");
        when(professionalService.updateImage(eq(professionalId), anyString(), eq(establishmentId)))
            .thenReturn(professional);
        
        // When & Then
        mockMvc.perform(multipart("/api/files/professional/{id}/upload", professionalId)
                .file(file)
                .param("establishmentId", establishmentId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.imageUrl").value("/uploads/professionals/test.png"));
    }
    
    @Test
    void testUploadServiceImage_PngFile() throws Exception {
        // Given
        Long serviceId = 1L;
        Long establishmentId = 1L;
        
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "service.png",
            "image/png",
            "fake png content".getBytes()
        );
        
        Service service = new Service();
        service.setId(serviceId);
        service.setName("Test Service");
        service.setEstablishmentId(establishmentId);
        service.setPrice(BigDecimal.valueOf(50.00));
        service.setDurationMinutes(30);
        service.setImageUrl("/uploads/services/test.png");
        
        when(fileStorageService.storeFile(any(), eq("services")))
            .thenReturn("services/test.png");
        when(serviceService.updateImage(eq(serviceId), anyString(), eq(establishmentId)))
            .thenReturn(service);
        
        // When & Then
        mockMvc.perform(multipart("/api/files/service/{id}/upload", serviceId)
                .file(file)
                .param("establishmentId", establishmentId.toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.imageUrl").value("/uploads/services/test.png"));
    }
}
