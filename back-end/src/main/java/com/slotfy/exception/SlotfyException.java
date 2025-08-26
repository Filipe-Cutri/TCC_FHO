package com.slotfy.exception;

/**
 * Base exception class for Slotfy application.
 * All custom exceptions should extend this class.
 */
public abstract class SlotfyException extends RuntimeException {
    
    public SlotfyException(String message) {
        super(message);
    }
    
    public SlotfyException(String message, Throwable cause) {
        super(message, cause);
    }
}