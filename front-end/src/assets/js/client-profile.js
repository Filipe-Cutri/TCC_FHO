/**
 * Client Profile Page - Specific functionality for client profile management
 */

/**
 * Initialize client profile page
 */
function initClientProfile() {
    document.addEventListener('DOMContentLoaded', function() {
        loadProfileData();
        loadPreferencesDisplay();
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
            // Reload preferences display
            loadPreferencesDisplay();
        });
    });
}

/**
 * Load profile data from session/API
 */
async function loadProfileData() {
    try {
        // Get client session
        const session = window.clientSession ? window.clientSession.getSession() : null;
        
        if (!session || !session.id) {
            console.warn('No client session found');
            return;
        }
        
        // Populate form fields with session data
        const nameInput = document.querySelector('input[name="name"], input[id="name"]');
        if (nameInput && session.name) {
            nameInput.value = session.name;
        }
        
        const emailInput = document.querySelector('input[type="email"]');
        if (emailInput && session.email) {
            emailInput.value = session.email;
        }
        
        const phoneInput = document.querySelector('input[name="phone"], input[id="phone"]');
        if (phoneInput && session.phone) {
            phoneInput.value = session.phone;
        }
    } catch (error) {
        console.error('Error loading profile data:', error);
    }
}

/**
 * Load and display preferences from localStorage
 */
function loadPreferencesDisplay() {
    try {
        const preferencesStr = localStorage.getItem('clientPreferences');
        if (!preferencesStr) {
            // Show empty state (default HTML already shows this)
            return;
        }
        
        const preferences = JSON.parse(preferencesStr);
        const container = document.getElementById('preferencesDisplay');
        
        if (!container) return;
        
        // Build preferences display HTML
        let html = '';
        
        // Services
        if (preferences.services && preferences.services.length > 0) {
            const serviceLabels = {
                'corte-masculino': 'Corte Masculino',
                'corte-feminino': 'Corte Feminino',
                'barba': 'Barba Completa',
                'sobrancelha': 'Design de Sobrancelha',
                'tratamento': 'Tratamento Capilar',
                'coloracao': 'Coloração',
                'escova': 'Escova/Chapinha',
                'massagem': 'Massagem Relaxante'
            };
            const serviceNames = preferences.services.map(s => serviceLabels[s] || s).join(', ');
            html += `
                <div class="preference-item">
                    <div class="preference-label">Tipos de Serviços Preferidos:</div>
                    <div class="preference-value">${serviceNames}</div>
                </div>
            `;
        }
        
        // Interval
        if (preferences.intervalo) {
            const intervalLabels = {
                '2-semanas': 'A cada 2 semanas',
                '1-mes': 'Mensalmente',
                '6-semanas': 'A cada 6 semanas',
                '3-meses': 'A cada 3 meses',
                'personalizado': 'Personalizado'
            };
            html += `
                <div class="preference-item">
                    <div class="preference-label">Intervalo de Reagendamento:</div>
                    <div class="preference-value">${intervalLabels[preferences.intervalo] || preferences.intervalo}</div>
                </div>
            `;
        }
        
        // Advance notice
        if (preferences.antecedencia) {
            const advanceLabels = {
                '3-dias': '3 dias antes',
                '1-semana': '1 semana antes',
                '2-semanas': '2 semanas antes'
            };
            html += `
                <div class="preference-item">
                    <div class="preference-label">Antecedência Preferida:</div>
                    <div class="preference-value">${advanceLabels[preferences.antecedencia] || preferences.antecedencia}</div>
                </div>
            `;
        }
        
        // Time preference
        if (preferences.horario) {
            const timeLabels = {
                'manha': 'Manhã (08:00 - 12:00)',
                'tarde': 'Tarde (12:00 - 18:00)',
                'noite': 'Noite (18:00 - 22:00)'
            };
            html += `
                <div class="preference-item">
                    <div class="preference-label">Horário Preferido:</div>
                    <div class="preference-value">${timeLabels[preferences.horario] || preferences.horario}</div>
                </div>
            `;
        }
        
        // Days of week
        if (preferences.diasSemana && preferences.diasSemana.length > 0) {
            const dayLabels = {
                'segunda': 'Segunda-feira',
                'terca': 'Terça-feira',
                'quarta': 'Quarta-feira',
                'quinta': 'Quinta-feira',
                'sexta': 'Sexta-feira',
                'sabado': 'Sábado',
                'domingo': 'Domingo'
            };
            const dayNames = preferences.diasSemana.map(d => dayLabels[d] || d).join(', ');
            html += `
                <div class="preference-item">
                    <div class="preference-label">Dias da Semana:</div>
                    <div class="preference-value">${dayNames}</div>
                </div>
            `;
        }
        
        // Style preference
        if (preferences.estilos && preferences.estilos.length > 0) {
            const styleLabels = {
                'classico': 'Clássico/Tradicional',
                'moderno': 'Moderno/Contemporâneo',
                'alternativo': 'Alternativo/Ousado',
                'casual': 'Casual/Descontraído'
            };
            const styleNames = preferences.estilos.map(s => styleLabels[s] || s).join(', ');
            html += `
                <div class="preference-item">
                    <div class="preference-label">Estilo Preferido:</div>
                    <div class="preference-value">${styleNames}</div>
                </div>
            `;
        }
        
        // Restrictions
        if (preferences.restricoes) {
            html += `
                <div class="preference-item">
                    <div class="preference-label">Restrições/Alergias:</div>
                    <div class="preference-value">${preferences.restricoes}</div>
                </div>
            `;
        }
        
        // Observations
        if (preferences.observacoes) {
            html += `
                <div class="preference-item">
                    <div class="preference-label">Observações Especiais:</div>
                    <div class="preference-value">${preferences.observacoes}</div>
                </div>
            `;
        }
        
        // If we have any preferences, show them
        if (html) {
            container.innerHTML = html;
        }
        
    } catch (error) {
        console.error('Error loading preferences display:', error);
    }
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
        
        // Reload preferences display
        loadPreferencesDisplay();
        
        // Close modal
        ModalManager.hide('#preferencesModal');
    }
}

// Make functions globally available for onclick handlers
window.savePreferences = savePreferences;

// Initialize the page
initClientProfile();