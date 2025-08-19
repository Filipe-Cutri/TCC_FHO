package com.slotify.model;

/**
 * Enum defining establishment status
 */
public enum EstablishmentStatus {
    ACTIVE("active", "Ativo"),
    INACTIVE("inactive", "Inativo"),
    SUSPENDED("suspended", "Suspenso"),
    PENDING("pending", "Pendente");
    
    private final String code;
    private final String description;
    
    EstablishmentStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static EstablishmentStatus fromCode(String code) {
        for (EstablishmentStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid establishment status code: " + code);
    }
}