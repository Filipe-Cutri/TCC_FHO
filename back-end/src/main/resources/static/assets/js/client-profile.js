/**
 * Client Profile Page - Specific functionality for client profile management
 */

/**
 * Initialize client profile page
 */
function initClientProfile() {
    document.addEventListener('DOMContentLoaded', function() {
        ClientCommonManager.setupCommonInteractions();
        setupProfileFormHandling();
        setupInputEffects();
        
        // Setup preferences modal
        ModalManager.setupFormModal('#preferencesModal', '#preferencesForm', async (data, form) => {
            const preferences = ClientPreferencesManager.collectPreferencesFromForm(form);
            ClientPreferencesManager.savePreferences(preferences, {
                updateUI: true,
                showSuccessMessage: true,
                successMessage: 'Preferências salvas com sucesso!'
            });
        });
    });
}

/**
 * Setup profile form handling
 */
function setupProfileFormHandling() {
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const submitBtn = this.querySelector('button[type="submit"]');
            const resetButton = LoadingManager.setButtonLoading(submitBtn, 'Salvando...');
            
            // Simulate save operation
            setTimeout(() => {
                LoadingManager.setButtonSuccess(submitBtn, 'Salvo com sucesso!');
                resetButton();
            }, 1500);
        });
    });
}

/**
 * Setup input focus effects
 */
function setupInputEffects() {
    document.querySelectorAll('.client-form-control').forEach(input => {
        input.addEventListener('focus', function() {
            this.parentElement.style.transform = 'scale(1.02)';
            this.parentElement.style.transition = 'transform 0.3s ease';
        });
        
        input.addEventListener('blur', function() {
            this.parentElement.style.transform = 'scale(1)';
        });
    });
}

/**
 * Save preferences function (called by onclick)
 */
function savePreferences() {
    const form = document.getElementById('preferencesForm');
    if (form) {
        const preferences = ClientPreferencesManager.collectPreferencesFromForm(form);
        ClientPreferencesManager.savePreferences(preferences, {
            updateUI: true,
            showSuccessMessage: true,
            successMessage: 'Preferências salvas com sucesso!'
        });
        
        // Close modal
        ModalManager.hide('#preferencesModal');
    }
}

// Make functions globally available for onclick handlers
window.savePreferences = savePreferences;

// Initialize the page
initClientProfile();