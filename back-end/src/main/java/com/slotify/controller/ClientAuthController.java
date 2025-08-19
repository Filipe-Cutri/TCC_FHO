package com.slotify.controller;

import com.slotify.model.Client;
import com.slotify.service.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Controller for client authentication and management
 */
@RestController
@RequestMapping("/api/client")
@CrossOrigin(origins = "*")
public class ClientAuthController {
    
    @Autowired
    private ClientService clientService;
    
    /**
     * Login endpoint for clients
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            
            if (email == null || password == null || 
                email.trim().isEmpty() || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false, 
                        "message", "Email e senha são obrigatórios"
                    ));
            }
            
            Optional<Client> clientOpt = clientService.authenticate(email, password);
            
            if (clientOpt.isPresent()) {
                Client client = clientOpt.get();
                
                return ResponseEntity.ok()
                    .body(Map.of(
                        "success", true,
                        "message", "Login realizado com sucesso",
                        "client", Map.of(
                            "id", client.getId(),
                            "name", client.getName(),
                            "email", client.getEmail(),
                            "phone", client.getPhone() != null ? client.getPhone() : ""
                        )
                    ));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Email ou senha inválidos"
                    ));
            }
            
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
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
            
            if (name == null || email == null || password == null ||
                name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Nome, email e senha são obrigatórios"
                    ));
            }
            
            if (password.length() < 6) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Senha deve ter pelo menos 6 caracteres"
                    ));
            }
            
            Client client = clientService.registerClient(name, email, password, phone);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Conta criada com sucesso",
                    "client", Map.of(
                        "id", client.getId(),
                        "name", client.getName(),
                        "email", client.getEmail(),
                        "phone", client.getPhone() != null ? client.getPhone() : ""
                    )
                ));
                
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }
}