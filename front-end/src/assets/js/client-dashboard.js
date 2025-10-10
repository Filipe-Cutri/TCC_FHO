/**
 * Client Dashboard Manager - Handles dashboard functionality and backend integration
 */
class ClientDashboard {
    /**
     * Initialize dashboard functionality
     */
    static init() {
        this.loadDashboardData();
        this.setupEventListeners();
    }

    /**
     * Load dashboard data from backend
     */
    static async loadDashboardData() {
        try {
            // Get client session
            const session = window.clientSession.getSession();
            
            if (!session || !session.id) {
                console.error('No client session found');
                return;
            }

            // Update user name in welcome section
            const userNameElement = document.querySelector('.client-user-name');
            if (userNameElement) {
                userNameElement.textContent = session.name;
            }

            // Fetch dashboard data from API
            const response = await fetch(`/api/client/dashboard?clientId=${session.id}`);
            const result = await response.json();

            if (result.success && result.data) {
                this.updateDashboardStats(result.data.stats);
                this.updateNextAppointment(result.data.nextAppointment);
            } else {
                console.error('Failed to load dashboard data:', result.message);
            }
            
        } catch (error) {
            console.error('Error loading dashboard data:', error);
        }
    }

    /**
     * Update dashboard statistics
     */
    static updateDashboardStats(stats) {
        // Update appointments count
        const statsCards = document.querySelectorAll('.client-stats-card');
        if (statsCards.length >= 2) {
            // First card - appointments
            const appointmentsNumber = statsCards[0].querySelector('.client-stats-number');
            if (appointmentsNumber) {
                appointmentsNumber.textContent = stats.totalAppointments || '0';
            }

            // Second card - favorite professionals
            const professionalsNumber = statsCards[1].querySelector('.client-stats-number');
            if (professionalsNumber) {
                professionalsNumber.textContent = stats.favoriteProfessionals || '0';
            }
        }
    }

    /**
     * Update next appointment display
     */
    static updateNextAppointment(appointment) {
        // Find the appointment card specifically (first card in col-lg-8)
        const appointmentCard = document.querySelector('.col-lg-8 .client-card .card-body');
        
        if (!appointmentCard) {
            console.warn('Appointment card container not found');
            return;
        }
        
        if (!appointment) {
            this.renderNoAppointment(appointmentCard);
            return;
        }

        this.renderNextAppointment(appointment, appointmentCard);
    }

    /**
     * Render next appointment data
     */
    static renderNextAppointment(appointment, container) {
        const appointmentDate = new Date(appointment.date);
        const formattedDate = appointmentDate.toLocaleDateString('pt-BR', {
            day: 'numeric',
            month: 'long',
            year: 'numeric'
        });

        container.innerHTML = `
            <h5 class="card-title">
                <span class="card-icon">
                    <i class="fas fa-calendar-check"></i>
                </span>
                Próximo Agendamento
            </h5>
            <div class="row align-items-center">
                <div class="col-md-8">
                    <p class="card-text mb-2"><strong>${appointment.service}</strong> com ${appointment.professional}</p>
                    <p class="card-text mb-1">
                        <i class="fas fa-calendar me-2 text-primary"></i>
                        <strong>${formattedDate}</strong>
                    </p>
                    <p class="card-text mb-1">
                        <i class="fas fa-clock me-2 text-primary"></i>
                        <strong>${appointment.time} - ${appointment.endTime}</strong>
                    </p>
                    <p class="card-text mb-3">
                        <i class="fas fa-map-marker-alt me-2 text-primary"></i>
                        ${appointment.establishment}
                    </p>
                    <span class="client-status-badge client-status-${appointment.status.toLowerCase()}">
                        ${this.getStatusText(appointment.status)}
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
            <h5 class="card-title">
                <span class="card-icon">
                    <i class="fas fa-calendar-check"></i>
                </span>
                Próximo Agendamento
            </h5>
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