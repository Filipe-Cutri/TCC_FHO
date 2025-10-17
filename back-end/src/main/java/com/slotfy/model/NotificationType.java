package com.slotfy.model;

/**
 * Enum for notification types
 */
public enum NotificationType {
    APPOINTMENT_REMINDER("Lembrete de Agendamento"),
    APPOINTMENT_CONFIRMATION("Confirmação de Agendamento"),
    APPOINTMENT_CANCELLED("Agendamento Cancelado"),
    APPOINTMENT_RESCHEDULED("Agendamento Reagendado"),
    APPOINTMENT_COMPLETED("Agendamento Concluído"),
    PROMOTIONAL("Promoção"),
    SYSTEM("Sistema"),
    GENERAL("Geral");
    
    private final String description;
    
    NotificationType(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static NotificationType fromCode(String code) {
        for (NotificationType type : NotificationType.values()) {
            if (type.name().equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Tipo de notificação inválido: " + code);
    }
}
