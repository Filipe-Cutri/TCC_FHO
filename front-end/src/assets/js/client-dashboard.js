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
            // TODO: Replace with actual API call
            // const response = await fetch('/api/client/appointments/next');
            // const appointment = await response.json();
            
            // Mock data for now - will be replaced with real API call
            const mockAppointment = {
                id: 1,
                service: "Corte + Barba",
                professional: "João Silva",
                date: "2024-12-10",
                time: "14:00",
                endTime: "15:00",
                establishment: "Barbearia Premium - Centro",
                status: "CONFIRMED"
            };

            // Simulate API delay
            await new Promise(resolve => setTimeout(resolve, 1000));

            this.renderNextAppointment(mockAppointment, appointmentContainer);
            
        } catch (error) {
            console.error('Error loading next appointment:', error);
            this.renderNoAppointment(appointmentContainer);
        }
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