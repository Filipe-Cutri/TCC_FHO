/**
 * Client Services Page - Specific functionality for client services page
 */

/**
 * Initialize client services page
 */
function initClientServices() {
    document.addEventListener('DOMContentLoaded', function() {
        ClientCommonManager.setupCommonInteractions();
        setupAISchedulingButton();
        ClientServiceManager.setupServiceBooking();
    });
}

/**
 * Setup AI scheduling button functionality
 */
function setupAISchedulingButton() {
    const aiSchedulingBtn = document.getElementById('aiSchedulingBtn');
    if (aiSchedulingBtn) {
        aiSchedulingBtn.addEventListener('click', function() {
            handleAISchedulingClick(this);
        });
    }
}

/**
 * Handle AI scheduling button click
 * @param {HTMLElement} button - The AI scheduling button
 */
function handleAISchedulingClick(button) {
    const originalHTML = button.innerHTML;
    button.disabled = true;
    button.style.background = 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)';
    
    // Show AI thinking state
    button.innerHTML = `
        <span class="ai-btn-icon">
            <i class="fas fa-spinner fa-spin"></i>
        </span>
        <span class="ai-btn-text">
            <span class="ai-btn-main">IA Analisando...</span>
            <span class="ai-btn-sub">Encontrando o melhor horário</span>
        </span>
    `;
    
    // Simulate AI processing
    setTimeout(() => {
        // Show AI results
        button.innerHTML = `
            <span class="ai-btn-icon">
                <i class="fas fa-check-circle"></i>
            </span>
            <span class="ai-btn-text">
                <span class="ai-btn-main">Horário Sugerido!</span>
                <span class="ai-btn-sub">Amanhã às 14:30</span>
            </span>
        `;
        button.style.background = 'linear-gradient(135deg, #10b981 0%, #059669 100%)';
        
        // Show detailed AI suggestion
        setTimeout(() => {
            const recommendation = ClientAIManager.generateAIRecommendation('Corte Masculino');
            const confirmed = ClientAIManager.showAIRecommendation(recommendation);
            
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

// Initialize the page
initClientServices();