package com.slotfy.model;

/**
 * Enum for payment methods
 */
public enum PaymentMethod {
    CASH("Dinheiro"),
    CREDIT_CARD("Cartão de Crédito"),
    DEBIT_CARD("Cartão de Débito"),
    PIX("PIX"),
    BANK_TRANSFER("Transferência Bancária"),
    OTHER("Outro");
    
    private final String description;
    
    PaymentMethod(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static PaymentMethod fromCode(String code) {
        for (PaymentMethod method : PaymentMethod.values()) {
            if (method.name().equalsIgnoreCase(code)) {
                return method;
            }
        }
        throw new IllegalArgumentException("Método de pagamento inválido: " + code);
    }
}
