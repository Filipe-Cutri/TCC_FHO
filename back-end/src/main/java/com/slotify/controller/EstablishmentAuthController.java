package com.slotify.controller;

import com.slotify.model.EstablishmentUser;
import com.slotify.model.UserRole;
import com.slotify.service.EstablishmentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

/**
 * Controller for establishment user authentication and management
 */
@RestController
@RequestMapping("/api/establishment")
@CrossOrigin(originPatterns = "*")
public class EstablishmentAuthController {
    
    @Autowired
    private EstablishmentUserService establishmentUserService;
    
    /**
     * Login endpoint for establishment users
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String password = request.get("password");
            String roleCode = request.get("role"); // Optional role specification
            
            if (email == null || password == null || 
                email.trim().isEmpty() || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false, 
                        "message", "Email e senha são obrigatórios"
                    ));
            }
            
            Optional<EstablishmentUser> userOpt = establishmentUserService.authenticate(email, password);
            
            if (userOpt.isPresent()) {
                EstablishmentUser user = userOpt.get();
                
                // If role was specified, verify it matches
                if (roleCode != null && !roleCode.trim().isEmpty()) {
                    try {
                        UserRole requestedRole = UserRole.fromCode(roleCode);
                        if (!user.getRole().equals(requestedRole)) {
                            return ResponseEntity.badRequest()
                                .body(Map.of(
                                    "success", false,
                                    "message", "Credenciais inválidas para o tipo de acesso solicitado"
                                ));
                        }
                    } catch (IllegalArgumentException e) {
                        return ResponseEntity.badRequest()
                            .body(Map.of(
                                "success", false,
                                "message", "Tipo de acesso inválido"
                            ));
                    }
                }
                
                return ResponseEntity.ok()
                    .body(Map.of(
                        "success", true,
                        "message", "Login realizado com sucesso",
                        "user", Map.of(
                            "id", user.getId(),
                            "name", user.getName(),
                            "email", user.getEmail(),
                            "role", user.getRole().getCode(),
                            "roleDescription", user.getRole().getDescription(),
                            "establishmentId", user.getEstablishmentId()
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
     * Create new staff user (only accessible by admin)
     */
    @PostMapping("/create-staff")
    public ResponseEntity<Map<String, Object>> createStaff(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String email = request.get("email");
            String password = request.get("password");
            String establishmentIdStr = request.get("establishmentId");
            
            if (name == null || email == null || password == null || establishmentIdStr == null ||
                name.trim().isEmpty() || email.trim().isEmpty() || password.trim().isEmpty()) {
                return ResponseEntity.badRequest()
                    .body(Map.of(
                        "success", false,
                        "message", "Todos os campos são obrigatórios"
                    ));
            }
            
            Long establishmentId = Long.parseLong(establishmentIdStr);
            
            EstablishmentUser newUser = establishmentUserService.createUser(
                name, email, password, UserRole.STAFF, establishmentId
            );
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Funcionário criado com sucesso",
                    "user", Map.of(
                        "id", newUser.getId(),
                        "name", newUser.getName(),
                        "email", newUser.getEmail(),
                        "role", newUser.getRole().getCode()
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
    
    /**
     * Get user roles
     */
    @GetMapping("/roles")
    public ResponseEntity<Map<String, Object>> getRoles() {
        return ResponseEntity.ok()
            .body(Map.of(
                "success", true,
                "roles", Map.of(
                    "admin", Map.of(
                        "code", UserRole.ADMIN.getCode(),
                        "description", UserRole.ADMIN.getDescription()
                    ),
                    "staff", Map.of(
                        "code", UserRole.STAFF.getCode(),
                        "description", UserRole.STAFF.getDescription()
                    )
                )
            ));
    }
}