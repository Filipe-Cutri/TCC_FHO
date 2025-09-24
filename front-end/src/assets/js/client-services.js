/**
 * Client Services Manager - Enhanced AI recommendations and service booking
 */
class ClientServices {
    constructor() {
        this.userPreferences = this.loadUserPreferences();
        this.aiRecommendations = [];
        this.selectedService = null;
    }

    /**
     * Initialize services functionality
     */
    static init() {
        const instance = new ClientServices();
        instance.setupEventListeners();
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

        // Service booking buttons
        document.addEventListener('click', (e) => {
            if (e.target.matches('.client-btn')) {
                const serviceCard = e.target.closest('.client-card');
                if (serviceCard) {
                    const serviceName = serviceCard.querySelector('.card-title').textContent.trim();
                    this.handleServiceBooking(serviceName);
                }
            }
        });
    }

    /**
     * Load user preferences from localStorage
     */
    loadUserPreferences() {
        const stored = localStorage.getItem('clientPreferences');
        return stored ? JSON.parse(stored) : {
            preferredTimes: ['afternoon'],
            serviceHistory: [{
                serviceName: 'Corte Masculino',
                category: 'corte',
                date: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000)
            }],
            budget: 'medium'
        };
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
            // Get client preferences from localStorage
            const user = JSON.parse(localStorage.getItem('user') || '{}');
            const clientId = user.id;
            
            if (!clientId) {
                throw new Error('Sessão expirada');
            }

            // For now, return mock data until backend AI endpoint is implemented
            // const response = await apiClient.post('/api/client/ai/recommendations', {
            //     clientId: clientId,
            //     preferences: JSON.parse(localStorage.getItem('clientPreferences') || '{}')
            // });
            
            // Simulate API delay
            await new Promise(resolve => setTimeout(resolve, 2000));
            
            return this.generateAIRecommendations();
            
        } catch (error) {
            throw error;
        }
    }

    /**
     * Generate AI recommendations
     */
    generateAIRecommendations() {
        const baseDate = new Date();
        
        const recommendations = [
            {
                id: 1,
                service: 'Corte + Barba',
                professional: 'João Silva',
                establishment: 'Barbearia Premium - Centro',
                date: new Date(baseDate.getTime() + 2 * 24 * 60 * 60 * 1000),
                time: '14:00',
                price: 65,
                confidence: 95,
                reason: 'Baseado no seu histórico de cortes, você prefere combos completos nos horários da tarde'
            },
            {
                id: 2,
                service: 'Corte Masculino',
                professional: 'Carlos Santos',
                establishment: 'Salão Modern',
                date: new Date(baseDate.getTime() + 4 * 24 * 60 * 60 * 1000),
                time: '15:30',
                price: 35,
                confidence: 88,
                reason: 'Opção econômica com profissional bem avaliado'
            }
        ];

        this.aiRecommendations = recommendations;
        return recommendations;
    }

    /**
     * Show AI recommendations
     */
    showAIRecommendations(recommendations) {
        const bestRec = recommendations[0];
        
        const message = `🤖 IA do Slotfy encontrou a melhor opção!\n\n` +
                       `✂️ ${bestRec.service} com ${bestRec.professional}\n` +
                       `📅 ${bestRec.date.toLocaleDateString('pt-BR')} às ${bestRec.time}\n` +
                       `💰 R$ ${bestRec.price},00 (${bestRec.confidence}% confiança)\n\n` +
                       `💡 ${bestRec.reason}\n\n` +
                       `Aceitar esta recomendação?`;

        if (confirm(message)) {
            this.acceptAIRecommendation(bestRec);
        }
    }

    /**
     * Accept AI recommendation
     */
    acceptAIRecommendation(recommendation) {
        // Update preferences
        const prefs = this.userPreferences;
        if (!prefs.serviceHistory) prefs.serviceHistory = [];
        prefs.serviceHistory.push({
            serviceName: recommendation.service,
            date: new Date(),
            aiAccepted: true
        });
        localStorage.setItem('clientPreferences', JSON.stringify(prefs));
        
        alert(`✅ Perfeito! Agendamento confirmado:\n${recommendation.service} com ${recommendation.professional}\n${recommendation.date.toLocaleDateString('pt-BR')} às ${recommendation.time}`);
        
        // Redirect to bookings
        setTimeout(() => {
            window.location.href = 'client-bookings.html';
        }, 2000);
    }

    /**
     * Handle service booking
     */
    handleServiceBooking(serviceName) {
        localStorage.setItem('selectedService', JSON.stringify({ name: serviceName }));
        window.location.href = `client-professionals.html?service=${encodeURIComponent(serviceName)}`;
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    window.clientServicesInstance = ClientServices.init();
});