package com.slotfy.controller;

import com.slotfy.dto.EstablishmentRegisterRequest;
import com.slotfy.dto.EstablishmentRegisterResponse;
import com.slotfy.model.Establishment;
import com.slotfy.model.EstablishmentUser;
import com.slotfy.model.UserRole;
import com.slotfy.service.EstablishmentService;
import com.slotfy.service.EstablishmentUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;
import java.util.Optional;

/**
 * Controller for establishment user authentication and management
 */
@RestController
@RequestMapping("/api/establishment")
@CrossOrigin(originPatterns = "*")
@Validated
public class EstablishmentAuthController {
    
    @Autowired
    private EstablishmentUserService establishmentUserService;
    
    @Autowired
    private EstablishmentService establishmentService;
    
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
     * Complete establishment registration endpoint
     */
    @PostMapping("/register-complete")
    public ResponseEntity<EstablishmentRegisterResponse> registerComplete(@Valid @RequestBody EstablishmentRegisterRequest request) {
        try {
            // First, create the establishment
            Establishment establishment = establishmentService.createEstablishment(
                request.getNomeEstabelecimento(),
                request.getEmail(),
                request.getTelefone(),
                null, // address - not provided in frontend form
                null, // description - not provided in frontend form
                request.getCategory(),
                null  // cnpj - not provided in frontend form
            );
            
            // Then, create the admin user for this establishment
            EstablishmentUser adminUser = establishmentUserService.createUser(
                "Administrador", // default name since not provided in form
                request.getEmail(),
                request.getSenha(),
                UserRole.ADMIN,
                establishment.getId()
            );
            
            return ResponseEntity.ok(new EstablishmentRegisterResponse(
                true,
                "Estabelecimento registrado com sucesso!",
                establishment.getId(),
                adminUser.getId(),
                establishment.getName(),
                adminUser.getEmail(),
                adminUser.getRole().getDescription()
            ));
            
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(new EstablishmentRegisterResponse(false, e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(new EstablishmentRegisterResponse(false, "Erro interno do servidor"));
        }
    }
    
    /**
     * Registration endpoint for establishment users (admin account creation)
     */
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, String> request) {
        try {
            String name = request.get("name");
            String email = request.get("email");
            String password = request.get("password");
            String establishmentName = request.get("establishmentName");
            String establishmentIdStr = request.get("establishmentId");
            
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
            
            // If establishmentId is provided, use it; otherwise generate a simple ID
            Long establishmentId = establishmentIdStr != null && !establishmentIdStr.trim().isEmpty() 
                ? Long.parseLong(establishmentIdStr) 
                : System.currentTimeMillis() % 100000; // Simple ID generation for demo
                
            EstablishmentUser newUser = establishmentUserService.createUser(
                name, email, password, UserRole.ADMIN, establishmentId
            );
            
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Conta criada com sucesso",
                    "user", Map.of(
                        "id", newUser.getId(),
                        "name", newUser.getName(),
                        "email", newUser.getEmail(),
                        "role", newUser.getRole().getCode(),
                        "roleDescription", newUser.getRole().getDescription(),
                        "establishmentId", newUser.getEstablishmentId()
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