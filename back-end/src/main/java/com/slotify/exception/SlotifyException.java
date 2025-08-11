package com.slotify.exception;

/**
 * Base exception class for Slotify application.
 * All custom exceptions should extend this class.
 */
public abstract class SlotifyException extends RuntimeException {
    
    public SlotifyException(String message) {
        super(message);
    }
    
    public SlotifyException(String message, Throwable cause) {
        super(message, cause);
    }
}