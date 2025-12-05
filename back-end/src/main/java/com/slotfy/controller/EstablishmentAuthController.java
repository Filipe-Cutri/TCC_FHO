package com.slotfy.controller;

import com.slotfy.dto.EstablishmentRegisterRequest;
import com.slotfy.dto.EstablishmentRegisterResponse;
import com.slotfy.model.Establishment;
import com.slotfy.model.EstablishmentUser;
import com.slotfy.model.UserRole;
import com.slotfy.service.EstablishmentService;
import com.slotfy.service.EstablishmentUserService;
import com.slotfy.service.ForgotPasswordService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Transactional;
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
    
    private static final Logger logger = LoggerFactory.getLogger(EstablishmentAuthController.class);
    
    @Autowired
    private EstablishmentUserService establishmentUserService;
    
    @Autowired
    private EstablishmentService establishmentService;
    
    @Autowired
    private ForgotPasswordService forgotPasswordService;
    
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
            logger.error("Unexpected error during establishment login", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro interno do servidor"
                ));
        }
    }

    @PostMapping("/register-complete")
    @Transactional
    public ResponseEntity<EstablishmentRegisterResponse> registerComplete(@Valid @RequestBody EstablishmentRegisterRequest request) {
        logger.info("=== INÍCIO DO CADASTRO DE ESTABELECIMENTO ===");
        logger.debug("Request recebido - Nome: {}, Email: {}, Categoria: {}", 
                    request.getNomeEstabelecimento(), 
                    request.getEmail(), 
                    request.getCategory());
        
        try {
            // First, create the establishment
            logger.info("Etapa 1/2: Criando estabelecimento no banco de dados");
            logger.debug("Dados do estabelecimento - Nome: {}, Email: {}, Telefone: {}, Categoria: {}",
                        request.getNomeEstabelecimento(),
                        request.getEmail(),
                        request.getTelefone(),
                        request.getCategory());
            
            Establishment establishment = establishmentService.createEstablishment(
                    request.getNomeEstabelecimento(),
                    request.getEmail(),
                    request.getTelefone(),
                    null, // address - not provided in frontend form
                    null, // description - not provided in frontend form
                    request.getCategory(), // ✅ Usa o método que converte tipoEstabelecimento para categoria
                    null  // cnpj - not provided in frontend form
            );
            
            logger.info("✓ Estabelecimento criado com sucesso - ID: {}", establishment.getId());
            logger.debug("Detalhes do estabelecimento criado - ID: {}, Nome: {}, Email: {}, Status: {}",
                        establishment.getId(),
                        establishment.getName(),
                        establishment.getEmail(),
                        establishment.getStatus());

            // Then, create the admin user for this establishment
            logger.info("Etapa 2/2: Criando usuário administrador para o estabelecimento ID: {}", establishment.getId());
            logger.debug("Dados do usuário admin - Nome: {}, Email: {}, Role: {}, EstablishmentId: {}",
                        request.getNomeEstabelecimento(),
                        request.getEmail(),
                        UserRole.ADMIN,
                        establishment.getId());
            
            EstablishmentUser adminUser = establishmentUserService.createUser(
                    request.getNomeEstabelecimento(), // ✅ Usa o nome do estabelecimento como nome do admin
                    request.getEmail(),
                    request.getSenha(),
                    UserRole.ADMIN,
                    establishment.getId()
            );
            
            logger.info("✓ Usuário administrador criado com sucesso - ID: {}", adminUser.getId());
            logger.debug("Detalhes do usuário criado - ID: {}, Nome: {}, Email: {}, Role: {}, EstablishmentId: {}",
                        adminUser.getId(),
                        adminUser.getName(),
                        adminUser.getEmail(),
                        adminUser.getRole(),
                        adminUser.getEstablishmentId());

            logger.info("=== CADASTRO CONCLUÍDO COM SUCESSO - Estabelecimento ID: {}, Admin ID: {} ===",
                       establishment.getId(), adminUser.getId());

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
            logger.error("❌ Erro de validação durante cadastro de estabelecimento: {}", e.getMessage());
            logger.debug("Stack trace da validação:", e);
            return ResponseEntity.badRequest()
                    .body(new EstablishmentRegisterResponse(false, e.getMessage()));
        } catch (Exception e) {
            logger.error("❌ ERRO CRÍTICO durante cadastro de estabelecimento", e);
            logger.error("Tipo da exceção: {}", e.getClass().getName());
            logger.error("Mensagem: {}", e.getMessage());
            if (e.getCause() != null) {
                logger.error("Causa raiz: {} - {}", e.getCause().getClass().getName(), e.getCause().getMessage());
            }
            return ResponseEntity.internalServerError()
                    .body(new EstablishmentRegisterResponse(false, "Erro interno do servidor: " + e.getMessage()));
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
            logger.error("Validation error during establishment user registration: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            logger.error("Unexpected error during establishment user registration", e);
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
            logger.error("Validation error during staff user creation: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            logger.error("Unexpected error during staff user creation", e);
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
            
            forgotPasswordService.sendEstablishmentPasswordResetEmail(email.trim());
            
            // Always return success to prevent email enumeration
            return ResponseEntity.ok()
                .body(Map.of(
                    "success", true,
                    "message", "Se o email existir em nosso sistema, você receberá instruções para redefinir sua senha"
                ));
                
        } catch (Exception e) {
            logger.error("Unexpected error during password reset initiation", e);
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
            logger.error("Error during password reset: {}", e.getMessage());
            return ResponseEntity.badRequest()
                .body(Map.of(
                    "success", false,
                    "message", e.getMessage()
                ));
        } catch (Exception e) {
            logger.error("Unexpected error during password reset", e);
            return ResponseEntity.internalServerError()
                .body(Map.of(
                    "success", false,
                    "message", "Erro ao processar solicitação"
                ));
        }
    }
}