package com.slotfy.model;

/**
 * Enum defining professional status
 */
public enum ProfessionalStatus {
    ACTIVE("active", "Ativo"),
    INACTIVE("inactive", "Inativo"),
    SUSPENDED("suspended", "Suspenso");
    
    private final String code;
    private final String description;
    
    ProfessionalStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static ProfessionalStatus fromCode(String code) {
        for (ProfessionalStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid professional status code: " + code);
    }
}