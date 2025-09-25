package com.slotfy.service;

import com.slotfy.model.Appointment;
import com.slotfy.model.AppointmentStatus;
import com.slotfy.repository.AppointmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service class for Appointment entity
 */
@Service
public class AppointmentService extends BaseService<Appointment, Long> {
    
    private final AppointmentRepository appointmentRepository;
    
    @Autowired
    public AppointmentService(AppointmentRepository appointmentRepository) {
        super(appointmentRepository);
        this.appointmentRepository = appointmentRepository;
    }
    
    /**
     * Get all appointments for an establishment
     */
    public List<Appointment> getByEstablishmentId(Long establishmentId) {
        return appointmentRepository.findByEstablishmentIdOrderByAppointmentDateTimeDesc(establishmentId);
    }
    
    /**
     * Get appointments by status
     */
    public List<Appointment> getByEstablishmentAndStatus(Long establishmentId, AppointmentStatus status) {
        return appointmentRepository.findByEstablishmentIdAndStatusOrderByAppointmentDateTimeAsc(establishmentId, status);
    }
    
    /**
     * Get appointments for a professional
     */
    public List<Appointment> getByProfessional(Long professionalId) {
        return appointmentRepository.findByProfessionalIdOrderByAppointmentDateTimeAsc(professionalId);
    }
    
    /**
     * Get appointments for a client
     */
    public List<Appointment> getByClient(Long clientId) {
        return appointmentRepository.findByClientIdOrderByAppointmentDateTimeDesc(clientId);
    }
    
    /**
     * Get today's appointments
     */
    public List<Appointment> getTodayAppointments(Long establishmentId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return appointmentRepository.findTodayAppointments(establishmentId, startOfDay, endOfDay);
    }
    
    /**
     * Get upcoming appointments
     */
    public List<Appointment> getUpcomingAppointments(Long establishmentId) {
        return appointmentRepository.findUpcomingAppointments(establishmentId, LocalDateTime.now());
    }
    
    /**
     * Get appointments in date range
     */
    public List<Appointment> getByDateRange(Long establishmentId, LocalDateTime startDate, LocalDateTime endDate) {
        return appointmentRepository.findByEstablishmentIdAndDateRange(establishmentId, startDate, endDate);
    }
    
    /**
     * Create a new appointment
     */
    public Appointment createAppointment(Long clientId, Long professionalId, Long serviceId, Long establishmentId,
                                       LocalDateTime appointmentDateTime, String notes, String clientName,
                                       String professionalName, String serviceName, Integer serviceDurationMinutes,
                                       BigDecimal servicePrice) {
        // Validate input
        if (clientId == null) {
            throw new IllegalArgumentException("ID do cliente é obrigatório");
        }
        if (professionalId == null) {
            throw new IllegalArgumentException("ID do profissional é obrigatório");
        }
        if (serviceId == null) {
            throw new IllegalArgumentException("ID do serviço é obrigatório");
        }
        if (establishmentId == null) {
            throw new IllegalArgumentException("ID do estabelecimento é obrigatório");
        }
        if (appointmentDateTime == null) {
            throw new IllegalArgumentException("Data e hora são obrigatórias");
        }
        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é possível agendar para uma data passada");
        }
        
        // Check for conflicts
        LocalDateTime endTime = appointmentDateTime.plusMinutes(serviceDurationMinutes != null ? serviceDurationMinutes : 60);
        List<Appointment> conflicts = findConflictingAppointments(professionalId, appointmentDateTime, endTime);
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Já existe um agendamento para este profissional neste horário");
        }
        
        Appointment appointment = new Appointment(clientId, professionalId, serviceId, establishmentId, appointmentDateTime);
        appointment.setNotes(notes);
        appointment.setClientName(clientName);
        appointment.setProfessionalName(professionalName);
        appointment.setServiceName(serviceName);
        appointment.setServiceDurationMinutes(serviceDurationMinutes);
        appointment.setServicePrice(servicePrice);
        
        return appointmentRepository.save(appointment);
    }
    
    /**
     * Update appointment status
     */
    public Appointment updateStatus(Long appointmentId, AppointmentStatus status) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isEmpty()) {
            throw new IllegalArgumentException("Agendamento não encontrado");
        }
        
        Appointment appointment = optionalAppointment.get();
        appointment.setStatus(status);
        
        return appointmentRepository.save(appointment);
    }
    
    /**
     * Update appointment date/time
     */
    public Appointment reschedule(Long appointmentId, LocalDateTime newDateTime) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isEmpty()) {
            throw new IllegalArgumentException("Agendamento não encontrado");
        }
        
        Appointment appointment = optionalAppointment.get();
        
        if (newDateTime.isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Não é possível reagendar para uma data passada");
        }
        
        // Check for conflicts
        LocalDateTime endTime = newDateTime.plusMinutes(appointment.getServiceDurationMinutes() != null ? appointment.getServiceDurationMinutes() : 60);
        List<Appointment> conflicts = findConflictingAppointments(appointment.getProfessionalId(), newDateTime, endTime);
        
        // Remove current appointment from conflicts (it should not conflict with itself)
        conflicts.removeIf(conflict -> conflict.getId().equals(appointmentId));
        
        if (!conflicts.isEmpty()) {
            throw new IllegalArgumentException("Já existe um agendamento para este profissional no novo horário");
        }
        
        appointment.setAppointmentDateTime(newDateTime);
        
        return appointmentRepository.save(appointment);
    }
    
    /**
     * Update appointment notes
     */
    public Appointment updateNotes(Long appointmentId, String notes) {
        Optional<Appointment> optionalAppointment = appointmentRepository.findById(appointmentId);
        if (optionalAppointment.isEmpty()) {
            throw new IllegalArgumentException("Agendamento não encontrado");
        }
        
        Appointment appointment = optionalAppointment.get();
        appointment.setNotes(notes);
        
        return appointmentRepository.save(appointment);
    }
    
    /**
     * Cancel appointment
     */
    public Appointment cancelAppointment(Long appointmentId) {
        return updateStatus(appointmentId, AppointmentStatus.CANCELLED);
    }
    
    /**
     * Confirm appointment
     */
    public Appointment confirmAppointment(Long appointmentId) {
        return updateStatus(appointmentId, AppointmentStatus.CONFIRMED);
    }
    
    /**
     * Complete appointment
     */
    public Appointment completeAppointment(Long appointmentId) {
        return updateStatus(appointmentId, AppointmentStatus.COMPLETED);
    }
    
    /**
     * Count appointments for establishment
     */
    public long countByEstablishment(Long establishmentId) {
        return appointmentRepository.countByEstablishmentId(establishmentId);
    }
    
    /**
     * Count today's appointments
     */
    public long countTodayAppointments(Long establishmentId) {
        LocalDateTime startOfDay = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        return appointmentRepository.countTodayAppointments(establishmentId, startOfDay, endOfDay);
    }
    
    /**
     * Count this month's appointments
     */
    public long countThisMonthAppointments(Long establishmentId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        return appointmentRepository.countThisMonthAppointments(establishmentId, startOfMonth, endOfMonth);
    }
    
    /**
     * Calculate monthly revenue
     */
    public BigDecimal calculateMonthlyRevenue(Long establishmentId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        return appointmentRepository.calculateMonthlyRevenue(establishmentId, startOfMonth, endOfMonth);
    }
    
    /**
     * Get professional performance statistics
     */
    public List<Object[]> getProfessionalPerformanceStats(Long establishmentId) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
        return appointmentRepository.findProfessionalPerformanceStats(establishmentId, startOfMonth, endOfMonth);
    }
    
    /**
     * Check if time slot is available
     */
    public boolean isTimeSlotAvailable(Long professionalId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Appointment> conflicts = findConflictingAppointments(professionalId, startTime, endTime);
        return conflicts.isEmpty();
    }

    /**
     * Find conflicting appointments using improved logic
     */
    private List<Appointment> findConflictingAppointments(Long professionalId, LocalDateTime startTime, LocalDateTime endTime) {
        // Get potential conflicts from a broader time range
        LocalDateTime dayStart = startTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
        List<Appointment> potentialConflicts = appointmentRepository.findPotentialConflictingAppointments(
            professionalId, dayStart, endTime.plusHours(1));
        
        // Filter to actual conflicts using Java logic
        return potentialConflicts.stream()
            .filter(appointment -> {
                LocalDateTime existingStart = appointment.getAppointmentDateTime();
                LocalDateTime existingEnd = appointment.getEndDateTime();
                if (existingEnd == null) {
                    // Default to 60 minutes if duration is not set
                    existingEnd = existingStart.plusMinutes(60);
                }
                
                // Check for overlap: appointments conflict if one starts before the other ends
                return existingStart.isBefore(endTime) && existingEnd.isAfter(startTime);
            })
            .collect(Collectors.toList());
    }
    
    /**
     * Get client's upcoming appointments
     */
    public List<Appointment> getClientUpcomingAppointments(Long clientId) {
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepository.findByClientIdAndAppointmentDateTimeAfterOrderByAppointmentDateTimeAsc(clientId, now);
    }
    
    /**
     * Get all client appointments
     */
    public List<Appointment> getClientAppointments(Long clientId) {
        return appointmentRepository.findByClientIdOrderByAppointmentDateTimeDesc(clientId);
    }
    
    /**
     * Get client appointment history (past appointments)
     */
    public List<Appointment> getClientAppointmentHistory(Long clientId) {
        LocalDateTime now = LocalDateTime.now();
        return appointmentRepository.findByClientIdAndAppointmentDateTimeBeforeOrderByAppointmentDateTimeDesc(clientId, now);
    }
    
    /**
     * Check if time slot is available for client (no conflicts)
     */
    public boolean isClientTimeSlotAvailable(Long clientId, LocalDateTime startTime, LocalDateTime endTime) {
        List<Appointment> conflicts = appointmentRepository.findByClientIdAndAppointmentDateTimeBetween(clientId, startTime.minusHours(2), endTime.plusHours(2));
        
        // Filter to actual conflicts using Java logic
        return conflicts.stream()
            .noneMatch(appointment -> {
                LocalDateTime existingStart = appointment.getAppointmentDateTime();
                LocalDateTime existingEnd = appointment.getEndDateTime();
                if (existingEnd == null) {
                    // Default to 60 minutes if duration is not set
                    existingEnd = existingStart.plusMinutes(60);
                }
                
                // Check for overlap: appointments conflict if one starts before the other ends
                return existingStart.isBefore(endTime) && existingEnd.isAfter(startTime);
            });
    }
    
    /**
     * Create appointment for client
     */
    public Appointment createClientAppointment(Long clientId, Long professionalId, Long serviceId, 
                                             Long establishmentId, LocalDateTime appointmentDateTime, String notes) {
        return createAppointment(clientId, professionalId, serviceId, establishmentId, appointmentDateTime,
                               notes, null, null, null, null, null);
    }
}