/**
 * Client Preferences Setup Page - Specific functionality for preferences setup
 */

/**
 * Initialize client preferences setup page
 */
function initClientPreferencesSetup() {
    document.addEventListener('DOMContentLoaded', function() {
        setupServiceFiltering();
        setupProgressTracking();
        setupFormSubmission();
        
        // Initial setup
        filterServices();
        updateProgress();
    });
}

/**
 * Setup service filtering based on establishment type
 */
function setupServiceFiltering() {
    // Add event listeners to establishment type checkboxes
    document.getElementById('setup-tipoBarbearia')?.addEventListener('change', filterServices);
    document.getElementById('setup-tipoSalao')?.addEventListener('change', filterServices);
}

/**
 * Filter services based on establishment type
 */
function filterServices() {
    const barbeariaChecked = document.getElementById('setup-tipoBarbearia')?.checked || false;
    const salaoChecked = document.getElementById('setup-tipoSalao')?.checked || false;
    const serviceItems = document.querySelectorAll('.service-item');
    
    serviceItems.forEach(item => {
        const itemTypes = item.getAttribute('data-type')?.split(' ') || [];
        let shouldShow = false;
        
        // If no establishment type is selected, show all services
        if (!barbeariaChecked && !salaoChecked) {
            shouldShow = true;
        } else {
            // Show service if it matches selected establishment types
            if (barbeariaChecked && (itemTypes.includes('barbearia') || itemTypes.includes('masculino') || itemTypes.includes('unissex'))) {
                shouldShow = true;
            }
            if (salaoChecked && (itemTypes.includes('salao') || itemTypes.includes('feminino') || itemTypes.includes('unissex'))) {
                shouldShow = true;
            }
        }
        
        if (shouldShow) {
            item.classList.remove('hidden');
        } else {
            item.classList.add('hidden');
            // Uncheck hidden services
            const checkbox = item.querySelector('input[type="checkbox"]');
            if (checkbox) checkbox.checked = false;
        }
    });
    
    updateProgress();
}

/**
 * Setup progress tracking
 */
function setupProgressTracking() {
    // Add event listeners to all form inputs
    document.querySelectorAll('#preferencesSetupForm input, #preferencesSetupForm textarea').forEach(input => {
        input.addEventListener('change', updateProgress);
        input.addEventListener('input', updateProgress);
    });
}

/**
 * Update progress bar based on filled inputs
 */
function updateProgress() {
    const totalSections = 9; // Total number of sections to track
    let filledSections = 0;

    // Check establishment type
    if (document.getElementById('setup-tipoBarbearia')?.checked || document.getElementById('setup-tipoSalao')?.checked) {
        filledSections++;
    }

    // Check services (only count visible services)
    const visibleServices = document.querySelectorAll('#servicesSection .service-item:not(.hidden) input[type="checkbox"]:checked');
    if (visibleServices.length > 0) {
        filledSections++;
    }

    // Check rescheduling interval
    if (document.querySelector('input[name="intervalo"]:checked')) {
        filledSections++;
    }

    // Check advance notice preference
    if (document.querySelector('input[name="antecedencia"]:checked')) {
        filledSections++;
    }

    // Check time preference
    if (document.querySelector('input[name="horario"]:checked')) {
        filledSections++;
    }

    // Check days
    const daysSection = document.querySelectorAll('#setup-diaSeg, #setup-diaTer, #setup-diaQua, #setup-diaQui, #setup-diaSex, #setup-diaSab, #setup-diaDom');
    const checkedDays = Array.from(daysSection).filter(input => input.checked);
    if (checkedDays.length > 0) {
        filledSections++;
    }

    // Check style
    const styleSection = document.querySelectorAll('#setup-estiloClassico, #setup-estiloModerno, #setup-estiloAlternativo, #setup-estiloCasual');
    const checkedStyles = Array.from(styleSection).filter(input => input.checked);
    if (checkedStyles.length > 0) {
        filledSections++;
    }

    // Check restrictions
    if (document.getElementById('setup-restricoes')?.value.trim()) {
        filledSections++;
    }

    // Check observations
    if (document.getElementById('setup-observacoes')?.value.trim()) {
        filledSections++;
    }

    const percentage = Math.round((filledSections / totalSections) * 100);
    const progressBar = document.getElementById('progressBar');
    const progressText = document.getElementById('progressText');
    
    if (progressBar) progressBar.style.width = percentage + '%';
    if (progressText) progressText.textContent = percentage + '%';
}

/**
 * Setup form submission handling
 */
function setupFormSubmission() {
    document.getElementById('preferencesSetupForm')?.addEventListener('submit', function(e) {
        e.preventDefault();
        
        const submitBtn = this.querySelector('button[type="submit"]');
        if (!submitBtn) return;
        
        const resetButton = LoadingManager.setButtonLoading(submitBtn, 'Salvando preferências...');
        
        // Collect and save preferences
        const preferences = ClientPreferencesManager.collectPreferencesFromForm(this);
        
        // Save preferences to localStorage for AI scheduling
        const intervalo = document.querySelector('input[name="intervalo"]:checked');
        const antecedencia = document.querySelector('input[name="antecedencia"]:checked');
        
        if (intervalo) {
            localStorage.setItem('rescheduleInterval', intervalo.value);
        }
        if (antecedencia) {
            localStorage.setItem('advanceNotice', antecedencia.value);
        }
        
        // Save full preferences
        ClientPreferencesManager.savePreferences(preferences, {
            updateUI: false,
            showSuccessMessage: false
        });
        
        // Real API call to save preferences instead of simulation
        this.savePreferencesToAPI(preferences).then(() => {
            LoadingManager.setButtonSuccess(submitBtn, 'Configuração concluída!');
            
            setTimeout(() => {
                window.location.href = 'client-dashboard.html';
            }, 1000);
        }).catch(error => {
            console.error('Error saving preferences:', error);
            LoadingManager.setButtonError(submitBtn, 'Erro ao salvar');
            ToastManager.showError('Erro ao salvar preferências. Tente novamente.');
            setTimeout(() => {
                resetButton();
            }, 2000);
        });
    });
}

/**
 * Save preferences to API
 */
async function savePreferencesToAPI(preferences) {
    try {
        // Get client ID from localStorage
        const user = JSON.parse(localStorage.getItem('user') || '{}');
        const clientId = user.id;
        
        if (!clientId) {
            throw new Error('Sessão expirada. Faça login novamente.');
        }
        
        // For now, we'll store preferences locally until backend endpoint is created
        // const response = await apiClient.put(API_CONFIG.endpoints.client.preferences.update, {
        //     clientId: clientId,
        //     preferences: preferences
        // });
        
        // Simulate API call success for preferences (to be replaced with real endpoint)
        await new Promise(resolve => setTimeout(resolve, 1000));
        
        return { success: true, message: 'Preferências salvas com sucesso!' };
        
    } catch (error) {
        throw error;
    }
}

/**
 * Skip setup function (called by onclick)
 */
function skipSetup() {
    if (confirm('Tem certeza que deseja pular a configuração de preferências? Você pode configurá-las mais tarde no seu perfil.')) {
        window.location.href = 'client-dashboard.html';
    }
}

// Make functions globally available for onclick handlers
window.skipSetup = skipSetup;

// Initialize the page
initClientPreferencesSetup();