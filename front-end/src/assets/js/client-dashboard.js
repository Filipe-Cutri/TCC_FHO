/**
 * Client Dashboard Manager - Handles dashboard functionality and backend integration
 */
class ClientDashboard {
    /**
     * Initialize dashboard functionality
     */
    static init() {
        this.loadNextAppointment();
        this.setupEventListeners();
    }

    /**
     * Load next appointment from backend
     */
    static async loadNextAppointment() {
        const appointmentContainer = document.getElementById('next-appointment-content');
        
        try {
            // Get client ID from localStorage (set during login)
            const user = JSON.parse(localStorage.getItem('user') || '{}');
            const clientId = user.id;
            
            if (!clientId) {
                console.warn('No client ID found in session');
                this.renderNoAppointment(appointmentContainer);
                return;
            }
            
            // Real API call instead of mock data
            const response = await apiClient.get(API_CONFIG.endpoints.client.appointments.next, {
                clientId: clientId
            });
            
            if (response.success && response.data) {
                this.renderNextAppointment(response.data, appointmentContainer);
            } else {
                this.renderNoAppointment(appointmentContainer);
            }
            
        } catch (error) {
            console.error('Error loading next appointment:', error);
            this.renderNoAppointment(appointmentContainer);
        }
    }

    /**
     * Render next appointment data
     */
    static renderNextAppointment(appointment, container) {
        // Parse appointment datetime from API response
        const appointmentDateTime = new Date(appointment.appointmentDateTime);
        const formattedDate = appointmentDateTime.toLocaleDateString('pt-BR', {
            day: 'numeric',
            month: 'long',
            year: 'numeric'
        });
        const formattedTime = appointmentDateTime.toLocaleTimeString('pt-BR', {
            hour: '2-digit',
            minute: '2-digit'
        });
        
        // Calculate end time (default to 1 hour if not provided)
        let endTime;
        if (appointment.endDateTime) {
            const endDateTime = new Date(appointment.endDateTime);
            endTime = endDateTime.toLocaleTimeString('pt-BR', {
                hour: '2-digit',
                minute: '2-digit'
            });
        } else if (appointment.serviceDurationMinutes) {
            const endDateTime = new Date(appointmentDateTime.getTime() + (appointment.serviceDurationMinutes * 60000));
            endTime = endDateTime.toLocaleTimeString('pt-BR', {
                hour: '2-digit',
                minute: '2-digit'
            });
        } else {
            const endDateTime = new Date(appointmentDateTime.getTime() + (60 * 60000)); // 1 hour default
            endTime = endDateTime.toLocaleTimeString('pt-BR', {
                hour: '2-digit',
                minute: '2-digit'
            });
        }

        container.innerHTML = `
            <div class="row align-items-center">
                <div class="col-md-8">
                    <p class="card-text mb-2"><strong>${appointment.serviceName || 'Serviço'}</strong> com ${appointment.professionalName || 'Profissional'}</p>
                    <p class="card-text mb-1">
                        <i class="fas fa-calendar me-2 text-primary"></i>
                        <strong>${formattedDate}</strong>
                    </p>
                    <p class="card-text mb-1">
                        <i class="fas fa-clock me-2 text-primary"></i>
                        <strong>${formattedTime} - ${endTime}</strong>
                    </p>
                    <p class="card-text mb-3">
                        <i class="fas fa-map-marker-alt me-2 text-primary"></i>
                        ${appointment.establishmentName || 'Estabelecimento'}
                    </p>
                    <span class="client-status-badge client-status-${appointment.status ? appointment.status.toLowerCase() : 'pending'}">
                        ${this.getStatusText(appointment.status || 'PENDING')}
                    </span>
                </div>
                <div class="col-md-4 text-md-end">
                    <a href="client-bookings.html" class="client-btn client-btn-secondary mb-2 d-block">
                        <i class="fas fa-eye me-2"></i>Ver Detalhes
                    </a>
                    <button class="client-btn mb-2 d-block" onclick="ClientDashboard.rescheduleAppointment(${appointment.id})">
                        <i class="fas fa-edit me-2"></i>Reagendar
                    </button>
                </div>
            </div>
        `;
    }

    /**
     * Render no appointment message
     */
    static renderNoAppointment(container) {
        container.innerHTML = `
            <div class="text-center py-4">
                <i class="fas fa-calendar-times text-muted mb-3" style="font-size: 3rem;"></i>
                <h6 class="text-muted mb-3">Nenhum agendamento próximo</h6>
                <p class="text-muted mb-4">Que tal agendar um novo serviço?</p>
                <a href="client-services.html" class="client-btn">
                    <i class="fas fa-plus me-2"></i>Agendar Serviço
                </a>
            </div>
        `;
    }

    /**
     * Get status text in Portuguese
     */
    static getStatusText(status) {
        const statusMap = {
            'CONFIRMED': 'Confirmado',
            'PENDING': 'Pendente',
            'CANCELLED': 'Cancelado',
            'COMPLETED': 'Concluído'
        };
        return statusMap[status] || status;
    }

    /**
     * Handle appointment rescheduling
     */
    static rescheduleAppointment(appointmentId) {
        // Redirect to reschedule page with appointment ID
        window.location.href = `client-bookings.html?reschedule=${appointmentId}`;
    }

    /**
     * Setup event listeners
     */
    static setupEventListeners() {
        // Add any additional event listeners here
        console.log('Client Dashboard initialized');
    }
}