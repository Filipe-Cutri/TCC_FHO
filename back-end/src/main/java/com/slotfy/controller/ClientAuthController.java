package com.slotfy.controller;

import com.slotfy.model.Client;
import com.slotfy.service.AuthenticatableService;
import com.slotfy.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Controller for client authentication and management
 */
@RestController
@RequestMapping("/api/client")
@CrossOrigin(originPatterns = "*")
public class ClientAuthController extends BaseAuthController<Client> {
    
    @Autowired
    private ClientService clientService;
    
    @Override
    protected AuthenticatableService<Client> getAuthService() {
        return clientService;
    }
    
    @Override
    protected Map<String, Object> createUserResponse(Client client) {
        return Map.of(
            "id", client.getId(),
            "name", client.getName(),
            "email", client.getEmail(),
            "phone", client.getPhone() != null ? client.getPhone() : ""
        );
    }
    
    @Override
    protected String getUserTypeName() {
        return "client";
    }
    
    /**
     * Login endpoint for clients
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        return performLogin(request);
    }
    
    /**
     * Registration endpoint for clients
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String email = request.get("email");
            String password = request.get("password");
            String phone = request.get("phone");
            
            // Validate input
            ResponseEntity<Map<String, Object>> validationError = validateRegistrationData(name, email, password);
            if (validationError != null) {
                return validationError;
            }
            
            Client client = clientService.registerClient(name, email, password, phone);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Conta criada com sucesso",
                    "client", createUserResponse(client)
                ));
                
        } catch (Exception e) {
            return handleRegistrationError(e);
        }
    }
}