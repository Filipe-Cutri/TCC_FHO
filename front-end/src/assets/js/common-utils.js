/**
 * Common Utilities - Shared functionality for main site
 * Navigation, landing page animations, and common interactions
 */

/**
 * Main site navigation and interactions
 */
class MainSiteManager {
    /**
     * Initialize main site functionality
     */
    static init() {
        document.addEventListener('DOMContentLoaded', function() {
            MainSiteManager.setupNavigation();
            MainSiteManager.setupAnimations();
            MainSiteManager.setupInteractions();
        });
    }

    /**
     * Setup navigation functionality
     */
    static setupNavigation() {
        // Setup smooth scrolling for anchor links
        ScrollManager.setupSmoothScroll();

        // Setup navbar scroll effects
        const navbar = document.querySelector('.navbar');
        if (navbar) {
            AnimationManager.setupNavbarScrollEffect(navbar, {
                scrollThreshold: 100,
                scrolledBackground: 'rgba(59, 130, 246, 0.95)',
                defaultBackground: 'linear-gradient(135deg, var(--color-primary-600) 0%, var(--color-primary-700) 100%)',
                backdropFilter: 'blur(20px)'
            });
        }
    }

    /**
     * Setup landing page animations
     */
    static setupAnimations() {
        // Enhanced card hover effects for access cards
        document.querySelectorAll('.access-card').forEach((card, index) => {
            card.addEventListener('mouseenter', function() {
                this.style.transform = 'translateY(-10px) scale(1.02)';
                this.style.boxShadow = '0 20px 40px rgba(0, 0, 0, 0.15)';
            });
            
            card.addEventListener('mouseleave', function() {
                this.style.transform = 'translateY(0) scale(1)';
                this.style.boxShadow = '0 10px 30px rgba(0, 0, 0, 0.1)';
            });
        });

        // Setup scroll animations for feature cards and testimonials
        AnimationManager.setupScrollAnimation('.feature-card, .testimonial-card', {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px',
            animationDelay: 0.1
        });

        // Setup counter animations for stats
        MainSiteManager.setupStatsAnimation();
    }

    /**
     * Setup statistics counter animation
     */
    static setupStatsAnimation() {
        const statsObserver = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    document.querySelectorAll('.stat-number').forEach((stat, index) => {
                        const text = stat.textContent;
                        let target = parseInt(text.replace(/[^\d]/g, ''));
                        let suffix = '';
                        
                        if (text.includes('k+')) {
                            target *= 1000;
                            suffix = '+';
                        }
                        if (text.includes('.')) {
                            target = 4.9;
                            suffix = '★';
                        }
                        if (text.includes('+') && !text.includes('k+')) {
                            suffix = '+';
                        }
                        
                        setTimeout(() => {
                            AnimationManager.animateCounter(stat, target, {
                                duration: 2000,
                                suffix: suffix
                            });
                        }, index * 200);
                    });
                    statsObserver.disconnect();
                }
            });
        });
        
        const statsSection = document.querySelector('.hero-stats');
        if (statsSection) {
            statsObserver.observe(statsSection);
        }
    }

    /**
     * Setup interactive elements
     */
    static setupInteractions() {
        // Setup ripple effects for buttons
        document.querySelectorAll('.btn').forEach(btn => {
            AnimationManager.addRippleEffect(btn);
        });
    }
}

/**
 * Navigation utilities for different user types
 */
class NavigationManager {
    /**
     * Navigate to client login page
     */
    static goToClientLogin() {
        window.location.href = 'pages/client/client-login.html';
    }
    
    /**
     * Navigate to establishment login page
     */
    static goToEstablishmentLogin() {
        window.location.href = 'pages/establishment/establishment-login-enhanced.html';
    }
    
    /**
     * Navigate to establishment register page
     */
    static goToEstablishmentRegister() {
        window.location.href = 'pages/establishment/establishment-register.html';
    }
    
    /**
     * Navigate to client register page
     */
    static goToClientRegister() {
        window.location.href = 'pages/client/client-register.html';
    }
    
    /**
     * Handle contact sales action
     */
    static contactSales() {
        const contactInfo = 'Entre em contato conosco pelo email: vendas@slotfy.com.br ou WhatsApp: (11) 99999-9999';
        
        // Create a more elegant contact modal instead of alert
        if (window.bootstrap && document.querySelector('#contactModal')) {
            // Use existing modal if available
            ModalManager.show('#contactModal');
        } else {
            // Fallback to alert
            alert(contactInfo);
        }
    }

    /**
     * Setup navigation buttons globally
     */
    static setupGlobalNavigation() {
        // Make navigation functions globally available
        window.goToClientLogin = NavigationManager.goToClientLogin;
        window.goToEstablishmentLogin = NavigationManager.goToEstablishmentLogin;
        window.goToEstablishmentRegister = NavigationManager.goToEstablishmentRegister;
        window.goToClientRegister = NavigationManager.goToClientRegister;
        window.contactSales = NavigationManager.contactSales;
    }
}

/**
 * Form handling for common pages
 */
class CommonFormManager {
    /**
     * Setup login form handling
     * @param {string} formSelector - CSS selector for login form
     * @param {string} type - 'client' or 'establishment'
     */
    static setupLoginForm(formSelector, type) {
        const form = document.querySelector(formSelector);
        if (!form) return;

        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const email = form.querySelector('input[type="email"]').value;
            const password = form.querySelector('input[type="password"]').value;
            
            // Basic validation
            if (!FormValidator.isValidEmail(email)) {
                ToastManager.showError('Por favor, insira um email válido.');
                return;
            }
            
            if (!FormValidator.minLength(password, 6)) {
                ToastManager.showError('A senha deve ter pelo menos 6 caracteres.');
                return;
            }
            
            const submitBtn = form.querySelector('button[type="submit"]');
            const resetButton = LoadingManager.setButtonLoading(submitBtn, 'Entrando...');
            
            // Simulate login process
            setTimeout(() => {
                LoadingManager.setButtonSuccess(submitBtn, 'Logado!');
                ToastManager.showSuccess('Login realizado com sucesso!');
                
                // Redirect based on type
                setTimeout(() => {
                    if (type === 'client') {
                        window.location.href = 'client-dashboard.html';
                    } else {
                        window.location.href = 'establishment-dashboard.html';
                    }
                }, 1000);
            }, 2000);
        });
    }

    /**
     * Setup register form handling
     * @param {string} formSelector - CSS selector for register form
     * @param {string} type - 'client' or 'establishment'
     */
    static setupRegisterForm(formSelector, type) {
        const form = document.querySelector(formSelector);
        if (!form) return;

        // Add real-time validation
        const emailField = form.querySelector('input[type="email"]');
        if (emailField) {
            FormValidator.addRealTimeValidation(
                emailField,
                FormValidator.isValidEmail,
                'Por favor, insira um email válido.'
            );
        }

        const passwordField = form.querySelector('input[name="password"]');
        if (passwordField) {
            FormValidator.addRealTimeValidation(
                passwordField,
                (value) => FormValidator.minLength(value, 6),
                'A senha deve ter pelo menos 6 caracteres.'
            );
        }

        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(form);
            const data = Object.fromEntries(formData.entries());
            
            // Validation
            if (!FormValidator.isValidEmail(data.email)) {
                ToastManager.showError('Por favor, insira um email válido.');
                return;
            }
            
            if (!FormValidator.minLength(data.password, 6)) {
                ToastManager.showError('A senha deve ter pelo menos 6 caracteres.');
                return;
            }
            
            if (data.password !== data.confirmPassword) {
                ToastManager.showError('As senhas não coincidem.');
                return;
            }
            
            const submitBtn = form.querySelector('button[type="submit"]');
            const resetButton = LoadingManager.setButtonLoading(submitBtn, 'Criando conta...');
            
            // Simulate registration process
            setTimeout(() => {
                LoadingManager.setButtonSuccess(submitBtn, 'Conta criada!');
                ToastManager.showSuccess('Conta criada com sucesso!');
                
                // Redirect based on type
                setTimeout(() => {
                    if (type === 'client') {
                        window.location.href = 'client-preferences-setup.html';
                    } else {
                        window.location.href = 'establishment-dashboard.html';
                    }
                }, 1000);
            }, 2000);
        });
    }

    /**
     * Setup forgot password form
     * @param {string} formSelector - CSS selector for forgot password form
     */
    static setupForgotPasswordForm(formSelector) {
        const form = document.querySelector(formSelector);
        if (!form) return;

        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const email = form.querySelector('input[type="email"]').value;
            
            if (!FormValidator.isValidEmail(email)) {
                ToastManager.showError('Por favor, insira um email válido.');
                return;
            }
            
            const submitBtn = form.querySelector('button[type="submit"]');
            const resetButton = LoadingManager.setButtonLoading(submitBtn, 'Enviando...');
            
            // Simulate email sending
            setTimeout(() => {
                LoadingManager.setButtonSuccess(submitBtn, 'Enviado!');
                ToastManager.showSuccess('Email de recuperação enviado! Verifique sua caixa de entrada.');
                form.reset();
                resetButton();
            }, 2000);
        });
    }
}

/**
 * Responsive design utilities
 */
class ResponsiveManager {
    /**
     * Handle responsive navigation toggle
     */
    static setupResponsiveNavigation() {
        const navToggler = document.querySelector('.navbar-toggler');
        const navCollapse = document.querySelector('.navbar-collapse');
        
        if (navToggler && navCollapse) {
            // Add smooth transition
            navCollapse.style.transition = 'height 0.3s ease';
            
            // Close menu when clicking outside
            document.addEventListener('click', function(e) {
                if (!navToggler.contains(e.target) && !navCollapse.contains(e.target)) {
                    const bsCollapse = bootstrap.Collapse.getInstance(navCollapse);
                    if (bsCollapse) {
                        bsCollapse.hide();
                    }
                }
            });
        }
    }

    /**
     * Setup responsive utilities
     */
    static init() {
        ResponsiveManager.setupResponsiveNavigation();
    }
}

// Initialize main site functionality
MainSiteManager.init();

// Setup global navigation
NavigationManager.setupGlobalNavigation();

// Initialize responsive features
ResponsiveManager.init();

// Export classes globally
window.MainSiteManager = MainSiteManager;
window.NavigationManager = NavigationManager;
window.CommonFormManager = CommonFormManager;
window.ResponsiveManager = ResponsiveManager;

// Export for module usage if needed
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { 
        MainSiteManager, 
        NavigationManager, 
        CommonFormManager, 
        ResponsiveManager 
    };
}