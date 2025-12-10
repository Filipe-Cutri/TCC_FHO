package com.slotfy.controller;

import com.slotfy.service.FileStorageService;
import com.slotfy.service.ProfessionalService;
import com.slotfy.service.ServiceService;
import com.slotfy.service.EstablishmentService;
import com.slotfy.model.Professional;
import com.slotfy.model.Service;
import com.slotfy.model.Establishment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
import java.util.Optional;

/**
 * Controller for handling file uploads
 */
@RestController
@RequestMapping("/api/files")
@CrossOrigin(originPatterns = "*")
public class FileUploadController {
    
    @Autowired
    private FileStorageService fileStorageService;
    
    @Autowired
    private ProfessionalService professionalService;
    
    @Autowired
    private ServiceService serviceService;
    
    @Autowired
    private EstablishmentService establishmentService;
    
    /**
     * Upload image for a professional
     */
    @PostMapping("/professional/{id}/upload")
    public ResponseEntity<Map<String, Object>> uploadProfessionalImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("establishmentId") Long establishmentId) {
        try {
            // Store the file
            String filePath = fileStorageService.storeFile(file, "professionals");
            
            // Update professional with new image path
            String imageUrl = "/uploads/" + filePath;
            Professional professional = professionalService.updateImage(id, imageUrl, establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Imagem enviada com sucesso",
                    "imageUrl", imageUrl,
                    "data", professional
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao enviar imagem: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Upload image for a service
     */
    @PostMapping("/service/{id}/upload")
    public ResponseEntity<Map<String, Object>> uploadServiceImage(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file,
            @RequestParam("establishmentId") Long establishmentId) {
        try {
            // Store the file
            String filePath = fileStorageService.storeFile(file, "services");
            
            // Update service with new image path
            String imageUrl = "/uploads/" + filePath;
            Service service = serviceService.updateImage(id, imageUrl, establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Imagem enviada com sucesso",
                    "imageUrl", imageUrl,
                    "data", service
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao enviar imagem: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Upload logo for an establishment
     */
    @PostMapping("/establishment/{id}/upload")
    public ResponseEntity<Map<String, Object>> uploadEstablishmentLogo(
            @PathVariable Long id,
            @RequestParam("file") MultipartFile file) {
        try {
            
            // Store the file
            String filePath = fileStorageService.storeFile(file, "establishments");
            
            // Update establishment with new image path
            String imageUrl = "/uploads/" + filePath;
            Optional<Establishment> establishmentOpt = establishmentService.findById(id);
            
            if (establishmentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Establishment establishment = establishmentOpt.get();
            establishment.setImageUrl(imageUrl);
            establishment = establishmentService.save(establishment);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Logo enviado com sucesso",
                    "imageUrl", imageUrl,
                    "data", establishment
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao enviar logo: " + e.getMessage()
                ));
        }
    }
}
