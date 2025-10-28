package com.slotfy.controller;

import com.slotfy.model.Payment;
import com.slotfy.model.PaymentMethod;
import com.slotfy.model.PaymentStatus;
import com.slotfy.service.PaymentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@Import({com.slotfy.config.SecurityConfig.class})
public class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser
    public void testGetClientPayments() throws Exception {
        List<Payment> payments = Arrays.asList(
            createMockPayment(1L, new BigDecimal("100.00"), PaymentStatus.COMPLETED),
            createMockPayment(2L, new BigDecimal("200.00"), PaymentStatus.PENDING)
        );
        
        when(paymentService.getClientPayments(1L)).thenReturn(payments);

        mockMvc.perform(get("/api/payments/client/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(2))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testGetClientPaymentsError() throws Exception {
        when(paymentService.getClientPayments(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/payments/client/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testGetEstablishmentPayments() throws Exception {
        List<Payment> payments = Arrays.asList(
            createMockPayment(1L, new BigDecimal("100.00"), PaymentStatus.COMPLETED)
        );
        
        when(paymentService.getEstablishmentPayments(1L)).thenReturn(payments);

        mockMvc.perform(get("/api/payments/establishment/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.count").value(1))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @WithMockUser
    public void testGetEstablishmentPaymentsError() throws Exception {
        when(paymentService.getEstablishmentPayments(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/payments/establishment/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testCreatePaymentWithoutAppointment() throws Exception {
        Payment payment = createMockPayment(1L, new BigDecimal("100.00"), PaymentStatus.PENDING);
        
        when(paymentService.createPayment(anyLong(), anyLong(), any(BigDecimal.class), any(PaymentMethod.class)))
                .thenReturn(payment);
        when(paymentService.save(any(Payment.class))).thenReturn(payment);

        Map<String, Object> request = new HashMap<>();
        request.put("clientId", "1");
        request.put("establishmentId", "1");
        request.put("amount", "100.00");
        request.put("paymentMethod", "CREDIT_CARD");

        mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pagamento criado com sucesso"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser
    public void testCreatePaymentWithAppointment() throws Exception {
        Payment payment = createMockPayment(1L, new BigDecimal("100.00"), PaymentStatus.PENDING);
        
        when(paymentService.createAppointmentPayment(anyLong(), anyLong(), anyLong(), any(BigDecimal.class), any(PaymentMethod.class)))
                .thenReturn(payment);
        when(paymentService.save(any(Payment.class))).thenReturn(payment);

        Map<String, Object> request = new HashMap<>();
        request.put("clientId", "1");
        request.put("establishmentId", "1");
        request.put("appointmentId", "1");
        request.put("amount", "100.00");
        request.put("paymentMethod", "CREDIT_CARD");

        mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pagamento criado com sucesso"));
    }

    @Test
    @WithMockUser
    public void testCreatePaymentWithAdditionalFields() throws Exception {
        Payment payment = createMockPayment(1L, new BigDecimal("100.00"), PaymentStatus.PENDING);
        
        when(paymentService.createPayment(anyLong(), anyLong(), any(BigDecimal.class), any(PaymentMethod.class)))
                .thenReturn(payment);
        when(paymentService.save(any(Payment.class))).thenReturn(payment);

        Map<String, Object> request = new HashMap<>();
        request.put("clientId", "1");
        request.put("establishmentId", "1");
        request.put("amount", "100.00");
        request.put("paymentMethod", "CREDIT_CARD");
        request.put("clientName", "John Doe");
        request.put("establishmentName", "Test Establishment");
        request.put("serviceName", "Test Service");
        request.put("notes", "Test notes");

        mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser
    public void testCreatePaymentInvalidArgument() throws Exception {
        when(paymentService.createPayment(anyLong(), anyLong(), any(BigDecimal.class), any(PaymentMethod.class)))
                .thenThrow(new IllegalArgumentException("Invalid payment method"));

        Map<String, Object> request = new HashMap<>();
        request.put("clientId", "1");
        request.put("establishmentId", "1");
        request.put("amount", "100.00");
        request.put("paymentMethod", "INVALID");

        mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testCreatePaymentError() throws Exception {
        when(paymentService.createPayment(anyLong(), anyLong(), any(BigDecimal.class), any(PaymentMethod.class)))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, Object> request = new HashMap<>();
        request.put("clientId", "1");
        request.put("establishmentId", "1");
        request.put("amount", "100.00");
        request.put("paymentMethod", "CREDIT_CARD");

        mockMvc.perform(post("/api/payments")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testCompletePaymentSuccess() throws Exception {
        Payment payment = createMockPayment(1L, new BigDecimal("100.00"), PaymentStatus.COMPLETED);
        
        when(paymentService.completePayment(anyLong(), anyString())).thenReturn(payment);

        Map<String, String> request = new HashMap<>();
        request.put("transactionId", "TXN123456");

        mockMvc.perform(put("/api/payments/1/complete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pagamento concluído com sucesso"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser
    public void testCompletePaymentInvalidArgument() throws Exception {
        when(paymentService.completePayment(anyLong(), anyString()))
                .thenThrow(new IllegalArgumentException("Payment not found"));

        Map<String, String> request = new HashMap<>();
        request.put("transactionId", "TXN123456");

        mockMvc.perform(put("/api/payments/1/complete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testCompletePaymentError() throws Exception {
        when(paymentService.completePayment(anyLong(), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, String> request = new HashMap<>();
        request.put("transactionId", "TXN123456");

        mockMvc.perform(put("/api/payments/1/complete")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testCancelPaymentSuccess() throws Exception {
        Payment payment = createMockPayment(1L, new BigDecimal("100.00"), PaymentStatus.CANCELLED);
        
        when(paymentService.cancelPayment(anyLong(), anyString())).thenReturn(payment);

        Map<String, String> request = new HashMap<>();
        request.put("reason", "Customer request");

        mockMvc.perform(put("/api/payments/1/cancel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Pagamento cancelado com sucesso"))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser
    public void testCancelPaymentInvalidArgument() throws Exception {
        when(paymentService.cancelPayment(anyLong(), anyString()))
                .thenThrow(new IllegalArgumentException("Payment not found"));

        Map<String, String> request = new HashMap<>();
        request.put("reason", "Customer request");

        mockMvc.perform(put("/api/payments/1/cancel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testCancelPaymentError() throws Exception {
        when(paymentService.cancelPayment(anyLong(), anyString()))
                .thenThrow(new RuntimeException("Database error"));

        Map<String, String> request = new HashMap<>();
        request.put("reason", "Customer request");

        mockMvc.perform(put("/api/payments/1/cancel")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testGetPaymentStatistics() throws Exception {
        when(paymentService.calculateTotalRevenue(1L)).thenReturn(new BigDecimal("1000.00"));
        when(paymentService.countPaymentsByStatus(1L, PaymentStatus.COMPLETED)).thenReturn(10L);
        when(paymentService.countPaymentsByStatus(1L, PaymentStatus.PENDING)).thenReturn(5L);

        mockMvc.perform(get("/api/payments/establishment/1/statistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalRevenue").value(1000.00))
                .andExpect(jsonPath("$.data.completedPayments").value(10))
                .andExpect(jsonPath("$.data.pendingPayments").value(5));
    }

    @Test
    @WithMockUser
    public void testGetPaymentStatisticsError() throws Exception {
        when(paymentService.calculateTotalRevenue(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/payments/establishment/1/statistics"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @WithMockUser
    public void testGetPayment() throws Exception {
        Payment payment = createMockPayment(1L, new BigDecimal("100.00"), PaymentStatus.COMPLETED);
        
        when(paymentService.findById(1L)).thenReturn(Optional.of(payment));

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").exists());
    }

    @Test
    @WithMockUser
    public void testGetPaymentNotFound() throws Exception {
        when(paymentService.findById(1L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser
    public void testGetPaymentError() throws Exception {
        when(paymentService.findById(1L))
                .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(get("/api/payments/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }

    private Payment createMockPayment(Long id, BigDecimal amount, PaymentStatus status) {
        Payment payment = new Payment();
        payment.setId(id);
        payment.setAmount(amount);
        payment.setStatus(status);
        payment.setPaymentMethod(PaymentMethod.CREDIT_CARD);
        payment.setCreatedAt(LocalDateTime.now());
        return payment;
    }
}
