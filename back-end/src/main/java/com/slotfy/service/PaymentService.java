package com.slotfy.service;

import com.slotfy.model.Payment;
import com.slotfy.model.PaymentMethod;
import com.slotfy.model.PaymentStatus;
import com.slotfy.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Service for managing payments
 */
@Service
public class PaymentService extends BaseService<Payment, Long> {
    
    @Autowired
    private PaymentRepository paymentRepository;
    
    public PaymentService(PaymentRepository repository) {
        super(repository);
        this.paymentRepository = repository;
    }
    
    /**
     * Create a payment
     */
    public Payment createPayment(Long clientId, Long establishmentId, BigDecimal amount, PaymentMethod paymentMethod) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Valor do pagamento deve ser maior que zero");
        }
        
        Payment payment = new Payment(clientId, establishmentId, amount, paymentMethod);
        return save(payment);
    }
    
    /**
     * Create a payment for an appointment
     */
    public Payment createAppointmentPayment(Long clientId, Long establishmentId, Long appointmentId, 
                                          BigDecimal amount, PaymentMethod paymentMethod) {
        Payment payment = createPayment(clientId, establishmentId, amount, paymentMethod);
        payment.setAppointmentId(appointmentId);
        return save(payment);
    }
    
    /**
     * Get all payments for a client
     */
    public List<Payment> getClientPayments(Long clientId) {
        return paymentRepository.findByClientIdOrderByCreatedAtDesc(clientId);
    }
    
    /**
     * Get all payments for an establishment
     */
    public List<Payment> getEstablishmentPayments(Long establishmentId) {
        return paymentRepository.findByEstablishmentIdOrderByCreatedAtDesc(establishmentId);
    }
    
    /**
     * Get payments by status for an establishment
     */
    public List<Payment> getPaymentsByStatus(Long establishmentId, PaymentStatus status) {
        return paymentRepository.findByEstablishmentIdAndStatusOrderByCreatedAtDesc(establishmentId, status);
    }
    
    /**
     * Get payments in date range
     */
    public List<Payment> getPaymentsInDateRange(Long establishmentId, LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.findByEstablishmentIdAndDateRange(establishmentId, startDate, endDate);
    }
    
    /**
     * Get payments for an appointment
     */
    public List<Payment> getAppointmentPayments(Long appointmentId) {
        return paymentRepository.findByAppointmentId(appointmentId);
    }
    
    /**
     * Complete a payment
     */
    public Payment completePayment(Long paymentId, String transactionId) {
        Optional<Payment> paymentOpt = findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new IllegalArgumentException("Pagamento não encontrado");
        }
        
        Payment payment = paymentOpt.get();
        payment.markAsCompleted();
        if (transactionId != null && !transactionId.trim().isEmpty()) {
            payment.setTransactionId(transactionId);
        }
        return save(payment);
    }
    
    /**
     * Cancel a payment
     */
    public Payment cancelPayment(Long paymentId, String reason) {
        Optional<Payment> paymentOpt = findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new IllegalArgumentException("Pagamento não encontrado");
        }
        
        Payment payment = paymentOpt.get();
        payment.markAsCancelled();
        if (reason != null && !reason.trim().isEmpty()) {
            payment.setNotes(reason);
        }
        return save(payment);
    }
    
    /**
     * Update payment status
     */
    public Payment updatePaymentStatus(Long paymentId, PaymentStatus status) {
        Optional<Payment> paymentOpt = findById(paymentId);
        if (paymentOpt.isEmpty()) {
            throw new IllegalArgumentException("Pagamento não encontrado");
        }
        
        Payment payment = paymentOpt.get();
        payment.setStatus(status);
        if (status == PaymentStatus.COMPLETED && payment.getPaymentDate() == null) {
            payment.setPaymentDate(LocalDateTime.now());
        }
        return save(payment);
    }
    
    /**
     * Calculate total revenue for an establishment
     */
    public BigDecimal calculateTotalRevenue(Long establishmentId) {
        return paymentRepository.calculateTotalRevenue(establishmentId);
    }
    
    /**
     * Calculate revenue for a specific period
     */
    public BigDecimal calculateRevenueForPeriod(Long establishmentId, LocalDateTime startDate, LocalDateTime endDate) {
        return paymentRepository.calculateRevenueForPeriod(establishmentId, startDate, endDate);
    }
    
    /**
     * Count payments by status
     */
    public long countPaymentsByStatus(Long establishmentId, PaymentStatus status) {
        return paymentRepository.countByEstablishmentIdAndStatus(establishmentId, status);
    }
}
