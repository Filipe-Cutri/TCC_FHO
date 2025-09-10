package com.slotfy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entity representing an establishment user (admin or staff)
 */
@Entity
@Table(name = "establishment_users")
public class EstablishmentUser extends BaseEntity {
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(max = 255)
    @Column(name = "name", nullable = false)
    private String name;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter um formato válido")
    @Size(max = 255)
    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    @Column(name = "password", nullable = false)
    private String password;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;
    
    @Column(name = "establishment_id")
    private Long establishmentId;
    
    @Column(name = "active")
    private Boolean active = true;
    
    // Default constructor
    public EstablishmentUser() {}
    
    // Constructor with required fields
    public EstablishmentUser(String name, String email, String password, UserRole role, Long establishmentId) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.establishmentId = establishmentId;
        this.active = true;
    }
    
    // Getters and Setters
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
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public Long getEstablishmentId() {
        return establishmentId;
    }
    
    public void setEstablishmentId(Long establishmentId) {
        this.establishmentId = establishmentId;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    // Helper methods
    public boolean isAdmin() {
        return UserRole.ADMIN.equals(this.role);
    }
    
    public boolean isStaff() {
        return UserRole.STAFF.equals(this.role);
    }
}