package com.slotfy.model;

/**
 * Enum for payment status
 */
public enum PaymentStatus {
    PENDING("Pendente"),
    COMPLETED("Concluído"),
    CANCELLED("Cancelado"),
    REFUNDED("Reembolsado"),
    FAILED("Falhou");
    
    private final String description;
    
    PaymentStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static PaymentStatus fromCode(String code) {
        for (PaymentStatus status : PaymentStatus.values()) {
            if (status.name().equalsIgnoreCase(code)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Status de pagamento inválido: " + code);
    }
}
