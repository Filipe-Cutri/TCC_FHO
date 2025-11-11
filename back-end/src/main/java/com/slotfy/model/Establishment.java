package com.slotfy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entity representing an establishment
 */
@Entity
@Table(name = "establishments")
public class Establishment extends BaseEntity {
    
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
    @Column(name = "address")
    private String address;
    
    @Size(max = 1000)
    @Column(name = "description")
    private String description;
    
    @Size(max = 500)
    @Column(name = "working_hours")
    private String workingHours;
    
    @Size(max = 500)
    @Column(name = "image_url")
    private String imageUrl;
    
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private EstablishmentStatus status = EstablishmentStatus.ACTIVE;
    
    @Size(max = 100)
    @Column(name = "category")
    private String category;
    
    @Size(max = 14)
    @Column(name = "cnpj")
    private String cnpj;

    @Lob
    @Column(name = "settings")
    private String settings;
    
    // Constructors
    public Establishment() {}
    
    public Establishment(String name, String email, String phone, String address) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.address = address;
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
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getWorkingHours() {
        return workingHours;
    }
    
    public void setWorkingHours(String workingHours) {
        this.workingHours = workingHours;
    }
    
    public String getImageUrl() {
        return imageUrl;
    }
    
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
    
    public EstablishmentStatus getStatus() {
        return status;
    }
    
    public void setStatus(EstablishmentStatus status) {
        this.status = status;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        this.category = category;
    }
    
    public String getCnpj() {
        return cnpj;
    }
    
    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }
    
    public String getSettings() {
        return settings;
    }
    
    public void setSettings(String settings) {
        this.settings = settings;
    }
    
    // Helper methods
    public boolean isActive() {
        return EstablishmentStatus.ACTIVE.equals(this.status);
    }
}