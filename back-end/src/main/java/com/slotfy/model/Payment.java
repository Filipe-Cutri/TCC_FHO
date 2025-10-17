package com.slotfy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity representing a payment transaction
 */
@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {
    
    @NotNull(message = "Cliente é obrigatório")
    @Column(name = "client_id", nullable = false)
    private Long clientId;
    
    @NotNull(message = "Estabelecimento é obrigatório")
    @Column(name = "establishment_id", nullable = false)
    private Long establishmentId;
    
    @Column(name = "appointment_id")
    private Long appointmentId;
    
    @NotNull(message = "Valor é obrigatório")
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @NotNull(message = "Método de pagamento é obrigatório")
    @Column(name = "payment_method", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;
    
    @NotNull(message = "Status é obrigatório")
    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private PaymentStatus status = PaymentStatus.PENDING;
    
    @Column(name = "payment_date")
    private LocalDateTime paymentDate;
    
    @Size(max = 500)
    @Column(name = "transaction_id")
    private String transactionId;
    
    @Size(max = 1000)
    @Column(name = "notes")
    private String notes;
    
    @Size(max = 255)
    @Column(name = "client_name")
    private String clientName;
    
    @Size(max = 255)
    @Column(name = "establishment_name")
    private String establishmentName;
    
    @Size(max = 255)
    @Column(name = "service_name")
    private String serviceName;
    
    // Constructors
    public Payment() {}
    
    public Payment(Long clientId, Long establishmentId, BigDecimal amount, PaymentMethod paymentMethod) {
        this.clientId = clientId;
        this.establishmentId = establishmentId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = PaymentStatus.PENDING;
    }
    
    // Getters and setters
    public Long getClientId() {
        return clientId;
    }
    
    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
    
    public Long getEstablishmentId() {
        return establishmentId;
    }
    
    public void setEstablishmentId(Long establishmentId) {
        this.establishmentId = establishmentId;
    }
    
    public Long getAppointmentId() {
        return appointmentId;
    }
    
    public void setAppointmentId(Long appointmentId) {
        this.appointmentId = appointmentId;
    }
    
    public BigDecimal getAmount() {
        return amount;
    }
    
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }
    
    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }
    
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }
    
    public PaymentStatus getStatus() {
        return status;
    }
    
    public void setStatus(PaymentStatus status) {
        this.status = status;
    }
    
    public LocalDateTime getPaymentDate() {
        return paymentDate;
    }
    
    public void setPaymentDate(LocalDateTime paymentDate) {
        this.paymentDate = paymentDate;
    }
    
    public String getTransactionId() {
        return transactionId;
    }
    
    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public void setNotes(String notes) {
        this.notes = notes;
    }
    
    public String getClientName() {
        return clientName;
    }
    
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }
    
    public String getEstablishmentName() {
        return establishmentName;
    }
    
    public void setEstablishmentName(String establishmentName) {
        this.establishmentName = establishmentName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    // Helper methods
    public boolean isPending() {
        return PaymentStatus.PENDING.equals(this.status);
    }
    
    public boolean isCompleted() {
        return PaymentStatus.COMPLETED.equals(this.status);
    }
    
    public boolean isCancelled() {
        return PaymentStatus.CANCELLED.equals(this.status);
    }
    
    public void markAsCompleted() {
        this.status = PaymentStatus.COMPLETED;
        this.paymentDate = LocalDateTime.now();
    }
    
    public void markAsCancelled() {
        this.status = PaymentStatus.CANCELLED;
    }
}
