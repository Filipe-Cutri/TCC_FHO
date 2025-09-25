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
 * Setup profile form handling with real API integration
 */
function setupProfileFormHandling() {
    const forms = document.querySelectorAll('form');
    forms.forEach(form => {
        form.addEventListener('submit', async function(e) {
            e.preventDefault();
            
            const submitBtn = this.querySelector('button[type="submit"]');
            const resetButton = LoadingManager.setButtonLoading(submitBtn, 'Salvando...');
            
            try {
                // Get client ID from localStorage
                const user = JSON.parse(localStorage.getItem('user') || '{}');
                const clientId = user.id;
                
                if (!clientId) {
                    ToastManager.showError('Sessão expirada. Faça login novamente.');
                    resetButton();
                    return;
                }
                
                // Collect form data
                const formData = new FormData(this);
                const data = Object.fromEntries(formData.entries());
                
                // Real API call to update profile
                const response = await apiClient.put(API_CONFIG.endpoints.client.profile.update, {
                    clientId: clientId,
                    name: data.name,
                    phone: data.phone
                });
                
                if (response.success) {
                    LoadingManager.setButtonSuccess(submitBtn, 'Salvo com sucesso!');
                    ToastManager.showSuccess(response.message || 'Perfil atualizado com sucesso!');
                    
                    // Update localStorage with new data
                    if (response.data) {
                        localStorage.setItem('user', JSON.stringify(response.data));
                    }
                } else {
                    throw new Error(response.message || 'Erro ao salvar perfil');
                }
                
            } catch (error) {
                console.error('Error saving profile:', error);
                LoadingManager.setButtonError(submitBtn, 'Erro ao salvar');
                ToastManager.showError(error.message || 'Erro ao salvar perfil. Tente novamente.');
            } finally {
                setTimeout(() => {
                    resetButton();
                }, 2000);
            }
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