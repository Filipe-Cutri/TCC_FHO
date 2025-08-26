package com.slotfy.model;

/**
 * Enum defining appointment status
 */
public enum AppointmentStatus {
    SCHEDULED("scheduled", "Agendado"),
    CONFIRMED("confirmed", "Confirmado"),
    IN_PROGRESS("in_progress", "Em Andamento"),
    COMPLETED("completed", "Concluído"),
    CANCELLED("cancelled", "Cancelado"),
    NO_SHOW("no_show", "Não Compareceu");
    
    private final String code;
    private final String description;
    
    AppointmentStatus(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    public String getCode() {
        return code;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static AppointmentStatus fromCode(String code) {
        for (AppointmentStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Invalid appointment status code: " + code);
    }
}