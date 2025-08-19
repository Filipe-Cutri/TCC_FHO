package com.slotify.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Entity representing an appointment/booking
 */
@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity {
    
    @NotNull(message = "Cliente é obrigatório")
    @Column(name = "client_id", nullable = false)
    private Long clientId;
    
    @NotNull(message = "Profissional é obrigatório")
    @Column(name = "professional_id", nullable = false)
    private Long professionalId;
    
    @NotNull(message = "Serviço é obrigatório")
    @Column(name = "service_id", nullable = false)
    private Long serviceId;
    
    @NotNull(message = "Estabelecimento é obrigatório")
    @Column(name = "establishment_id", nullable = false)
    private Long establishmentId;
    
    @NotNull(message = "Data e hora são obrigatórias")
    @Column(name = "appointment_datetime", nullable = false)
    private LocalDateTime appointmentDateTime;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private AppointmentStatus status = AppointmentStatus.SCHEDULED;
    
    @Size(max = 1000)
    @Column(name = "notes")
    private String notes;
    
    @Size(max = 255)
    @Column(name = "client_name")
    private String clientName;
    
    @Size(max = 255)
    @Column(name = "professional_name")
    private String professionalName;
    
    @Size(max = 255)
    @Column(name = "service_name")
    private String serviceName;
    
    @Column(name = "service_duration_minutes")
    private Integer serviceDurationMinutes;
    
    @Column(name = "service_price", precision = 10, scale = 2)
    private java.math.BigDecimal servicePrice;
    
    // Constructors
    public Appointment() {}
    
    public Appointment(Long clientId, Long professionalId, Long serviceId, Long establishmentId, LocalDateTime appointmentDateTime) {
        this.clientId = clientId;
        this.professionalId = professionalId;
        this.serviceId = serviceId;
        this.establishmentId = establishmentId;
        this.appointmentDateTime = appointmentDateTime;
    }
    
    // Getters and setters
    public Long getClientId() {
        return clientId;
    }
    
    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
    
    public Long getProfessionalId() {
        return professionalId;
    }
    
    public void setProfessionalId(Long professionalId) {
        this.professionalId = professionalId;
    }
    
    public Long getServiceId() {
        return serviceId;
    }
    
    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }
    
    public Long getEstablishmentId() {
        return establishmentId;
    }
    
    public void setEstablishmentId(Long establishmentId) {
        this.establishmentId = establishmentId;
    }
    
    public LocalDateTime getAppointmentDateTime() {
        return appointmentDateTime;
    }
    
    public void setAppointmentDateTime(LocalDateTime appointmentDateTime) {
        this.appointmentDateTime = appointmentDateTime;
    }
    
    public AppointmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(AppointmentStatus status) {
        this.status = status;
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
    
    public String getProfessionalName() {
        return professionalName;
    }
    
    public void setProfessionalName(String professionalName) {
        this.professionalName = professionalName;
    }
    
    public String getServiceName() {
        return serviceName;
    }
    
    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }
    
    public Integer getServiceDurationMinutes() {
        return serviceDurationMinutes;
    }
    
    public void setServiceDurationMinutes(Integer serviceDurationMinutes) {
        this.serviceDurationMinutes = serviceDurationMinutes;
    }
    
    public java.math.BigDecimal getServicePrice() {
        return servicePrice;
    }
    
    public void setServicePrice(java.math.BigDecimal servicePrice) {
        this.servicePrice = servicePrice;
    }
    
    // Helper methods
    public boolean isScheduled() {
        return AppointmentStatus.SCHEDULED.equals(this.status);
    }
    
    public boolean isConfirmed() {
        return AppointmentStatus.CONFIRMED.equals(this.status);
    }
    
    public boolean isCancelled() {
        return AppointmentStatus.CANCELLED.equals(this.status);
    }
    
    public boolean isCompleted() {
        return AppointmentStatus.COMPLETED.equals(this.status);
    }
    
    public String getFormattedDateTime() {
        if (appointmentDateTime == null) return "";
        return appointmentDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm"));
    }
    
    public String getFormattedDate() {
        if (appointmentDateTime == null) return "";
        return appointmentDateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
    
    public String getFormattedTime() {
        if (appointmentDateTime == null) return "";
        return appointmentDateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
    
    public LocalDateTime getEndDateTime() {
        if (appointmentDateTime == null || serviceDurationMinutes == null) return null;
        return appointmentDateTime.plusMinutes(serviceDurationMinutes);
    }
}