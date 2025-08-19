package com.slotify.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import java.math.BigDecimal;

/**
 * Entity representing a professional working at an establishment
 */
@Entity
@Table(name = "professionals")
public class Professional extends BaseEntity {
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;
    
    @Email(message = "Email deve ter um formato válido")
    @Size(max = 255)
    @Column(name = "email")
    private String email;
    
    @Size(max = 20)
    @Column(name = "phone")
    private String phone;
    
    @Size(max = 500)
    @Column(name = "specialties")
    private String specialties;
    
    @Column(name = "establishment_id", nullable = false)
    private Long establishmentId;
    
    @Column(name = "rating", precision = 3, scale = 2)
    @Min(value = 0, message = "Avaliação deve ser no mínimo 0")
    @Max(value = 5, message = "Avaliação deve ser no máximo 5")
    private BigDecimal rating = BigDecimal.ZERO;
    
    @Column(name = "total_appointments")
    private Integer totalAppointments = 0;
    
    @Column(name = "satisfaction_rate", precision = 5, scale = 2)
    @Min(value = 0, message = "Taxa de satisfação deve ser no mínimo 0")
    @Max(value = 100, message = "Taxa de satisfação deve ser no máximo 100")
    private BigDecimal satisfactionRate = BigDecimal.ZERO;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ProfessionalStatus status = ProfessionalStatus.ACTIVE;
    
    @Size(max = 500)
    @Column(name = "image_url")
    private String imageUrl;
    
    // Constructors
    public Professional() {}
    
    public Professional(String name, String email, String phone, String specialties, Long establishmentId) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.specialties = specialties;
        this.establishmentId = establishmentId;
    }
    
    // Getters and setters
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public String getSpecialties() {
        return specialties;
    }
    
    public void setSpecialties(String specialties) {
        this.specialties = specialties;
    }
    
    public Long getEstablishmentId() {
        return establishmentId;
    }
    
    public void setEstablishmentId(Long establishmentId) {
        this.establishmentId = establishmentId;
    }
    
    public BigDecimal getRating() {
        return rating;
    }
    
    public void setRating(BigDecimal rating) {
        this.rating = rating;
    }
    
    public Integer getTotalAppointments() {
        return totalAppointments;
    }
    
    public void setTotalAppointments(Integer totalAppointments) {
        this.totalAppointments = totalAppointments;
    }
    
    public BigDecimal getSatisfactionRate() {
        return satisfactionRate;
    }
    
    public void setSatisfactionRate(BigDecimal satisfactionRate) {
        this.satisfactionRate = satisfactionRate;
    }
    
    public ProfessionalStatus getStatus() {
        return status;
    }
    
    public void setStatus(ProfessionalStatus status) {
        this.status = status;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    // Helper methods
    public boolean isActive() {
        return ProfessionalStatus.ACTIVE.equals(this.status);
    }
    
    public void incrementAppointments() {
        this.totalAppointments = (this.totalAppointments == null ? 0 : this.totalAppointments) + 1;
    }
}