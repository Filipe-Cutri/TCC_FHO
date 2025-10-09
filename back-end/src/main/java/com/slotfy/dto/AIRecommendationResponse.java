package com.slotfy.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for AI scheduling recommendations
 */
public class AIRecommendationResponse {
    
    private Long id;
    private String service;
    private Long serviceId;
    private String professional;
    private Long professionalId;
    private String establishment;
    private Long establishmentId;
    private LocalDateTime date;
    private String time;
    private BigDecimal price;
    private Integer confidence;
    private String reason;
    
    public AIRecommendationResponse() {}
    
    public AIRecommendationResponse(Long id, String service, Long serviceId, String professional, 
                                   Long professionalId, String establishment, Long establishmentId, 
                                   LocalDateTime date, String time, BigDecimal price, 
                                   Integer confidence, String reason) {
        this.id = id;
        this.service = service;
        this.serviceId = serviceId;
        this.professional = professional;
        this.professionalId = professionalId;
        this.establishment = establishment;
        this.establishmentId = establishmentId;
        this.date = date;
        this.time = time;
        this.price = price;
        this.confidence = confidence;
        this.reason = reason;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getService() {
        return service;
    }
    
    public void setService(String service) {
        this.service = service;
    }
    
    public Long getServiceId() {
        return serviceId;
    }
    
    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }
    
    public String getProfessional() {
        return professional;
    }
    
    public void setProfessional(String professional) {
        this.professional = professional;
    }
    
    public Long getProfessionalId() {
        return professionalId;
    }
    
    public void setProfessionalId(Long professionalId) {
        this.professionalId = professionalId;
    }
    
    public String getEstablishment() {
        return establishment;
    }
    
    public void setEstablishment(String establishment) {
        this.establishment = establishment;
    }
    
    public Long getEstablishmentId() {
        return establishmentId;
    }
    
    public void setEstablishmentId(Long establishmentId) {
        this.establishmentId = establishmentId;
    }
    
    public LocalDateTime getDate() {
        return date;
    }
    
    public void setDate(LocalDateTime date) {
        this.date = date;
    }
    
    public String getTime() {
        return time;
    }
    
    public void setTime(String time) {
        this.time = time;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Integer getConfidence() {
        return confidence;
    }
    
    public void setConfidence(Integer confidence) {
        this.confidence = confidence;
    }
    
    public String getReason() {
        return reason;
    }
    
    public void setReason(String reason) {
        this.reason = reason;
    }
}
