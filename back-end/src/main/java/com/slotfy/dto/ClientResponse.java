package com.slotfy.dto;

/**
 * DTO for client response data
 */
public class ClientResponse {
    
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Boolean active;
    private Long selectedEstablishmentId;
    
    // Default constructor
    public ClientResponse() {}
    
    // Constructor with all fields
    public ClientResponse(Long id, String name, String email, String phone, Boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.active = active;
    }
    
    // Constructor with establishment
    public ClientResponse(Long id, String name, String email, String phone, Boolean active, Long selectedEstablishmentId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.active = active;
        this.selectedEstablishmentId = selectedEstablishmentId;
    }
    
    // Getters and setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
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
}