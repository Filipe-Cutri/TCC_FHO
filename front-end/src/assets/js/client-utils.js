/**
 * Client Utilities - Client-specific functionality
 * Shared functionality for client preferences, profiles, services, and AI features
 */

/**
 * Client preferences manager
 */
class ClientPreferencesManager {
    /**
     * Save preferences to localStorage and update UI
     * @param {Object} preferences - Preferences object
     * @param {Object} options - Configuration options
     */
    static savePreferences(preferences, options = {}) {
        const defaultOptions = {
            updateUI: true,
            showSuccessMessage: true,
            successMessage: 'Preferências salvas com sucesso!',
            ...options
        };

        try {
            // Save to localStorage for AI scheduling
            if (preferences.intervalo) {
                localStorage.setItem('rescheduleInterval', preferences.intervalo);
            }
            if (preferences.antecedencia) {
                localStorage.setItem('advanceNotice', preferences.antecedencia);
            }

            // Save full preferences
            localStorage.setItem('clientPreferences', JSON.stringify(preferences));

            if (defaultOptions.updateUI) {
                this.updatePreferencesDisplay(preferences);
            }

            if (defaultOptions.showSuccessMessage) {
                ToastManager.showSuccess(defaultOptions.successMessage);
            }

            return true;
        } catch (error) {
            console.error('Error saving preferences:', error);
            ToastManager.showError('Erro ao salvar preferências. Tente novamente.');
            return false;
        }
    }

    /**
     * Load preferences from localStorage
     * @returns {Object} Saved preferences or empty object
     */
    static loadPreferences() {
        try {
            const saved = localStorage.getItem('clientPreferences');
            return saved ? JSON.parse(saved) : {};
        } catch (error) {
            console.error('Error loading preferences:', error);
            return {};
        }
    }

    /**
     * Update preferences display in UI
     * @param {Object} preferences - Preferences object
     */
    static updatePreferencesDisplay(preferences) {
        const preferenceItems = document.querySelectorAll('.preference-item .preference-value');
        
        if (preferenceItems.length >= 8) {
            // Update services
            preferenceItems[0].textContent = preferences.servicos?.join(', ') || 'Não definido';
            
            // Update interval
            const intervalTexts = {
                '2-semanas': 'A cada 2 semanas',
                '1-mes': 'Mensalmente',
                '6-semanas': 'A cada 6 semanas',
                '3-meses': 'A cada 3 meses',
                'personalizado': 'Personalizado'
            };
            preferenceItems[1].textContent = intervalTexts[preferences.intervalo] || 'Não definido';
            
            // Update advance notice
            const antecedenciaTexts = {
                '3-dias': '3 dias de antecedência',
                '1-semana': '1 semana de antecedência',
                '2-semanas': '2 semanas de antecedência'
            };
            preferenceItems[2].textContent = antecedenciaTexts[preferences.antecedencia] || 'Não definido';
            
            // Update time preference
            const horarioTexts = {
                'manha': 'Manhã (8h às 12h)',
                'tarde': 'Tarde (12h às 18h)',
                'noite': 'Noite (18h às 22h)'
            };
            preferenceItems[3].textContent = preferences.horario ? 
                horarioTexts[preferences.horario] : 'Não definido';
            
            // Update days
            preferenceItems[4].textContent = preferences.dias?.join(', ') || 'Não definido';
            
            // Update styles
            preferenceItems[5].textContent = preferences.estilos?.join(', ') || 'Não definido';
            
            // Update restrictions
            preferenceItems[6].textContent = preferences.restricoes || 'Nenhuma restrição informada';
            
            // Update observations
            preferenceItems[7].textContent = preferences.observacoes || 'Nenhuma observação especial';
        }
    }

    /**
     * Collect preferences from form
     * @param {HTMLFormElement} form - Preferences form
     * @returns {Object} Collected preferences
     */
    static collectPreferencesFromForm(form) {
        const data = FormDataCollector.collectFormData(form);
        
        const preferences = {
            servicos: [],
            dias: [],
            estilos: [],
            intervalo: null,
            antecedencia: null,
            horario: null,
            restricoes: '',
            observacoes: ''
        };

        // Collect services
        data.checkboxes.servicos?.forEach(item => {
            preferences.servicos.push(item.text);
        });

        // Collect days
        data.checkboxes.dias?.forEach(item => {
            preferences.dias.push(item.text);
        });

        // Collect styles
        data.checkboxes.estilos?.forEach(item => {
            preferences.estilos.push(item.text);
        });

        // Collect radio button selections
        if (data.radioButtons.intervalo) {
            preferences.intervalo = data.radioButtons.intervalo.value;
        }
        if (data.radioButtons.antecedencia) {
            preferences.antecedencia = data.radioButtons.antecedencia.value;
        }
        if (data.radioButtons.horario) {
            preferences.horario = data.radioButtons.horario.value;
        }

        // Collect text fields
        preferences.restricoes = form.querySelector('#restricoes, #setup-restricoes')?.value || '';
        preferences.observacoes = form.querySelector('#observacoes, #setup-observacoes')?.value || '';

        return preferences;
    }
}

/**
 * Client AI features manager
 */
class ClientAIManager {
    /**
     * Generate AI scheduling recommendation
     * @param {string} serviceName - Name of the service
     * @returns {Object} AI recommendation object
     */
    static generateAIRecommendation(serviceName) {
        const userInterval = localStorage.getItem('rescheduleInterval') || '1-mes';
        const userAdvanceNotice = localStorage.getItem('advanceNotice') || '1-semana';
        const today = new Date();
        
        // Calculate next appointment date based on interval
        let nextAppointmentDate = new Date(today);
        
        switch(userInterval) {
            case '2-semanas':
                nextAppointmentDate.setDate(today.getDate() + 14);
                break;
            case '6-semanas':
                nextAppointmentDate.setDate(today.getDate() + 42);
                break;
            case '3-meses':
                nextAppointmentDate.setMonth(today.getMonth() + 3);
                break;
            case 'personalizado':
                nextAppointmentDate.setDate(today.getDate() + 30); // Default to 30 days
                break;
            default:
                nextAppointmentDate.setMonth(today.getMonth() + 1);
        }
        
        // Format date for display
        const formattedDate = nextAppointmentDate.toLocaleDateString('pt-BR', {
            day: '2-digit',
            month: '2-digit',
            year: 'numeric'
        });
        
        // Calculate when to schedule based on advance notice
        let scheduleDate = new Date(nextAppointmentDate);
        switch(userAdvanceNotice) {
            case '3-dias':
                scheduleDate.setDate(nextAppointmentDate.getDate() - 3);
                break;
            case '1-semana':
                scheduleDate.setDate(nextAppointmentDate.getDate() - 7);
                break;
            case '2-semanas':
                scheduleDate.setDate(nextAppointmentDate.getDate() - 14);
                break;
        }
        
        const intervalTexts = {
            '2-semanas': 'a cada 2 semanas',
            '1-mes': 'mensalmente',
            '6-semanas': 'a cada 6 semanas',
            '3-meses': 'a cada 3 meses',
            'personalizado': 'conforme necessidade'
        };
        
        const advanceTexts = {
            '3-dias': '3 dias de antecedência',
            '1-semana': '1 semana de antecedência',
            '2-semanas': '2 semanas de antecedência'
        };
        
        return {
            serviceName,
            appointmentDate: formattedDate,
            time: '14:30',
            professional: '',
            interval: intervalTexts[userInterval] || 'mensalmente',
            advanceNotice: advanceTexts[userAdvanceNotice] || '1 semana',
            scheduleDate
        };
    }

    /**
     * Show AI recommendation modal/alert
     * @param {Object} recommendation - AI recommendation object
     * @returns {boolean} User confirmation
     */
    static showAIRecommendation(recommendation) {
        const message = `🤖 IA Slotfy Recomenda:\n\n` +
            `📅 Próximo Agendamento: ${recommendation.appointmentDate}\n` +
            `🕐 Horário: ${recommendation.time}\n` +
            `👨‍💼 Profissional: ${recommendation.professional}\n` +
            `✂️ Serviço: ${recommendation.serviceName}\n\n` +
            `✨ Baseado nas suas preferências:\n` +
            `• Intervalo preferido: ${recommendation.interval}\n` +
            `• Antecedência preferida: ${recommendation.advanceNotice}\n` +
            `• Horário baseado no seu perfil\n` +
            `• Profissional com melhor avaliação\n\n` +
            `💡 A IA agendará automaticamente baseada no seu intervalo preferido.\n\n` +
            `Deseja confirmar este agendamento?`;
        
        return confirm(message);
    }

    /**
     * Trigger AI recommendation for a service
     * @param {string} serviceName - Name of the service
     * @param {HTMLElement} button - Button that triggered the action
     */
    static triggerAIRecommendation(serviceName, button) {
        const originalHTML = button.innerHTML;
        
        // Show AI thinking animation
        button.innerHTML = '<i class="fas fa-brain me-2"></i>IA Analisando...';
        button.disabled = true;
        button.style.background = 'linear-gradient(135deg, #6366f1 0%, #8b5cf6 100%)';
        
        setTimeout(() => {
            button.innerHTML = '<i class="fas fa-magic me-2"></i>IA Recomenda!';
            button.style.background = 'linear-gradient(135deg, #10b981 0%, #059669 100%)';
            
            setTimeout(() => {
                const recommendation = this.generateAIRecommendation(serviceName);
                const confirmed = this.showAIRecommendation(recommendation);
                
                if (confirmed) {
                    ToastManager.showSuccess('Agendamento confirmado! Você receberá uma notificação no momento ideal.');
                } else {
                    ToastManager.showInfo('Agendamento cancelado. Você pode agendar manualmente quando desejar.');
                }
                
                // Reset button
                setTimeout(() => {
                    button.innerHTML = originalHTML;
                    button.disabled = false;
                    button.style.background = 'linear-gradient(135deg, #ff6b6b 0%, #ffa726 100%)';
                }, 1000);
            }, 2000);
        }, 3000);
    }
}

/**
 * Client service booking manager
 */
class ClientServiceManager {
    /**
     * Handle service booking click
     * @param {HTMLElement} button - Booking button
     * @param {string} serviceName - Name of the service
     */
    static async handleServiceBooking(button, serviceName) {
        const originalText = button.innerHTML;
        
        // Add loading state
        button.innerHTML = '<span class="client-loading">Agendando...</span>';
        button.disabled = true;
        
        try {
            // Get client ID from localStorage
            const user = JSON.parse(localStorage.getItem('user') || '{}');
            const clientId = user.id;
            
            if (!clientId) {
                ToastManager.showError('Sessão expirada. Faça login novamente.');
                return;
            }
            
            // For now, this is a placeholder until we have service selection and appointment booking flow
            // In a real implementation, this would:
            // 1. Open a service selection modal
            // 2. Allow user to select professional, date, time
            // 3. Make API call to book appointment
            
            // Real API call to book appointment (placeholder structure)
            // const response = await apiClient.post(API_CONFIG.endpoints.client.appointments.book, {
            //     clientId: clientId,
            //     serviceId: serviceId,
            //     professionalId: professionalId,
            //     establishmentId: establishmentId,
            //     appointmentDateTime: selectedDateTime,
            //     notes: notes
            // });
            
            // Simulate API call for now
            await new Promise(resolve => setTimeout(resolve, 1000));
            
            button.innerHTML = '<i class="fas fa-check me-2"></i>Agendado!';
            button.classList.add('client-btn-success');
            
            ToastManager.showSuccess(`${serviceName} agendado com sucesso!`);
            
        } catch (error) {
            console.error('Error booking service:', error);
            button.innerHTML = '<i class="fas fa-times me-2"></i>Erro';
            button.classList.add('client-btn-error');
            ToastManager.showError(error.message || 'Erro ao agendar serviço. Tente novamente.');
        } finally {
            setTimeout(() => {
                button.innerHTML = originalText;
                button.disabled = false;
                button.classList.remove('client-btn-success', 'client-btn-error');
            }, 2000);
        }
    }

    /**
     * Setup service booking interactions
     */
    static setupServiceBooking() {
        document.querySelectorAll('.client-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const serviceName = this.closest('.client-card')?.querySelector('.card-title')?.textContent?.trim() || 'Serviço';
                ClientServiceManager.handleServiceBooking(this, serviceName);
            });
        });
    }

    /**
     * Setup AI booking buttons
     */
    static setupAIBooking() {
        document.querySelectorAll('.ai-btn').forEach(btn => {
            btn.addEventListener('click', function() {
                const serviceName = this.closest('.client-card')?.querySelector('.card-title')?.textContent?.trim() || 'Serviço';
                ClientAIManager.triggerAIRecommendation(serviceName, this);
            });
        });
    }
}

/**
 * Client profile manager
 */
class ClientProfileManager {
    /**
     * Update profile display with data
     * @param {Object} profileData - Profile data
     */
    static updateProfileDisplay(profileData) {
        // Update profile fields
        const fields = {
            '.profile-name': profileData.name,
            '.profile-email': profileData.email,
            '.profile-phone': profileData.phone,
            '.profile-location': profileData.location
        };

        Object.entries(fields).forEach(([selector, value]) => {
            const element = document.querySelector(selector);
            if (element && value) {
                element.textContent = value;
            }
        });
    }

    /**
     * Setup profile form handling
     * @param {string} formSelector - CSS selector for profile form
     */
    static setupProfileForm(formSelector) {
        const form = document.querySelector(formSelector);
        if (!form) return;

        form.addEventListener('submit', function(e) {
            e.preventDefault();
            
            const formData = new FormData(form);
            const profileData = Object.fromEntries(formData.entries());
            
            // Save to localStorage
            localStorage.setItem('clientProfile', JSON.stringify(profileData));
            
            // Update UI
            ClientProfileManager.updateProfileDisplay(profileData);
            
            ToastManager.showSuccess('Perfil atualizado com sucesso!');
        });
    }
}

/**
 * Client navigation and common interactions
 */
class ClientCommonManager {
    /**
     * Setup common client page interactions
     */
    static setupCommonInteractions() {
        // Setup navbar scroll effects
        const navbar = document.querySelector('.client-navbar');
        if (navbar) {
            AnimationManager.setupNavbarScrollEffect(navbar, {
                scrollThreshold: 50,
                scrolledBackground: 'linear-gradient(135deg, rgba(37, 99, 235, 0.95) 0%, rgba(29, 78, 216, 0.95) 100%)',
                defaultBackground: 'linear-gradient(135deg, var(--color-primary-600) 0%, var(--color-primary-700) 100%)'
            });
        }

        // Setup card hover effects
        document.querySelectorAll('.client-card').forEach(card => {
            AnimationManager.addCardHoverEffect(card);
        });

        // Setup button ripple effects
        document.querySelectorAll('.client-btn').forEach(btn => {
            AnimationManager.addRippleEffect(btn);
        });

        // Setup scroll animations
        AnimationManager.setupScrollAnimation('.client-card, .feature-card');
    }

    /**
     * Initialize client page
     */
    static initClientPage() {
        document.addEventListener('DOMContentLoaded', function() {
            ClientCommonManager.setupCommonInteractions();
            ClientServiceManager.setupServiceBooking();
            ClientServiceManager.setupAIBooking();
        });
    }
}

// Export classes globally
window.ClientPreferencesManager = ClientPreferencesManager;
window.ClientAIManager = ClientAIManager;
window.ClientServiceManager = ClientServiceManager;
window.ClientProfileManager = ClientProfileManager;
window.ClientCommonManager = ClientCommonManager;

// Export for module usage if needed
if (typeof module !== 'undefined' && module.exports) {
    module.exports = { 
        ClientPreferencesManager, 
        ClientAIManager, 
        ClientServiceManager, 
        ClientProfileManager, 
        ClientCommonManager 
    };
}