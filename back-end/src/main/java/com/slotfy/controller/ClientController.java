package com.slotfy.controller;

import com.slotfy.model.Appointment;
import com.slotfy.model.Client;
import com.slotfy.service.AppointmentService;
import com.slotfy.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for client-specific operations
 */
@RestController
@RequestMapping("/api/client")
@CrossOrigin(originPatterns = "*")
public class ClientController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    @Autowired
    private ClientService clientService;
    
    /**
     * Get client's next upcoming appointment
     */
    @GetMapping("/appointments/next")
    public ResponseEntity<Map<String, Object>> getNextAppointment(@RequestParam Long clientId) {
        try {
            // For now, return empty result since there are no appointments in the system
            // This can be implemented fully once we have sample data
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", (Object) null,
                    "message", "Nenhum agendamento próximo encontrado"
                ));
                
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Get client's appointments
     */
    @GetMapping("/appointments")
    public ResponseEntity<Map<String, Object>> getClientAppointments(@RequestParam Long clientId) {
        try {
            // For now, return empty list since there are no appointments in the system
            // This can be implemented fully once we have sample data
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", java.util.Collections.emptyList(),
                    "count", 0
                ));
        } catch (Exception e) {
            e.printStackTrace(); // For debugging
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Get client's appointment history
     */
    @GetMapping("/appointments/history")
    public ResponseEntity<Map<String, Object>> getAppointmentHistory(@RequestParam Long clientId) {
        try {
            List<Appointment> appointments = appointmentService.getClientAppointmentHistory(clientId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", appointments,
                    "count", appointments.size()
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
     * Book a new appointment
     */
    @PostMapping("/appointments/book")
    public ResponseEntity<Map<String, Object>> bookAppointment(@RequestBody Map<String, Object> request) {
        try {
            Long clientId = request.get("clientId") != null ? 
                Long.valueOf(request.get("clientId").toString()) : null;
            Long professionalId = request.get("professionalId") != null ? 
                Long.valueOf(request.get("professionalId").toString()) : null;
            Long serviceId = request.get("serviceId") != null ? 
                Long.valueOf(request.get("serviceId").toString()) : null;
            Long establishmentId = request.get("establishmentId") != null ? 
                Long.valueOf(request.get("establishmentId").toString()) : null;
            
            String appointmentDateTimeStr = (String) request.get("appointmentDateTime");
            LocalDateTime appointmentDateTime = appointmentDateTimeStr != null ? 
                LocalDateTime.parse(appointmentDateTimeStr) : null;
            
            String notes = (String) request.get("notes");
            
            // Basic validation
            if (clientId == null || professionalId == null || serviceId == null || 
                establishmentId == null || appointmentDateTime == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Campos obrigatórios: clientId, professionalId, serviceId, establishmentId, appointmentDateTime"
                    ));
            }
            
            // Check availability
            boolean available = appointmentService.isClientTimeSlotAvailable(clientId, appointmentDateTime, appointmentDateTime.plusHours(1));
            if (!available) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Horário não disponível"
                    ));
            }
            
            Appointment appointment = appointmentService.createClientAppointment(
                clientId, professionalId, serviceId, establishmentId, appointmentDateTime, notes
            );
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Agendamento criado com sucesso",
                    "data", appointment
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
     * Get client profile
     */
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@RequestParam Long clientId) {
        try {
            Optional<Client> client = clientService.findById(clientId);
            
            if (client.isPresent()) {
                return ResponseEntity.ok()
                    .body(Map.of(
                        "success", true,
                        "data", Map.of(
                            "id", client.get().getId(),
                            "name", client.get().getName(),
                            "email", client.get().getEmail(),
                            "phone", client.get().getPhone() != null ? client.get().getPhone() : ""
                        )
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
     * Update client profile
     */
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@RequestBody Map<String, String> request) {
        try {
            Long clientId = request.get("clientId") != null ? 
                Long.valueOf(request.get("clientId")) : null;
            String name = request.get("name");
            String phone = request.get("phone");
            
            if (clientId == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "ID do cliente é obrigatório"
                    ));
            }
            
            Client updatedClient = clientService.updateProfile(clientId, name, phone);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Perfil atualizado com sucesso",
                    "data", Map.of(
                        "id", updatedClient.getId(),
                        "name", updatedClient.getName(),
                        "email", updatedClient.getEmail(),
                        "phone", updatedClient.getPhone() != null ? updatedClient.getPhone() : ""
                    )
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
}