package com.slotfy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Entity representing a service offered by an establishment
 */
@Entity
@Table(name = "services")
public class Service extends BaseEntity {
    
    @NotBlank(message = "Nome do serviço é obrigatório")
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;
    
    @Size(max = 1000)
    @Column(name = "description")
    private String description;
    
    @NotNull(message = "Duração é obrigatória")
    @Min(value = 1, message = "Duração deve ser no mínimo 1 minuto")
    @Column(name = "duration_minutes", nullable = false)
    private Integer durationMinutes;
    
    @NotNull(message = "Preço é obrigatório")
    @Min(value = 0, message = "Preço deve ser no mínimo 0")
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "establishment_id", nullable = false)
    private Long establishmentId;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ServiceStatus status = ServiceStatus.ACTIVE;
    
    @Size(max = 100)
    @Column(name = "category")
    private String category;
    
    @Size(max = 500)
    @Column(name = "image_url")
    private String imageUrl;
    
    // Constructors
    public Service() {}
    
    public Service(String name, String description, Integer durationMinutes, BigDecimal price, Long establishmentId) {
        this.name = name;
        this.description = description;
        this.durationMinutes = durationMinutes;
        this.price = price;
        this.establishmentId = establishmentId;
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public Integer getDurationMinutes() {
        return durationMinutes;
    }
    
    public void setDurationMinutes(Integer durationMinutes) {
        this.durationMinutes = durationMinutes;
    }
    
    public BigDecimal getPrice() {
        return price;
    }
    
    public void setPrice(BigDecimal price) {
        this.price = price;
    }
    
    public Long getEstablishmentId() {
        return establishmentId;
    }
    
    public void setEstablishmentId(Long establishmentId) {
        this.establishmentId = establishmentId;
    }
    
    public ServiceStatus getStatus() {
        return status;
    }
    
    public void setStatus(ServiceStatus status) {
        this.status = status;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    // Helper methods
    public boolean isActive() {
        return ServiceStatus.ACTIVE.equals(this.status);
    }
    
    public String getFormattedPrice() {
        return String.format("R$ %.2f", this.price);
    }
    
    public String getFormattedDuration() {
        if (durationMinutes == null) return "";
        
        if (durationMinutes < 60) {
            return durationMinutes + " min";
        } else {
            int hours = durationMinutes / 60;
            int minutes = durationMinutes % 60;
            if (minutes == 0) {
                return hours + "h";
            } else {
                return hours + "h " + minutes + "min";
            }
        }
    }
}