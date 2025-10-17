package com.slotfy.controller;

import com.slotfy.dto.AIRecommendationRequest;
import com.slotfy.dto.AIRecommendationResponse;
import com.slotfy.model.Appointment;
import com.slotfy.model.Client;
import com.slotfy.model.Establishment;
import com.slotfy.model.EstablishmentStatus;
import com.slotfy.model.Professional;
import com.slotfy.model.Service;
import com.slotfy.service.AISchedulingService;
import com.slotfy.service.AppointmentService;
import com.slotfy.service.ClientService;
import com.slotfy.service.EstablishmentService;
import com.slotfy.service.ProfessionalService;
import com.slotfy.service.ServiceService;
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

    @Autowired
    private AISchedulingService aiSchedulingService;

    @Autowired
    private EstablishmentService establishmentService;

    @Autowired
    private ServiceService serviceService;

    @Autowired
    private ProfessionalService professionalService;

    /**
     * Get AI scheduling recommendations
     */
    @PostMapping("/ai/recommendations")
    public ResponseEntity<Map<String, Object>> getAIRecommendations(@RequestBody AIRecommendationRequest request) {
        try {
            // Validate required fields
            if (request.getClientId() == null || request.getEstablishmentId() == null) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "clientId e establishmentId são obrigatórios"
                        ));
            }

            // Verify client exists
            Optional<Client> client = clientService.findById(request.getClientId());
            if (client.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Cliente não encontrado"
                        ));
            }

            // Generate AI recommendations
            List<AIRecommendationResponse> recommendations = aiSchedulingService.generateRecommendations(request);

            return ResponseEntity.ok()
                    .body(Map.of(
                            "success", true,
                            "data", recommendations,
                            "count", recommendations.size()
                    ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Erro ao gerar recomendações: " + e.getMessage()
                    ));
        }
    }

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
            List<Appointment> appointments = appointmentService.getByClient(clientId);
            
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
     * Get client dashboard data
     */
    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> getDashboardData(@RequestParam Long clientId) {
        try {
            Optional<Client> clientOpt = clientService.findById(clientId);

            if (clientOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Cliente não encontrado"
                        ));
            }

            Client client = clientOpt.get();
            List<Appointment> allAppointments = appointmentService.getClientAppointmentHistory(clientId);
            long totalAppointments = allAppointments.size();

            // Construir manualmente o mapa para permitir valores nulos
            Map<String, Object> clientData = Map.of(
                    "id", client.getId(),
                    "name", client.getName(),
                    "email", client.getEmail(),
                    "phone", client.getPhone() != null ? client.getPhone() : ""
            );

            Map<String, Object> statsData = Map.of(
                    "totalAppointments", totalAppointments,
                    "favoriteProfessionals", 0
            );

            Map<String, Object> data = new java.util.HashMap<>();
            data.put("client", clientData);
            data.put("stats", statsData);
            data.put("nextAppointment", null); // agora permitido

            Map<String, Object> response = Map.of(
                    "success", true,
                    "data", data
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Map.of(
                            "success", false,
                            "message", "Erro interno do servidor: " + e.getMessage()
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

    /**
     * List all active establishments for client browsing
     */
    @GetMapping("/establishments")
    public ResponseEntity<Map<String, Object>> listEstablishments() {
        try {
            List<Establishment> establishments = establishmentService.getByStatus(EstablishmentStatus.ACTIVE);
            
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
                            "message", "Erro ao listar estabelecimentos"
                    ));
        }
    }

    /**
     * Get establishment details by ID
     */
    @GetMapping("/establishments/{id}")
    public ResponseEntity<Map<String, Object>> getEstablishmentDetails(@PathVariable Long id) {
        try {
            Optional<Establishment> establishment = establishmentService.findById(id);
            
            if (establishment.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            if (!establishment.get().isActive()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Estabelecimento não está ativo"
                        ));
            }
            
            return ResponseEntity.ok()
                    .body(Map.of(
                            "success", true,
                            "data", establishment.get()
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
     * Get active services of an establishment
     */
    @GetMapping("/establishments/{id}/services")
    public ResponseEntity<Map<String, Object>> getEstablishmentServices(@PathVariable Long id) {
        try {
            // Verify establishment exists and is active
            Optional<Establishment> establishment = establishmentService.findById(id);
            if (establishment.isEmpty() || !establishment.get().isActive()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Estabelecimento não encontrado ou inativo"
                        ));
            }
            
            List<Service> services = serviceService.getActiveByEstablishmentId(id);
            
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
                            "message", "Erro ao listar serviços"
                    ));
        }
    }

    /**
     * Get active professionals of an establishment
     */
    @GetMapping("/establishments/{id}/professionals")
    public ResponseEntity<Map<String, Object>> getEstablishmentProfessionals(@PathVariable Long id) {
        try {
            // Verify establishment exists and is active
            Optional<Establishment> establishment = establishmentService.findById(id);
            if (establishment.isEmpty() || !establishment.get().isActive()) {
                return ResponseEntity.badRequest()
                        .body(Map.of(
                                "success", false,
                                "message", "Estabelecimento não encontrado ou inativo"
                        ));
            }
            
            List<Professional> professionals = professionalService.getActiveByEstablishmentId(id);
            
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
                            "message", "Erro ao listar profissionais"
                    ));
        }
    }

    /**
     * Check availability for booking
     */
    @GetMapping("/establishments/{id}/availability")
    public ResponseEntity<Map<String, Object>> checkAvailability(
            @PathVariable Long id,
            @RequestParam Long professionalId,
            @RequestParam String dateTime,
            @RequestParam Integer durationMinutes) {
        try {
            LocalDateTime startTime = LocalDateTime.parse(dateTime);
            LocalDateTime endTime = startTime.plusMinutes(durationMinutes);
            
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
                            "message", "Erro ao verificar disponibilidade"
                    ));
        }
    }

    /**
     * Get appointment details by ID (for client)
     */
    @GetMapping("/appointments/{id}")
    public ResponseEntity<Map<String, Object>> getAppointmentDetails(
            @PathVariable Long id,
            @RequestParam Long clientId) {
        try {
            Optional<Appointment> appointmentOpt = appointmentService.findById(id);
            
            if (appointmentOpt.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            Appointment appointment = appointmentOpt.get();
            
            // Verify that this appointment belongs to the requesting client
            if (!appointment.getClientId().equals(clientId)) {
                return ResponseEntity.status(403)
                        .body(Map.of(
                                "success", false,
                                "message", "Acesso negado"
                        ));
            }
            
            return ResponseEntity.ok()
                    .body(Map.of(
                            "success", true,
                            "data", appointment
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
     * Cancel appointment (client side)
     */
    @PutMapping("/appointments/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelAppointment(
            @PathVariable Long id,
            @RequestParam Long clientId) {
        try {
            Appointment appointment = appointmentService.cancelClientAppointment(id, clientId);
            
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
}