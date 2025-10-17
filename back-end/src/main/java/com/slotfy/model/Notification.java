package com.slotfy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Entity representing a notification/reminder for clients
 */
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {
    
    @NotNull(message = "Cliente é obrigatório")
    @Column(name = "client_id", nullable = false)
    private Long clientId;
    
    @NotNull(message = "Estabelecimento é obrigatório")
    @Column(name = "establishment_id", nullable = false)
    private Long establishmentId;
    
    @Column(name = "appointment_id")
    private Long appointmentId;
    
    @NotBlank(message = "Título é obrigatório")
    @Size(max = 255)
    @Column(name = "title", nullable = false)
    private String title;
    
    @NotBlank(message = "Mensagem é obrigatória")
    @Size(max = 1000)
    @Column(name = "message", nullable = false)
    private String message;
    
    @NotNull(message = "Tipo é obrigatório")
    @Column(name = "type", nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;
    
    @Column(name = "read")
    private Boolean read = false;
    
    @Column(name = "sent")
    private Boolean sent = false;
    
    @Column(name = "scheduled_for")
    private LocalDateTime scheduledFor;
    
    @Column(name = "sent_at")
    private LocalDateTime sentAt;
    
    // Constructors
    public Notification() {}
    
    public Notification(Long clientId, Long establishmentId, String title, String message, NotificationType type) {
        this.clientId = clientId;
        this.establishmentId = establishmentId;
        this.title = title;
        this.message = message;
        this.type = type;
        this.read = false;
        this.sent = false;
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
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public NotificationType getType() {
        return type;
    }
    
    public void setType(NotificationType type) {
        this.type = type;
    }
    
    public Boolean getRead() {
        return read;
    }
    
    public void setRead(Boolean read) {
        this.read = read;
    }
    
    public Boolean getSent() {
        return sent;
    }
    
    public void setSent(Boolean sent) {
        this.sent = sent;
    }
    
    public LocalDateTime getScheduledFor() {
        return scheduledFor;
    }
    
    public void setScheduledFor(LocalDateTime scheduledFor) {
        this.scheduledFor = scheduledFor;
    }
    
    public LocalDateTime getSentAt() {
        return sentAt;
    }
    
    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
    
    // Helper methods
    public boolean isRead() {
        return Boolean.TRUE.equals(this.read);
    }
    
    public boolean isSent() {
        return Boolean.TRUE.equals(this.sent);
    }
    
    public void markAsRead() {
        this.read = true;
    }
    
    public void markAsSent() {
        this.sent = true;
        this.sentAt = LocalDateTime.now();
    }
}
