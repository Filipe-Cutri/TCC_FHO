package com.slotfy.model;

/**
 * Enum defining service status
 */
public enum ServiceStatus {
    ACTIVE("active", "Ativo"),
    INACTIVE("inactive", "Inativo"),
    SUSPENDED("suspended", "Suspenso");
    
    private final String code;
    private final String description;
    
    ServiceStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ServiceStatus fromCode(String code) {
        for (ServiceStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid service status code: " + code);
    }
}