package com.slotfy.controller;

import com.slotfy.model.Client;
import com.slotfy.service.AuthenticatableService;
import com.slotfy.service.ClientService;
import com.slotfy.service.ForgotPasswordService;
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
    
    @Autowired
    private ForgotPasswordService forgotPasswordService;
    
    @Override
    protected AuthenticatableService<Client> getAuthService() {
        return clientService;
    }
    
    @Override
    protected Map<String, Object> createUserResponse(Client client) {
        Map<String, Object> response = new java.util.HashMap<>(Map.of(
            "id", client.getId(),
            "name", client.getName(),
            "email", client.getEmail(),
            "phone", client.getPhone() != null ? client.getPhone() : ""
        ));
        
        if (client.getSelectedEstablishmentId() != null) {
            response.put("selectedEstablishmentId", client.getSelectedEstablishmentId());
        }
        
        return response;
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
            String establishmentIdStr = request.get("establishmentId");
            
            // Validate input
            ResponseEntity<Map<String, Object>> validationError = validateRegistrationData(name, email, password);
            if (validationError != null) {
                return validationError;
            }
            
            Long establishmentId = null;
            if (establishmentIdStr != null && !establishmentIdStr.trim().isEmpty()) {
                try {
                    establishmentId = Long.parseLong(establishmentIdStr);
                } catch (NumberFormatException e) {
                    // Ignore invalid establishment ID
                }
            }
            
            Client client = clientService.registerClient(name, email, password, phone, establishmentId);
            
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
    
    /**
     * Update client's selected establishment
     */
    @PutMapping("/establishment")
    public ResponseEntity<Map<String, Object>> updateSelectedEstablishment(@RequestBody Map<String, String> request) {
        try {
            String clientIdStr = request.get("clientId");
            String establishmentIdStr = request.get("establishmentId");
            
            if (clientIdStr == null || clientIdStr.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "ID do cliente é obrigatório"
                    ));
            }
            
            Long clientId = Long.parseLong(clientIdStr);
            Long establishmentId = null;
            
            if (establishmentIdStr != null && !establishmentIdStr.trim().isEmpty()) {
                establishmentId = Long.parseLong(establishmentIdStr);
            }
            
            Client client = clientService.updateSelectedEstablishment(clientId, establishmentId);
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Estabelecimento selecionado com sucesso",
                    "client", createUserResponse(client)
                ));
                
        } catch (NumberFormatException e) {
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", "ID inválido"
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
                    "message", "Erro ao atualizar estabelecimento selecionado"
                ));
        }
    }
    
    /**
     * Forgot password endpoint - initiates password reset process
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Email é obrigatório"
                    ));
            }
            
            forgotPasswordService.sendClientPasswordResetEmail(email.trim());
            
            // Always return success to prevent email enumeration
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Se o email existir em nosso sistema, você receberá instruções para redefinir sua senha"
                ));
                
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao processar solicitação"
                ));
        }
    }
    
    /**
     * Reset password endpoint - resets password using token
     */
    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String token = request.get("token");
            String email = request.get("email");
            String newPassword = request.get("newPassword");
            
            if (token == null || token.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Token é obrigatório"
                    ));
            }
            
            if (email == null || email.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Email é obrigatório"
                    ));
            }
            
            if (newPassword == null || newPassword.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Nova senha é obrigatória"
                    ));
            }
            
            boolean success = forgotPasswordService.resetPassword(email.trim(), token.trim(), newPassword.trim());
            
            if (success) {
                return ResponseEntity.ok()
                    .body(Map.of(
                        "success", true,
                        "message", "Senha redefinida com sucesso"
                    ));
            } else {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Token de redefinição inválido ou expirado"
                    ));
            }
                
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
                    "message", "Erro ao processar solicitação"
                ));
        }
    }
}