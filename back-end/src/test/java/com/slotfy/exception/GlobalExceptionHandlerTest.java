package com.slotfy.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Test class for GlobalExceptionHandler
 */
public class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler exceptionHandler;

    @BeforeEach
    void setUp() {
        exceptionHandler = new GlobalExceptionHandler();
    }

    @Test
    void testHandleResourceNotFoundException() {
        ResourceNotFoundException exception = new ResourceNotFoundException("Client", 123L);
        
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleResourceNotFoundException(exception);
        
        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(404, body.get("status"));
        assertEquals("Not Found", body.get("error"));
        assertEquals("Client not found with id: 123", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void testHandleValidationException() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        List<FieldError> fieldErrors = new ArrayList<>();
        fieldErrors.add(new FieldError("object", "email", "Email é obrigatório"));
        fieldErrors.add(new FieldError("object", "password", "Senha é obrigatória"));
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(fieldErrors);
        
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationException(exception);
        
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(400, body.get("status"));
        assertEquals("Validation Failed", body.get("error"));
        assertEquals("Email é obrigatório", body.get("message"));
        assertNotNull(body.get("timestamp"));
        assertNotNull(body.get("fieldErrors"));
        
        @SuppressWarnings("unchecked")
        Map<String, String> errors = (Map<String, String>) body.get("fieldErrors");
        assertTrue(errors.containsKey("email"));
        assertTrue(errors.containsKey("password"));
    }

    @Test
    void testHandleValidationExceptionWithEmptyFieldErrors() {
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(new ArrayList<>());
        
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleValidationException(exception);
        
        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("Dados inválidos", body.get("message"));
    }

    @Test
    void testHandleGenericException() {
        Exception exception = new Exception("Unexpected error");
        
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(exception);
        
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(500, body.get("status"));
        assertEquals("Internal Server Error", body.get("error"));
        assertEquals("An unexpected error occurred", body.get("message"));
        assertNotNull(body.get("timestamp"));
    }

    @Test
    void testHandleGenericExceptionWithRuntimeException() {
        RuntimeException exception = new RuntimeException("Runtime error");
        
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(exception);
        
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }

    @Test
    void testHandleGenericExceptionWithNullPointerException() {
        NullPointerException exception = new NullPointerException("Null pointer");
        
        ResponseEntity<Map<String, Object>> response = exceptionHandler.handleGenericException(exception);
        
        assertNotNull(response);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        
        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals("An unexpected error occurred", body.get("message"));
    }
}
