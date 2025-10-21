package com.slotfy.controller;

import com.slotfy.model.Professional;
import com.slotfy.model.ProfessionalStatus;
import com.slotfy.service.ProfessionalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for Professional management
 */
@RestController
@RequestMapping("/api/establishment/professionals")
@CrossOrigin(originPatterns = "*")
public class ProfessionalController {
    
    @Autowired
    private ProfessionalService professionalService;
    
    /**
     * Get all professionals for an establishment
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getProfessionals(@RequestParam Long establishmentId) {
        try {
            List<Professional> professionals = professionalService.getByEstablishmentId(establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", professionals,
                    "count", professionals.size()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Get active professionals for an establishment
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveProfessionals(@RequestParam Long establishmentId) {
        try {
            List<Professional> professionals = professionalService.getActiveByEstablishmentId(establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", professionals,
                    "count", professionals.size()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Get professional by ID
     * SECURITY: Validates establishment ownership to ensure multi-establishment data isolation
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getProfessional(
            @PathVariable Long id,
            @RequestParam Long establishmentId) {
        try {
            Optional<Professional> professional = professionalService.findByIdAndEstablishment(id, establishmentId);
            
            if (professional.isPresent()) {
                return ResponseEntity.ok()
                    .body(Map.of(
                        "success", true,
                        "data", professional.get()
                    ));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Create a new professional
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createProfessional(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String email = (String) request.get("email");
            String phone = (String) request.get("phone");
            String specialties = (String) request.get("specialties");
            Long establishmentId = request.get("establishmentId") != null ? 
                Long.valueOf(request.get("establishmentId").toString()) : null;
            
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Nome é obrigatório"
                    ));
            }
            
            if (establishmentId == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "ID do estabelecimento é obrigatório"
                    ));
            }
            
            Professional professional = professionalService.createProfessional(
                name, email, phone, specialties, establishmentId
            );
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Profissional criado com sucesso",
                    "data", professional
                ));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Update a professional
     * SECURITY: Validates establishment ownership to ensure multi-establishment data isolation
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateProfessional(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> request,
            @RequestParam Long establishmentId) {
        try {
            String name = (String) request.get("name");
            String email = (String) request.get("email");
            String phone = (String) request.get("phone");
            String specialties = (String) request.get("specialties");
            
            Professional professional = professionalService.updateProfessional(id, name, email, phone, specialties, establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Profissional atualizado com sucesso",
                    "data", professional
                ));
                
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Update professional status
     * SECURITY: Validates establishment ownership to ensure multi-establishment data isolation
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, String> request,
            @RequestParam Long establishmentId) {
        try {
            String statusCode = request.get("status");
            
            if (statusCode == null || statusCode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Status é obrigatório"
                    ));
            }
            
            ProfessionalStatus status = ProfessionalStatus.fromCode(statusCode);
            Professional professional = professionalService.updateStatus(id, status, establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Status atualizado com sucesso",
                    "data", professional
                ));
                
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Update professional statistics
     */
    @PutMapping("/{id}/statistics")
    public ResponseEntity<Map<String, Object>> updateStatistics(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            BigDecimal rating = request.get("rating") != null ? 
                new BigDecimal(request.get("rating").toString()) : null;
            BigDecimal satisfactionRate = request.get("satisfactionRate") != null ? 
                new BigDecimal(request.get("satisfactionRate").toString()) : null;
            
            Professional professional = professionalService.updateStatistics(id, rating, satisfactionRate);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Estatísticas atualizadas com sucesso",
                    "data", professional
                ));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Get top rated professionals
     */
    @GetMapping("/top-rated")
    public ResponseEntity<Map<String, Object>> getTopRatedProfessionals(
        @RequestParam Long establishmentId,
        @RequestParam(defaultValue = "4.0") BigDecimal minRating) {
        try {
            List<Professional> professionals = professionalService.getTopRatedProfessionals(establishmentId, minRating);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", professionals,
                    "count", professionals.size()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Search professionals by specialty
     */
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchBySpecialty(
        @RequestParam Long establishmentId,
        @RequestParam String specialty) {
        try {
            List<Professional> professionals = professionalService.searchBySpecialty(establishmentId, specialty);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", professionals,
                    "count", professionals.size()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Delete professional
     * SECURITY: Validates establishment ownership to ensure multi-establishment data isolation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteProfessional(
            @PathVariable Long id,
            @RequestParam Long establishmentId) {
        try {
            professionalService.deleteProfessional(id, establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Profissional removido com sucesso"
                ));
                
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Update professional image
     * SECURITY: Validates establishment ownership to ensure multi-establishment data isolation
     */
    @PutMapping("/{id}/image")
    public ResponseEntity<Map<String, Object>> updateImage(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestParam Long establishmentId) {
        try {
            String imageUrl = request.get("imageUrl");
            
            Professional professional = professionalService.updateImage(id, imageUrl, establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Imagem atualizada com sucesso",
                    "data", professional
                ));
                
        } catch (SecurityException e) {
            return ResponseEntity.status(403)
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
    
    /**
     * Get professional statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(@RequestParam Long establishmentId) {
        try {
            long totalCount = professionalService.countByEstablishment(establishmentId);
            long activeCount = professionalService.countActiveByEstablishment(establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", Map.of(
                        "total", totalCount,
                        "active", activeCount,
                        "inactive", totalCount - activeCount
                    )
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
}