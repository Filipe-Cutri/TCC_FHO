package com.slotfy.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Entity representing a client user
 */
@Entity
@Table(name = "clients")
public class Client extends BaseEntity {
    
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
    @Size(min = 6, message = "Senha deve ter pelo menos 6 caracteres")
    @Column(name = "password", nullable = false)
    private String password;
    
    @Size(max = 20)
    @Column(name = "phone")
    private String phone;
    
    @Column(name = "active")
    private Boolean active = true;
    
    @Column(name = "selected_establishment_id")
    private Long selectedEstablishmentId;
    
    @Column(name = "reset_password_token_hash")
    private String resetPasswordTokenHash;
    
    @Column(name = "reset_password_expiry")
    private Long resetPasswordExpiry;
    
    // Default constructor
    public Client() {}
    
    // Constructor with required fields
    public Client(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.active = true;
    }
    
    // Constructor with all fields
    public Client(String name, String email, String password, String phone) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.phone = phone;
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
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public Boolean getActive() {
        return active;
    }
    
    public void setActive(Boolean active) {
        this.active = active;
    }
    
    public Long getSelectedEstablishmentId() {
        return selectedEstablishmentId;
    }
    
    public void setSelectedEstablishmentId(Long selectedEstablishmentId) {
        this.selectedEstablishmentId = selectedEstablishmentId;
    }
    
    public String getResetPasswordTokenHash() {
        return resetPasswordTokenHash;
    }
    
    public void setResetPasswordTokenHash(String resetPasswordTokenHash) {
        this.resetPasswordTokenHash = resetPasswordTokenHash;
    }
    
    public Long getResetPasswordExpiry() {
        return resetPasswordExpiry;
    }
    
    public void setResetPasswordExpiry(Long resetPasswordExpiry) {
        this.resetPasswordExpiry = resetPasswordExpiry;
    }
}