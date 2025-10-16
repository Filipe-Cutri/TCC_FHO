package com.slotfy.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for ResourceNotFoundException
 */
public class ResourceNotFoundExceptionTest {

    @Test
    void testConstructorWithMessage() {
        String message = "Resource not found";
        ResourceNotFoundException exception = new ResourceNotFoundException(message);
        
        assertNotNull(exception);
        assertEquals(message, exception.getMessage());
        assertTrue(exception instanceof SlotfyException);
    }

    @Test
    void testConstructorWithResourceAndId() {
        String resource = "Client";
        Long id = 123L;
        ResourceNotFoundException exception = new ResourceNotFoundException(resource, id);
        
        assertNotNull(exception);
        assertEquals("Client not found with id: 123", exception.getMessage());
        assertTrue(exception instanceof SlotfyException);
    }

    @Test
    void testConstructorWithResourceAndStringId() {
        String resource = "Appointment";
        String id = "ABC123";
        ResourceNotFoundException exception = new ResourceNotFoundException(resource, id);
        
        assertNotNull(exception);
        assertEquals("Appointment not found with id: ABC123", exception.getMessage());
    }

    @Test
    void testExceptionIsRuntimeException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Test message");
        
        assertTrue(exception instanceof RuntimeException);
    }
}
