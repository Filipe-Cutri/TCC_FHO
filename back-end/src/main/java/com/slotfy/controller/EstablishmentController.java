package com.slotfy.controller;

import com.slotfy.model.Establishment;
import com.slotfy.model.EstablishmentStatus;
import com.slotfy.service.EstablishmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for Establishment management
 */
@RestController
@RequestMapping("/api/establishment")
@CrossOrigin(originPatterns = "*")
public class EstablishmentController {
    
    @Autowired
    private EstablishmentService establishmentService;
    
    /**
     * Get all active establishments for client selection
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> listActiveEstablishments() {
        try {
            List<Establishment> establishments = establishmentService.getByStatus(EstablishmentStatus.ACTIVE);
            
            // Create simplified response with only necessary fields
            List<Map<String, Object>> simplifiedEstablishments = new java.util.ArrayList<>();
            for (Establishment est : establishments) {
                Map<String, Object> estMap = new java.util.HashMap<>();
                estMap.put("id", est.getId());
                estMap.put("name", est.getName());
                estMap.put("category", est.getCategory() != null ? est.getCategory() : "");
                estMap.put("address", est.getAddress() != null ? est.getAddress() : "");
                estMap.put("description", est.getDescription() != null ? est.getDescription() : "");
                estMap.put("imageUrl", est.getImageUrl() != null ? est.getImageUrl() : "");
                simplifiedEstablishments.add(estMap);
            }
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", simplifiedEstablishments,
                    "count", simplifiedEstablishments.size()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao listar estabelecimentos"
                ));
        }
    }
    
    /**
     * Get establishment by ID
     */
    @GetMapping("/profile/{id}")
    public ResponseEntity<Map<String, Object>> getEstablishment(@PathVariable Long id) {
        try {
            Optional<Establishment> establishment = establishmentService.findById(id);
            
            if (establishment.isPresent()) {
                return ResponseEntity.ok()
                    .body(Map.of(
                        "success", true,
                        "data", establishment.get()
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
     * Create a new establishment
     */
    @PostMapping("/profile")
    public ResponseEntity<Map<String, Object>> createEstablishment(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String email = (String) request.get("email");
            String phone = (String) request.get("phone");
            String address = (String) request.get("address");
            String description = (String) request.get("description");
            String category = (String) request.get("category");
            String cnpj = (String) request.get("cnpj");
            
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Nome é obrigatório"
                    ));
            }
            
            Establishment establishment = establishmentService.createEstablishment(
                name, email, phone, address, description, category, cnpj
            );
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Estabelecimento criado com sucesso",
                    "data", establishment
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
     * Update establishment information
     */
    @PutMapping("/profile/{id}")
    public ResponseEntity<Map<String, Object>> updateEstablishment(@PathVariable Long id, @RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String email = (String) request.get("email");
            String phone = (String) request.get("phone");
            String address = (String) request.get("address");
            String description = (String) request.get("description");
            String category = (String) request.get("category");
            String cnpj = (String) request.get("cnpj");
            String workingHours = (String) request.get("workingHours");
            
            Establishment establishment = establishmentService.updateEstablishment(
                id, name, email, phone, address, description, category, cnpj, workingHours
            );
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Estabelecimento atualizado com sucesso",
                    "data", establishment
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
     * Update establishment status
     */
    @PutMapping("/profile/{id}/status")
    public ResponseEntity<Map<String, Object>> updateStatus(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String statusCode = request.get("status");
            
            if (statusCode == null || statusCode.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Status é obrigatório"
                    ));
            }
            
            EstablishmentStatus status = EstablishmentStatus.fromCode(statusCode);
            Establishment establishment = establishmentService.updateStatus(id, status);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Status atualizado com sucesso",
                    "data", establishment
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
     * Update establishment settings
     */
    @PutMapping("/profile/{id}/settings")
    public ResponseEntity<Map<String, Object>> updateSettings(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String settings = request.get("settings");
            
            Establishment establishment = establishmentService.updateSettings(id, settings);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Configurações atualizadas com sucesso",
                    "data", establishment
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
     * Update establishment image
     */
    @PutMapping("/profile/{id}/image")
    public ResponseEntity<Map<String, Object>> updateImage(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String imageUrl = request.get("imageUrl");
            
            Establishment establishment = establishmentService.updateImage(id, imageUrl);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Imagem atualizada com sucesso",
                    "data", establishment
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
     * Get establishments by status
     */
    @GetMapping("/profile/status/{status}")
    public ResponseEntity<Map<String, Object>> getEstablishmentsByStatus(@PathVariable String status) {
        try {
            EstablishmentStatus establishmentStatus = EstablishmentStatus.fromCode(status);
            List<Establishment> establishments = establishmentService.getByStatus(establishmentStatus);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", establishments,
                    "count", establishments.size()
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
     * Get establishments by category
     */
    @GetMapping("/profile/category/{category}")
    public ResponseEntity<Map<String, Object>> getEstablishmentsByCategory(@PathVariable String category) {
        try {
            List<Establishment> establishments = establishmentService.getByCategory(category);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", establishments,
                    "count", establishments.size()
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
     * Get all categories
     */
    @GetMapping("/profile/categories")
    public ResponseEntity<Map<String, Object>> getCategories() {
        try {
            List<String> categories = establishmentService.getCategories();
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", categories,
                    "count", categories.size()
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
     * Search establishments
     */
    @GetMapping("/profile/search")
    public ResponseEntity<Map<String, Object>> searchEstablishments(@RequestParam String term) {
        try {
            List<Establishment> establishments = establishmentService.searchEstablishments(term);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", establishments,
                    "count", establishments.size()
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
     * Find establishment by email
     */
    @GetMapping("/profile/email/{email}")
    public ResponseEntity<Map<String, Object>> findByEmail(@PathVariable String email) {
        try {
            Optional<Establishment> establishment = establishmentService.findByEmail(email);
            
            if (establishment.isPresent()) {
                return ResponseEntity.ok()
                    .body(Map.of(
                        "success", true,
                        "data", establishment.get()
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
     * Find establishment by CNPJ
     */
    @GetMapping("/profile/cnpj/{cnpj}")
    public ResponseEntity<Map<String, Object>> findByCnpj(@PathVariable String cnpj) {
        try {
            Optional<Establishment> establishment = establishmentService.findByCnpj(cnpj);
            
            if (establishment.isPresent()) {
                return ResponseEntity.ok()
                    .body(Map.of(
                        "success", true,
                        "data", establishment.get()
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
     * Activate establishment
     */
    @PutMapping("/profile/{id}/activate")
    public ResponseEntity<Map<String, Object>> activateEstablishment(@PathVariable Long id) {
        try {
            Establishment establishment = establishmentService.activateEstablishment(id);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Estabelecimento ativado com sucesso",
                    "data", establishment
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
     * Deactivate establishment
     */
    @PutMapping("/profile/{id}/deactivate")
    public ResponseEntity<Map<String, Object>> deactivateEstablishment(@PathVariable Long id) {
        try {
            Establishment establishment = establishmentService.deactivateEstablishment(id);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Estabelecimento desativado com sucesso",
                    "data", establishment
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
     * Get establishment statistics
     */
    @GetMapping("/profile/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        try {
            long activeCount = establishmentService.countByStatus(EstablishmentStatus.ACTIVE);
            long inactiveCount = establishmentService.countByStatus(EstablishmentStatus.INACTIVE);
            long suspendedCount = establishmentService.countByStatus(EstablishmentStatus.SUSPENDED);
            long pendingCount = establishmentService.countByStatus(EstablishmentStatus.PENDING);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", Map.of(
                        "active", activeCount,
                        "inactive", inactiveCount,
                        "suspended", suspendedCount,
                        "pending", pendingCount,
                        "total", activeCount + inactiveCount + suspendedCount + pendingCount
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