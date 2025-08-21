package com.slotify.dto;

import java.time.LocalDateTime;

/**
 * DTO for appointment creation request
 */
public class AppointmentCreateRequest {
    
    private Long clientId;
    private Long professionalId;
    private Long serviceId;
    private Long establishmentId;
    private LocalDateTime appointmentDateTime;
    private String notes;
    private String clientName;
    private String professionalName;
    private String serviceName;
    
    // Default constructor
    public AppointmentCreateRequest() {}
    
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
}