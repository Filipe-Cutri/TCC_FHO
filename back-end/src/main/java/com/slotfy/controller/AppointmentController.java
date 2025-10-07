package com.slotfy.controller;

import com.slotfy.model.Appointment;
import com.slotfy.model.AppointmentStatus;
import com.slotfy.service.AppointmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for Appointment management
 */
@RestController
@RequestMapping("/api/establishment/appointments")
@CrossOrigin(originPatterns = "*")
public class AppointmentController {
    
    @Autowired
    private AppointmentService appointmentService;
    
    /**
     * Get all appointments for an establishment
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAppointments(@RequestParam Long establishmentId) {
        try {
            List<Appointment> appointments = appointmentService.getByEstablishmentId(establishmentId);
            
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
     * Get appointments by status
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Map<String, Object>> getAppointmentsByStatus(
        @PathVariable String status,
        @RequestParam Long establishmentId) {
        try {
            AppointmentStatus appointmentStatus = AppointmentStatus.fromCode(status);
            List<Appointment> appointments = appointmentService.getByEstablishmentAndStatus(establishmentId, appointmentStatus);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", appointments,
                    "count", appointments.size()
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
     * Get today's appointments
     */
    @GetMapping("/today")
    public ResponseEntity<Map<String, Object>> getTodayAppointments(@RequestParam Long establishmentId) {
        try {
            List<Appointment> appointments = appointmentService.getTodayAppointments(establishmentId);
            
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
     * Get upcoming appointments
     */
    @GetMapping("/upcoming")
    public ResponseEntity<Map<String, Object>> getUpcomingAppointments(@RequestParam Long establishmentId) {
        try {
            List<Appointment> appointments = appointmentService.getUpcomingAppointments(establishmentId);
            
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
     * Get appointments in date range
     */
    @GetMapping("/range")
    public ResponseEntity<Map<String, Object>> getAppointmentsByDateRange(
        @RequestParam Long establishmentId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        try {
            List<Appointment> appointments = appointmentService.getByDateRange(establishmentId, startDate, endDate);
            
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
     * Get appointments for a professional
     */
    @GetMapping("/professional/{professionalId}")
    public ResponseEntity<Map<String, Object>> getAppointmentsByProfessional(@PathVariable Long professionalId) {
        try {
            List<Appointment> appointments = appointmentService.getByProfessional(professionalId);
            
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
     * Get appointment by ID
     * SECURITY: Validates establishment ownership to ensure multi-establishment data isolation
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getAppointment(
            @PathVariable Long id,
            @RequestParam Long establishmentId) {
        try {
            Optional<Appointment> appointment = appointmentService.findByIdAndEstablishment(id, establishmentId);
            
            if (appointment.isPresent()) {
                return ResponseEntity.ok()
                    .body(Map.of(
                        "success", true,
                        "data", appointment.get()
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
     * Create a new appointment
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createAppointment(@RequestBody Map<String, Object> request) {
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
            String clientName = (String) request.get("clientName");
            String professionalName = (String) request.get("professionalName");
            String serviceName = (String) request.get("serviceName");
            
            Integer serviceDurationMinutes = request.get("serviceDurationMinutes") != null ? 
                Integer.valueOf(request.get("serviceDurationMinutes").toString()) : null;
            BigDecimal servicePrice = request.get("servicePrice") != null ? 
                new BigDecimal(request.get("servicePrice").toString()) : null;
            
            // Basic validation
            if (clientId == null || professionalId == null || serviceId == null || 
                establishmentId == null || appointmentDateTime == null) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Campos obrigatórios: clientId, professionalId, serviceId, establishmentId, appointmentDateTime"
                    ));
            }
            
            Appointment appointment = appointmentService.createAppointment(
                clientId, professionalId, serviceId, establishmentId, appointmentDateTime,
                notes, clientName, professionalName, serviceName, serviceDurationMinutes, servicePrice
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
     * Update appointment status
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
            
            AppointmentStatus status = AppointmentStatus.fromCode(statusCode);
            Appointment appointment = appointmentService.updateStatus(id, status, establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Status atualizado com sucesso",
                    "data", appointment
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
     * Reschedule appointment
     * SECURITY: Validates establishment ownership to ensure multi-establishment data isolation
     */
    @PutMapping("/{id}/reschedule")
    public ResponseEntity<Map<String, Object>> reschedule(
            @PathVariable Long id, 
            @RequestBody Map<String, String> request,
            @RequestParam Long establishmentId) {
        try {
            String newDateTimeStr = request.get("newDateTime");
            
            if (newDateTimeStr == null || newDateTimeStr.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Nova data e hora são obrigatórias"
                    ));
            }
            
            LocalDateTime newDateTime = LocalDateTime.parse(newDateTimeStr);
            Appointment appointment = appointmentService.reschedule(id, newDateTime, establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Agendamento reagendado com sucesso",
                    "data", appointment
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
     * Update appointment notes
     * SECURITY: Validates establishment ownership to ensure multi-establishment data isolation
     */
    @PutMapping("/{id}/notes")
    public ResponseEntity<Map<String, Object>> updateNotes(
            @PathVariable Long id, 
            @RequestBody Map<String, String> request,
            @RequestParam Long establishmentId) {
        try {
            String notes = request.get("notes");
            
            Appointment appointment = appointmentService.updateNotes(id, notes, establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Observações atualizadas com sucesso",
                    "data", appointment
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
     * Cancel appointment
     * SECURITY: Validates establishment ownership to ensure multi-establishment data isolation
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelAppointment(
            @PathVariable Long id,
            @RequestParam Long establishmentId) {
        try {
            Appointment appointment = appointmentService.updateStatus(id, AppointmentStatus.CANCELLED, establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Agendamento cancelado com sucesso",
                    "data", appointment
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
     * Confirm appointment
     * SECURITY: Validates establishment ownership to ensure multi-establishment data isolation
     */
    @PutMapping("/{id}/confirm")
    public ResponseEntity<Map<String, Object>> confirmAppointment(
            @PathVariable Long id,
            @RequestParam Long establishmentId) {
        try {
            Appointment appointment = appointmentService.updateStatus(id, AppointmentStatus.CONFIRMED, establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Agendamento confirmado com sucesso",
                    "data", appointment
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
     * Complete appointment
     * SECURITY: Validates establishment ownership to ensure multi-establishment data isolation
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> completeAppointment(
            @PathVariable Long id,
            @RequestParam Long establishmentId) {
        try {
            Appointment appointment = appointmentService.updateStatus(id, AppointmentStatus.COMPLETED, establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Agendamento finalizado com sucesso",
                    "data", appointment
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
     * Check time slot availability
     */
    @GetMapping("/availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
        @RequestParam Long professionalId,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        try {
            boolean available = appointmentService.isTimeSlotAvailable(professionalId, startTime, endTime);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "available", available,
                    "message", available ? "Horário disponível" : "Horário indisponível"
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
     * Get appointment statistics
     */
    @GetMapping("/statistics")
    public ResponseEntity<Map<String, Object>> getStatistics(@RequestParam Long establishmentId) {
        try {
            long totalCount = appointmentService.countByEstablishment(establishmentId);
            long todayCount = appointmentService.countTodayAppointments(establishmentId);
            long monthCount = appointmentService.countThisMonthAppointments(establishmentId);
            BigDecimal monthlyRevenue = appointmentService.calculateMonthlyRevenue(establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", Map.of(
                        "total", totalCount,
                        "today", todayCount,
                        "thisMonth", monthCount,
                        "monthlyRevenue", monthlyRevenue
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
    
    /**
     * Get professional performance statistics
     */
    @GetMapping("/performance")
    public ResponseEntity<Map<String, Object>> getPerformanceStats(@RequestParam Long establishmentId) {
        try {
            List<Object[]> performanceStats = appointmentService.getProfessionalPerformanceStats(establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", performanceStats
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