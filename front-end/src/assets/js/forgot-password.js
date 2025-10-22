/**
 * Slotfy - Forgot Password Module
 * Reusable module for handling forgot password functionality
 */

/**
 * Initialize forgot password form
 * @param {string} formId - ID of the form element
 * @param {string} successMessageId - ID of the success message element
 * @param {string} apiEndpoint - API endpoint for forgot password
 */
function initializeForgotPasswordForm(formId, successMessageId, apiEndpoint) {
    const form = document.getElementById(formId);
    if (!form) {
        console.error(`Form with id '${formId}' not found`);
        return;
    }

    form.addEventListener('submit', async function(e) {
        e.preventDefault();
        
        const submitBtn = this.querySelector('button[type="submit"]');
        const originalText = submitBtn.innerHTML;
        const email = document.getElementById('email').value.trim();
        
        // Basic email validation
        if (!email || !email.includes('@')) {
            alert('Por favor, digite um e-mail válido.');
            return;
        }
        
        // Add loading state
        submitBtn.innerHTML = '<i class="fas fa-spinner fa-spin"></i> Enviando...';
        submitBtn.disabled = true;
        
        try {
            // Call forgot password API
            const response = await window.apiClient.post(apiEndpoint, {
                email: email
            });
            
            if (response.success) {
                // Show success message
                const successMessage = document.getElementById(successMessageId);
                if (successMessage) {
                    successMessage.style.display = 'block';
                }
                
                // Hide form
                form.style.display = 'none';
            } else {
                throw new Error(response.message || 'E-mail não encontrado');
            }
        } catch (error) {
            console.error('Forgot password error:', error);
            
            // Show error message
            alert(error.message || 'Erro ao enviar e-mail. Verifique se o e-mail está correto.');
            
            // Reset button
            submitBtn.innerHTML = originalText;
            submitBtn.disabled = false;
        }
    });
}

/**
 * Initialize form interactions
 */
function initializeFormInteractions() {
    // Enhanced form interactions
    document.querySelectorAll('.form-control').forEach(input => {
        input.addEventListener('focus', function() {
            this.parentElement.classList.add('focused');
        });
        
        input.addEventListener('blur', function() {
            this.parentElement.classList.remove('focused');
        });
        
        // Real-time validation feedback
        input.addEventListener('input', function() {
            if (this.value.length > 0) {
                this.classList.remove('is-invalid');
                this.classList.add('is-valid');
            }
        });
    });
}

/**
 * Initialize page animations
 */
function initializePageAnimations() {
    // Add subtle animation on page load
    window.addEventListener('load', function() {
        const forgotPasswordCard = document.querySelector('.forgot-password-card');
        if (forgotPasswordCard) {
            forgotPasswordCard.style.opacity = '0';
            forgotPasswordCard.style.transform = 'translateY(20px)';
            
            setTimeout(() => {
                forgotPasswordCard.style.transition = 'all 0.6s ease';
                forgotPasswordCard.style.opacity = '1';
                forgotPasswordCard.style.transform = 'translateY(0)';
            }, 100);
        }
    });

    // Navbar scroll effects
    window.addEventListener('scroll', function() {
        const navbar = document.querySelector('.navbar');
        if (navbar) {
            if (window.scrollY > 50) {
                navbar.style.backgroundColor = 'rgba(255, 255, 255, 0.95)';
                navbar.style.backdropFilter = 'blur(20px)';
            } else {
                navbar.style.backgroundColor = 'var(--color-neutral-0)';
                navbar.style.backdropFilter = 'blur(20px)';
            }
        }
    });
}

// Export functions for module usage
if (typeof module !== 'undefined' && module.exports) {
    module.exports = {
        initializeForgotPasswordForm,
        initializeFormInteractions,
        initializePageAnimations
    };
}
