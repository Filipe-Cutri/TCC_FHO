package com.slotfy.service;

import com.slotfy.model.Payment;
import com.slotfy.model.PaymentMethod;
import com.slotfy.model.PaymentStatus;
import com.slotfy.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService
 */
@ExtendWith(MockitoExtension.class)
public class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentService paymentService;

    private Payment testPayment;

    @BeforeEach
    void setUp() {
        paymentService = new PaymentService(paymentRepository);

        // Create test payment
        testPayment = new Payment(1L, 1L, new BigDecimal("100.00"), PaymentMethod.CREDIT_CARD);
        testPayment.setId(1L);
    }

    @Test
    void testCreatePayment_Success() {
        // Arrange
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        Payment result = paymentService.createPayment(1L, 1L, new BigDecimal("100.00"), PaymentMethod.CREDIT_CARD);

        // Assert
        assertNotNull(result);
        assertEquals(new BigDecimal("100.00"), result.getAmount());
        assertEquals(PaymentMethod.CREDIT_CARD, result.getPaymentMethod());
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testCreatePayment_NullAmount() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.createPayment(1L, 1L, null, PaymentMethod.CREDIT_CARD);
        });

        assertEquals("Valor do pagamento deve ser maior que zero", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testCreatePayment_ZeroAmount() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.createPayment(1L, 1L, BigDecimal.ZERO, PaymentMethod.CREDIT_CARD);
        });

        assertEquals("Valor do pagamento deve ser maior que zero", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testCreatePayment_NegativeAmount() {
        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.createPayment(1L, 1L, new BigDecimal("-50.00"), PaymentMethod.CREDIT_CARD);
        });

        assertEquals("Valor do pagamento deve ser maior que zero", exception.getMessage());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testCreateAppointmentPayment_Success() {
        // Arrange
        testPayment.setAppointmentId(10L);
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        Payment result = paymentService.createAppointmentPayment(1L, 1L, 10L, new BigDecimal("100.00"), PaymentMethod.CREDIT_CARD);

        // Assert
        assertNotNull(result);
        assertEquals(10L, result.getAppointmentId());
        verify(paymentRepository, times(2)).save(any(Payment.class));
    }

    @Test
    void testGetClientPayments_Success() {
        // Arrange
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByClientIdOrderByCreatedAtDesc(1L)).thenReturn(payments);

        // Act
        List<Payment> result = paymentService.getClientPayments(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository).findByClientIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void testGetEstablishmentPayments_Success() {
        // Arrange
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByEstablishmentIdOrderByCreatedAtDesc(1L)).thenReturn(payments);

        // Act
        List<Payment> result = paymentService.getEstablishmentPayments(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository).findByEstablishmentIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void testGetPaymentsByStatus_Success() {
        // Arrange
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByEstablishmentIdAndStatusOrderByCreatedAtDesc(1L, PaymentStatus.PENDING)).thenReturn(payments);

        // Act
        List<Payment> result = paymentService.getPaymentsByStatus(1L, PaymentStatus.PENDING);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository).findByEstablishmentIdAndStatusOrderByCreatedAtDesc(1L, PaymentStatus.PENDING);
    }

    @Test
    void testGetPaymentsInDateRange_Success() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(7);
        LocalDateTime endDate = LocalDateTime.now();
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByEstablishmentIdAndDateRange(1L, startDate, endDate)).thenReturn(payments);

        // Act
        List<Payment> result = paymentService.getPaymentsInDateRange(1L, startDate, endDate);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository).findByEstablishmentIdAndDateRange(1L, startDate, endDate);
    }

    @Test
    void testGetAppointmentPayments_Success() {
        // Arrange
        List<Payment> payments = Arrays.asList(testPayment);
        when(paymentRepository.findByAppointmentId(10L)).thenReturn(payments);

        // Act
        List<Payment> result = paymentService.getAppointmentPayments(10L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(paymentRepository).findByAppointmentId(10L);
    }

    @Test
    void testCompletePayment_Success() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        Payment result = paymentService.completePayment(1L, "TXN12345");

        // Assert
        assertNotNull(result);
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testCompletePayment_WithoutTransactionId() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        Payment result = paymentService.completePayment(1L, null);

        // Assert
        assertNotNull(result);
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testCompletePayment_PaymentNotFound() {
        // Arrange
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.completePayment(999L, "TXN12345");
        });

        assertEquals("Pagamento não encontrado", exception.getMessage());
        verify(paymentRepository).findById(999L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testCancelPayment_Success() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        Payment result = paymentService.cancelPayment(1L, "Customer request");

        // Assert
        assertNotNull(result);
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testCancelPayment_WithoutReason() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        Payment result = paymentService.cancelPayment(1L, null);

        // Assert
        assertNotNull(result);
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testCancelPayment_PaymentNotFound() {
        // Arrange
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.cancelPayment(999L, "Reason");
        });

        assertEquals("Pagamento não encontrado", exception.getMessage());
        verify(paymentRepository).findById(999L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testUpdatePaymentStatus_Success() {
        // Arrange
        when(paymentRepository.findById(1L)).thenReturn(Optional.of(testPayment));
        when(paymentRepository.save(any(Payment.class))).thenReturn(testPayment);

        // Act
        Payment result = paymentService.updatePaymentStatus(1L, PaymentStatus.COMPLETED);

        // Assert
        assertNotNull(result);
        verify(paymentRepository).findById(1L);
        verify(paymentRepository).save(any(Payment.class));
    }

    @Test
    void testUpdatePaymentStatus_PaymentNotFound() {
        // Arrange
        when(paymentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            paymentService.updatePaymentStatus(999L, PaymentStatus.COMPLETED);
        });

        assertEquals("Pagamento não encontrado", exception.getMessage());
        verify(paymentRepository).findById(999L);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void testCalculateTotalRevenue_Success() {
        // Arrange
        BigDecimal expectedRevenue = new BigDecimal("1500.00");
        when(paymentRepository.calculateTotalRevenue(1L)).thenReturn(expectedRevenue);

        // Act
        BigDecimal result = paymentService.calculateTotalRevenue(1L);

        // Assert
        assertEquals(expectedRevenue, result);
        verify(paymentRepository).calculateTotalRevenue(1L);
    }

    @Test
    void testCalculateRevenueForPeriod_Success() {
        // Arrange
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);
        LocalDateTime endDate = LocalDateTime.now();
        BigDecimal expectedRevenue = new BigDecimal("500.00");
        when(paymentRepository.calculateRevenueForPeriod(1L, startDate, endDate)).thenReturn(expectedRevenue);

        // Act
        BigDecimal result = paymentService.calculateRevenueForPeriod(1L, startDate, endDate);

        // Assert
        assertEquals(expectedRevenue, result);
        verify(paymentRepository).calculateRevenueForPeriod(1L, startDate, endDate);
    }

    @Test
    void testCountPaymentsByStatus_Success() {
        // Arrange
        when(paymentRepository.countByEstablishmentIdAndStatus(1L, PaymentStatus.COMPLETED)).thenReturn(10L);

        // Act
        long result = paymentService.countPaymentsByStatus(1L, PaymentStatus.COMPLETED);

        // Assert
        assertEquals(10L, result);
        verify(paymentRepository).countByEstablishmentIdAndStatus(1L, PaymentStatus.COMPLETED);
    }
}
