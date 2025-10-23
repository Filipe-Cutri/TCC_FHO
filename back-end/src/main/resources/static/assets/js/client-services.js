/**
 * Client Services Manager - Enhanced AI recommendations and service booking
 */
class ClientServices {
    constructor() {
        this.userPreferences = this.loadUserPreferences();
        this.aiRecommendations = [];
        this.selectedService = null;
        this.services = [];
        this.establishmentId = null;
        this.establishmentCategory = null;
    }

    /**
     * Initialize services functionality
     */
    static init() {
        const instance = new ClientServices();
        instance.setupEventListeners();
        instance.loadServices();
        return instance;
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        // AI Scheduling button
        const aiBtn = document.getElementById('aiSchedulingBtn');
        if (aiBtn) {
            aiBtn.addEventListener('click', () => this.handleAIScheduling());
        }

        // Service booking buttons - using event delegation for dynamic content
        document.addEventListener('click', (e) => {
            if (e.target.closest('.client-btn[data-service-id]')) {
                const button = e.target.closest('.client-btn[data-service-id]');
                const serviceName = button.getAttribute('data-service-name');
                this.handleServiceBooking(serviceName);
            }
        });

        // Professional selection - load time slots when professional is selected
        const professionalSelect = document.getElementById('professionalSelect');
        if (professionalSelect) {
            professionalSelect.addEventListener('change', (e) => {
                const date = document.getElementById('appointmentDate').value;
                if (e.target.value && date) {
                    this.loadAvailableTimeSlots(e.target.value, date);
                }
            });
        }

        // Date selection - load time slots when date is selected
        const dateInput = document.getElementById('appointmentDate');
        if (dateInput) {
            // Set minimum date to today
            const today = new Date().toISOString().split('T')[0];
            dateInput.setAttribute('min', today);

            dateInput.addEventListener('change', (e) => {
                const professionalId = document.getElementById('professionalSelect').value;
                if (professionalId && e.target.value) {
                    this.loadAvailableTimeSlots(professionalId, e.target.value);
                }
            });
        }

        // Confirm booking button
        const confirmBtn = document.getElementById('confirmBookingBtn');
        if (confirmBtn) {
            confirmBtn.addEventListener('click', () => this.confirmBooking());
        }
    }

    /**
     * Load user preferences from localStorage
     */
    loadUserPreferences() {
        const stored = localStorage.getItem('clientPreferences');
        return stored ? JSON.parse(stored) : {};
    }

    /**
     * Handle AI scheduling process
     */
    async handleAIScheduling() {
        const aiBtn = document.getElementById('aiSchedulingBtn');
        const originalText = aiBtn.innerHTML;
        
        // Show loading state
        aiBtn.innerHTML = `
            <span class="ai-btn-icon">
                <i class="fas fa-spinner fa-spin"></i>
            </span>
            <span class="ai-btn-text">
                <span class="ai-btn-main">IA Analisando...</span>
                <span class="ai-btn-sub">Encontrando o melhor horário</span>
            </span>
        `;
        aiBtn.disabled = true;

        // Real AI processing - replace with actual backend API call
        try {
            const recommendations = await this.getAIRecommendationsFromAPI();
            
            // Reset button
            aiBtn.innerHTML = originalText;
            aiBtn.disabled = false;

            // Show recommendations
            this.showAIRecommendations(recommendations);
        } catch (error) {
            console.error('Error getting AI recommendations:', error);
            
            // Reset button on error
            aiBtn.innerHTML = originalText;
            aiBtn.disabled = false;
            
            ToastManager.showError('Erro ao obter recomendações. Tente novamente.');
        }
    }

    /**
     * Get AI recommendations from API
     */
    async getAIRecommendationsFromAPI() {
        try {
            // Get client session using ClientSessionManager
            const session = this.getUserSession();
            const clientId = session ? session.id : null;
            const establishmentId = (session ? session.selectedEstablishmentId : null) || 
                                    this.establishmentId || 
                                    sessionStorage.getItem('selectedEstablishmentId');
            
            if (!clientId) {
                throw new Error('Sessão expirada. Por favor, faça login novamente.');
            }
            
            if (!establishmentId) {
                throw new Error('Nenhum estabelecimento selecionado. Por favor, selecione um estabelecimento.');
            }

            // Get client preferences
            const preferences = JSON.parse(localStorage.getItem('clientPreferences') || '{}');
            
            // Call backend API
            const response = await window.apiClient.post('/api/client/ai/recommendations', {
                clientId: clientId,
                establishmentId: establishmentId,
                preferences: preferences
            });
            
            if (response.success && response.data) {
                return response.data;
            } else {
                throw new Error(response.message || 'Erro ao obter recomendações');
            }
            
        } catch (error) {
            console.error('Error getting AI recommendations:', error);
            throw error;
        }
    }

    /**
     * Generate AI recommendations - removed mock data, now relies on backend API
     */
    generateAIRecommendations() {
        // This method is deprecated - AI recommendations should come from the backend API
        // via getAIRecommendationsFromAPI()
        console.warn('generateAIRecommendations is deprecated. Use getAIRecommendationsFromAPI() instead.');
        return [];
    }

    /**
     * Show AI recommendations
     */
    showAIRecommendations(recommendations) {
        if (!recommendations || recommendations.length === 0) {
            alert('❌ Nenhuma recomendação disponível no momento.\n\nTente novamente mais tarde ou use o agendamento manual.');
            return;
        }
        
        const bestRec = recommendations[0];
        
        // Parse date if it's a string
        let dateObj;
        if (typeof bestRec.date === 'string') {
            dateObj = new Date(bestRec.date);
        } else if (bestRec.date instanceof Date) {
            dateObj = bestRec.date;
        } else {
            dateObj = new Date();
        }
        
        const formattedDate = dateObj.toLocaleDateString('pt-BR');
        const formattedPrice = typeof bestRec.price === 'number' 
            ? bestRec.price.toFixed(2) 
            : parseFloat(bestRec.price).toFixed(2);
        
        const message = `🤖 IA do Slotfy encontrou a melhor opção!\n\n` +
                       `✂️ ${bestRec.service} com ${bestRec.professional}\n` +
                       `📅 ${formattedDate} às ${bestRec.time}\n` +
                       `💰 R$ ${formattedPrice} (${bestRec.confidence}% confiança)\n\n` +
                       `💡 ${bestRec.reason}\n\n` +
                       `Aceitar esta recomendação?`;

        if (confirm(message)) {
            this.acceptAIRecommendation(bestRec);
        }
    }

    /**
     * Accept AI recommendation and book appointment
     */
    async acceptAIRecommendation(recommendation) {
        try {
            // Get client session
            const session = this.getUserSession();
            if (!session || !session.id) {
                alert('Sessão expirada. Por favor, faça login novamente.');
                window.location.href = 'client-login.html';
                return;
            }
            
            // Parse date
            let dateObj;
            if (typeof recommendation.date === 'string') {
                dateObj = new Date(recommendation.date);
            } else if (recommendation.date instanceof Date) {
                dateObj = recommendation.date;
            } else {
                dateObj = new Date();
            }
            
            // Format appointment datetime
            const year = dateObj.getFullYear();
            const month = String(dateObj.getMonth() + 1).padStart(2, '0');
            const day = String(dateObj.getDate()).padStart(2, '0');
            const appointmentDateTime = `${year}-${month}-${day}T${recommendation.time}:00`;
            
            // Book appointment via API
            const response = await window.apiClient.post('/api/client/appointments/book', {
                clientId: session.id,
                professionalId: recommendation.professionalId,
                serviceId: recommendation.serviceId,
                establishmentId: recommendation.establishmentId,
                appointmentDateTime: appointmentDateTime,
                notes: `Agendamento via IA - Confiança: ${recommendation.confidence}%`
            });
            
            if (response.success) {
                // Update preferences
                const prefs = this.userPreferences;
                if (!prefs.serviceHistory) prefs.serviceHistory = [];
                prefs.serviceHistory.push({
                    serviceName: recommendation.service,
                    date: new Date(),
                    aiAccepted: true
                });
                localStorage.setItem('clientPreferences', JSON.stringify(prefs));
                
                alert(`✅ Perfeito! Agendamento confirmado:\n${recommendation.service} com ${recommendation.professional}\n${dateObj.toLocaleDateString('pt-BR')} às ${recommendation.time}`);
                
                // Redirect to bookings
                setTimeout(() => {
                    window.location.href = 'client-bookings.html';
                }, 1500);
            } else {
                alert('Erro ao confirmar agendamento: ' + (response.message || 'Tente novamente.'));
            }
        } catch (error) {
            console.error('Error accepting AI recommendation:', error);
            alert('Erro ao processar agendamento. Por favor, tente novamente.');
        }
    }

    /**
     * Handle service booking - opens manual booking modal
     */
    async handleServiceBooking(serviceName) {
        const service = this.services.find(s => s.name === serviceName);
        
        if (!service) {
            alert('Serviço não encontrado. Por favor, tente novamente.');
            return;
        }

        // Store selected service
        this.selectedService = service;
        
        // Display service info in modal
        const serviceDisplay = document.getElementById('selectedServiceDisplay');
        serviceDisplay.innerHTML = `
            <strong>${service.name}</strong><br>
            <small>Duração: ${service.durationMinutes} minutos | Preço: R$ ${service.price.toFixed(2)}</small>
        `;
        
        // Load professionals for this service
        await this.loadProfessionalsForService(service);
        
        // Show the modal
        const modal = new bootstrap.Modal(document.getElementById('manualBookingModal'));
        modal.show();
    }

    /**
     * Load professionals for the selected service
     */
    async loadProfessionalsForService(service) {
        try {
            const professionalSelect = document.getElementById('professionalSelect');
            professionalSelect.innerHTML = '<option value="">Carregando...</option>';
            
            // Fetch professionals using new client endpoint
            const professionalsEndpoint = window.apiClient.replacePathParams(
                API_CONFIG.endpoints.client.establishments.professionals,
                { id: this.establishmentId }
            );
            const response = await window.apiClient.get(professionalsEndpoint);
            
            if (response.success && response.data && response.data.length > 0) {
                professionalSelect.innerHTML = '<option value="">Selecione um profissional</option>';
                response.data.forEach(prof => {
                    const option = document.createElement('option');
                    option.value = prof.id;
                    option.textContent = `${prof.name} - ${prof.specialties || 'Especialista'}`;
                    professionalSelect.appendChild(option);
                });
            } else {
                professionalSelect.innerHTML = '<option value="">Nenhum profissional disponível</option>';
            }
        } catch (error) {
            console.error('Error loading professionals:', error);
            const professionalSelect = document.getElementById('professionalSelect');
            professionalSelect.innerHTML = '<option value="">Erro ao carregar profissionais</option>';
        }
    }

    /**
     * Load available time slots for selected professional and date
     */
    async loadAvailableTimeSlots(professionalId, date) {
        try {
            const timeSelect = document.getElementById('appointmentTime');
            timeSelect.innerHTML = '<option value="">Carregando horários...</option>';
            
            // For now, generate time slots (8:00 to 18:00, every 30 minutes)
            // In a real implementation, this would call the backend API to get available slots
            const slots = this.generateTimeSlots();
            
            timeSelect.innerHTML = '<option value="">Selecione um horário</option>';
            slots.forEach(slot => {
                const option = document.createElement('option');
                option.value = slot;
                option.textContent = slot;
                timeSelect.appendChild(option);
            });
        } catch (error) {
            console.error('Error loading time slots:', error);
            const timeSelect = document.getElementById('appointmentTime');
            timeSelect.innerHTML = '<option value="">Erro ao carregar horários</option>';
        }
    }

    /**
     * Generate time slots for booking
     */
    generateTimeSlots() {
        const slots = [];
        for (let hour = 8; hour < 18; hour++) {
            slots.push(`${hour.toString().padStart(2, '0')}:00`);
            slots.push(`${hour.toString().padStart(2, '0')}:30`);
        }
        return slots;
    }

    /**
     * Confirm and submit the booking
     */
    async confirmBooking() {
        try {
            const professionalId = document.getElementById('professionalSelect').value;
            const date = document.getElementById('appointmentDate').value;
            const time = document.getElementById('appointmentTime').value;
            const notes = document.getElementById('appointmentNotes').value;

            // Validation
            if (!professionalId) {
                alert('Por favor, selecione um profissional.');
                return;
            }
            if (!date) {
                alert('Por favor, selecione uma data.');
                return;
            }
            if (!time) {
                alert('Por favor, selecione um horário.');
                return;
            }

            // Get client ID from session
            const session = this.getUserSession();
            if (!session || !session.id) {
                alert('Sessão expirada. Por favor, faça login novamente.');
                window.location.href = 'client-login.html';
                return;
            }

            const confirmBtn = document.getElementById('confirmBookingBtn');
            const originalText = confirmBtn.innerHTML;
            confirmBtn.innerHTML = '<i class="fas fa-spinner fa-spin me-2"></i>Processando...';
            confirmBtn.disabled = true;

            // Combine date and time
            const appointmentDateTime = `${date}T${time}:00`;

            // Submit booking to API
            const response = await window.apiClient.post('/api/client/appointments/book', {
                clientId: session.id,
                professionalId: parseInt(professionalId),
                serviceId: this.selectedService.id,
                establishmentId: this.establishmentId,
                appointmentDateTime: appointmentDateTime,
                notes: notes
            });

            if (response.success) {
                // Close modal
                const modal = bootstrap.Modal.getInstance(document.getElementById('manualBookingModal'));
                modal.hide();

                // Show success message
                alert('✅ Agendamento realizado com sucesso!\n\n' +
                      `Serviço: ${this.selectedService.name}\n` +
                      `Data: ${new Date(appointmentDateTime).toLocaleDateString('pt-BR')}\n` +
                      `Horário: ${time}`);

                // Redirect to bookings page
                setTimeout(() => {
                    window.location.href = 'client-bookings.html';
                }, 1500);
            } else {
                confirmBtn.innerHTML = originalText;
                confirmBtn.disabled = false;
                alert('Erro ao realizar agendamento: ' + (response.message || 'Tente novamente.'));
            }
        } catch (error) {
            console.error('Error confirming booking:', error);
            const confirmBtn = document.getElementById('confirmBookingBtn');
            confirmBtn.disabled = false;
            alert('Erro ao processar agendamento. Por favor, tente novamente.');
        }
    }

    /**
     * Load services from API based on selected establishment
     */
    async loadServices() {
        try {
            // Check for establishment ID from URL parameter or session
            const urlParams = new URLSearchParams(window.location.search);
            const establishmentIdFromUrl = urlParams.get('establishmentId');
            
            // Try URL first, then session, then sessionStorage
            const session = this.getUserSession();
            const establishmentIdFromSession = session ? session.selectedEstablishmentId : null;
            const establishmentIdFromStorage = sessionStorage.getItem('selectedEstablishmentId');
            
            this.establishmentId = establishmentIdFromUrl || establishmentIdFromSession || establishmentIdFromStorage;
            
            if (!this.establishmentId) {
                this.showEmptyState('Por favor, selecione um estabelecimento primeiro.');
                this.promptEstablishmentSelection();
                return;
            }
            
            // Save establishment ID to session storage for consistency
            if (this.establishmentId) {
                sessionStorage.setItem('selectedEstablishmentId', this.establishmentId);
                if (window.clientSession && session && session.id) {
                    window.clientSession.setSelectedEstablishmentId(this.establishmentId);
                }
            }

            // Show loading state
            this.showLoading();

            // Fetch establishment details using new client endpoint
            const establishmentEndpoint = window.apiClient.replacePathParams(
                API_CONFIG.endpoints.client.establishments.details,
                { id: this.establishmentId }
            );
            const establishmentResponse = await window.apiClient.get(establishmentEndpoint);
            
            if (establishmentResponse.success && establishmentResponse.data) {
                this.establishmentCategory = establishmentResponse.data.category;
            }

            // Fetch services using new client endpoint
            const servicesEndpoint = window.apiClient.replacePathParams(
                API_CONFIG.endpoints.client.establishments.services,
                { id: this.establishmentId }
            );
            const response = await window.apiClient.get(servicesEndpoint);

            if (response.success && response.data) {
                this.services = response.data;
                this.renderServices();
            } else {
                this.showEmptyState('Nenhum serviço disponível no momento.');
            }

        } catch (error) {
            console.error('Error loading services:', error);
            this.showEmptyState('Erro ao carregar serviços. Tente novamente mais tarde.');
        }
    }

    /**
     * Get user session from localStorage
     */
    getUserSession() {
        try {
            const sessionData = localStorage.getItem('slotfy_client_session');
            if (sessionData) {
                const session = JSON.parse(sessionData);
                return session.user;
            }
        } catch (error) {
            console.error('Error reading session:', error);
        }
        return null;
    }

    /**
     * Show loading state
     */
    showLoading() {
        document.getElementById('servicesLoading').style.display = 'block';
        document.getElementById('servicesEmpty').style.display = 'none';
        document.getElementById('servicesGrid').style.display = 'none';
    }

    /**
     * Show empty state
     */
    showEmptyState(message) {
        document.getElementById('servicesLoading').style.display = 'none';
        document.getElementById('servicesEmpty').style.display = 'block';
        document.getElementById('servicesGrid').style.display = 'none';
        
        const emptyStateText = document.querySelector('#servicesEmpty p');
        if (emptyStateText) {
            emptyStateText.textContent = message;
        }
    }

    /**
     * Render services grid
     */
    renderServices() {
        const grid = document.getElementById('servicesGrid');
        
        if (!this.services || this.services.length === 0) {
            this.showEmptyState('Nenhum serviço disponível no momento.');
            return;
        }

        // Hide loading and empty states
        document.getElementById('servicesLoading').style.display = 'none';
        document.getElementById('servicesEmpty').style.display = 'none';
        grid.style.display = 'flex';

        // Clear existing content
        grid.innerHTML = '';

        // Render each service
        this.services.forEach((service, index) => {
            const serviceCard = this.createServiceCard(service, index);
            grid.appendChild(serviceCard);
        });
    }

    /**
     * Create a service card element
     */
    createServiceCard(service, index) {
        const col = document.createElement('div');
        col.className = 'col-lg-4 mb-4';

        // Determine color based on index
        const colors = [
            { border: 'var(--color-primary-500)', gradient: 'var(--color-primary-500) 0%, var(--color-primary-600) 100%' },
            { border: 'var(--color-accent-success)', gradient: 'var(--color-accent-success) 0%, #059669 100%' },
            { border: 'var(--color-accent-warning)', gradient: 'var(--color-accent-warning) 0%, #d97706 100%' },
            { border: '#8b5cf6', gradient: '#8b5cf6 0%, #7c3aed 100%' },
            { border: '#ec4899', gradient: '#ec4899 0%, #db2777 100%' },
            { border: '#14b8a6', gradient: '#14b8a6 0%, #0d9488 100%' }
        ];
        const color = colors[index % colors.length];

        // Get icon based on category or use default
        const icon = this.getServiceIcon(service.category || service.name);

        // Build image HTML - show image if available, otherwise show icon
        let imageHtml;
        if (service.imageUrl) {
            imageHtml = `<img src="${this.escapeHtml(service.imageUrl)}" alt="${this.escapeHtml(service.name)}" style="width: 100%; height: 200px; object-fit: cover; border-radius: 8px 8px 0 0;">`;
        } else {
            imageHtml = `
                <div style="width: 100%; height: 200px; background: linear-gradient(135deg, ${color.gradient}); display: flex; align-items: center; justify-content: center; border-radius: 8px 8px 0 0;">
                    <i class="${icon}" style="font-size: 4rem; color: white;"></i>
                </div>
            `;
        }

        col.innerHTML = `
            <div class="client-card" style="border-left: 4px solid ${color.border};">
                ${imageHtml}
                <div class="card-body">
                    <h5 class="card-title">
                        <span class="card-icon" style="background: linear-gradient(135deg, ${color.gradient}); color: white;">
                            <i class="${icon}"></i>
                        </span>
                        ${this.escapeHtml(service.name)}
                    </h5>
                    <p class="card-text">${this.escapeHtml(service.description || 'Serviço de qualidade com profissionais especializados')}</p>
                    <div class="d-flex justify-content-between align-items-center mb-3">
                        <span class="client-stats-number text-primary">R$ ${service.price.toFixed(2)}</span>
                        <span class="client-stats-label">⏱ ${service.durationMinutes} min</span>
                    </div>
                    <button class="client-btn w-100" data-service-id="${service.id}" data-service-name="${this.escapeHtml(service.name)}">
                        <i class="fas fa-calendar-plus me-2"></i>Agendar
                    </button>
                </div>
            </div>
        `;

        return col;
    }

    /**
     * Get appropriate icon for service
     */
    getServiceIcon(categoryOrName) {
        const name = (categoryOrName || '').toLowerCase();
        
        // Map common service names to icons
        if (name.includes('corte') && name.includes('masculino')) return 'fas fa-cut';
        if (name.includes('corte') && name.includes('feminino')) return 'fas fa-scissors';
        if (name.includes('barba')) return 'fas fa-beard';
        if (name.includes('escova')) return 'fas fa-wind';
        if (name.includes('manicure') || name.includes('pedicure') || name.includes('unha')) return 'fas fa-hand-sparkles';
        if (name.includes('coloração') || name.includes('tintura')) return 'fas fa-palette';
        if (name.includes('hidratação') || name.includes('tratamento')) return 'fas fa-spa';
        if (name.includes('maquiagem')) return 'fas fa-magic';
        if (name.includes('sobrancelha')) return 'fas fa-eye';
        if (name.includes('depilação')) return 'fas fa-burn';
        if (name.includes('combo') || name.includes('pacote')) return 'fas fa-star';
        
        // Default icon
        return 'fas fa-cut';
    }

    /**
     * Escape HTML to prevent XSS
     */
    escapeHtml(text) {
        const map = {
            '&': '&amp;',
            '<': '&lt;',
            '>': '&gt;',
            '"': '&quot;',
            "'": '&#039;'
        };
        return text ? text.replace(/[&<>"']/g, m => map[m]) : '';
    }

    /**
     * Prompt user to select an establishment
     */
    promptEstablishmentSelection() {
        setTimeout(() => {
            if (confirm('Você precisa selecionar um estabelecimento primeiro. Gostaria de ir para a página de estabelecimentos?')) {
                window.location.href = 'client-establishments.html';
            }
        }, 500);
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    window.clientServicesInstance = ClientServices.init();
});