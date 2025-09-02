/**
 * UI Utilities - Common UI interactions and animations
 * Shared functionality for toasts, loading states, animations, and common UI elements
 */

/**
 * Toast notification utility
 */
class ToastManager {
    /**
     * Show a success toast notification
     * @param {string} message - The message to display
     * @param {Object} options - Optional configuration
     */
    static showSuccess(message, options = {}) {
        this.showToast(message, 'success', options);
    }

    /**
     * Show an error toast notification
     * @param {string} message - The message to display
     * @param {Object} options - Optional configuration
     */
    static showError(message, options = {}) {
        this.showToast(message, 'danger', options);
    }

    /**
     * Show an info toast notification
     * @param {string} message - The message to display
     * @param {Object} options - Optional configuration
     */
    static showInfo(message, options = {}) {
        this.showToast(message, 'info', options);
    }

    /**
     * Show a warning toast notification
     * @param {string} message - The message to display
     * @param {Object} options - Optional configuration
     */
    static showWarning(message, options = {}) {
        this.showToast(message, 'warning', options);
    }

    /**
     * Show a toast notification
     * @param {string} message - The message to display
     * @param {string} type - Toast type (success, danger, info, warning)
     * @param {Object} options - Optional configuration
     */
    static showToast(message, type = 'success', options = {}) {
        const defaultOptions = {
            duration: 4000,
            position: 'top-right',
            icon: this.getIconForType(type)
        };
        
        const config = { ...defaultOptions, ...options };
        
        // Create toast element
        const toast = document.createElement('div');
        toast.className = `toast align-items-center text-white bg-${type} border-0 position-fixed`;
        toast.style.cssText = `top: 100px; right: 20px; z-index: 9999;`;
        toast.innerHTML = `
            <div class="d-flex">
                <div class="toast-body">
                    <i class="${config.icon} me-2"></i>${message}
                </div>
                <button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>
            </div>
        `;
        
        document.body.appendChild(toast);
        const bsToast = new bootstrap.Toast(toast, { autohide: true, delay: config.duration });
        bsToast.show();
        
        // Remove toast after it's hidden
        toast.addEventListener('hidden.bs.toast', () => {
            if (document.body.contains(toast)) {
                document.body.removeChild(toast);
            }
        });
    }

    /**
     * Get appropriate icon for toast type
     * @param {string} type - Toast type
     * @returns {string} Font Awesome icon class
     */
    static getIconForType(type) {
        const icons = {
            success: 'fas fa-check-circle',
            danger: 'fas fa-exclamation-circle',
            error: 'fas fa-exclamation-circle',
            info: 'fas fa-info-circle',
            warning: 'fas fa-exclamation-triangle'
        };
        return icons[type] || icons.info;
    }
}

/**
 * Loading state utility
 */
class LoadingManager {
    /**
     * Add loading state to a button
     * @param {HTMLElement} button - The button element
     * @param {string} loadingText - Text to show during loading
     * @returns {Function} Function to reset the button state
     */
    static setButtonLoading(button, loadingText = 'Carregando...') {
        const originalText = button.innerHTML;
        const originalDisabled = button.disabled;
        
        button.innerHTML = `<i class="fas fa-spinner fa-spin me-2"></i>${loadingText}`;
        button.disabled = true;
        
        return function resetButton() {
            button.innerHTML = originalText;
            button.disabled = originalDisabled;
        };
    }

    /**
     * Set button to success state temporarily
     * @param {HTMLElement} button - The button element
     * @param {string} successText - Text to show on success
     * @param {number} duration - Duration to show success state
     */
    static setButtonSuccess(button, successText = 'Concluído!', duration = 2000) {
        const originalText = button.innerHTML;
        const originalDisabled = button.disabled;
        
        button.innerHTML = `<i class="fas fa-check me-2"></i>${successText}`;
        button.disabled = true;
        
        setTimeout(() => {
            button.innerHTML = originalText;
            button.disabled = originalDisabled;
        }, duration);
    }
}

/**
 * Animation utilities
 */
class AnimationManager {
    /**
     * Add ripple effect to element on click
     * @param {HTMLElement} element - Element to add ripple effect to
     */
    static addRippleEffect(element) {
        element.addEventListener('click', function(e) {
            const ripple = document.createElement('span');
            ripple.style.position = 'absolute';
            ripple.style.borderRadius = '50%';
            ripple.style.background = 'rgba(255, 255, 255, 0.6)';
            ripple.style.transform = 'scale(0)';
            ripple.style.animation = 'ripple 0.6s linear';
            ripple.style.left = (e.offsetX - 10) + 'px';
            ripple.style.top = (e.offsetY - 10) + 'px';
            ripple.style.width = ripple.style.height = '20px';
            
            this.style.position = 'relative';
            this.style.overflow = 'hidden';
            this.appendChild(ripple);
            
            setTimeout(() => {
                if (ripple.parentNode) {
                    ripple.remove();
                }
            }, 600);
        });
    }

    /**
     * Add hover animation to card elements
     * @param {HTMLElement} card - Card element to animate
     * @param {Object} options - Animation options
     */
    static addCardHoverEffect(card, options = {}) {
        const defaultOptions = {
            translateY: '-4px',
            scale: '1.02',
            boxShadow: '0 10px 25px rgba(0, 0, 0, 0.15)'
        };
        
        const config = { ...defaultOptions, ...options };
        
        card.addEventListener('mouseenter', function() {
            this.style.transform = `translateY(${config.translateY}) scale(${config.scale})`;
            if (config.boxShadow) {
                this.style.boxShadow = config.boxShadow;
            }
        });
        
        card.addEventListener('mouseleave', function() {
            this.style.transform = 'translateY(0) scale(1)';
            this.style.boxShadow = '';
        });
    }

    /**
     * Setup navbar scroll effects
     * @param {HTMLElement} navbar - Navbar element
     * @param {Object} options - Configuration options
     */
    static setupNavbarScrollEffect(navbar, options = {}) {
        const defaultOptions = {
            scrollThreshold: 50,
            scrolledBackground: 'linear-gradient(135deg, rgba(37, 99, 235, 0.95) 0%, rgba(29, 78, 216, 0.95) 100%)',
            defaultBackground: 'linear-gradient(135deg, var(--color-primary-600) 0%, var(--color-primary-700) 100%)',
            backdropFilter: 'blur(20px)'
        };
        
        const config = { ...defaultOptions, ...options };
        
        window.addEventListener('scroll', function() {
            if (window.scrollY > config.scrollThreshold) {
                navbar.style.background = config.scrolledBackground;
                if (config.backdropFilter) {
                    navbar.style.backdropFilter = config.backdropFilter;
                }
            } else {
                navbar.style.background = config.defaultBackground;
                navbar.style.backdropFilter = 'none';
            }
        });
    }

    /**
     * Setup intersection observer for element animations
     * @param {string} selector - CSS selector for elements to observe
     * @param {Object} options - Observer and animation options
     */
    static setupScrollAnimation(selector, options = {}) {
        const defaultOptions = {
            threshold: 0.1,
            rootMargin: '0px 0px -50px 0px',
            animationDelay: 0.1 // seconds between each element
        };
        
        const config = { ...defaultOptions, ...options };
        
        const observerOptions = {
            threshold: config.threshold,
            rootMargin: config.rootMargin
        };
        
        const observer = new IntersectionObserver((entries) => {
            entries.forEach(entry => {
                if (entry.isIntersecting) {
                    entry.target.style.opacity = '1';
                    entry.target.style.transform = 'translateY(0)';
                }
            });
        }, observerOptions);
        
        // Setup elements for animation
        document.querySelectorAll(selector).forEach((el, index) => {
            el.style.opacity = '0';
            el.style.transform = 'translateY(30px)';
            el.style.transition = `all 0.6s ease ${index * config.animationDelay}s`;
            observer.observe(el);
        });
    }

    /**
     * Animate counter from 0 to target value
     * @param {HTMLElement} element - Element containing the number
     * @param {number} target - Target number
     * @param {Object} options - Animation options
     */
    static animateCounter(element, target, options = {}) {
        const defaultOptions = {
            duration: 2000, // ms
            suffix: '',
            prefix: ''
        };
        
        const config = { ...defaultOptions, ...options };
        const increment = target / (config.duration / 20);
        let current = 0;
        
        const timer = setInterval(() => {
            current += increment;
            if (current >= target) {
                current = target;
                clearInterval(timer);
            }
            element.textContent = config.prefix + Math.floor(current) + config.suffix;
        }, 20);
    }
}

/**
 * Smooth scroll utility
 */
class ScrollManager {
    /**
     * Setup smooth scrolling for anchor links
     * @param {string} selector - CSS selector for anchor links (default: 'a[href^="#"]')
     */
    static setupSmoothScroll(selector = 'a[href^="#"]') {
        document.querySelectorAll(selector).forEach(anchor => {
            anchor.addEventListener('click', function (e) {
                e.preventDefault();
                const target = document.querySelector(this.getAttribute('href'));
                if (target) {
                    const navHeight = document.querySelector('.navbar')?.offsetHeight || 0;
                    const targetPosition = target.offsetTop - navHeight - 20;
                    
                    window.scrollTo({
                        top: targetPosition,
                        behavior: 'smooth'
                    });
                }
            });
        });
    }
}

// Add ripple animation CSS if not exists
if (!document.getElementById('ripple-animation-styles')) {
    const style = document.createElement('style');
    style.id = 'ripple-animation-styles';
    style.textContent = `
        @keyframes ripple {
            to {
                transform: scale(4);
                opacity: 0;
            }
        }
    `;
    document.head.appendChild(style);
}

// Export classes globally
window.ToastManager = ToastManager;
window.LoadingManager = LoadingManager;
window.AnimationManager = AnimationManager;
window.ScrollManager = ScrollManager;

// Export for module usage if needed
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { ToastManager, LoadingManager, AnimationManager, ScrollManager };
}