package com.slotfy.controller;

import com.slotfy.model.Service;
import com.slotfy.model.ServiceStatus;
import com.slotfy.service.ServiceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for Service management
 */
@RestController
@RequestMapping("/api/establishment/services")
@CrossOrigin(originPatterns = "*")
public class ServiceController {
    
    @Autowired
    private ServiceService serviceService;
    
    /**
     * Get all services for an establishment
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getServices(@RequestParam Long establishmentId) {
        try {
            List<Service> services = serviceService.getByEstablishmentId(establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", services,
                    "count", services.size()
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
     * Get active services for an establishment
     */
    @GetMapping("/active")
    public ResponseEntity<Map<String, Object>> getActiveServices(@RequestParam Long establishmentId) {
        try {
            List<Service> services = serviceService.getActiveByEstablishmentId(establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", services,
                    "count", services.size()
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
     * Get service by ID
     * SECURITY: Validates establishment ownership to ensure multi-establishment data isolation
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getService(
            @PathVariable Long id,
            @RequestParam Long establishmentId) {
        try {
            Optional<Service> service = serviceService.findByIdAndEstablishment(id, establishmentId);
            
            if (service.isPresent()) {
                return ResponseEntity.ok()
                    .body(Map.of(
                        "success", true,
                        "data", service.get()
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
     * Create a new service
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createService(@RequestBody Map<String, Object> request) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            Integer durationMinutes = request.get("durationMinutes") != null ? 
                Integer.valueOf(request.get("durationMinutes").toString()) : null;
            BigDecimal price = request.get("price") != null ? 
                new BigDecimal(request.get("price").toString()) : null;
            Long establishmentId = request.get("establishmentId") != null ? 
                Long.valueOf(request.get("establishmentId").toString()) : null;
            String category = (String) request.get("category");
            
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Nome do serviço é obrigatório"
                    ));
            }
            
            if (durationMinutes == null || durationMinutes <= 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Duração deve ser maior que zero"
                    ));
            }
            
            if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Preço não pode ser negativo"
                    ));
            }
            
            if (establishmentId == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "ID do estabelecimento é obrigatório"
                    ));
            }
            
            Service service = serviceService.createService(
                name, description, durationMinutes, price, establishmentId, category
            );
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Serviço criado com sucesso",
                    "data", service
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
     * Update a service
     * SECURITY: Validates establishment ownership to ensure multi-establishment data isolation
     */
    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> updateService(
            @PathVariable Long id, 
            @RequestBody Map<String, Object> request,
            @RequestParam Long establishmentId) {
        try {
            String name = (String) request.get("name");
            String description = (String) request.get("description");
            Integer durationMinutes = request.get("durationMinutes") != null ? 
                Integer.valueOf(request.get("durationMinutes").toString()) : null;
            BigDecimal price = request.get("price") != null ? 
                new BigDecimal(request.get("price").toString()) : null;
            String category = (String) request.get("category");
            
            Service service = serviceService.updateService(id, name, description, durationMinutes, price, category, establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Serviço atualizado com sucesso",
                    "data", service
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
     * Update service status
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
            
            ServiceStatus status = ServiceStatus.fromCode(statusCode);
            Service service = serviceService.updateStatus(id, status, establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Status atualizado com sucesso",
                    "data", service
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
     * Get services by category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<Map<String, Object>> getServicesByCategory(
        @PathVariable String category,
        @RequestParam Long establishmentId) {
        try {
            List<Service> services = serviceService.getByCategory(establishmentId, category);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", services,
                    "count", services.size()
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
     * Get service categories
     */
    @GetMapping("/categories")
    public ResponseEntity<Map<String, Object>> getCategories(@RequestParam Long establishmentId) {
        try {
            List<String> categories = serviceService.getCategories(establishmentId);
            
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
     * Search services by price range
     */
    @GetMapping("/search/price")
    public ResponseEntity<Map<String, Object>> searchByPriceRange(
        @RequestParam Long establishmentId,
        @RequestParam BigDecimal minPrice,
        @RequestParam BigDecimal maxPrice) {
        try {
            List<Service> services = serviceService.getByPriceRange(establishmentId, minPrice, maxPrice);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", services,
                    "count", services.size()
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
     * Search services by duration range
     */
    @GetMapping("/search/duration")
    public ResponseEntity<Map<String, Object>> searchByDurationRange(
        @RequestParam Long establishmentId,
        @RequestParam Integer minDuration,
        @RequestParam Integer maxDuration) {
        try {
            List<Service> services = serviceService.getByDurationRange(establishmentId, minDuration, maxDuration);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", services,
                    "count", services.size()
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
     * Delete service
     * SECURITY: Validates establishment ownership to ensure multi-establishment data isolation
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> deleteService(
            @PathVariable Long id,
            @RequestParam Long establishmentId) {
        try {
            serviceService.deleteService(id, establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Serviço removido com sucesso"
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
     * Update service image
     * SECURITY: Validates establishment ownership to ensure multi-establishment data isolation
     */
    @PutMapping("/{id}/image")
    public ResponseEntity<Map<String, Object>> updateImage(
            @PathVariable Long id,
            @RequestBody Map<String, String> request,
            @RequestParam Long establishmentId) {
        try {
            String imageUrl = request.get("imageUrl");
            
            Service service = serviceService.updateImage(id, imageUrl, establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Imagem atualizada com sucesso",
                    "data", service
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
     * Get service statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(@RequestParam Long establishmentId) {
        try {
            long totalCount = serviceService.countByEstablishment(establishmentId);
            long activeCount = serviceService.countActiveByEstablishment(establishmentId);
            List<String> categories = serviceService.getCategories(establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", Map.of(
                        "total", totalCount,
                        "active", activeCount,
                        "inactive", totalCount - activeCount,
                        "categories", categories.size()
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