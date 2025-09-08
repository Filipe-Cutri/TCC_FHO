package com.slotfy.dto;

/**
 * DTO for establishment registration response
 */
public class EstablishmentRegisterResponse {
    
    private boolean success;
    private String message;
    private Long establishmentId;
    private Long userId;
    private String establishmentName;
    private String userEmail;
    private String userRole;
    
    // Default constructor
    public EstablishmentRegisterResponse() {}
    
    // Constructor for success response
    public EstablishmentRegisterResponse(boolean success, String message, Long establishmentId, 
                                       Long userId, String establishmentName, String userEmail, String userRole) {
        this.success = success;
        this.message = message;
        this.establishmentId = establishmentId;
        this.userId = userId;
        this.establishmentName = establishmentName;
        this.userEmail = userEmail;
        this.userRole = userRole;
    }
    
    // Constructor for error response
    public EstablishmentRegisterResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }
    
    // Getters and setters
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public Long getEstablishmentId() {
        return establishmentId;
    }
    
    public void setEstablishmentId(Long establishmentId) {
        this.establishmentId = establishmentId;
    }
    
    public Long getUserId() {
        return userId;
    }
    
    public void setUserId(Long userId) {
        this.userId = userId;
    }
    
    public String getEstablishmentName() {
        return establishmentName;
    }
    
    public void setEstablishmentName(String establishmentName) {
        this.establishmentName = establishmentName;
    }
    
    public String getUserEmail() {
        return userEmail;
    }
    
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }
    
    public String getUserRole() {
        return userRole;
    }
    
    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }
}