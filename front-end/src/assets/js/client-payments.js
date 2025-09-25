/**
 * Client Payments Manager - Handles PIX payments and payment history
 */
class ClientPayments {
    constructor() {
        this.selectedAppointment = null;
        this.countdownTimer = null;
        this.timeRemaining = 15 * 60; // 15 minutes in seconds
    }

    /**
     * Initialize payments functionality
     */
    static init() {
        const instance = new ClientPayments();
        instance.setupEventListeners();
        instance.loadPaymentHistory();
    }

    /**
     * Setup event listeners
     */
    setupEventListeners() {
        const appointmentSelect = document.getElementById('appointment-select');
        if (appointmentSelect) {
            appointmentSelect.addEventListener('change', (e) => {
                this.onAppointmentSelect(e.target.value);
            });
        }
    }

    /**
     * Handle appointment selection
     */
    async onAppointmentSelect(appointmentId) {
        if (!appointmentId) {
            this.hideQRCode();
            return;
        }

        try {
            // Get client ID from localStorage
            const user = JSON.parse(localStorage.getItem('user') || '{}');
            const clientId = user.id;
            
            if (!clientId) {
                ToastManager.showError('Sessão expirada. Faça login novamente.');
                return;
            }

            // Real API call to get client appointments
            const response = await apiClient.get(API_CONFIG.endpoints.client.appointments.list, {
                clientId: clientId
            });

            if (response.success && response.data) {
                const appointment = response.data.find(apt => apt.id.toString() === appointmentId.toString());
                
                if (appointment) {
                    this.selectedAppointment = appointment;
                    this.generatePixPayment();
                } else {
                    ToastManager.showError('Agendamento não encontrado.');
                }
            } else {
                ToastManager.showError('Erro ao carregar agendamentos.');
            }
            
        } catch (error) {
            console.error('Error loading appointment:', error);
            ToastManager.showError('Erro ao carregar dados do agendamento.');
        }
    }

    /**
     * Generate PIX payment and QR Code
     */
    generatePixPayment() {
        if (!this.selectedAppointment) return;

        const appointment = this.selectedAppointment;
        
        // Generate PIX code (in real implementation, this would come from backend)
        const pixCode = this.generatePixCode(appointment);
        
        // Generate QR Code
        const qrCodeUrl = `https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=${encodeURIComponent(pixCode)}`;
        
        this.displayQRCode(qrCodeUrl, pixCode, appointment);
        this.startCountdown();
    }

    /**
     * Generate PIX code string
     */
    generatePixCode(appointment) {
        // Parse appointment datetime
        const appointmentDateTime = new Date(appointment.appointmentDateTime);
        const formattedDate = appointmentDateTime.toLocaleDateString('pt-BR');
        const formattedTime = appointmentDateTime.toLocaleTimeString('pt-BR', {
            hour: '2-digit',
            minute: '2-digit'
        });

        // This is a simplified PIX code format
        // In real implementation, this should follow the official PIX specification
        const pixData = {
            merchantName: appointment.establishmentName || 'Estabelecimento',
            merchantCity: 'SAO_PAULO',
            amount: (appointment.servicePrice || 50.00).toFixed(2),
            description: `${appointment.serviceName || 'Serviço'} - ${formattedDate} ${formattedTime}`,
            transactionId: `SLT${appointment.id}${Date.now()}`
        };

        // Simplified PIX string (in real implementation, use proper PIX EMV format)
        return `PIX|${pixData.merchantName}|${pixData.amount}|${pixData.description}|${pixData.transactionId}`;
    }

    /**
     * Display QR Code and payment details
     */
    displayQRCode(qrCodeUrl, pixCode, appointment) {
        const qrContainer = document.getElementById('qr-code-container');
        const qrDisplay = document.getElementById('qr-code-display');
        const paymentDetails = document.getElementById('payment-details');

        // Parse appointment datetime for display
        const appointmentDateTime = new Date(appointment.appointmentDateTime);
        const formattedDate = appointmentDateTime.toLocaleDateString('pt-BR');
        const formattedTime = appointmentDateTime.toLocaleTimeString('pt-BR', {
            hour: '2-digit',
            minute: '2-digit'
        });

        // Display QR Code
        qrDisplay.innerHTML = `
            <img src="${qrCodeUrl}" alt="QR Code PIX" class="img-fluid" style="max-width: 200px;">
        `;

        // Display payment details
        paymentDetails.innerHTML = `
            <div class="payment-details-info">
                <div class="row text-start">
                    <div class="col-6"><strong>Serviço:</strong></div>
                    <div class="col-6">${appointment.serviceName || 'Serviço'}</div>
                </div>
                <div class="row text-start">
                    <div class="col-6"><strong>Profissional:</strong></div>
                    <div class="col-6">${appointment.professionalName || 'Profissional'}</div>
                </div>
                <div class="row text-start">
                    <div class="col-6"><strong>Data/Hora:</strong></div>
                    <div class="col-6">${formattedDate} ${formattedTime}</div>
                </div>
                <div class="row text-start">
                    <div class="col-6"><strong>Valor:</strong></div>
                    <div class="col-6"><strong class="text-success">R$ ${(appointment.servicePrice || 50.00).toFixed(2)}</strong></div>
                </div>
            </div>
        `;

        // Store PIX code for copying
        this.currentPixCode = pixCode;

        // Show QR Code container
        qrContainer.classList.remove('d-none');
    }

    /**
     * Hide QR Code
     */
    hideQRCode() {
        const qrContainer = document.getElementById('qr-code-container');
        qrContainer.classList.add('d-none');
        this.stopCountdown();
    }

    /**
     * Start countdown timer
     */
    startCountdown() {
        this.timeRemaining = 15 * 60; // Reset to 15 minutes
        this.updateCountdownDisplay();

        this.countdownTimer = setInterval(() => {
            this.timeRemaining--;
            this.updateCountdownDisplay();

            if (this.timeRemaining <= 0) {
                this.onCountdownExpired();
            }
        }, 1000);
    }

    /**
     * Stop countdown timer
     */
    stopCountdown() {
        if (this.countdownTimer) {
            clearInterval(this.countdownTimer);
            this.countdownTimer = null;
        }
    }

    /**
     * Update countdown display
     */
    updateCountdownDisplay() {
        const timerElement = document.getElementById('countdown-timer');
        if (timerElement) {
            const minutes = Math.floor(this.timeRemaining / 60);
            const seconds = this.timeRemaining % 60;
            timerElement.textContent = `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
        }
    }

    /**
     * Handle countdown expiration
     */
    onCountdownExpired() {
        this.stopCountdown();
        ToastManager.showWarning('QR Code expirado. Gere um novo código para continuar.');
        
        // Optionally regenerate QR code automatically
        if (this.selectedAppointment) {
            setTimeout(() => {
                this.generatePixPayment();
                ToastManager.showInfo('Novo QR Code gerado automaticamente.');
            }, 2000);
        }
    }

    /**
     * Load payment history from API
     */
    async loadPaymentHistory() {
        try {
            // Get client ID from localStorage
            const user = JSON.parse(localStorage.getItem('user') || '{}');
            const clientId = user.id;
            
            if (!clientId) {
                console.warn('No client ID found in session');
                return;
            }

            // Real API call to get appointment history
            const response = await apiClient.get(API_CONFIG.endpoints.client.appointments.history, {
                clientId: clientId
            });

            if (response.success && response.data) {
                this.renderPaymentHistory(response.data);
            } else {
                console.log('No payment history found');
            }
            
        } catch (error) {
            console.error('Error loading payment history:', error);
        }
    }

    /**
     * Render payment history
     */
    renderPaymentHistory(appointments) {
        const historyContainer = document.getElementById('payment-history');
        if (!historyContainer) return;

        if (!appointments || appointments.length === 0) {
            historyContainer.innerHTML = `
                <div class="text-center py-4">
                    <i class="fas fa-receipt text-muted mb-3" style="font-size: 2rem;"></i>
                    <p class="text-muted">Nenhum histórico de pagamentos encontrado</p>
                </div>
            `;
            return;
        }

        const historyHtml = appointments.map(appointment => {
            const appointmentDateTime = new Date(appointment.appointmentDateTime);
            const formattedDate = appointmentDateTime.toLocaleDateString('pt-BR');
            const amount = appointment.servicePrice || 0;

            return `
                <div class="payment-history-item border-bottom py-3">
                    <div class="row align-items-center">
                        <div class="col-md-8">
                            <h6 class="mb-1">${appointment.serviceName || 'Serviço'}</h6>
                            <p class="text-muted mb-1">${appointment.professionalName || 'Profissional'}</p>
                            <small class="text-muted">${formattedDate}</small>
                        </div>
                        <div class="col-md-4 text-md-end">
                            <span class="fw-bold text-success">R$ ${amount.toFixed(2)}</span>
                            <br>
                            <small class="text-muted">${appointment.status || 'COMPLETED'}</small>
                        </div>
                    </div>
                </div>
            `;
        }).join('');

        historyContainer.innerHTML = historyHtml;
    }
}

/**
 * Copy PIX code to clipboard
 */
function copyPixCode() {
    const instance = window.clientPaymentsInstance;
    if (instance && instance.currentPixCode) {
        navigator.clipboard.writeText(instance.currentPixCode).then(() => {
            ToastManager.showSuccess('Código PIX copiado para a área de transferência!');
        }).catch(() => {
            ToastManager.showError('Erro ao copiar código PIX.');
        });
    }
}

/**
 * Download QR Code image
 */
function downloadQR() {
    const qrImage = document.querySelector('#qr-code-display img');
    if (qrImage) {
        const link = document.createElement('a');
        link.href = qrImage.src;
        link.download = `qr-code-pix-${Date.now()}.png`;
        link.click();
        ToastManager.showSuccess('QR Code baixado com sucesso!');
    }
}

// Initialize when DOM is loaded
document.addEventListener('DOMContentLoaded', function() {
    window.clientPaymentsInstance = ClientPayments.init();
});

/**
 * Basic Toast Manager for notifications
 */
class ToastManager {
    static showSuccess(message) {
        this.show(message, 'success');
    }

    static showError(message) {
        this.show(message, 'error');
    }

    static showWarning(message) {
        this.show(message, 'warning');
    }

    static showInfo(message) {
        this.show(message, 'info');
    }

    static show(message, type = 'info') {
        // Create toast container if it doesn't exist
        let toastContainer = document.getElementById('toast-container');
        if (!toastContainer) {
            toastContainer = document.createElement('div');
            toastContainer.id = 'toast-container';
            toastContainer.style.cssText = `
                position: fixed;
                top: 20px;
                right: 20px;
                z-index: 9999;
            `;
            document.body.appendChild(toastContainer);
        }

        // Create toast element
        const toast = document.createElement('div');
        toast.className = `alert alert-${this.getBootstrapClass(type)} alert-dismissible fade show`;
        toast.style.cssText = `
            margin-bottom: 10px;
            min-width: 300px;
        `;
        toast.innerHTML = `
            ${message}
            <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
        `;

        toastContainer.appendChild(toast);

        // Auto remove after 5 seconds
        setTimeout(() => {
            if (toast.parentNode) {
                toast.remove();
            }
        }, 5000);
    }

    static getBootstrapClass(type) {
        const map = {
            'success': 'success',
            'error': 'danger',
            'warning': 'warning',
            'info': 'info'
        };
        return map[type] || 'info';
    }
}