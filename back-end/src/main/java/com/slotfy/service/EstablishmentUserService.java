package com.slotfy.service;

import com.slotfy.model.EstablishmentUser;
import com.slotfy.model.UserRole;
import com.slotfy.repository.EstablishmentUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service for managing establishment users
 */
@Service
public class EstablishmentUserService extends BaseAuthService<EstablishmentUser, EstablishmentUserRepository> {
    
    private static final Logger logger = LoggerFactory.getLogger(EstablishmentUserService.class);
    
    @Autowired
    private EstablishmentUserRepository establishmentUserRepository;
    
    @Autowired
    private PasswordResetService passwordResetService;
    
    @Autowired
    private EmailService emailService;
    
    @Value("${frontend.url}")
    private String frontendUrl;
    
    public EstablishmentUserService(EstablishmentUserRepository repository) {
        super(repository);
        this.establishmentUserRepository = repository;
    }
    
    @Override
    protected String getEntityPassword(EstablishmentUser entity) {
        return entity.getPassword();
    }
    
    @Override
    protected Optional<EstablishmentUser> findActiveByEmail(String email) {
        return establishmentUserRepository.findByEmailAndActive(email, true);
    }
    
    @Override
    protected boolean checkEmailExists(String email) {
        return establishmentUserRepository.existsByEmail(email);
    }
    
    /**
     * Find user by email
     */
    @Override
    public Optional<EstablishmentUser> findByEmail(String email) {
        return establishmentUserRepository.findByEmail(email);
    }
    
    /**
     * Register new establishment user
     */
    @Override
    public EstablishmentUser register(String name, String email, String password, String... additionalParams) {
        if (additionalParams.length < 2) {
            throw new IllegalArgumentException("Role e establishmentId são obrigatórios");
        }
        
        UserRole role = UserRole.valueOf(additionalParams[0]);
        Long establishmentId = Long.parseLong(additionalParams[1]);
        
        return createUser(name, email, password, role, establishmentId);
    }
    
    /**
     * Find users by establishment
     */
    public List<EstablishmentUser> findByEstablishment(Long establishmentId) {
        return establishmentUserRepository.findByEstablishmentIdAndActive(establishmentId, true);
    }
    
    /**
     * Find users by establishment and role
     */
    public List<EstablishmentUser> findByEstablishmentAndRole(Long establishmentId, UserRole role) {
        return establishmentUserRepository.findByEstablishmentIdAndRole(establishmentId, role);
    }
    
    /**
     * Create new user
     */
    public EstablishmentUser createUser(String name, String email, String password, UserRole role, Long establishmentId) {
        logger.debug("Iniciando criação de usuário - Nome: {}, Email: {}, Role: {}, EstablishmentId: {}", 
                    name, email, role, establishmentId);
        
        logger.debug("Verificando se email já existe: {}", email);
        if (existsByEmail(email)) {
            logger.warn("Tentativa de criar usuário com email já existente: {}", email);
            throw new IllegalArgumentException("Email já está em uso");
        }
        
        logger.debug("Validando formato do email: {}", email);
        if (!isValidEmail(email)) {
            logger.error("Email inválido fornecido: {}", email);
            throw new IllegalArgumentException("Email inválido");
        }
        
        logger.debug("Validando senha (comprimento mínimo)");
        if (!isValidPassword(password)) {
            logger.error("Senha fornecida não atende aos requisitos mínimos");
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
        }
        
        logger.info("Gerando hash da senha para o usuário: {}", email);
        String encodedPassword = hashPassword(password);
        
        logger.debug("Criando objeto EstablishmentUser - Nome: {}, Email: {}, Role: {}, EstablishmentId: {}", 
                    name, email, role, establishmentId);
        EstablishmentUser user = new EstablishmentUser(name, email, encodedPassword, role, establishmentId);
        
        logger.info("Salvando usuário no banco de dados - Email: {}, Role: {}", email, role);
        try {
            EstablishmentUser saved = save(user);
            logger.info("Usuário salvo com sucesso - ID: {}, Email: {}, Role: {}, EstablishmentId: {}", 
                       saved.getId(), saved.getEmail(), saved.getRole(), saved.getEstablishmentId());
            return saved;
        } catch (Exception e) {
            logger.error("Erro ao salvar usuário no banco de dados", e);
            logger.error("Tipo de erro: {}, Mensagem: {}", e.getClass().getName(), e.getMessage());
            if (e.getCause() != null) {
                logger.error("Causa raiz: {} - {}", e.getCause().getClass().getName(), e.getCause().getMessage());
            }
            throw e;
        }
    }
    
    /**
     * Update user password
     */
    public void updatePassword(String email, String newPassword) {
        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
        }
        
        Optional<EstablishmentUser> userOpt = findByEmail(email);
        if (userOpt.isPresent()) {
            EstablishmentUser user = userOpt.get();
            user.setPassword(hashPassword(newPassword));
            save(user);
        }
    }
    
    /**
     * Deactivate user
     */
    public void deactivateUser(Long userId) {
        Optional<EstablishmentUser> user = findById(userId);
        if (user.isPresent()) {
            user.get().setActive(false);
            save(user.get());
        }
    }
    
    /**
     * Initiate password reset process by generating token and sending email
     */
    public boolean initiatePasswordReset(String email) {
        Optional<EstablishmentUser> userOpt = findByEmail(email);
        if (!userOpt.isPresent()) {
            // Return false but don't reveal that email doesn't exist for security
            return false;
        }
        
        EstablishmentUser user = userOpt.get();
        if (!user.getActive()) {
            throw new IllegalArgumentException("Conta desativada");
        }
        
        // Generate reset token
        String token = passwordResetService.generateToken();
        String hashedToken = passwordResetService.hashToken(token);
        Long expiry = passwordResetService.getTokenExpiry();
        
        // Save token to database
        user.setResetPasswordTokenHash(hashedToken);
        user.setResetPasswordExpiry(expiry);
        save(user);
        
        // Build reset link - this should point to the frontend reset page
        String resetLink = buildResetLink(token, "establishment");
        
        // Send email
        return emailService.sendPasswordResetEmail(user.getEmail(), resetLink);
    }
    
    /**
     * Reset password using token
     */
    public boolean resetPassword(String token, String newPassword) {
        if (!isValidPassword(newPassword)) {
            throw new IllegalArgumentException("Senha deve ter pelo menos 6 caracteres");
        }
        
        String hashedToken = passwordResetService.hashToken(token);
        Optional<EstablishmentUser> userOpt = establishmentUserRepository.findByResetPasswordTokenHash(hashedToken);
        
        if (!userOpt.isPresent()) {
            throw new IllegalArgumentException("Token de redefinição inválido");
        }
        
        EstablishmentUser user = userOpt.get();
        
        // Check if token is expired
        if (passwordResetService.isTokenExpired(user.getResetPasswordExpiry())) {
            // Clear expired token
            user.setResetPasswordTokenHash(null);
            user.setResetPasswordExpiry(null);
            save(user);
            throw new IllegalArgumentException("Token de redefinição expirado");
        }
        
        // Update password
        String encodedPassword = hashPassword(newPassword);
        user.setPassword(encodedPassword);
        
        // Clear reset token
        user.setResetPasswordTokenHash(null);
        user.setResetPasswordExpiry(null);
        
        save(user);
        return true;
    }
    
    /**
     * Build password reset link for frontend
     */
    private String buildResetLink(String token, String userType) {
        return String.format("%s/pages/reset-password.html?token=%s&type=%s", frontendUrl, token, userType);
    }
}