package com.slotfy.controller;

import com.slotfy.model.Payment;
import com.slotfy.model.PaymentMethod;
import com.slotfy.model.PaymentStatus;
import com.slotfy.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Controller for Payment operations
 */
@RestController
@RequestMapping("/api/payments")
@CrossOrigin(originPatterns = "*")
public class PaymentController {
    
    @Autowired
    private PaymentService paymentService;
    
    /**
     * Get all payments for a client
     */
    @GetMapping("/client/{clientId}")
    public ResponseEntity<Map<String, Object>> getClientPayments(@PathVariable Long clientId) {
        try {
            List<Payment> payments = paymentService.getClientPayments(clientId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", payments,
                    "count", payments.size()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao buscar pagamentos: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Get all payments for an establishment
     */
    @GetMapping("/establishment/{establishmentId}")
    public ResponseEntity<Map<String, Object>> getEstablishmentPayments(@PathVariable Long establishmentId) {
        try {
            List<Payment> payments = paymentService.getEstablishmentPayments(establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", payments,
                    "count", payments.size()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao buscar pagamentos: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Create a payment
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> createPayment(@RequestBody Map<String, Object> request) {
        try {
            Long clientId = Long.parseLong(request.get("clientId").toString());
            Long establishmentId = Long.parseLong(request.get("establishmentId").toString());
            BigDecimal amount = new BigDecimal(request.get("amount").toString());
            String paymentMethodStr = (String) request.get("paymentMethod");
            
            PaymentMethod paymentMethod = PaymentMethod.fromCode(paymentMethodStr);
            
            Payment payment;
            if (request.containsKey("appointmentId") && request.get("appointmentId") != null) {
                Long appointmentId = Long.parseLong(request.get("appointmentId").toString());
                payment = paymentService.createAppointmentPayment(clientId, establishmentId, appointmentId, amount, paymentMethod);
            } else {
                payment = paymentService.createPayment(clientId, establishmentId, amount, paymentMethod);
            }
            
            // Set additional fields if provided
            if (request.containsKey("clientName")) {
                payment.setClientName((String) request.get("clientName"));
            }
            if (request.containsKey("establishmentName")) {
                payment.setEstablishmentName((String) request.get("establishmentName"));
            }
            if (request.containsKey("serviceName")) {
                payment.setServiceName((String) request.get("serviceName"));
            }
            if (request.containsKey("notes")) {
                payment.setNotes((String) request.get("notes"));
            }
            
            payment = paymentService.save(payment);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Pagamento criado com sucesso",
                    "data", payment
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
                    "message", "Erro ao criar pagamento: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Complete a payment
     */
    @PutMapping("/{id}/complete")
    public ResponseEntity<Map<String, Object>> completePayment(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String transactionId = request.get("transactionId");
            Payment payment = paymentService.completePayment(id, transactionId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Pagamento concluído com sucesso",
                    "data", payment
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
                    "message", "Erro ao concluir pagamento: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Cancel a payment
     */
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Map<String, Object>> cancelPayment(@PathVariable Long id, @RequestBody Map<String, String> request) {
        try {
            String reason = request.get("reason");
            Payment payment = paymentService.cancelPayment(id, reason);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Pagamento cancelado com sucesso",
                    "data", payment
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
                    "message", "Erro ao cancelar pagamento: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Get payment statistics for an establishment
     */
    @GetMapping("/establishment/{establishmentId}/statistics")
    public ResponseEntity<Map<String, Object>> getPaymentStatistics(@PathVariable Long establishmentId) {
        try {
            BigDecimal totalRevenue = paymentService.calculateTotalRevenue(establishmentId);
            long completedCount = paymentService.countPaymentsByStatus(establishmentId, PaymentStatus.COMPLETED);
            long pendingCount = paymentService.countPaymentsByStatus(establishmentId, PaymentStatus.PENDING);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", Map.of(
                        "totalRevenue", totalRevenue,
                        "completedPayments", completedCount,
                        "pendingPayments", pendingCount
                    )
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao buscar estatísticas: " + e.getMessage()
                ));
        }
    }
    
    /**
     * Get payment details
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getPayment(@PathVariable Long id) {
        try {
            var payment = paymentService.findById(id);
            
            if (payment.isEmpty()) {
                return ResponseEntity.notFound().build();
            }
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "data", payment.get()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao buscar pagamento: " + e.getMessage()
                ));
        }
    }
}
